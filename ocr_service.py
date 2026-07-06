from fastapi import FastAPI
from pydantic import BaseModel
import easyocr
import cv2
import numpy as np
import requests
import re
import base64

app = FastAPI()

# ================= MODELO =================

class OcrRequest(BaseModel):
    imageUrl: str       # pode ser URL http:// ou base64 data:image/...

# ================= EASYOCR =================
# Inicializa uma única vez — caro demais pra reiniciar por request
reader = easyocr.Reader(['en'], gpu=False, verbose=False)

# ================= FILTRO DE LIXO =================

LIXO_REGEX = re.compile(
    r"(scan|scans|luacomic|facebook|instagram|discord|twitter|read at|"
    r"support us|official domain|website|team|www\.|http|©|chapter|\bch\b)",
    re.IGNORECASE
)

def texto_valido(texto: str) -> bool:
    texto = texto.strip()
    if len(texto) < 3:
        return False
    if LIXO_REGEX.search(texto):
        return False
    letras = sum(c.isalpha() for c in texto)
    if letras < 2:
        return False
    return True

# ================= CARREGAR IMAGEM =================

def carregar_imagem(image_url: str) -> np.ndarray | None:
    """
    Aceita:
      - URL HTTP/HTTPS  → baixa com requests
      - data:image/...  → decodifica base64
    Suporta webp, png, jpg automaticamente via cv2.imdecode.
    """
    try:
        if image_url.startswith("data:"):
            # base64: "data:image/webp;base64,AAAA..."
            header, encoded = image_url.split(",", 1)
            img_bytes = base64.b64decode(encoded)
        else:
            resp = requests.get(
                image_url,
                headers={
                    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                                  "AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36",
                    "Referer": "https://ezmanga.net/",
                },
                timeout=20,
            )
            resp.raise_for_status()
            img_bytes = resp.content

        img_array = np.frombuffer(img_bytes, np.uint8)
        # IMREAD_UNCHANGED preserva alpha (png) e aceita webp
        img = cv2.imdecode(img_array, cv2.IMREAD_UNCHANGED)

        if img is None:
            print("[ERRO] cv2.imdecode retornou None — formato não suportado?")
            return None

        # Garante BGR de 3 canais (remove alpha se existir)
        if img.ndim == 2:
            img = cv2.cvtColor(img, cv2.COLOR_GRAY2BGR)
        elif img.shape[2] == 4:
            img = cv2.cvtColor(img, cv2.COLOR_BGRA2BGR)

        print(f"[OK] Imagem carregada: {img.shape}")
        return img

    except Exception as e:
        print(f"[ERRO] carregar_imagem: {e}")
        return None

# ================= PREPROCESSAR PARA OCR =================

def preprocessar(img: np.ndarray) -> np.ndarray:
    """
    Pré-processamento leve — não agressivo demais para não destruir o texto.
    EasyOCR já lida bem com imagens coloridas, então só fazemos o mínimo.
    """
    # Upscale se a imagem for muito pequena (OCR melhora muito com isso)
    h, w = img.shape[:2]
    if w < 800:
        scale = 800 / w
        img = cv2.resize(img, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)

    return img

# ================= AGRUPAR REGIÕES PRÓXIMAS =================

def agrupar_regioes(regioes: list[dict], gap_x=60, gap_y=40) -> list[dict]:
    """
    Agrupa caixas de texto que estão próximas verticalmente e horizontalmente
    (mesma "bolha" de fala). Resultado: menos regiões, mais contexto por região.
    """
    if not regioes:
        return []

    # Ordena por Y
    regioes = sorted(regioes, key=lambda r: r["y"])
    grupos = []
    atual = regioes[0].copy()

    for reg in regioes[1:]:
        # Verifica sobreposição / proximidade
        mesmo_x = (reg["x"] < atual["x"] + atual["w"] + gap_x and
                   reg["x"] + reg["w"] > atual["x"] - gap_x)
        mesmo_y = reg["y"] < atual["y"] + atual["h"] + gap_y

        if mesmo_x and mesmo_y:
            # Expande o grupo atual
            novo_x = min(atual["x"], reg["x"])
            novo_y = min(atual["y"], reg["y"])
            novo_x2 = max(atual["x"] + atual["w"], reg["x"] + reg["w"])
            novo_y2 = max(atual["y"] + atual["h"], reg["y"] + reg["h"])
            atual = {
                "x": novo_x,
                "y": novo_y,
                "w": novo_x2 - novo_x,
                "h": novo_y2 - novo_y,
                "texto": atual["texto"] + " " + reg["texto"],
            }
        else:
            grupos.append(atual)
            atual = reg.copy()

    grupos.append(atual)
    return grupos

# ================= INPAINTING (LIMPEZA DO TEXTO) =================

def limpar_baloes(img: np.ndarray, baloes: list[dict], pad: int = 4) -> np.ndarray:
    """
    Remove o texto original de cada balão, preservando o fundo.

    Estratégia (leve, sem rede neural):
      1. Recorta só a bounding box do balão (não a imagem toda).
      2. Otsu threshold local → separa texto (escuro) do fundo automaticamente,
         se adaptando a balões com fundo branco OU levemente texturizado.
      3. Dilata a máscara ~2-3px para cobrir bordas com anti-aliasing
         (sem isso sobra uma "auréola" ao redor das letras).
      4. cv2.inpaint só naquela região pequena — rápido e sem manchar
         o resto da imagem.
    """
    resultado = img.copy()

    for b in baloes:
        x, y, w, h = b["x"], b["y"], b["w"], b["h"]

        # Padding pra garantir contexto de fundo suficiente no inpaint
        x0 = max(x - pad, 0)
        y0 = max(y - pad, 0)
        x1 = min(x + w + pad, img.shape[1])
        y1 = min(y + h + pad, img.shape[0])

        roi = resultado[y0:y1, x0:x1]
        if roi.size == 0:
            continue

        gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY)

        # THRESH_BINARY_INV: texto (escuro) vira branco (255) na máscara
        _, mask = cv2.threshold(
            gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU
        )

        # Dilata levemente pra cobrir anti-aliasing nas bordas das letras
        kernel = np.ones((3, 3), np.uint8)
        mask = cv2.dilate(mask, kernel, iterations=1)

        inpainted = cv2.inpaint(roi, mask, 3, cv2.INPAINT_TELEA)
        resultado[y0:y1, x0:x1] = inpainted

    return resultado


def imagem_para_base64(img: np.ndarray) -> str:
    """Codifica a imagem (numpy array) de volta para base64 PNG."""
    ok, buffer = cv2.imencode(".png", img)
    if not ok:
        return ""
    return "data:image/png;base64," + base64.b64encode(buffer).decode("utf-8")

# ================= ENDPOINT PRINCIPAL =================

@app.post("/ocr")
def executar_ocr(req: OcrRequest):
    """
    Pipeline:
      1. Carrega imagem (URL ou base64, qualquer formato)
      2. Pré-processa levemente
      3. EasyOCR detecta texto + posição diretamente
      4. Filtra lixo por confiança e regex
      5. Agrupa regiões próximas (mesmo balão)
      6. Retorna lista de {x, y, w, h, texto}
    """
    img = carregar_imagem(req.imageUrl)

    if img is None:
        print("[FALHA] Imagem não pôde ser carregada.")
        return {"baloes": [], "erro": "imagem_invalida"}

    img_proc = preprocessar(img)

    # EasyOCR — detail=1 retorna [(bbox, texto, confiança)]
    # paragraph=False → uma entrada por linha de texto (mais preciso para posição)
    try:
        resultados_raw = reader.readtext(
            img_proc,
            detail=1,
            paragraph=False,
            contrast_ths=0.1,
            adjust_contrast=0.5,
            text_threshold=0.6,   # confiança mínima interna do EasyOCR
            low_text=0.3,
        )
    except Exception as e:
        print(f"[ERRO] EasyOCR falhou: {e}")
        return {"baloes": [], "erro": "ocr_falhou"}

    print(f"[OCR] {len(resultados_raw)} regiões brutas detectadas")

    regioes = []
    for bbox, texto, conf in resultados_raw:
        # bbox = [[x1,y1],[x2,y1],[x2,y2],[x1,y2]]
        xs = [p[0] for p in bbox]
        ys = [p[1] for p in bbox]
        x, y = int(min(xs)), int(min(ys))
        w = int(max(xs) - min(xs))
        h = int(max(ys) - min(ys))

        if conf < 0.35:
            continue

        if not texto_valido(texto):
            continue

        regioes.append({
            "x": x,
            "y": y,
            "w": w,
            "h": h,
            "texto": texto.strip(),
        })

    print(f"[FILTRO] {len(regioes)} regiões após filtro de lixo")

    # Agrupa regiões próximas do mesmo balão
    baloes = agrupar_regioes(regioes)

    print(f"[RESULTADO] {len(baloes)} balões finais")
    for b in baloes:
        print(f"  → ({b['x']},{b['y']}) {b['w']}x{b['h']}: {b['texto'][:60]}")

    # Limpa o texto original de cada balão (inpainting local, sem rede neural)
    img_limpa = limpar_baloes(img_proc, baloes)
    imagem_limpa_b64 = imagem_para_base64(img_limpa)

    return {
        "baloes": baloes,
        "imagemLimpa": imagem_limpa_b64,  # já pronta pra renderizar o texto traduzido em cima
    }

# ================= HEALTH CHECK =================

@app.get("/health")
def health():
    return {"status": "ok"}

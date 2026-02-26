from fastapi import FastAPI
from pydantic import BaseModel
import easyocr
import cv2
import numpy as np
import requests
import re

app = FastAPI()

# ===================== MODELO DE ENTRADA =====================
class OcrRequest(BaseModel):
    imageUrl: str

# ===================== INICIALIZA OCR =====================
reader = easyocr.Reader(
    ['en'], 
    gpu=False,          # use True se tiver GPU
    verbose=False
)

# ===================== FILTRO DE LIXO =====================
LIXO_REGEX = re.compile(
    r"(scan|scans|facebook|instagram|discord|twitter|read at|support us|www\.|http)",
    re.IGNORECASE
)

def texto_valido(texto: str) -> bool:
    if not texto:
        return False

    texto = texto.strip()

    if len(texto) < 4:
        return False

    if LIXO_REGEX.search(texto):
        return False

    letras = sum(c.isalpha() for c in texto)
    if letras < 3:
        return False

    return True

# ===================== OCR ENDPOINT =====================
@app.post("/ocr")
def executar_ocr(req: OcrRequest):
    try:
        # Baixa imagem
        resp = requests.get(
            req.imageUrl,
            headers={
                "User-Agent": "Mozilla/5.0"
            },
            timeout=15
        )
        img_array = np.frombuffer(resp.content, np.uint8)
        img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

        if img is None:
            return {"baloes": []}

        h_img, w_img = img.shape[:2]

        resultados = reader.readtext(
            img,
            detail=1,
            paragraph=False,
            contrast_ths=0.05,
            adjust_contrast=0.7
        )

        baloes = []

        for bbox, texto, conf in resultados:
            if conf < 0.45:
                continue

            if not texto_valido(texto):
                continue

            xs = [p[0] for p in bbox]
            ys = [p[1] for p in bbox]

            x = int(min(xs))
            y = int(min(ys))
            w = int(max(xs) - x)
            h = int(max(ys) - y)

            baloes.append({
                "x": x,
                "y": y,
                "w": w,
                "h": h,
                "texto": texto.strip()
            })

        return {"baloes": baloes}

    except Exception as e:
        print("Erro OCR:", e)
        return {"baloes": []}
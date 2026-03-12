// ===================== CONFIG =====================
const API_URL = "http://localhost:8080/api/manhwa/traduzir";


// ===================== OBSERVER LOGIC =====================
/**
 * O segredo para não sobrepor: Cada página é processada apenas quando entra no campo de visão.
 */
const ocrObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const img = entry.target;
            if (img.dataset.ocrStatus !== "processado") {
                processarPaginaIndividual(img);
            }
        }
    });
}, { rootMargin: "300px" }); // Começa a traduzir 300px antes de aparecer

// ===================== FUNÇÕES DE PROCESSO =====================

async function processarPaginaIndividual(img) {
    const src = img.src;
    const element = img.parentElement || img;

    img.dataset.ocrStatus = "processado"; // Marca para não repetir
    console.log(` Traduzindo página: ${src.split('/').pop()}`);

    try {
        const resultado = await enviarImagem(src);

        if (!resultado || !resultado.baloes || resultado.baloes.length === 0) {
            console.warn(" Nenhum balão encontrado nesta página.");
            return;
        }

        const container = garantirContainerImagem(element);
        // Importante: limpa apenas os balões desta página específica
        removerOverlays(container);

        resultado.baloes.forEach(balao => {
            desenharTraducaoNoBalao(container, balao);
        });

        console.log(` ${resultado.baloes.length} balões inseridos.`);
    } catch (e) {
        console.error(" Falha na página:", e.message);
        img.dataset.ocrStatus = "erro"; // Permite tentar de novo se necessário
    }
}

// ===================== UI & DRAWING =====================

function garantirContainerImagem(element) {
    if (!element.classList.contains("ocr-container")) {
        element.classList.add("ocr-container");
        element.style.position = "relative";
        element.style.display = "inline-block";
        element.style.width = "100%";
    }
    return element;
}

// ===================== API & CONTROL =====================

async function enviarImagem(imageUrl) {
    const resp = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ imageUrl })
    });
    if (!resp.ok) throw new Error("Erro na API");
    return await resp.json();
}

function removerOverlays(container) {
    container.querySelectorAll(".overlay-balao").forEach(el => el.remove());
}

function iniciarOCR() {
    const imgs = document.querySelectorAll("img.wp-manga-chapter-img");
    if (imgs.length === 0) {
        alert("Nenhuma imagem de capítulo encontrada!");
        return;
    }
    console.log(`Iniciando observador para ${imgs.length} imagens.`);
    imgs.forEach(img => ocrObserver.observe(img));
}


// ===================== DESENHO DO BALÃO =====================



function desenharTraducaoNoBalao(container, balao) {
    const img = container.querySelector("img");
    if (!img || !balao.texto || balao.texto.length < 2) return;

    const scaleX = img.clientWidth / img.naturalWidth;
    const scaleY = img.clientHeight / img.naturalHeight;

    const overlay = document.createElement("div");
    overlay.className = "overlay-balao";

    // ajustar tamanho de fonte dinamicamente com base na área do balão (em pixels após escala)
    const scaledW = balao.w * scaleX;
    const scaledH = balao.h * scaleY;
    const area = scaledW * scaledH;
    const computedFontSize = area > 40000 ? "14px" : area > 20000 ? "13px" : "12px";

    Object.assign(overlay.style, {
        position: "absolute",
        left: `${balao.x * scaleX}px`,
        top: `${balao.y * scaleY}px`,
        width: `${scaledW}px`,
        height: `${scaledH}px`,
        backgroundColor: "#fff",
        color: "#111",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        textAlign: "center",
        fontSize: computedFontSize,
        fontWeight: "700",
        lineHeight: "1.15",
        padding: "8px",
        borderRadius: "50%",
        border: "2px solid #000",
        zIndex: "9999",
        pointerEvents: "none",
        boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
        fontFamily: "'Segoe UI', Roboto, sans-serif",
        wordBreak: "break-word",
        overflowWrap: "anywhere"
    });

    overlay.innerText = balao.texto.toUpperCase();
    container.appendChild(overlay);
}

// ===================== BOTÃO ESTILIZADO =====================
(function criarBotaoModerno() {
    if (document.getElementById("btn-ocr-master")) return;

    const btn = document.createElement("button");
    btn.id = "btn-ocr-master";

    btn.innerHTML = `
        <span style="font-size: 20px; margin-bottom: 2px;"></span>
        <span>TRADUZIR</span>
    `;

    Object.assign(btn.style, {
        position: "fixed",
        bottom: "80px",
        right: "30px",
        zIndex: "99999",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        width: "90px",
        height: "90px",
        backgroundColor: "#9159f1",
        color: "white",
        border: "4px solid rgba(255,255,255,0.3)",
        borderRadius: "50%",
        cursor: "pointer",
        fontWeight: "bold",
        fontSize: "11px",
        boxShadow: "0 8px 32px rgba(145, 89, 241, 0.4)",
        transition: "all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)",
        backdropFilter: "blur(4px)"
    });

    // Efeitos de Hover
    btn.onmouseenter = () => {
        btn.style.transform = "scale(1.1) translateY(-5px)";
        btn.style.backgroundColor = "#a575f4";
    };
    btn.onmouseleave = () => {
        btn.style.transform = "scale(1) translateY(0)";
        btn.style.backgroundColor = "#9159f1";
    };

    btn.onclick = () => {
        btn.style.backgroundColor = "#6a3db3";
        btn.innerText = "TRADUZINDO...";
        iniciarOCR();
    };

    document.body.appendChild(btn);
})();



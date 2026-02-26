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
/*

function desenharTraducaoNoBalao(container, balao) {
    const img = container.querySelector("img");
    if (!img || !balao.traducao) return;

    const scaleX = img.clientWidth / img.naturalWidth;
    const scaleY = img.clientHeight / img.naturalHeight;

    const overlay = document.createElement("div");
    overlay.className = "overlay-balao";
    
    // Configurações para legibilidade máxima
    Object.assign(overlay.style, {
        position: "absolute",
        left: `${balao.x * scaleX}px`,
        top: `${balao.y * scaleY}px`,
        width: `${balao.w * scaleX}px`,
        height: `${balao.h * scaleY}px`,
        backgroundColor: "white", 
        color: "black",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        textAlign: "center",
        fontSize: "12px",
        lineHeight: "1.2",
        fontWeight: "bold",
        padding: "4px",
        borderRadius: "50%",
        zIndex: "1000",
        overflow: "hidden", 
        pointerEvents: "none"
    });

    overlay.innerText = balao.traducao;
    container.appendChild(overlay);
}
*/
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


// ===================== DESENHO DO BALÃO (MELHORADO) =====================
function desenharTraducaoNoBalao(container, balao) {
    const img = container.querySelector("img");
    if (!img || !balao.traducao || balao.traducao.length < 2) return;

    const scaleX = img.clientWidth / img.naturalWidth;
    const scaleY = img.clientHeight / img.naturalHeight;

    const overlay = document.createElement("div");
    overlay.className = "overlay-balao";
    
    Object.assign(overlay.style, {
        position: "absolute",
        left: `${balao.x * scaleX}px`,
        top: `${balao.y * scaleY}px`,
        width: `${balao.w * scaleX}px`,
        height: `${balao.h * scaleY}px`,
        backgroundColor: "rgba(255, 255, 255, 0.98)", 
        color: "#1a1a1a",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        textAlign: "center",
        fontSize: "min(13px, 3.5vw)",
        lineHeight: "1.1",
        fontWeight: "700",
        padding: "5px",
        borderRadius: "50%",
        zIndex: "1000",
        boxShadow: "0 2px 10px rgba(0,0,0,0.2)",
        pointerEvents: "none",
        fontFamily: "'Segoe UI', Roboto, sans-serif"
    });

    overlay.innerText = balao.traducao.toUpperCase(); 
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



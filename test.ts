/***********************
 * CONFIG
 ***********************/
const API_URL = 'http://localhost:8080/api/manhwa/traduzir';

/***********************
 * UTIL
 ***********************/
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

/***********************
 * SCROLL ATÉ O FIM
 ***********************/
async function scrollAteOFim() {
  let lastHeight = 0;

  for (let i = 0; i < 40; i++) {
    window.scrollTo(0, document.body.scrollHeight);
    await sleep(600);

    const newHeight = document.body.scrollHeight;
    if (newHeight === lastHeight) break;
    lastHeight = newHeight;
  }
}

/***********************
 * COLETA IMAGENS
 ***********************/
function coletarImagensDoCapitulo() {
  return [...new Set(
    Array.from(document.querySelectorAll('img'))
      .map(img => img.src || img.dataset.src)
      .filter(src =>
        src &&
        src.includes('/chapter_') &&
        /ch_\d+_\d+\.jpg$/.test(src)
      )
  )];
}

/***********************
 * ENVIO PARA API
 ***********************/
async function enviarImagem(url) {
  console.log('[API] URL enviada:', url);

  const res = await fetch(API_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ imageUrl: url })
  });

  if (!res.ok) {
    throw new Error(await res.text());
  }

  return res.text();
}

/***********************
 * FLUXO PRINCIPAL
 ***********************/
async function iniciarOCR() {
  console.clear();
  console.log('⬇ Descendo a página...');
  await scrollAteOFim();

  console.log(' Coletando imagens...');
  const imagens = coletarImagensDoCapitulo();

  if (!imagens.length) {
    console.error('Nenhuma imagem encontrada');
    return;
  }

  console.log(` ${imagens.length} páginas encontradas`);

  for (let i = 0; i < imagens.length; i++) {
    console.log(` Página ${i + 1}/${imagens.length}`);
    try {
      await enviarImagem(imagens[i]);
      await sleep(2000); // ESSENCIAL pro Tesseract não quebrar
    } catch (e) {
      console.error('Erro na imagem:', imagens[i], e.message);
    }
  }

  console.log(' OCR finalizado');
}

/***********************
 * BOTÃO FLUTUANTE
 ***********************/
(function criarBotao() {
  if (document.getElementById('ocr-btn')) return;

  const btn = document.createElement('button');
  btn.id = 'ocr-btn';
  btn.innerText = 'OCR';
  btn.style.cssText = `
    position: fixed;
    bottom: 20px;
    right: 20px;
    z-index: 99999;
    padding: 16px 20px;
    background: linear-gradient(135deg, #6a00ff, #9d4edd);
    color: #fff;
    border: none;
    border-radius: 50%;
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    box-shadow: 0 4px 10px rgba(0,0,0,0.3);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  `;

  btn.onmouseover = () => {
    btn.style.transform = 'scale(1.1)';
    btn.style.boxShadow = '0 6px 14px rgba(0,0,0,0.4)';
  };

  btn.onmouseout = () => {
    btn.style.transform = 'scale(1)';
    btn.style.boxShadow = '0 4px 10px rgba(0,0,0,0.3)';
  };



  btn.onclick = iniciarOCR;
  document.body.appendChild(btn);

  console.log(' Botão OCR injetado');
})();

//Criar o balão de tradução
function aplicarTraducaoNaImagem(imgElement, textoTraduzido) {
  if (!textoTraduzido || textoTraduzido.trim().length === 0) return;

  // evita duplicar overlay
  if (imgElement.parentElement.querySelector(".manga-ocr-overlay")) return;

  // wrapper
  const wrapper = document.createElement("div");
  wrapper.style.position = "relative";
  wrapper.style.display = "inline-block";

  imgElement.parentNode.insertBefore(wrapper, imgElement);
  wrapper.appendChild(imgElement);

  // overlay
  const overlay = document.createElement("div");
  overlay.className = "manga-ocr-overlay";
  overlay.innerText = textoTraduzido;

  overlay.style.position = "absolute";
  overlay.style.bottom = "20px";
  overlay.style.left = "50%";
  overlay.style.transform = "translateX(-50%)";
  overlay.style.maxWidth = "90%";

  overlay.style.background = "rgba(0,0,0,0.75)";
  overlay.style.color = "#fff";
  overlay.style.padding = "12px 16px";
  overlay.style.borderRadius = "12px";

  overlay.style.fontSize = "16px";
  overlay.style.lineHeight = "1.5";
  overlay.style.fontFamily = "Arial, sans-serif";
  overlay.style.textAlign = "center";
  overlay.style.zIndex = "9999";

  overlay.style.boxShadow = "0 4px 12px rgba(0,0,0,0.4)";

  wrapper.appendChild(overlay);
}



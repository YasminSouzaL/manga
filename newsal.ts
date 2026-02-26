// Botão flutuante
const translateBtn = document.createElement("button");
translateBtn.innerText = "Traduzir Página (EN)";
translateBtn.style.cssText = `
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  background: #38bdf8;
  color: white;
  border: none;
  border-radius: 10px;
  padding: 12px;
  cursor: pointer;
  font-weight: bold;
`;
document.body.appendChild(translateBtn);

translateBtn.onclick = async () => {

  const img = document.querySelector(".reading-content img, .wp-manga-chapter-img");
  if (!img) {
    alert("Nenhuma imagem encontrada");
    return;
  }

  translateBtn.innerText = "Processando...";

  // 🔥 BAIXA A IMAGEM DE VERDADE
  const imgResponse = await fetch(img.src);
  const blob = await imgResponse.blob();

  // 🔥 CONVERTE BLOB → BASE64
  const base64Image = await new Promise((resolve) => {
    const reader = new FileReader();
    reader.onloadend = () => resolve(reader.result);
    reader.readAsDataURL(blob);
  });

  console.log("Base64 size:", base64Image.length);

  // 🔥 ENVIA PARA O BACKEND
  const response = await fetch("http://localhost:8080/api/manhwa/traduzir", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ image: base64Image })
  });

  console.log("Response status:", response.status);

  console.log("Response headers:", [...response.headers.entries()]);



  const data = await response.json();

  const overlay = document.createElement("div");
  overlay.innerText = data.traduzido || "Nenhum texto detectado";
  overlay.style.cssText = `
    position: fixed;
    bottom: 20px;
    right: 20px;
    background: rgba(0,0,0,0.85);
    color: white;
    padding: 15px;
    border-radius: 10px;
    z-index: 10000;
    max-width: 300px;
  `;
  document.body.appendChild(overlay);

  translateBtn.innerText = "Traduzir Página (EN)";
};


public BufferedImage baixarESalvarImagem(String imageUrl) {
        try {
            System.out.println("[OCR] Baixando imagem...");
            URL url = new URL(imageUrl);

            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                throw new RuntimeException("Imagem não pôde ser lida (BufferedImage null)");
            }

            // cria pasta ./imagem se não existir
            File pasta = new File("imagem");
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            File arquivo = new File(pasta, "teste.jpg");
            ImageIO.write(image, "jpg", arquivo);

            System.out.println("[OCR] Imagem salva em: " + arquivo.getAbsolutePath());
            System.out.println("[OCR] Dimensões: " + image.getWidth() + "x" + image.getHeight());

            return image;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar/salvar imagem", e);
        }
    }

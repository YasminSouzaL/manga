from PIL import Image
import os

# Nome do arquivo da sua imagem original
input_path = "icon.png"

try:
    # Abre a imagem e converte para RGBA (suporte a transparência, se necessário)
    img = Image.open(input_path).convert("RGBA")

    # Tamanhos exigidos pelo manifest.json
    sizes = [16, 48, 128]

    # Cria a pasta 'icons' se ela não existir (como pede o seu manifest)
    if not os.path.exists("icons"):
        os.makedirs("icons")

    for s in sizes:
        # Redimensiona usando o método mais atual (LANCZOS para alta qualidade)
        resized = img.resize((s, s), Image.Resampling.LANCZOS)
        
        # Salva dentro da pasta 'icons'
        path = f"icons/icon{s}.png"
        resized.save(path)
        print(f"Ícone gerado com sucesso: {path}")

except FileNotFoundError:
    print(f"Erro: O arquivo '{input_path}' não foi encontrado na mesma pasta do script.")
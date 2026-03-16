

# Resumo do Funcionamento do Projeto

O projeto consiste em um sistema capaz de **identificar e traduzir automaticamente textos presentes em páginas de mangás ou manhwas**, utilizando técnicas de **Reconhecimento Óptico de Caracteres (OCR)** integradas a uma arquitetura baseada em **Spring Boot, Python e uma extensão de navegador**.

A solução foi desenvolvida para capturar imagens de páginas de quadrinhos diretamente de sites de leitura online, identificar os textos presentes nos balões de diálogo e realizar sua tradução para outro idioma, exibindo o resultado sobre a própria imagem.

---

# Arquitetura do Sistema

O sistema é dividido em três componentes principais:

1. **Extensão do navegador**
2. **API backend em Spring Boot**
3. **Serviço de OCR em Python utilizando EasyOCR**

Essa arquitetura permite separar as responsabilidades de captura de imagem, processamento e reconhecimento de texto.

---

# Funcionamento do Fluxo do Sistema

O funcionamento do sistema ocorre através das seguintes etapas:

### 1. Captura da imagem da página

A extensão do navegador identifica automaticamente as imagens da página do mangá ou manhwa. Cada imagem representa uma página da história.

A extensão então envia a URL da imagem para o backend através de uma requisição HTTP.

---

### 2. Envio da requisição para a API

A extensão envia uma requisição **POST** para o endpoint da aplicação Spring Boot:

```
/api/manhwa/traduzir
```

No corpo da requisição é enviada a URL da imagem:

```json
{
 "imageUrl": "https://site.com/pagina.jpg"
}
```

---

### 3. Processamento no Controller

O `ManhwaTranslatorController` recebe a requisição e valida se a URL da imagem foi enviada corretamente.

Após essa validação, o controller chama o serviço responsável pelo OCR:

```
EasyOcrService
```

Esse serviço é responsável por encaminhar a imagem para o sistema de reconhecimento de texto.

---

### 4. Envio da imagem para o serviço de OCR

O `EasyOcrService` envia a URL da imagem para um serviço externo rodando em **Python**, acessível através do endpoint:

```
http://localhost:9000/ocr
```

Esse serviço utiliza a biblioteca **EasyOCR**, especializada em reconhecimento de texto em imagens.

---

### 5. Detecção dos textos na imagem

O EasyOCR processa a imagem e detecta automaticamente regiões onde há texto.

Para cada região detectada, o sistema retorna:

* coordenada **x**
* coordenada **y**
* largura do texto (**w**)
* altura (**h**)
* texto identificado

Exemplo de resposta:

```
{
  baloes: [
    {x: 323, y: 308, w: 196, h: 43, texto: "SINCE FATHER"},
    {x: 260, y: 345, w: 312, h: 42, texto: "IS AWAY, AS THE HEIR"}
  ]
}
```

Essas coordenadas indicam exatamente onde o texto está localizado na imagem.

---

### 6. Retorno das informações para o backend

O serviço Python retorna os dados para o **Spring Boot**, que repassa essas informações para a extensão do navegador.

Durante esse processo o sistema também imprime logs no terminal para facilitar o monitoramento e depuração.

Exemplo de log:

```
====== RESPOSTA DO OCR ======
{baloes=[{x=323, y=308, w=196, h=43, texto=SinCE FATHER}, ...]}
```

---

### 7. Renderização dos balões na página

Ao receber as coordenadas e os textos detectados, a extensão do navegador cria **elementos visuais sobre a imagem da página**, posicionando os textos exatamente sobre os balões originais.

Dessa forma, o sistema consegue simular uma tradução diretamente sobre a página do mangá ou manhwa.

---

# Tecnologias Utilizadas

O projeto utiliza as seguintes tecnologias:

**Backend**

* Java
* Spring Boot
* REST API

**Processamento de imagem**

* Python
* EasyOCR
* OpenCV (opcional para melhorias)

**Frontend**

* JavaScript
* Chrome Extension API
* Manipulação do DOM

---

# Objetivo do Sistema

O objetivo do projeto é desenvolver um **tradutor automático de mangás e manhwas baseado em OCR**, capaz de identificar textos em imagens e integrá-los a um sistema de tradução e renderização diretamente no navegador.

Esse tipo de solução pode ser utilizado em aplicações de:

* leitura automática de quadrinhos
* acessibilidade linguística
* ferramentas de apoio à tradução de conteúdo visual.



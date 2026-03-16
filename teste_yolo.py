from ultralytics import YOLO

# carrega modelo
model = YOLO("yolov8n.pt")

# roda detecção
resultados = model("imagem/test.jpg")

# mostra resultado
for r in resultados:
    print(r.boxes)
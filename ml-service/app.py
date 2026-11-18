"""
Plastic verification service.

Mode 1 (default, works out of the box): MobileNetV2 pre-trained on ImageNet. ImageNet classes that
look like plastic items (water bottle, pop bottle, plastic bag, ...) are mapped to plastic types.
Mode 2 (recommended for real accuracy): run `python train.py` on your own dataset. It saves
models/plastic_model.pt + models/classes.json, which this service loads automatically.
"""
import io, json, os
import torch
import torch.nn as nn
from fastapi import FastAPI, File, UploadFile, HTTPException
from PIL import Image
from torchvision import models, transforms

MODEL_PATH = os.path.join("models", "plastic_model.pt")
CLASSES_PATH = os.path.join("models", "classes.json")

# ImageNet label -> plastic type (demo mapping for mode 1)
IMAGENET_MAP = {
    "water bottle": "PET", "pop bottle": "PET", "water jug": "HDPE", "pill bottle": "HDPE",
    "plastic bag": "LDPE", "bucket": "HDPE", "soap dispenser": "HDPE", "lotion": "HDPE",
    "measuring cup": "PP", "shower cap": "LDPE", "mixing bowl": "PP", "wooden spoon": "NON_PLASTIC",
    "beer bottle": "NON_PLASTIC", "wine bottle": "NON_PLASTIC", "whiskey jug": "NON_PLASTIC",
    "beer glass": "NON_PLASTIC", "can opener": "NON_PLASTIC", "paper towel": "NON_PLASTIC",
    "carton": "NON_PLASTIC", "envelope": "NON_PLASTIC", "packet": "NON_PLASTIC",
}

preprocess = transforms.Compose([
    transforms.Resize(256), transforms.CenterCrop(224), transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]),
])

app = FastAPI(title="Plastic Verification Service")
model, class_names, custom = None, None, False


def load():
    global model, class_names, custom
    if os.path.exists(MODEL_PATH) and os.path.exists(CLASSES_PATH):
        class_names = json.load(open(CLASSES_PATH))
        m = models.mobilenet_v2()
        m.classifier[1] = nn.Linear(m.last_channel, len(class_names))
        m.load_state_dict(torch.load(MODEL_PATH, map_location="cpu"))
        model, custom = m.eval(), True
        print("Loaded custom model:", class_names)
    else:
        weights = models.MobileNet_V2_Weights.DEFAULT
        model = models.mobilenet_v2(weights=weights).eval()
        class_names = weights.meta["categories"]
        custom = False
        print("Loaded ImageNet MobileNetV2 (demo mode). Train your own with train.py for better accuracy.")


@app.on_event("startup")
def startup():
    load()


@app.get("/health")
def health():
    return {"status": "ok", "mode": "custom" if custom else "imagenet-demo"}


@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    try:
        img = Image.open(io.BytesIO(await file.read())).convert("RGB")
    except Exception:
        raise HTTPException(400, "Invalid image")
    with torch.no_grad():
        probs = torch.softmax(model(preprocess(img).unsqueeze(0))[0], dim=0)

    if custom:
        idx = int(probs.argmax())
        return {"type": class_names[idx].upper(), "confidence": round(float(probs[idx]), 4), "mode": "custom"}

    totals = {}
    for i, name in enumerate(class_names):
        t = IMAGENET_MAP.get(name.lower())
        if t:
            totals[t] = totals.get(t, 0.0) + float(probs[i])
    if not totals:
        return {"type": "UNKNOWN", "confidence": 0.0, "mode": "imagenet-demo"}
    best = max(totals, key=totals.get)
    return {"type": best, "confidence": round(min(totals[best], 1.0), 4), "mode": "imagenet-demo"}

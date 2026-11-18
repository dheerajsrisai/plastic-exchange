"""
Fine-tune MobileNetV2 on your own plastic dataset.

Dataset layout (one folder per class; use TrashNet / Kaggle waste datasets, regroup as you like):
  data/train/PET/*.jpg   data/train/HDPE/*.jpg   data/train/PP/*.jpg   data/train/NON_PLASTIC/*.jpg ...
  data/val/PET/*.jpg     ...

Run:  python train.py --data data --epochs 8
Output: models/plastic_model.pt and models/classes.json  (picked up automatically by app.py)
"""
import argparse, json, os
import torch, torch.nn as nn
from torch.utils.data import DataLoader
from torchvision import datasets, models, transforms

p = argparse.ArgumentParser()
p.add_argument("--data", default="data")
p.add_argument("--epochs", type=int, default=8)
p.add_argument("--batch", type=int, default=32)
a = p.parse_args()

norm = transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
train_tf = transforms.Compose([transforms.RandomResizedCrop(224), transforms.RandomHorizontalFlip(),
                               transforms.ColorJitter(0.2, 0.2, 0.2), transforms.ToTensor(), norm])
val_tf = transforms.Compose([transforms.Resize(256), transforms.CenterCrop(224), transforms.ToTensor(), norm])

train_ds = datasets.ImageFolder(os.path.join(a.data, "train"), train_tf)
val_ds = datasets.ImageFolder(os.path.join(a.data, "val"), val_tf)
train_dl = DataLoader(train_ds, batch_size=a.batch, shuffle=True, num_workers=2)
val_dl = DataLoader(val_ds, batch_size=a.batch, num_workers=2)

device = "cuda" if torch.cuda.is_available() else "cpu"
model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.DEFAULT)
for prm in model.features.parameters():
    prm.requires_grad = False                      # transfer learning: train only the new head first
model.classifier[1] = nn.Linear(model.last_channel, len(train_ds.classes))
model.to(device)

opt = torch.optim.Adam(model.classifier.parameters(), lr=1e-3)
loss_fn = nn.CrossEntropyLoss()
best = 0.0
os.makedirs("models", exist_ok=True)

for ep in range(a.epochs):
    model.train()
    for x, y in train_dl:
        x, y = x.to(device), y.to(device)
        opt.zero_grad(); loss_fn(model(x), y).backward(); opt.step()
    model.eval(); ok = n = 0
    with torch.no_grad():
        for x, y in val_dl:
            ok += (model(x.to(device)).argmax(1).cpu() == y).sum().item(); n += len(y)
    acc = ok / max(n, 1)
    print(f"epoch {ep + 1}/{a.epochs}  val_acc={acc:.3f}")
    if acc >= best:
        best = acc
        torch.save(model.state_dict(), "models/plastic_model.pt")
        json.dump(train_ds.classes, open("models/classes.json", "w"))
print("Best val accuracy:", best)

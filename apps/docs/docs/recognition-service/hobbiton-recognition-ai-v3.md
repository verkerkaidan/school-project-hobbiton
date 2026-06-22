---
sidebar_position: 5
---
# Hobbiton recognition AI

AI image classification model for labeling images "car" or "no-car" for a content warning system in a student project social media app. <br>
For the resulting model versions used in hobbiton, see the below gitlab repository: <br>
https://gitlab.inf-hsleiden.nl/s1147087/my-model-artifacts/-/blob/main/README.md?ref_type=heads

### Generic imports

```python
import os
import pandas as pd
import glob
import numpy as np
from PIL import Image
import io
from tqdm import tqdm
import random

import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
from torchvision import models, transforms
from torch.utils.data import Dataset, DataLoader, WeightedRandomSampler
from torch.optim.lr_scheduler import CosineAnnealingLR
from torch.cuda.amp import GradScaler, autocast
from torchvision.datasets import CIFAR100

from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
from sklearn.metrics import classification_report

from concurrent.futures import ThreadPoolExecutor
```

### Setting up the dataframe

##### Udacity self driving car
https://www.kaggle.com/datasets/sshikamaru/udacity-self-driving-car-dataset

```python
UDACITY_ROOT = "/kaggle/input/datasets/mlharteveld/car-data/data/export"

df_udacity = pd.read_csv(f"{UDACITY_ROOT}/_annotations.csv").dropna()
df_udacity["class"] = df_udacity["class"].apply(lambda x: "car" if x == "car" else "no-car")

df_udacity["filepath"] = df_udacity["filename"].apply(
    lambda fn: os.path.join(UDACITY_ROOT, fn)
)
df_udacity = df_udacity[["filepath", "class"]].copy()
df_udacity["source"] = "udacity"

df_udacity = df_udacity.sample(n=5000, random_state=42).reset_index(drop=True)
```

##### Toycar
https://www.kaggle.com/datasets/dvaser/toycars

```python
TOYCARS_ROOT = "/kaggle/input/datasets/mlharteveld/car-data/data/Toy Cars Dataset"

CAR_CLASSES_TOYCARS = {"Cars", "Emergency"}

rows_toycars = []
for class_dir in os.scandir(TOYCARS_ROOT):
    if not class_dir.is_dir():
        continue
    label = "car" if class_dir.name in CAR_CLASSES_TOYCARS else "no-car"
    for img_path in glob.glob(os.path.join(class_dir.path, "*")):
        rows_toycars.append({"filepath": img_path, "class": label, "source": "toycars"})

df_toycars = pd.DataFrame(rows_toycars)
```

##### Vehicle image classification
https://www.kaggle.com/datasets/mohamedmaher5/vehicle-classification

```python
VEHICLES_ROOT = "/kaggle/input/datasets/mlharteveld/car-data/data/Vehicles"

CAR_CLASSES_VEHICLES = {"Cars"}

rows_vehicles = []
for class_dir in os.scandir(VEHICLES_ROOT):
    if not class_dir.is_dir():
        continue
    label = "car" if class_dir.name in CAR_CLASSES_VEHICLES else "no-car"
    for img_path in glob.glob(os.path.join(class_dir.path, "*")):
        rows_vehicles.append({"filepath": img_path, "class": label, "source": "vehicles"})

df_vehicles = pd.DataFrame(rows_vehicles)
```

##### Vehicle type recognition
https://www.kaggle.com/datasets/kaggleashwin/vehicle-type-recognition?select=Dataset

```python
VEHICLE_RECOGNITION_ROOT = "/kaggle/input/datasets/kaggleashwin/vehicle-type-recognition/Dataset"
CAR_CLASSES_VR = {"Car", "Truck"}  # adjust if the folder name differs (e.g. "car" lowercase)

rows_vr = []
for class_dir in os.scandir(VEHICLE_RECOGNITION_ROOT):
    if not class_dir.is_dir():
        continue
    label = "car" if class_dir.name in CAR_CLASSES_VR else "no-car"
    for img_path in glob.glob(os.path.join(class_dir.path, "*")):
        rows_vr.append({"filepath": img_path, "class": label, "source": "vehicle_recognition"})

df_vr = pd.DataFrame(rows_vr)
```

##### STL10 dataset
https://huggingface.co/datasets/tanganke/stl10

```python
# Constants
STL10_CLASSES = {
    0: "airplane", 1: "bird", 2: "car", 3: "deer", 4: "dog",
    5: "horse", 6: "monkey", 7: "ship", 8: "truck", 9: "cat"
}
CAR_CLASSES_STL10 = {"car"}
SAVE_DIR = "/kaggle/working/stl10_images"
os.makedirs(SAVE_DIR, exist_ok=True)

# Load
splits = {'train': 'data/train-00000-of-00001.parquet', 'test': 'data/test-00000-of-00001.parquet'}
df_stl10_raw_train = pd.read_parquet("hf://datasets/tanganke/stl10/" + splits["train"])
df_stl10_raw_test = pd.read_parquet("hf://datasets/tanganke/stl10/" + splits["test"])
df_stl10_raw = pd.concat([df_stl10_raw_train, df_stl10_raw_test], ignore_index=True)

# Save to disk in parallel
def save_row(args):
    idx, row = args
    class_name = STL10_CLASSES[row["label"]]
    label = "car" if class_name in CAR_CLASSES_STL10 else "no-car"
    img_path = os.path.join(SAVE_DIR, f"{idx}.jpg")
    
    # Decode from HuggingFace image dict
    img_bytes = row["image"]["bytes"]
    image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
    image.save(img_path)
    
    return {"filepath": img_path, "class": label, "source": "stl10"}

with ThreadPoolExecutor(max_workers=8) as executor:
    rows_stl10 = list(executor.map(save_row, df_stl10_raw.iterrows()))

df_stl10 = pd.DataFrame(rows_stl10)
```

##### Vehicle classification dataset
https://www.kaggle.com/datasets/ilhamhasib/vehicle-classification-dataset

```python
BASE = "/kaggle/input/datasets/ilhamhasib/vehicle-classification-dataset"
CAR_CLASSES_VC = {"SUV", "Sedan", "Hatchback", "MPV", "Pickup"}  # adjust if needed

rows_vc = []

# Part 1: Train folder with labels CSV
train_dir = os.path.join(BASE, "Car-Classification-Data-with-Labels-iHasib", "Train")
df_labels = pd.read_csv(os.path.join(BASE, "Car-Classification-Data-with-Labels-iHasib", "type-labels.csv"))

for _, row in df_labels.iterrows():
    img_path = os.path.join(train_dir, f"{row['ID']}.jpg")
    if os.path.exists(img_path):
        label = "car" if row["Type"] in CAR_CLASSES_VC else "no-car"
        rows_vc.append({"filepath": img_path, "class": label, "source": "vehicle_classification"})

# Part 2: Test folder with class subfolders
test_dir = os.path.join(BASE, "Vehicle Test Data")
for class_dir in os.scandir(test_dir):
    if not class_dir.is_dir():
        continue
    label = "car" if class_dir.name in CAR_CLASSES_VC else "no-car"
    for img_path in glob.glob(os.path.join(class_dir.path, "*")):
        rows_vc.append({"filepath": img_path, "class": label, "source": "vehicle_classification"})

df_vc = pd.DataFrame(rows_vc)
print(df_vc["class"].value_counts())
```

##### Vehicle image classification (4 classes)
https://www.kaggle.com/datasets/mohiychoithwani/vehicle-image-classification-dataset-4-classes?select=images

```python
VEHICLE4_ROOT = "/kaggle/input/datasets/mohiychoithwani/vehicle-image-classification-dataset-4-classes/images"
CAR_CLASSES_V4 = {"car"}

rows_v4 = []
for class_dir in os.scandir(VEHICLE4_ROOT):
    if not class_dir.is_dir():
        continue
    label = "car" if class_dir.name in CAR_CLASSES_V4 else "no-car"
    for img_path in glob.glob(os.path.join(class_dir.path, "*")):
        rows_v4.append({"filepath": img_path, "class": label, "source": "vehicle4"})

df_v4 = pd.DataFrame(rows_v4)
```

##### CIFAR100 Negatives

```python
# CIFAR-100 non-vehicle classes to EXCLUDE from negatives
# (so we don't accidentally label vehicle-adjacent classes as no-car)
VEHICLE_CLASSES = {
    'bus', 'pickup_truck', 'streetcar', 'tank', 'tractor'
}

CIFAR100_N_SAMPLES = 5000
CIFAR100_SAVE_DIR = "/kaggle/working/cifar100_negatives"
os.makedirs(CIFAR100_SAVE_DIR, exist_ok=True)

# Download CIFAR-100 (~170MB, auto-downloads)
_cifar = CIFAR100(root="/kaggle/working/cifar100", train=True, download=True)

# Map class index -> class name
_idx_to_class = {i: c for i, c in enumerate(_cifar.classes)}

# Build index of non-vehicle samples
non_vehicle_indices = [
    i for i, (_, class_idx) in enumerate(_cifar)
    if _idx_to_class[class_idx] not in VEHICLE_CLASSES
]

random.seed(42)
sampled_indices = random.sample(non_vehicle_indices, min(CIFAR100_N_SAMPLES, len(non_vehicle_indices)))

rows_inet = []
for idx in tqdm(sampled_indices, desc="Saving CIFAR-100 negatives"):
    img, _ = _cifar[idx]
    fname = f"cifar100_{idx}.png"
    dst_path = os.path.join(CIFAR100_SAVE_DIR, fname)
    if not os.path.exists(dst_path):
        img.save(dst_path)
    rows_inet.append({'filepath': dst_path, 'class': 'no-car', 'source': 'cifar100_negatives'})

df_cifar = pd.DataFrame(rows_inet)
print(f"CIFAR-100 negatives added: {len(df_cifar)}")
```

##### Combine

```python
df = pd.concat([df_toycars, df_vehicles, df_udacity, df_stl10, df_vr, df_vc, df_v4, df_cifar], ignore_index=True)

# Encode label: car=1, no-car=0  (factorize gives consistent ordering)
df["class_id"], label_names = pd.factorize(df["class"])

print(df["class"].value_counts())
print(df["source"].value_counts())
print("total rows:", len(df))
df.head()
```

### Model setup

##### Train/val/test split

```python
train_df, test_df = train_test_split(
    df,
    test_size=0.2,
    stratify=df["class"],
    random_state=42
)
train_df, val_df = train_test_split(
    train_df,
    test_size=0.1,
    stratify=train_df["class"],
    random_state=42
)

### Used to train model
print("Train size:", len(train_df))
### Used during training to validate model
print("Validation size:", len(val_df))
### Used after training to validate model
print("Test size:", len(test_df))
```

##### Dataset transformations

```python
class CarDataset(Dataset):
    def __init__(self, df, transform=None):
        self.df = df.reset_index(drop=True)
        self.transform = transform

    def __len__(self):
        return len(self.df)

    def __getitem__(self, idx):
        row = self.df.iloc[idx]
        image = Image.open(row["filepath"]).convert("RGB")
        label = row["class_id"]
        if self.transform:
            image = self.transform(image)
        return image, label
```

```python
# ResNet50 expects 224x224, normalized with ImageNet mean/std

train_transform = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.RandomCrop(224),
    transforms.RandomHorizontalFlip(),
    transforms.RandomVerticalFlip(p=0.1),
    transforms.RandomRotation(15),
    transforms.ColorJitter(brightness=0.3, contrast=0.3, saturation=0.2, hue=0.1),
    transforms.RandomGrayscale(p=0.05),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225]),
])

val_test_transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225]),
])
```

```python
train_dataset = CarDataset(train_df, transform=train_transform)
val_dataset   = CarDataset(val_df,   transform=val_test_transform)
test_dataset  = CarDataset(test_df,  transform=val_test_transform)

pin = torch.cuda.is_available()

train_loader = DataLoader(train_dataset, batch_size=128, shuffle=True,  num_workers=4, pin_memory=pin)
val_loader   = DataLoader(val_dataset,   batch_size=128, shuffle=False, num_workers=4, pin_memory=pin)
test_loader  = DataLoader(test_dataset,  batch_size=128, shuffle=False, num_workers=4, pin_memory=pin)
```

##### Loading model

```python
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print("Using device:", device)

model = models.efficientnet_b0(weights=models.EfficientNet_B0_Weights.DEFAULT)
model.classifier[1] = nn.Linear(model.classifier[1].in_features, 2)

# Freeze the feature extractor
for param in model.features.parameters():
    param.requires_grad = False

# Replace the classifier head
model.classifier[1] = nn.Linear(model.classifier[1].in_features, 2)
# classifier[1] is NOT frozen — it's newly created, so requires_grad=True by default

model = model.to(device)
```

### Training and validating

##### Setup

```python
EPOCHS = 5

class_counts = train_df["class_id"].value_counts().sort_index().values
class_weights = 1.0 / torch.tensor(class_counts, dtype=torch.float)
class_weights = class_weights / class_weights.sum()
criterion = nn.CrossEntropyLoss(weight=class_weights.to(device), label_smoothing=0.2)

optimizer = torch.optim.Adam(model.classifier.parameters(), lr=1e-3)
scheduler = CosineAnnealingLR(optimizer, T_max=EPOCHS, eta_min=1e-6)

scaler = GradScaler()

# After a few epochs, unfreeze and fine-tune the whole network at a much lower learning rate
```

##### Train + validate

```python
def run_epoch(loader, training=True):
    model.train() if training else model.eval()
    total_loss, correct, total = 0, 0, 0
    with torch.set_grad_enabled(training):
        for i, (images, labels) in enumerate(loader):
            images, labels = images.to(device), labels.to(device)
            
            with autocast():
                outputs = model(images)
                loss = criterion(outputs, labels)
            
            if training:
                optimizer.zero_grad()
                scaler.scale(loss).backward()
                scaler.step(optimizer)
                scaler.update()
            
            total_loss += loss.item() * len(labels)
            correct    += (outputs.argmax(1) == labels).sum().item()
            total      += len(labels)

            if i % 10 == 0:
                print(f"  Batch {i}/{len(loader)} | loss: {loss.item():.4f}", flush=True)

    return total_loss / total, correct / total
```

```python
best_val_loss = float('inf')

for epoch in range(1, EPOCHS + 1):
    train_loss, train_acc = run_epoch(train_loader, training=True)
    val_loss,   val_acc   = run_epoch(val_loader,   training=False)
    scheduler.step()

    print(f"Epoch {epoch}/{EPOCHS}", flush=True)
    print(f"Train loss: {train_loss:.4f} acc: {train_acc:.4f}", flush=True)
    print(f"Val loss:   {val_loss:.4f} acc: {val_acc:.4f}", flush=True)

    if val_loss < best_val_loss:
        best_val_loss = val_loss
        torch.save(model.state_dict(), f"best_model_{epoch}.pth")
        print("  ✓ Saved best model", flush=True)
```

##### Fine-tune

Unfreeze layer 4

```python
optimizer = torch.optim.Adam(model.classifier.parameters(), lr=1e-3)
scheduler = CosineAnnealingLR(optimizer, T_max=3, eta_min=1e-6)

# Fine-tune: unfreeze last few feature blocks
for param in model.features[6:].parameters():
    param.requires_grad = True

optimizer = torch.optim.Adam([
    {"params": model.features[6:].parameters(), "lr": 1e-4},
    {"params": model.classifier.parameters(),   "lr": 1e-4},
])

for epoch in range(1, 4):
    train_loss, train_acc = run_epoch(train_loader, training=True)
    val_loss,   val_acc   = run_epoch(val_loader,   training=False)
    scheduler.step()
    print(f"Fine-tune {epoch}/3 | "
          f"Train loss: {train_loss:.4f} acc: {train_acc:.4f} | "
          f"Val loss: {val_loss:.4f} acc: {val_acc:.4f}")
```

Unfreeze the rest of the layers

```python
for param in model.parameters():
    param.requires_grad = True

optimizer = torch.optim.Adam(model.parameters(), lr=1e-5)
scheduler = CosineAnnealingLR(optimizer, T_max=3, eta_min=1e-6)

for epoch in range(1, 4):
    train_loss, train_acc = run_epoch(train_loader, training=True)
    val_loss,   val_acc   = run_epoch(val_loader,   training=False)
    scheduler.step()
    print(f"Fine-tune {epoch}/3 | "
          f"Train loss: {train_loss:.4f} acc: {train_acc:.4f} | "
          f"Val loss: {val_loss:.4f} acc: {val_acc:.4f}")
```

### Evaluate

```python
model.eval()
all_preds, all_labels = [], []

with torch.no_grad():
    for images, labels in test_loader:
        images = images.to(device)
        preds  = model(images).argmax(1).cpu().numpy()
        all_preds.extend(preds)
        all_labels.extend(labels.numpy())

# label_names came from pd.factorize earlier
print(classification_report(all_labels, all_preds, target_names=label_names))
```

```python
print(label_names)
```

#### Confidence-threshold inference

Returns `'car'` only when the model's softmax score exceeds `CAR_THRESHOLD`. Anything the model is uncertain about falls back to `'no-car'`.

```python
# ── Confidence threshold ────────────────────────────────────────────────────
# Raise this to reduce false positives (faces, unknown objects labelled as car).
# Lower it if you find real cars are being missed.
CAR_THRESHOLD = 0.90

# Determine which index corresponds to 'car' from the factorized label_names
_car_class_idx = int((label_names == 'car').nonzero()[0][0])

def predict_image(model, image_tensor: torch.Tensor, threshold: float = CAR_THRESHOLD) -> dict:
    """
    Predict a single pre-transformed image tensor.

    Args:
        model:        trained EfficientNet model (already on `device`)
        image_tensor: CHW float tensor, normalised with ImageNet stats
        threshold:    minimum softmax probability to call something a car

    Returns:
        dict with keys 'label' ('car' | 'no-car') and 'confidence' (float)
    """
    model.eval()
    with torch.no_grad():
        logits = model(image_tensor.unsqueeze(0).to(device))  # (1, 2)
        probs  = F.softmax(logits, dim=1)                     # (1, 2)
        car_prob = probs[0, _car_class_idx].item()

    label = 'car' if car_prob >= threshold else 'no-car'
    return {'label': label, 'confidence': round(car_prob, 4)}


# ── Batch evaluation with threshold (replaces argmax in the eval loop) ────────
model.eval()
thresh_preds, all_labels_thresh = [], []

with torch.no_grad():
    for images, labels in test_loader:
        images = images.to(device)
        probs  = F.softmax(model(images), dim=1)          # (B, 2)
        car_probs = probs[:, _car_class_idx].cpu().numpy()
        preds = (car_probs >= CAR_THRESHOLD).astype(int)  # 1=car, 0=no-car

        # Map back: _car_class_idx==1 means car=1, no-car=0; adjust if reversed
        if _car_class_idx == 0:
            preds = 1 - preds  # flip so car=1 always

        thresh_preds.extend(preds)
        all_labels_thresh.extend(labels.numpy())

print(f"\n── Thresholded evaluation (threshold={CAR_THRESHOLD}) ──")
print(classification_report(all_labels_thresh, thresh_preds, target_names=label_names))

# ── Quick single-image usage example ─────────────────────────────────────────
# from PIL import Image
# img = Image.open('your_image.jpg').convert('RGB')
# tensor = val_test_transform(img)
# result = predict_image(model, tensor)
# print(result)  # {'label': 'no-car', 'confidence': 0.0312}
```

### Save

```python
torch.save(model.state_dict(), "/kaggle/working/car_classifier.pth")
```

```python
model.eval()
model_cpu = model.cpu()  # move model to CPU regardless of training device

example = torch.randn(1, 3, 224, 224)  # CPU tensor (no .to(device))
scripted = torch.jit.trace(model_cpu, example)
scripted.save("car_classifier.pt")
```

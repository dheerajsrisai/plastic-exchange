# PlastiSwap – Delivery Integrated Plastic Exchange Platform

Spring Boot + React.js + MongoDB + AI/ML (MobileNetV2 via FastAPI).

Users photograph plastic, the AI verifies it, an agent is auto-assigned, the agent weighs it at pickup,
and the user receives reward points that can be redeemed.

```
React (Vite)  ──/api──>  Spring Boot (JWT)  ──>  MongoDB
                              └──/predict──>  FastAPI ML service
```

## Option A – Run everything with Docker (easiest)

Requires Docker Desktop.

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| App (React) | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| ML service | http://localhost:8000/health |

The first build takes several minutes (it downloads PyTorch and Maven dependencies).
The ML service downloads the MobileNetV2 weights (~14 MB) the first time it starts.

## Option B – Run each part manually

Requires: Java 17+, Maven 3.9+, Node 18+, Python 3.10+, MongoDB running on localhost:27017.

```bash
# 1. ML service (terminal 1)
cd ml-service
python -m venv venv && source venv/bin/activate      # Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn app:app --port 8000

# 2. Backend (terminal 2)
cd backend
mvn spring-boot:run

# 3. Frontend (terminal 3)
cd frontend
npm install
npm run dev                                           # http://localhost:5173
```

The backend still works if the ML service is off: every pickup is simply sent for manual admin review.

## Default accounts

| Role | Login |
|---|---|
| Admin | `admin@plastic.com` / `admin123` (created automatically) |
| User / Agent | Register from the sign-up page |

## Try the full flow

1. Register an **agent** (area e.g. `Madanapalle`, with a vehicle number).
2. Log in as **admin** → *Delivery agents* → **Approve** the agent.
3. Register a **user** with the same area, then *Schedule pickup* with a photo of a plastic bottle.
   - AI confident → status goes to *Agent assigned* automatically.
   - AI unsure → appears in admin *Pickups* for **Approve / Reject**.
4. Log in as the agent → enter the weighed kg → **Confirm weight** (points are awarded) → **Mark completed**.
5. The user sees points under *Rewards* and can redeem them.

Pickup status flow: `REQUESTED → VERIFIED → SCHEDULED → ASSIGNED → COLLECTED → COMPLETED` (or `REJECTED`).

## REST API summary

| Method | Endpoint | Role |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| POST | `/api/pickups` (multipart: image, plasticType, estimatedWeightKg, address, area, scheduledAt) | user |
| GET | `/api/pickups/my`, `/api/pickups/{id}` | user |
| PUT | `/api/pickups/{id}/schedule?scheduledAt=` | user |
| GET | `/api/rewards/balance`, `/api/rewards/catalog` | user |
| POST | `/api/rewards/redeem` `{item}` | user |
| GET | `/api/agent/me`, `/api/agent/pickups` | agent |
| PUT | `/api/agent/availability`, `/api/agent/pickups/{id}/collect`, `/api/agent/pickups/{id}/complete` | agent |
| GET | `/api/admin/stats`, `/agents`, `/pickups`, `/rates` | admin |
| PUT | `/api/admin/agents/{id}/approve`, `/api/admin/pickups/{id}/review`, `/api/admin/rates/{type}` | admin |

## AI/ML notes

* **Out of the box** the service uses MobileNetV2 pre-trained on ImageNet and maps classes like *water bottle*,
  *pop bottle*, *plastic bag* to plastic types. This is a demo mapping, good enough to show the workflow.
  The auto-verify threshold is `ai.confidence-threshold` (default `0.5`, in `application.properties`).
* **For real accuracy**, train on your own dataset (TrashNet, Kaggle waste datasets):

  ```bash
  cd ml-service
  # data/train/<CLASS>/*.jpg and data/val/<CLASS>/*.jpg  (e.g. PET, HDPE, PP, NON_PLASTIC)
  python train.py --data data --epochs 8
  ```
  This writes `models/plastic_model.pt` and `models/classes.json`; restart the ML service and it uses them
  automatically. Then raise the threshold to ~0.75.

## Configuration

Environment variables (all optional): `MONGO_URI`, `JWT_SECRET` (32+ chars, change it!), `ML_URL`, `UPLOAD_DIR`, `AI_THRESHOLD`.

## Project structure

```
plastic-exchange/
├── backend/      Spring Boot (controllers, services, models, JWT security)
├── frontend/     React + Vite (pages: login, dashboard, pickups, rewards, agent, admin)
├── ml-service/   FastAPI + PyTorch plastic classifier (+ train.py)
└── docker-compose.yml
```

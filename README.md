# ArcaBank

An online banking system that allows users to manage accounts, make P2P transfers, and create shared "vaults" with an Escrow voting mechanism for secure fund withdrawals.

## 🚀 Tech Stack
- **Backend:** Java 21, Spring Boot, PostgreSQL
- **Frontend:** Angular 17 LTS, TypeScript, Tailwind CSS
- **Infrastructure:** Docker, Docker Compose

## 🛠 Getting Started

### 1. Prerequisites
Ensure you have the following installed on your local machine:
- `Java 21`
- `Node.js (v18+)`
- Angular CLI (`npm install -g @angular/cli`)
- `Docker` & `Docker Compose`

### 2. Infrastructure Setup (Database)
Run the following command to start the PostgreSQL database in a Docker container:
```bash
docker-compose up -d
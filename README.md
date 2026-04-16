# ArcaBank

An online banking system that allows users to manage accounts, make P2P transfers, and create shared "vaults" with an Escrow voting mechanism for secure fund withdrawals.

## 🚀 Tech Stack
- **Architecture:** Microservices (API Gateway, Auth, Core Finance, Notification)
- **Backend:** Java 21, Spring Boot 3.x, gRPC
- **Database & Migrations:** PostgreSQL, Flyway
- **Infrastructure & Messaging:** Docker, Apache Kafka, Zookeeper
- **Identity & Access Management:** Keycloak, OAuth2/JWT
- **Frontend:** Angular 17 LTS, TypeScript, Tailwind CSS

## 🛠 Getting Started

### 1. Prerequisites
Ensure you have the following installed on your local machine:
- [Docker](https://docs.docker.com/get-docker/) & [Docker Compose](https://docs.docker.com/compose/install/) (Recommended for full stack execution)
- `Java 21` (For local backend development)
- `Node.js` (v18+) & Angular CLI (`npm install -g @angular/cli`) (For local frontend development)

### 2. Installation & Running (Docker Way - Recommended)
The easiest way to start the entire infrastructure (Databases, Message Brokers, Keycloak, and all Microservices) is using Docker Compose.

1. Clone the repository:
   ```bash
   git clone https://github.com/Darmohrai/ArcaBank.git
   cd arcabank
   ```
2. Set up environment variables:
   ```bash
   cp .env.example .env
   ```
3. Build and start the entire system:
   ```bash
   docker-compose up -d --build
   ```
*Note: On the first run, the `init-dbs.sh` script will automatically create isolated databases for each microservice.*

### 3. Local Development (Running without Docker)
If you prefer to run services individually via your IDE or terminal for debugging:

1. Start only the foundational infrastructure (DBs, Kafka, Keycloak):
   ```bash
   docker-compose up -d postgres zookeeper kafka keycloak
   ```
2. Build the shared gRPC contracts first:
   ```bash
   cd server
   mvn clean install -pl common-proto -am -DskipTests
   ```
3. Start the Backend microservices (run each in a separate terminal or via IDE):
   ```bash
   mvn spring-boot:run -pl core-finance
   mvn spring-boot:run -pl auth-service
   # ... run other services similarly
   ```
4. Start the Frontend:
   ```bash
   cd ../client
   npm install
   ng serve

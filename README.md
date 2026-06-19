![img.png](docs/marketing/branding/arcabank_banner.png)

### ArcaBank - Your space, common rules.

An online banking system that allows users to manage accounts, make P2P transfers, and create shared "vaults" with an Escrow voting mechanism for secure fund withdrawals.

## 🛠 Tech Stack

![Angular](https://img.shields.io/badge/Angular_17-191919?style=for-the-badge&logo=angular&logoColor=299D91)
![TypeScript](https://img.shields.io/badge/TypeScript-191919?style=for-the-badge&logo=typescript&logoColor=299D91)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-191919?style=for-the-badge&logo=tailwind-css&logoColor=299D91)

![Java 21](https://img.shields.io/badge/Java_21-191919?style=for-the-badge&logo=openjdk&logoColor=299D91)
![Spring Boot 3](https://img.shields.io/badge/Spring_Boot_3-191919?style=for-the-badge&logo=spring-boot&logoColor=299D91)
![gRPC](https://img.shields.io/badge/gRPC-191919?style=for-the-badge&logo=grpc&logoColor=299D91)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-191919?style=for-the-badge&logo=apache-kafka&logoColor=299D91)

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-191919?style=for-the-badge&logo=postgresql&logoColor=299D91)
![Docker](https://img.shields.io/badge/Docker-191919?style=for-the-badge&logo=docker&logoColor=299D91)
![Figma](https://img.shields.io/badge/Figma-191919?style=for-the-badge&logo=figma&logoColor=299D91)
## 🛠 Getting Started

### 1. Prerequisites
Ensure you have the following installed on your local machine:
- [Docker](https://docs.docker.com/get-docker/) & [Docker Compose](https://docs.docker.com/compose/install/) (Recommended for full stack execution)
- `Java 21` (For local backend development)
- `Node.js` (v18+) & Angular CLI (`npm install -g @angular/cli`) (For local frontend development)
- `Maven 3.8+` (For building backend services)

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


### API Documentation (Swagger)
Once the system is running, explore and test the endpoints via Swagger UI:

Core Finance Service: http://localhost:8088/swagger-ui/index.html#/

Auth Service: http://localhost:8081/swagger-ui/index.html#/

Frontend Application
URL: http://localhost:4200

#### Test Credentials
Watch arcabank-realm in deploy.

#

## For User

#### Create account
![img.png](docs/marketing/assets/create_acc.gif)

#### Create card
![img.png](docs/marketing/assets/create_card.gif)

#### Create arca
![img.png](docs/marketing/assets/create_arca.gif)

#### Main screen
![img.png](docs/marketing/assets/main_screen.png)

#### Register screen
![img.png](docs/marketing/assets/register_screen.png)

#### Transaction history screen
![img.png](docs/marketing/assets/transaction_screen.png)

#### Video
https://youtu.be/739LhN-NiTU


## 🚀 Marketing Kit & Strategy
[tap here to see more](docs/marketing)

[tap here to see notion](https://app.notion.com/p/ArcaBank-Project-324fd27e06318077b087e546f5e0810c)

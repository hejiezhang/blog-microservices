# 📰 Blog Microservices Application

A **microservices-based Blog Platform** built with **Spring Boot**, following a **DB-per-service** architecture.  
Each microservice manages its own data and communicates via **OpenFeign** (synchronous REST).  
The system includes **AI integration**, centralized **monitoring (Prometheus + Grafana)**, and containerized **PostgreSQL** databases.

---

## 🚀 Architecture Overview

This application consists of **five independent Spring Boot services**:

| Service | Description | Port | Database |
|----------|--------------|------|-----------|
| 🧑‍💻 `user-service` | Manages user profiles and authentication | 8081 | `userdb` |
| 📝 `post-service` | Handles blog post creation, updates, and listing | 8082 | `postdb` |
| 💬 `comment-service` | Manages comments on posts | 8083 | `commentdb` |
| 🌐 `api-gateway` | Spring Cloud Gateway for unified routing | 8080 | — |
| 🤖 `ai-service` | Provides AI-driven content analysis and text generation | 8084 | — |

Each service is **independently deployable**, **maintains its own database**, and exposes REST APIs for interaction.

---

## 🧩 Key Features

- **Microservices Architecture** — loosely coupled services, each with its own PostgreSQL DB.
- **OpenFeign Integration** — inter-service REST communication.
- **AI Integration** — AI-powered service for generating or summarizing content.
- **Spring Boot Actuator + Prometheus + Grafana** — real-time metrics and dashboards.
- **Centralized Logging** — structured logs saved under `/log` directory.
- **Integration Testing** — using **RestAssured** for end-to-end service tests.
- **API Gateway** — unified entry point for routing and load balancing.

---

## 🗂️ Project Structure

```
blog-microservices/
│
├── user-service/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── post-service/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── comment-service/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── api-gateway/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── ai-service/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── pom.xml
├── docker-compose.yml
├── prometheus.yml
└── README.md
```

---

## ⚙️ Technology Stack

| Category | Technologies |
|-----------|---------------|
| **Backend Framework** | Spring Boot 3.x |
| **API Communication** | OpenFeign |
| **Database** | PostgreSQL (DB-per-service) |
| **Testing** | JUnit 5, RestAssured |
| **Monitoring** | Prometheus, Grafana |
| **Logging** | SLF4J + Logback (output to `/log`) |
| **Containerization** | Docker, Docker Compose |
| **Gateway** | Spring Cloud Gateway |
| **AI Integration** | Custom AI service (e.g., OpenAI API or local model) |

---

## 🐳 Docker Compose Setup

The included `docker-compose.yml` provisions:
- Three PostgreSQL databases (userdb, postdb, commentdb)
- Prometheus and Grafana monitoring stack

### Start the stack

```bash
docker-compose up -d
```

### Stop the stack

```bash
docker-compose down
```

### Access Points

| Service | URL |
|----------|-----|
| User DB | `localhost:5432` |
| Post DB | `localhost:5433` |
| Comment DB | `localhost:5434` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |

---

## 📊 Monitoring with Prometheus & Grafana

1. Each service exposes metrics via **Spring Boot Actuator** at:
   ```
   http://localhost:<service-port>/actuator/prometheus
   ```
2. **Prometheus** scrapes these metrics based on `prometheus.yml` configuration.
3. **Grafana** connects to Prometheus as a data source.

---

## 🧠 AI Integration (ai-service)

The **AI Service** enhances the blogging experience by integrating language model capabilities such as:

- Generating draft post
- Generating a comment for a certain post

---

## 🧪 Integration Testing

Each service includes **integration tests** under `src/test/java` using **RestAssured**:

```java
given()
    .baseUri("http://localhost:8081/users")
.when()
    .get("/1")
.then()
    .statusCode(200)
    .body("username", equalTo("john_doe"));
```

Run tests via Maven:

```bash
mvn test
```

---

## 📁 Logs

All application logs are stored under:

```
/log
  ├── user-service.log
  ├── post-service.log
  ├── comment-service.log
  ├── api-gateway.log
  └── ai-service.log
```

Each service uses **Logback** with rolling file appenders for production-safe logging.

---

## 🚦 API Gateway Routes

Example routes in `api-gateway`:

| Path | Routed To |
|------|------------|
| `/users/**` | user-service |
| `/posts/**` | post-service |
| `/comments/**` | comment-service |
| `/ai/**` | ai-service |

Example request:

```bash
curl http://localhost:8080/posts/1
```

---

## 🧰 Useful Commands

| Action | Command |
|--------|----------|
| Build all services | `mvn clean package -DskipTests` |
| Build Docker images | `docker build -t <service-name> .` |
| Run all services | `docker-compose up -d` |
| View logs | `docker-compose logs -f` |
| Stop all containers | `docker-compose down` |

---

## 🧑‍💻 Authors

**Hejie Zhang**  
Full-stack developer passionate about distributed systems, observability, and AI-enhanced software design.

---

## 📄 License

This project is licensed under the **MIT License** — feel free to use and modify with attribution.

---

### 🌟 Future Enhancements

- ✅ Add Kafka for asynchronous event streaming
- ✅ Introduce distributed tracing with Zipkin
- ✅ Deploy to Kubernetes with Helm charts
- ✅ Enable CI/CD with GitHub Actions

---

> _“Monitor everything, test thoroughly, and automate always.”_

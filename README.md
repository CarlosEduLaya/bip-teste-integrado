# Desafio Técnico – SICOOB

Solução fullstack para o desafio técnico, cobrindo banco de dados, módulo EJB, API REST e frontend Angular.

---



## Estrutura

```
├── db/               → scripts SQL (schema + seed)
├── ejb-module/       → serviço EJB com correção do bug de transferência
├── backend-module/   → API REST Spring Boot
├── frontend/         → app Angular 17
└── .github/          → pipeline CI/CD
```

---

## O que foi corrigido no EJB

O método `transfer` original tinha três problemas sérios:

- Não verificava se o saldo era suficiente antes de debitar, dava pra deixar valor negativo
- Sem nenhum tipo de locking, em ambiente concorrente duas threads podiam ler o mesmo saldo e as duas aprovar a transferência (lost update)
- Sem validações de entrada, então um `null` ou valor negativo passava direto

A correção aplicada em `BeneficioEjbService`:
- Validações de entrada antes de qualquer acesso ao banco
- `PESSIMISTIC_WRITE` lock nos dois registros envolvidos na transferência
- `@TransactionAttribute(REQUIRED)` garantindo rollback automático em qualquer falha
- A entidade `Beneficio` também usa `@Version` como camada extra de proteção

---

## Como rodar

### Requisitos
- Java 17+
- Maven 3.8+
- Node 20+ / npm

### Backend

```bash
cd backend-module
mvn spring-boot:run
```

Sobe na porta `8080`. Por padrão usa H2 em memória, já com os dados do seed carregados automaticamente.

- API: `http://localhost:8080/api/v1/beneficios`
- Swagger: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bipdb`)

### Frontend

```bash
cd frontend
npm install
npm start
```

Abre em `http://localhost:4200`. O proxy já está configurado pra redirecionar `/api` pro backend em `8080`, então basta ter os dois rodando.

### EJB (compilação e testes)

```bash
cd ejb-module
mvn verify
```

---

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/beneficios` | Lista todos |
| GET | `/api/v1/beneficios/{id}` | Busca por ID |
| POST | `/api/v1/beneficios` | Cria novo |
| PUT | `/api/v1/beneficios/{id}` | Atualiza |
| DELETE | `/api/v1/beneficios/{id}` | Desativa (exclusão lógica) |
| POST | `/api/v1/beneficios/transfer` | Transferência entre benefícios |

Exemplo de transferência:

```bash
curl -X POST http://localhost:8080/api/v1/beneficios/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromId": 1, "toId": 2, "amount": 100.00}'
# 204 No Content
```

---

## Testes

```bash
# Backend — unitários + integração (20 testes)
cd backend-module && mvn test

# EJB — unitários com Mockito (9 testes)
cd ejb-module && mvn test
```

---

## Banco de dados

O projeto usa H2 em memória por padrão. Para usar PostgreSQL, edite `backend-module/src/main/resources/application.properties`, descomente o bloco do PostgreSQL e comente o do H2. Depois execute os scripts:

```bash
psql -U postgres -f db/schema.sql
psql -U postgres -d bipdb -f db/seed.sql
```

---

## Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA, Bean Validation, springdoc-openapi
- **EJB:** Jakarta EE 10, JPA com pessimistic locking
- **Banco:** H2 (dev) / PostgreSQL (prod)
- **Frontend:** Angular 17, standalone components, ngx-mask
- **CI:** GitHub Actions

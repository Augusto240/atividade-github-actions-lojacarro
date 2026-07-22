# LojaCarro

API REST de uma loja de carros (Spring Boot + Java 17) com autenticação JWT,
controle de acesso por papel (GERENTE / VENDEDOR), rate limiting, e um frontend
simples em HTML/CSS/JS puro.

Autor: José Augusto

---

## Como rodar (leia isto primeiro)

Há duas formas. **A Forma 1 é a recomendada e a mais garantida** — sobe tudo
(backend + banco MySQL + frontend) com um único comando e **não precisa instalar
Java nem MySQL, nem configurar variável de ambiente nenhuma**.

> Importante sobre o JWT: o segredo do token (`JWT_SECRET`) já vem com um valor
> padrão embutido no projeto. Ou seja, **a aplicação sobe sozinha, não é preciso
> definir nenhuma variável de ambiente para o JWT funcionar.**

---

### Forma 1 — Docker (recomendada, um comando só)

Pré-requisito: ter o **Docker** instalado (Docker Desktop no Windows/Mac).

Na pasta do projeto, rode:

```bash
docker compose up --build
```

Isso sobe três serviços juntos:

| Serviço  | Endereço                       | O que é                    |
|----------|--------------------------------|----------------------------|
| Frontend | http://localhost:8081          | Tela web (login, carros)   |
| Backend  | http://localhost:8080          | API REST                   |
| MySQL    | localhost:3306 (banco `josecarro`) | Banco de dados         |

Quando aparecer no log `Started LojaCarroApplication`, está pronto.
Abra **http://localhost:8081** no navegador.

Para parar: `Ctrl+C` e depois `docker compose down`.

---

### Forma 2 — Manual (Java + MySQL na máquina)

Pré-requisitos:
- **Java 17**
- **MySQL** rodando em `localhost:3306`
- O Maven Wrapper (`mvnw`) já vem no projeto, não precisa instalar Maven.

Credenciais que o projeto usa por padrão:

- Banco: **`josecarro`** (é criado automaticamente na primeira execução)
- Usuário: **`root`**
- Senha: **`root`**

Passos:

```bash
# 1. Backend (na pasta do projeto)
./mvnw spring-boot:run
#   No Windows (cmd/powershell): mvnw spring-boot:run
```

O backend sobe em **http://localhost:8080**.

```bash
# 2. Frontend (em outro terminal, na pasta do projeto)
cd frontend
python3 -m http.server 8081
```

O frontend sobe em **http://localhost:8081**.

#### Se o seu MySQL usar credenciais/host diferentes

Não precisa editar código. Basta definir variáveis de ambiente antes de subir
o backend (todas são opcionais e têm o valor padrão mostrado):

| Variável      | Padrão      | Para que serve                    |
|---------------|-------------|-----------------------------------|
| `DB_HOST`     | `localhost` | Host do MySQL                     |
| `DB_PORT`     | `3306`      | Porta do MySQL                    |
| `DB_NAME`     | `josecarro` | Nome do banco                     |
| `DB_USERNAME` | `root`      | Usuário do banco                  |
| `DB_PASSWORD` | `root`      | Senha do banco                    |
| `JWT_SECRET`  | (valor padrão embutido) | Segredo de assinatura do JWT |

Exemplo (Linux/Mac), apontando para um MySQL com usuário `admin`:

```bash
DB_USERNAME=admin DB_PASSWORD=admin ./mvnw spring-boot:run
```

---

## Como usar

1. Abra **http://localhost:8081**.
2. Clique em **"Registre-se"** e crie uma conta:
   - **GERENTE**: pode cadastrar, editar e excluir carros.
   - **VENDEDOR**: só consulta a lista de carros.
3. Faça login e use o sistema.

Endpoint público de teste (não precisa de token):
`GET http://localhost:8080/boas-vindas`

---

## Endpoints principais

| Método | Rota              | Acesso            |
|--------|-------------------|-------------------|
| GET    | `/boas-vindas`    | Público           |
| POST   | `/auth/register`  | Público           |
| POST   | `/auth/login`     | Público           |
| GET    | `/carro`          | Autenticado       |
| GET    | `/carro/{id}`     | Autenticado       |
| POST   | `/carro/salvar`   | Somente GERENTE   |
| PUT    | `/carro/{id}`     | Somente GERENTE   |
| DELETE | `/carro/{id}`     | Somente GERENTE   |
| POST   | `/usuarios`       | Somente GERENTE   |

Autenticação: envie o token no header `Authorization: Bearer <token>`.
O token é retornado no login/registro e expira em 8 horas.

---

## Rodar os testes

```bash
./mvnw test
```

O projeto tem testes unitários e de integração (JUnit 5), cobertura via JaCoCo
(mínimo 80%), análise de qualidade com SonarCloud e testes de mutantes com PIT.

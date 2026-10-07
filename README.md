# LojaCarro - API

API da loja de carros em Spring Boot (Java 17). Cuida dos carros, dos logs e da auditoria.

Autor: Jose Augusto

O sistema foi dividido em 3 projetos:

| Projeto | O que faz | Porta |
|---------|-----------|-------|
| lojacarro (este) | CRUD de carros, logs e auditoria | 8080 |
| lojacarro-usuarios-api | CRUD de usuarios | 8081 |
| lojacarro-frontend | Telas em Angular | 4200 |

## Comunicacao com a API de usuarios

A LojaCarro nao tem mais usuarios. Toda operacao com carro recebe o id do usuario no header `X-Usuario-Id`,
a LojaCarro chama a API de usuarios em `GET /usuarios/{id}/nome` e grava o nome que voltou no log e na auditoria.

```
POST /carro/salvar  (X-Usuario-Id: 2)
   -> GET http://localhost:8081/usuarios/2/nome
   <- {"nome": "Maria Oliveira"}
log: "Maria Oliveira (#2) cadastrou o carro #1 Toyota Corolla 2024"
```

- cadastrar, editar e excluir carro: precisa do header. Usuario que nao existe recebe 401
- consultar carros: o header e opcional, se vier o nome tambem vai pro log
- se a API de usuarios estiver fora do ar: cadastro, edicao e exclusao devolvem 503 (sem o nome nao da pra
  registrar quem fez). A consulta continua funcionando e o log fica so com o id

A chamada usa `RestClient` com timeout de 2s para conectar e 3s para responder (`application.properties`).
O endereco da API fica em `usuarios.api.url` e pode ser trocado pela variavel `USUARIOS_API_URL`.

O header `X-Request-Id` e repassado para a API de usuarios, entao a mesma requisicao aparece com o mesmo id no
log dos dois projetos.

## Como rodar

### Docker (os 3 projetos)

Os 3 repositorios precisam estar na mesma pasta:

```
pasta/
  atividade-github-actions-lojacarro/
  lojacarro-usuarios-api/
  lojacarro-frontend/
```

Dentro desta pasta:

```bash
docker compose up --build
```

- Frontend: http://localhost:4200
- LojaCarro: http://localhost:8080
- Usuarios: http://localhost:8081

### Sem Docker

Primeiro sobe a API de usuarios (ver o README dela). Depois:

```bash
./mvnw clean package
SERVER=dev BD_NAME=josecarro BD_USER=root BD_PASS=root java -jar target/augusto.war
```

O perfil vem da variavel `SERVER`:

- `dev`: usa as variaveis `BD_NAME`, `BD_USER` e `BD_PASS`
- `prod`: usa o banco `carros` com o usuario `augusto`

## Logs

Cada requisicao recebe um id (`X-Request-Id`) que aparece em todas as linhas de log dela, junto com o nome do
usuario. Arquivos na pasta `logs/`:

- `lojacarro.log`: log geral
- `erros.log`: so os erros
- `auditoria.log`: copia da auditoria
- `lojacarro.json`: so no perfil prod, em JSON

## Auditoria (WORM)

As operacoes com carros ficam na tabela `auditoria`, que so recebe insert. Cada registro guarda o hash do
anterior, e `GET /auditoria/integridade` mostra se algum registro foi alterado ou apagado direto no banco.

## Endpoints

| Metodo | Rota | Header X-Usuario-Id |
|--------|------|---------------------|
| GET | `/carro`, `/carro/{id}` | opcional |
| POST | `/carro/salvar` | obrigatorio |
| PUT, DELETE | `/carro/{id}` | obrigatorio |
| GET | `/auditoria`, `/auditoria/integridade` | obrigatorio |
| POST | `/logs` | - |
| GET | `/boas-vindas` | - |

## Testes

```bash
./mvnw clean verify
```

Nos testes a API de usuarios e simulada com `@MockitoBean` e `MockRestServiceServer`, entao eles rodam sem ela.
O JaCoCo exige 80% de cobertura.

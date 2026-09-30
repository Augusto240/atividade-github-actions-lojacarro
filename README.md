# LojaCarro

Projeto da disciplina: API de uma loja de carros em Spring Boot (Java 17) com frontend em Angular.

Autor: Jose Augusto

## O que tem

- CRUD de carros
- CRUD de usuarios (id, nome e cargo)
- Controle de acesso pelo cargo, sem biblioteca de autorizacao
- Logs em todas as camadas
- Auditoria das acoes (WORM)

## Como rodar

### Docker

```bash
docker compose up --build
```

- Frontend: http://localhost:8081
- Backend: http://localhost:8080
- MySQL: localhost:3306 (banco `josecarro`, usuario `root`, senha `root`)

### Sem Docker

O perfil e escolhido pela variavel `SERVER`:

- `dev`: usa as variaveis `BD_NAME`, `BD_USER` e `BD_PASS`
- `prod`: usa o banco `carros` com o usuario `augusto`

```bash
./mvnw clean package
SERVER=dev BD_NAME=josecarro BD_USER=root BD_PASS=root java -jar target/augusto.war
```

Frontend (em outro terminal):

```bash
cd frontend
npm install
npm start
```

Abre em http://localhost:4200. As chamadas para `/api` sao redirecionadas para o backend (`proxy.conf.json`).

Quando o banco esta vazio o sistema cria um usuario `Administrador` com cargo GERENTE, para dar pra cadastrar os outros.

## Usuarios e cargos

O usuario que esta usando o sistema e escolhido no topo da tela. O front manda o id dele no header `X-Usuario-Id`
e o backend confere o cargo na classe `ControleAcessoUsuario`.

- GERENTE: cadastra, edita e exclui usuarios e carros, e ve a auditoria
- VENDEDOR: so consulta
- sem o header: so consulta

Outras regras: ninguem exclui o proprio usuario e o ultimo gerente nao pode virar vendedor.

## Logs

Cada classe tem seu logger (SLF4J/Logback) usando info, warn, error e debug.

O `RequisicaoLogFilter` registra toda requisicao que chega e o status que ela devolveu. Cada requisicao recebe um
id (header `X-Request-Id`) que aparece em todas as linhas de log dela, junto com o usuario. O front tambem gera
esse id, entao da pra achar no log do servidor uma acao feita na tela.

Erros que acontecem no front sao mandados para `POST /logs` e aparecem no log do servidor.

Arquivos na pasta `logs/`:

- `lojacarro.log`: log geral (rotaciona todo dia)
- `erros.log`: so os erros
- `auditoria.log`: copia da auditoria
- `lojacarro.json`: so no perfil prod, em JSON para ferramentas de log na nuvem

## Auditoria (WORM)

WORM significa write once, read many: o registro e gravado uma vez e nao pode ser alterado depois.

- a tabela `auditoria` so recebe insert, o repositorio nao tem update nem delete
- cada registro guarda o hash SHA-256 do registro anterior
- `GET /auditoria/integridade` recalcula os hashes. Se alguem alterar ou apagar um registro direto no banco, ele mostra
  qual registro foi mexido

Sao registrados: cadastro, edicao e exclusao de usuarios e carros, e tentativas de acesso negado.

## Endpoints

| Metodo | Rota | Acesso |
|--------|------|--------|
| GET | `/carro`, `/carro/{id}` | livre |
| POST | `/carro/salvar` | GERENTE |
| PUT, DELETE | `/carro/{id}` | GERENTE |
| GET | `/usuarios`, `/usuarios/{id}` | livre |
| POST | `/usuarios` | GERENTE |
| PUT, DELETE | `/usuarios/{id}` | GERENTE |
| GET | `/auditoria`, `/auditoria/integridade` | GERENTE |
| POST | `/logs` | livre |
| GET | `/boas-vindas` | livre |

Limite de 100 requisicoes por minuto por IP.

## Testes

```bash
./mvnw clean verify
```

Testes unitarios (Mockito) e de integracao (MockMvc com H2). O JaCoCo exige no minimo 80% de cobertura.

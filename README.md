# LojaCarro

API da loja de carros que venho fazendo na disciplina. Spring Boot com Java 17.

Autor: Jose Augusto

O projeto acabou ficando dividido em 4 repositorios:

- lojacarro (este): CRUD de carros, logs e auditoria
- lojacarro-usuarios-api: CRUD de usuarios
- lojacarro-log: modulo de log, recebe as operacoes dos carros pelo RabbitMQ
- lojacarro-frontend: as telas em Angular

## Usuarios

A LojaCarro nao guarda mais usuario nenhum. Quem mexe nos carros manda o proprio id no header `X-Usuario-Id`, a
LojaCarro pergunta o nome para a API de usuarios (`GET /usuarios/{id}/nome`) e usa esse nome no log e na auditoria.

Para cadastrar, editar ou excluir carro o header e obrigatorio. Se a API de usuarios estiver fora do ar essas tres
operacoes dao 503, porque sem o nome nao tem como registrar quem fez. Listar carros funciona mesmo assim.

## RabbitMQ

Sempre que um carro e cadastrado, atualizado ou excluido a LojaCarro manda um `OperacaoCarroDTO` em JSON para a fila
`augusto`. Quem le essa fila e o lojacarro-log. A mensagem fica assim:

```json
{
  "operacao": "CARRO_CRIADO",
  "dataHora": "2026-10-27T14:05:30",
  "usuarioId": 2,
  "usuarioNome": "Maria Oliveira",
  "carroId": 1,
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 2024,
  "requestId": "web-abc123"
}
```

O envio esta no `rabbitmq/Enviar.java`, que fiz em cima do codigo passado em aula. O RabbitMQ configurado e o do
laboratorio (10.209.1.53, usuario e senha admin), so que esse IP so funciona na rede cabeada. Fora de la eu uso o
RabbitMQ do docker compose, ou passo outro endereco na variavel `RABBITMQ_HOST`.

Se o RabbitMQ cair o carro e salvo do mesmo jeito, so fica um erro no log dizendo que a mensagem nao foi.

## Rodando

O jeito mais facil e com Docker. Os 4 repositorios precisam estar na mesma pasta, e dai e so rodar aqui dentro:

```
docker compose up --build
```

O front abre em http://localhost:4200 e o painel do RabbitMQ em http://localhost:15672 (admin / admin). O que o
modulo de log salvou fica em `logs/lojacarro-log/operacoes.log`.

Para usar o RabbitMQ do laboratorio em vez do local:

```
RABBITMQ_HOST=10.209.1.53 docker compose up --build
```

Sem Docker precisa subir antes a API de usuarios e o RabbitMQ. Depois:

```
./mvnw clean package
SERVER=dev BD_NAME=josecarro BD_USER=root BD_PASS=root java -jar target/augusto.war
```

A variavel `SERVER` escolhe o perfil. No `dev` o banco vem de `BD_NAME`, `BD_USER` e `BD_PASS`. No `prod` e o banco
`carros` com o usuario `augusto`.

## Rotas

- `GET /carro` e `GET /carro/{id}`
- `POST /carro/salvar`, `PUT /carro/{id}` e `DELETE /carro/{id}` (precisam do `X-Usuario-Id`)
- `GET /auditoria` e `GET /auditoria/integridade` (precisam do `X-Usuario-Id`)
- `POST /logs` (o front manda os erros dele para ca)
- `GET /boas-vindas`

## Logs e auditoria

Cada requisicao ganha um id (`X-Request-Id`) que aparece em todas as linhas de log dela. Esse mesmo id vai junto
para a API de usuarios e dentro da mensagem do RabbitMQ, entao da pra seguir uma operacao pelos tres projetos. Os
arquivos ficam na pasta `logs/`.

As operacoes com carro tambem ficam na tabela `auditoria`, que so recebe insert. Cada registro guarda o hash do
anterior, entao o `GET /auditoria/integridade` mostra se alguem alterou ou apagou alguma coisa direto no banco.

## Testes

```
./mvnw clean verify
```

Nos testes a API de usuarios e o RabbitMQ sao simulados, entao rodam sem precisar de nenhum dos dois. O JaCoCo
cobra no minimo 80% de cobertura.

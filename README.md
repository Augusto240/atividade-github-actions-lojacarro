# LojaCarro

API da loja de carros que venho fazendo na disciplina. Spring Boot com Java 17.

Autor: Jose Augusto

O projeto esta dividido em 4 repositorios:

- lojacarro (este): o back, com os modulos de carro, vendas, test drive, contabilidade e ADM
- lojacarro-usuarios-api: o modulo de usuarios
- lojacarro-log: o modulo de log, que recebe tudo pelo RabbitMQ
- lojacarro-frontend: as telas em Angular

## Modulos

Cada modulo tem o seu CRUD (listar, buscar, cadastrar, editar e excluir):

- Carros: `/carro`
- Vendas: `/vendas` (carro, cliente, valor e data)
- Test Drive: `/test-drives` (carro, cliente, data e hora, observacao)
- Contabilidade: `/contabilidade` (lancamentos de RECEITA ou DESPESA)
- ADM: `/adm/administradores` (nome e e-mail) e `/adm/logs`
- Usuarios: fica na lojacarro-usuarios-api

Para cadastrar, editar ou excluir precisa mandar o id do usuario no header `X-Usuario-Id`. A LojaCarro pergunta o
nome dele para a API de usuarios e usa esse nome nos logs. Consultar funciona sem o header.

## Logs pelo RabbitMQ

Toda operacao de qualquer modulo vira uma mensagem na fila `augusto_erick`, que o lojacarro-log le e salva. A
mensagem e o DTO `LogOperacaoDTO` em JSON:

```json
{
  "modulo": "VENDAS",
  "operacao": "VENDA_CRIADA",
  "dataHora": "2026-11-04T14:08:41",
  "usuarioId": 2,
  "usuarioNome": "Maria Oliveira",
  "registroId": 1,
  "descricao": "venda #1 do carro #1 para Ana Souza por R$ 145000.00",
  "requestId": "39097a1c"
}
```

O ADM e o unico que vai e volta: em `GET /adm/logs` ele manda um pedido para o modulo de log pelo RabbitMQ e fica
esperando a resposta com as ultimas operacoes salvas (usando `replyTo` e `correlationId`). Se o modulo de log nao
responder em 5 segundos da 504.

O RabbitMQ configurado e o do professor (15.228.215.36, usuario `aluno`). A senha nao fica no codigo: ela vem da
variavel `RABBITMQ_SENHA` (e a que o professor passou em aula). Host e usuario tambem podem ser trocados com
`RABBITMQ_HOST` e `RABBITMQ_USUARIO`. Se o RabbitMQ cair a operacao e salva do mesmo
jeito, so fica um erro no log.

Alem disso tudo vai pros arquivos da pasta `logs/` e as operacoes tambem ficam na tabela `auditoria`, que guarda o
hash do registro anterior (`GET /auditoria/integridade` mostra se alguem mexeu direto no banco).

## Banco para teste

O script `banco/script-teste.sql` cria as tabelas de todos os modulos e ja coloca dados de exemplo (carros, vendas,
test drives, lancamentos, administradores e usuarios):

```
mysql -u root -p < banco/script-teste.sql
```

Ele cria os bancos `usuarios` e `josecarro`. Para rodar no perfil prod, troque `josecarro` por `carros` no script.
Atencao: o script apaga e recria as tabelas, entao e so para teste.

## Rodando

Com Docker, deixando os 4 repositorios na mesma pasta:

```
docker compose up --build
```

Assim usa um RabbitMQ local. Para usar o do professor, passando a senha dele:

```
RABBITMQ_HOST=15.228.215.36 RABBITMQ_SENHA=senha_do_professor docker compose up --build
```

O front abre em http://localhost:4200 e o painel do RabbitMQ local em http://localhost:15672 (aluno / senha-local). O
que o modulo de log salvou fica em `logs/lojacarro-log/operacoes.log`.

Sem Docker precisa subir antes a API de usuarios e o modulo de log. Depois:

```
./mvnw clean package
SERVER=dev BD_NAME=josecarro BD_USER=root BD_PASS=root RABBITMQ_SENHA=senha_do_professor java -jar target/augusto.war
```

No `dev` o banco vem de `BD_NAME`, `BD_USER` e `BD_PASS`. No `prod` e o banco `carros` com o usuario `augusto`.

## Testes

```
./mvnw clean verify
```

Nos testes a API de usuarios e o RabbitMQ sao simulados, entao rodam sem eles. O JaCoCo cobra 80% de cobertura.

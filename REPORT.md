# Relatorio - GitHub Actions e CI

Autor: Jose Augusto

## Objetivo
Automatizar a execucao de testes com GitHub Actions e bloquear merge em `main` quando houver falhas.

## Como o workflow funciona
Arquivo: `.github/workflows/maven.yml`

Etapas do job `tests`:
1. Checkout do codigo.
2. Instalacao do JDK 17 (Temurin) com cache de dependencias Maven.
3. Permissao de execucao do Maven Wrapper.
4. Execucao de `./mvnw -B test`.

O workflow roda em `push` e `pull_request` para `main` e `dev`.

## Vantagens da integracao continua
- Feedback rapido.
- Menos chance de quebrar o main.
- Padroniza o processo de validacao.
- Automatiza tarefas repetitivas.

## Experiencia real da atividade
Na demonstracao eu forcei uma falha em um teste de service (mudei o valor esperado). O workflow rodou no GitHub Actions e falhou, entao o merge ficou bloqueado.
Depois corrigi o teste voltando o valor correto, fiz um novo push e o workflow passou. Com isso o merge voltou a ser permitido.

## Roteiro da demonstracao
1. Push com teste falhando.
2. Verificar a falha no workflow e bloqueio do merge em `main`.
3. Corrigir o teste e fazer novo push.
4. Verificar que o workflow passa e o merge e permitido.

## Como simular falha
Edite um teste (por exemplo em `CarroServiceTest`) e altere um valor esperado para falhar.
Exemplo: troque o `assertEquals(2, resultado.size())` por `assertEquals(3, resultado.size())` e faca push.
Depois, reverta a mudanca e faca um novo push.

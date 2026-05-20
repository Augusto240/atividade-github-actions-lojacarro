# LojaCarro - CI e testes

Projeto simples para mostrar testes unitarios e um pipeline de CI com GitHub Actions.

Autor: Jose Augusto

## Requisitos
- Java 17 (o workflow usa Temurin 17)
- Maven Wrapper (ja incluido no repo)

## Como rodar os testes localmente
```powershell
./mvnw -B test
```

## Estrutura de testes
- Testes de servico: `src/test/java/br/org/edu/ifrn/LojaCarro/services/CarroServiceTest.java`
- Testes de controller: `src/test/java/br/org/edu/ifrn/LojaCarro/controllers/CarroControllerTest.java`

## Workflow de CI
O workflow esta em `.github/workflows/maven.yml` e executa `mvn test` em push e pull request para `main` e `dev`.

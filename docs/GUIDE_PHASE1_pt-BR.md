# Guia de Modernização Fase 1 – Java 11→17 & Spring Boot 2.4→3.0

> Versão em português brasileiro do arquivo `GUIDE_PHASE1.md`. Mantenha este guia sincronizado com a versão em inglês.

## 0. Visão Geral & Objetivos

Este guia conduz você por uma **migração realista** de **Java 11 + Spring Boot 2.4.13** para **Java 17 + Spring Boot 3.0** usando **OpenRewrite** e **OWASP Dependency-Check**.

Para **cada etapa**, descrevemos:
- **Comando** – o que executar.
- **Resultado esperado** – o que você deve ver ou quais **mudanças de código** devem existir após a etapa.

### Pré-requisitos
- Toolchain Java 11 instalada e ativa.
- Maven 3.8+.
- Este repositório checado no estado de **baseline da Fase 0** (sem plugins de segurança pré-configurados).

---

## Seção 0 – Verificar Aplicação de Base (Ponto de Partida Real)

Objetivo: Confirmar que o projeto se parece com uma aplicação legada típica **antes** de adicionar qualquer ferramenta de segurança ou modernização.

### Passo 0.1 – Verificar se o pom.xml não possui o plugin OWASP Dependency-Check
- **O que verificar:** seção de plugins de build do `pom.xml`.
- **Resultado esperado:**
  - Não existe nenhum bloco `<plugin>` com `<groupId>org.owasp</groupId>` e `<artifactId>dependency-check-maven</artifactId>`.
  - Se esse plugin existir, remova-o e execute `mvn clean compile` novamente para garantir que o build continua passando.

### Passo 0.2 – Verificar se o pom.xml não possui o plugin OpenRewrite
- **O que verificar:** seção de plugins de build do `pom.xml`.
- **Resultado esperado:**
  - Não existe nenhum bloco `<plugin>` com `<groupId>org.openrewrite.maven</groupId>` e `<artifactId>rewrite-maven-plugin</artifactId>`.
  - Se o plugin existir, remova-o e confirme que `mvn clean compile` ainda é bem-sucedido.

### Passo 0.3 – Documentar a stack tecnológica de baseline
- **O que registrar neste guia:**
  - Versão do Java: **11** (LTS).
  - Versão do Spring Boot: **2.4.13**.
  - Nenhuma ferramenta de SCA (Software Composition Analysis), como OWASP Dependency-Check, configurada.
- **Resultado esperado:**
  - Este guia declara explicitamente o ponto de partida para comparação com as fases posteriores.

---

## Seção 1 – Configurar Plugin OWASP Dependency-Check

Objetivo: Integrar o OWASP Dependency-Check no `pom.xml` para estabelecer um **baseline de segurança** das dependências de terceiros.

### Passo 1.1 – Adicionar o plugin dependency-check-maven ao pom.xml
- **O que alterar:** adicionar um bloco `<plugin>` para `org.owasp:dependency-check-maven` dentro de `<build><plugins>`.
- **Resultado esperado (código):**
  - O `pom.xml` contém um plugin OWASP Dependency-Check.
  - O plugin está configurado para executar a goal `check`.

### Passo 1.2 – Configurar NVD API Key e opções básicas
- **O que alterar:** na configuração do plugin OWASP, definir:
  - `<nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>`
  - `<failBuildOnCVSS>7</failBuildOnCVSS>`
  - `<suppressionFile>dependency-suppression.xml</suppressionFile>`
  - `<formats>` contendo `HTML` e `JSON`.
- **Resultado esperado (código):**
  - O `pom.xml` referencia `${env.NVD_API_KEY}` para acesso ao NVD.
  - O build irá **falhar** quando forem encontrados problemas com CVSS ≥ 7, assim que o plugin for executado.

### Passo 1.3 – Documentar configuração da NVD API Key
- **O que escrever no guia:**
  - Link para a página de solicitação de API key do NVD.
  - Exemplo de export da variável de ambiente `NVD_API_KEY`.
- **Resultado esperado:**
  - Leitores entendem como obter uma API key e configurá-la localmente/CI.

---

## Seção 2 – Executar Baseline de Segurança Pré-Migração

Objetivo: Capturar a **postura inicial de vulnerabilidades** da stack legada.

### Passo 2.1 – Executar o primeiro scan de vulnerabilidades
- **Comando:**
  - `mvn dependency-check:check`
- **Resultado esperado:**
  - O build Maven termina com sucesso e gera `dependency-check-report.html` e `dependency-check-report.json` em `target/`.
  - O guia indica onde encontrar esses arquivos e como abrir o relatório HTML.

### Passo 2.2 – Documentar contagem de CVEs de baseline
- **O que registrar no guia:**
  - Número de vulnerabilidades Críticas, Altas, Médias e Baixas encontradas no primeiro scan.
- **Resultado esperado:**
  - Uma tabela ou lista com as contagens, para comparação após a migração.

### Passo 2.3 – Criar e documentar o arquivo dependency-suppression.xml
- **O que alterar:**
  - Criar `dependency-suppression.xml` com entradas para falsos positivos conhecidos.
- **Resultado esperado (código):**
  - O arquivo existe na raiz do projeto e é referenciado pelo plugin OWASP.
- **Resultado esperado (comportamento):**
  - Ao executar novamente `mvn dependency-check:check`, as vulnerabilidades suprimidas não aparecem mais.

### Passo 2.4 – Definir meta de segurança pós-migração
- **O que declarar no guia:**
  - "Após a migração, a contagem total de CVEs Críticos/Altos deve ser **menor ou igual** ao baseline; introduzir novos CVEs de alto risco não é aceitável."
- **Resultado esperado:**
  - Critério de aceitação claro para todos os scans futuros.

---

## Seção 3 – Configurar Plugin OpenRewrite

Objetivo: Preparar as transformações automáticas de código e configuração.

### Passo 3.1 – Adicionar rewrite-maven-plugin e dependências de recipes
- **O que alterar no `pom.xml`:**
  - Adicionar `<plugin>` para `org.openrewrite.maven:rewrite-maven-plugin`.
  - Dentro do plugin, adicionar dependências para:
    - `rewrite-spring` (5.21.0+)
    - `rewrite-migrate-java` (2.26.0+)
    - `rewrite-java-dependencies`.
- **Resultado esperado (código):**
  - O `pom.xml` contém um plugin do OpenRewrite com todas as dependências necessárias.

### Passo 3.2 – Configurar recipes ativas e checagens de segurança
- **O que configurar:**
  - `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0` como recipe ativa.
  - `org.openrewrite.java.dependencies.DependencyVulnerabilityCheck` com `<maximumUpgradeDelta>PATCH</maximumUpgradeDelta>`.
- **Resultado esperado (código):**
  - A configuração do plugin lista claramente as recipes de migração e de segurança.
- **Resultado esperado (comportamento):**
  - Ao executar o OpenRewrite, ele sugere upgrades de patch seguros para dependências vulneráveis.

---

## Seção 4 – Dry-Run do OpenRewrite

Objetivo: Pré-visualizar as mudanças da migração antes de aplicá-las ao código.

### Passo 4.1 – Executar dry-run
- **Comando:**
  - `mvn rewrite:dryRun -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0`
- **Resultado esperado:**
  - Maven finaliza com sucesso.
  - É gerado um arquivo `rewrite.patch` em `target/rewrite/` (ou caminho semelhante, dependendo da versão do plugin).

### Passo 4.2 – Revisar arquivo rewrite.patch
- **O que inspecionar:**
  - Mudanças de imports (`javax.*` → `jakarta.*`).
  - Refatoração da configuração de segurança Spring (DSL antigo com `antMatchers` → DSL baseada em lambdas).
- **Resultado esperado (prévia de mudanças de código):**
  - Propostas de substituição de imports JPA/validation e da configuração de segurança.
- **O que registrar no guia:**
  - Trechos de código antes/depois para:
    - Uma entidade exemplo usando `javax.persistence.*`.
    - A classe `SecurityConfig` legada usando `antMatchers`.

### Passo 4.3 – Identificar pontos de intervenção manual
- **O que listar:**
  - Locais onde o uso de HttpClient 4.x pode exigir refatoração manual.
  - Regras de segurança que pareçam semanticamente diferentes após a migração.
- **Resultado esperado:**
  - Uma checklist de arquivos/trechos que precisarão de revisão manual após aplicar as mudanças automáticas.

---

## Seção 5 – Aplicar Migração

Objetivo: Aplicar as recipes do OpenRewrite e persistir o código transformado.

### Passo 5.1 – Executar migração com OpenRewrite
- **Comando:**
  - `mvn rewrite:run`
- **Resultado esperado (código):**
  - Arquivos Java e de configuração são modificados, de acordo com o `rewrite.patch`.
  - O `pom.xml` passa a refletir Spring Boot 3.x e Java 17, se as recipes incluírem essas alterações.

### Passo 5.2 – Revisar e versionar mudanças
- **O que fazer:**
  - Usar `git diff` para inspecionar todas as modificações.
- **Resultado esperado:**
  - Somente arquivos esperados (entidades, controllers, `SecurityConfig`, `pom.xml`, etc.) foram alterados.
  - Não há arquivos inesperados modificados.

### Passo 5.3 – Documentar breaking changes
- **O que descrever neste guia:**
  - Breaking changes específicos em `SecurityConfig` (por exemplo, remoção de `WebSecurityConfigurerAdapter`, novo bean `SecurityFilterChain`).
  - Notas da migração de HttpClient 4.x para 5.x (interfaces alteradas, configuração movida).
- **Resultado esperado:**
  - Leitores entendem o que foi automático e o que exige validação manual.

---

## Seção 6 – Refatoração Manual

Objetivo: Corrigir incompatibilidades restantes e ajustar para os novos padrões.

### Passo 6.1 – Atualizar RestClientConfig para HttpClient 5.x
- **Mudanças esperadas de código:**
  - A configuração do cliente HTTP passa a usar as APIs do HttpClient 5.x.
  - Classes de 4.x deprecadas/removidas são substituídas.

### Passo 6.2 – Ajustar DSL Lambda do Spring Security
- **Mudanças esperadas de código:**
  - A configuração de segurança usa `http.authorizeHttpRequests`, `http.securityMatcher`, etc.
  - Todas as regras de autorização continuam equivalentes às da versão legada.

### Passo 6.3 – Atualizar propriedades de configuração deprecadas
- **Mudanças esperadas de código:**
  - Propriedades de servidor/segurança deprecadas são atualizadas para os equivalentes no Spring Boot 3.x.

### Passo 6.4 – Atualizar testes para novos padrões
- **Comportamento esperado:**
  - Todos os testes compilam e passam.
  - Mudanças de padrão (por exemplo, comportamento de CSRF, password encoder) estão refletidas nas asserções dos testes.

---

## Seção 7 – Validação de Segurança (Gated Check)

Objetivo: Garantir que a migração **não piorou** a postura de segurança.

### Passo 7.1 – Executar dependency-check pós-migração
- **Comando:**
  - `mvn dependency-check:check`
- **Resultado esperado:**
  - O build passa ou falha de acordo com `<failBuildOnCVSS>7</failBuildOnCVSS>`.
  - Novos relatórios HTML/JSON sobrescrevem os anteriores.

### Passo 7.2 – Comparar contagem de CVEs com a baseline
- **O que registrar no guia:**
  - Comparação antes/depois das contagens de vulnerabilidades.
- **Resultado esperado:**
  - A contagem de CVEs Críticos/Altos é **≤ baseline**.

### Passo 7.3 – Atualizar arquivo de supressão, se necessário
- **Mudanças esperadas de código:**
  - `dependency-suppression.xml` pode receber novas entradas para falsos positivos comprovados.

---

## Seção 8 – Testes & Validação

Objetivo: Validar comportamento funcional, testes e performance após a migração.

### Passo 8.1 – Executar build completo e testes
- **Comando:**
  - `mvn clean verify`
- **Resultado esperado:**
  - Build concluído com sucesso.
  - Todos os testes unitários e de integração passam.

### Passo 8.2 – Subir aplicação e validar endpoints
- **Comando:**
  - `mvn spring-boot:run`
- **Resultado esperado:**
  - A aplicação inicia sem erros na porta configurada.
  - Todos os endpoints de autenticação e de tarefas se comportam como antes (ou melhor).

### Passo 8.3 – Comparar métricas de performance
- **O que registrar:**
  - Métricas simples de antes/depois (throughput, latência) coletadas com a ferramenta de sua preferência.
- **Resultado esperado:**
  - Performance pelo menos igual ao baseline, idealmente melhor.

---

## Trechos Antes/Depois & Troubleshooting

A seção final deste guia deve incluir:
- Exemplos representativos **antes/depois** para:
  - Entidades (javax → jakarta).
  - Configuração de segurança (DSL legado → DSL com lambdas).
  - Configuração de cliente HTTP (HttpClient 4.x → 5.x).
- Uma subseção de **Troubleshooting** com:
  - Erros de build comuns após a migração e como corrigi-los.
  - Problemas típicos do OWASP Dependency-Check (API key ausente, problemas de proxy, banco local corrompido etc.).
  - Armadilhas comuns do OpenRewrite (recipes não aplicadas, dependências do plugin ausentes).

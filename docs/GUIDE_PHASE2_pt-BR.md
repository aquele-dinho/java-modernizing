# Guia de Modernização Fase 2 – Java 17→21 & Otimizações

> Versão em português brasileiro do arquivo `GUIDE_PHASE2.md`. Mantenha este guia sincronizado com a versão em inglês.

## 0. Visão Geral & Objetivos

Este guia cobre o upgrade de **Java 17** para **Java 21** sobre **Spring Boot 3.x**, com foco em:
- Migrações de linguagem/runtime via OpenRewrite.
- Habilitar **Virtual Threads**.
- Fazer **benchmark de performance**.
- Executar uma **auditoria final de segurança** e gerar **artefatos de conformidade**.

Para cada seção descrevemos:
- **Comando** – o que executar.
- **Resultado esperado** – comportamento em runtime, métricas de performance ou mudanças de código.

---

## Seção 1 – Migração para Java 21

Objetivo: Atualizar o projeto para compilar e rodar em Java 21.

### Passo 1.1 – Atualizar pom.xml para Java 21
- **O que alterar:**
  - Definir `<java.version>21</java.version>` (ou a propriedade equivalente usada no build).
- **Resultado esperado (código):**
  - O `pom.xml` indica claramente Java 21 como versão alvo.

### Passo 1.2 – Executar recipe OpenRewrite UpgradeToJava21
- **Comando:**
  - `mvn rewrite:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21`
- **Resultado esperado (código):**
  - Arquivos de código são atualizados para usar APIs/padrões compatíveis com Java 21.
  - Constructs deprecados cobertos pela recipe são migrados.

### Passo 1.3 – Documentar mudanças automatizadas
- **O que documentar no guia:**
  - Exemplos de coleções migradas para **Sequenced Collections**.
  - Exemplos de melhorias de pattern matching ou simplificações.
- **Resultado esperado:**
  - Leitores veem trechos antes/depois que ilustram a migração para Java 21.

---

## Seção 2 – Configuração de Virtual Threads

Objetivo: Habilitar e validar o uso de **Virtual Threads** na aplicação.

### Passo 2.1 – Habilitar Virtual Threads na configuração
- **O que alterar:**
  - Propriedades de aplicação ou beans de configuração para usar executores baseados em virtual threads (por exemplo, para Tomcat ou execução de tarefas).
- **Resultado esperado (código):**
  - Arquivos de configuração indicam explicitamente o uso de virtual threads.

### Passo 2.2 – Configurar Tomcat para usar Virtual Threads
- **O que alterar:**
  - Configuração do servidor Spring Boot ou uma classe de configuração dedicada que:
    - Troca o executor padrão por um executor baseado em virtual threads.
- **Resultado esperado (comportamento):**
  - A aplicação ainda inicia com sucesso.
  - Se você inspecionar thread dumps, verá virtual threads atendendo requisições.

### Passo 2.3 – Documentar mudanças de configuração e cuidados
- **O que descrever no guia:**
  - Nomes exatos das propriedades e beans utilizados.
  - Cuidados/limitações (por exemplo, chamadas bloqueantes que podem reduzir o benefício de virtual threads).
- **Resultado esperado:**
  - Leitores conseguem reproduzir a configuração de virtual threads em suas próprias aplicações.

---

## Seção 3 – Benchmarking de Performance

Objetivo: Quantificar o impacto de performance de Java 21 + Virtual Threads.

### Passo 3.1 – Estabelecer baseline em Java 17
- **O que fazer:**
  - Rodar um teste de carga simples ou cenário de benchmark na versão com Java 17.
- **Resultado esperado:**
  - Métricas de baseline registradas neste guia (por exemplo, requisições/segundo, latência média, P95).

### Passo 3.2 – Medir Java 21 + Virtual Threads
- **O que fazer:**
  - Rodar o **mesmo** cenário de carga contra a versão com Java 21 + Virtual Threads.
- **Resultado esperado:**
  - Novas métricas coletadas e documentadas.

### Passo 3.3 – Documentar ganhos de throughput e latência
- **O que apresentar no guia:**
  - Tabela ou gráfico comparando baseline vs. Java 21 + Virtual Threads.
- **Resultado esperado:**
  - Narrativa clara sobre o impacto de performance (ganhos ou estabilidade).

---

## Seção 4 – Auditoria Final de Segurança

Objetivo: Realizar um scan de segurança com **tolerância zero** ao final da modernização.

### Passo 4.1 – Executar OWASP Dependency-Check
- **Comando:**
  - `mvn dependency-check:check`
- **Resultado esperado:**
  - Scan concluído com relatórios HTML/JSON gerados em `target/`.

### Passo 4.2 – Garantir tolerância zero para CVEs Críticos/Altos
- **O que verificar:**
  - Relatórios mostram **zero** vulnerabilidades Críticas/Altas não suprimidas com justificativa.
- **Resultado esperado:**
  - Ou:
    - Build passa com **0 CVEs Críticos/Altos** não suprimidos, ou
    - Build falha, exigindo correção/upgrade das dependências vulneráveis.

### Passo 4.3 – Auditar e limpar supressões obsoletas
- **O que fazer:**
  - Revisar `dependency-suppression.xml` e remover entradas que não se aplicam mais.
- **Resultado esperado (código):**
  - O arquivo de supressão contém apenas entradas atuais e justificadas.

### Passo 4.4 – Documentar postura final de segurança
- **O que registrar no guia:**
  - Contagem final de vulnerabilidades por severidade.
  - Explicação de quaisquer riscos remanescentes que foram aceitos.
- **Resultado esperado:**
  - Registro claro e auditável do estado de segurança pós-modernização.

---

## Seção 5 – Artefatos de Conformidade

Objetivo: Produzir artefatos que suportem gestão de supply chain e vulnerabilidades.

### Passo 5.1 – Gerar SBOM (Software Bill of Materials)
- **O que fazer:**
  - Utilizar a ferramenta escolhida (por exemplo, plugin CycloneDX Maven ou ferramentas OWASP) para gerar a SBOM.
- **Resultado esperado:**
  - Arquivo de SBOM (JSON ou XML) gerado em `target/` ou em um diretório dedicado.

### Passo 5.2 – Gerar VDR (Vulnerability Disclosure Report)
- **O que produzir:**
  - Relatório resumindo vulnerabilidades identificadas, seu status e remediação.
- **Resultado esperado:**
  - Documento armazenado em `docs/`, pronto para ser compartilhado com stakeholders.

### Passo 5.3 – Gerar VEX (Vulnerability Exploitability eXchange)
- **O que produzir:**
  - Documento VEX descrevendo quais vulnerabilidades conhecidas **não são exploráveis** no contexto desta aplicação.
- **Resultado esperado:**
  - Arquivo VEX legível por máquina, alinhado com CSAF ou formato similar.

### Passo 5.4 – Documentar uso e manutenção dos artefatos
- **O que descrever no guia:**
  - Como ler e manter SBOM, VDR e VEX ao longo do tempo.
- **Resultado esperado:**
  - Leitores entendem como esses artefatos se encaixam em DevSecOps e auditorias.

---

## Materiais Adicionais

No final deste guia, inclua:
- Uma seção de **Performance Antes/Depois** com gráficos ou tabelas.
- Uma seção de **Adoção de Recursos do Java 21** com exemplos concretos (pattern matching, record patterns, sequenced collections) e simplificações de código esperadas.

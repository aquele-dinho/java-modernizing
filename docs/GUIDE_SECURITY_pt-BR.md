# Guia de Segurança – OWASP SCA & Gestão de Dependências

> Versão em português brasileiro do arquivo `GUIDE_SECURITY.md`. Mantenha este guia sincronizado com a versão em inglês.

## 0. Visão Geral & Objetivos

Este guia foca em **Software Composition Analysis (SCA)** usando **OWASP Dependency-Check** e ferramentas relacionadas. Ele explica:
- Como configurar e executar scans.
- Como interpretar relatórios.
- Como gerenciar falsos positivos de forma responsável.
- Como integrar SCA em pipelines de CI/CD e em fluxos de conformidade.

Cada seção inclui:
- **Comando** (quando aplicável).
- **Resultado esperado** (relatórios, mudanças de arquivo ou decisões).

---

## Seção 1 – NVD API Key: Benefícios e Configuração

### Passo 1.1 – Por que a NVD API Key é importante
- **O que descrever:**
  - Benefícios de performance e limites de rate ao usar uma API key.
  - Sincronização mais rápida e confiável do banco de vulnerabilidades.
- **Resultado esperado:**
  - Leitores entendem que a API key é fortemente recomendada em CI/CD.

### Passo 1.2 – Como solicitar uma API Key
- **O que documentar:**
  - URL para solicitar uma API key do NVD.
  - Visão geral do processo de aprovação por e-mail.
- **Resultado esperado:**
  - Leitores sabem onde e como obter a API key.

### Passo 1.3 – Como configurar a API Key
- **O que mostrar:**
  - Exemplo de configuração da variável de ambiente (por exemplo, `NVD_API_KEY`).
  - Como a key é referenciada no `pom.xml` via `${env.NVD_API_KEY}`.
- **Resultado esperado:**
  - Ao rodar o scan, o Dependency-Check conecta corretamente ao NVD usando a key.

---

## Seção 2 – Pontuação CVSS e Configuração de Limiares

### Passo 2.1 – Conceitos básicos de CVSS
- **O que descrever:**
  - Níveis de severidade (Crítico, Alto, Médio, Baixo).
  - Faixas típicas de pontuação CVSS.
- **Resultado esperado:**
  - Leitores entendem como as pontuações se traduzem em risco.

### Passo 2.2 – Configurar failBuildOnCVSS
- **O que mostrar:**
  - Exemplo de configuração: `<failBuildOnCVSS>7</failBuildOnCVSS>`.
- **Resultado esperado:**
  - Builds falham quando forem detectadas vulnerabilidades com CVSS ≥ 7 (a menos que suprimidas).

### Passo 2.3 – Definir política de segurança deste projeto
- **O que registrar:**
  - Regras específicas do projeto (por exemplo, "não introduzir novos CVEs Críticos/Altos após a migração").
- **Resultado esperado:**
  - Gate de segurança claro e documentado para todos os contribuidores.

---

## Seção 3 – Interpretando dependency-check-report.html

### Passo 3.1 – Executar o scan e abrir o relatório HTML
- **Comando:**
  - `mvn dependency-check:check`
- **Resultado esperado:**
  - Relatório HTML disponível em `target/`, abrível em um navegador.

### Passo 3.2 – Entender as seções principais do relatório
- **O que explicar:**
  - Visão de resumo (contagens por severidade).
  - Lista de dependências com CVEs associados.
  - Detalhes individuais de cada CVE (descrição, CVSS, referências).
- **Resultado esperado:**
  - Leitores conseguem navegar no relatório e identificar componentes de alto risco.

---

## Seção 4 – Gestão do Arquivo de Supressão

### Passo 4.1 – Propósito do dependency-suppression.xml
- **O que descrever:**
  - Quando e por que suprimir uma vulnerabilidade (por exemplo, falso positivo comprovado, não explorável no contexto).
- **Resultado esperado:**
  - Leitores entendem que supressão é exceção, não regra.

### Passo 4.2 – Criar e editar o arquivo de supressão
- **O que mostrar:**
  - Exemplo de entrada de supressão.
  - Referência do arquivo na configuração do plugin Dependency-Check.
- **Resultado esperado (código):**
  - `dependency-suppression.xml` existe e está sob controle de versão.

### Passo 4.3 – Documentar a justificativa de cada supressão
- **O que exigir:**
  - Cada entrada no arquivo precisa de comentário ou campo explicando **por que** é segura.
- **Resultado esperado:**
  - Trilha de auditoria clara das decisões de supressão.

### Passo 4.4 – Revisão periódica e limpeza de supressões
- **O que recomendar:**
  - Auditorias regulares para remover supressões obsoletas.
- **Resultado esperado:**
  - Arquivo de supressão enxuto, atual e relevante.

---

## Seção 5 – Cache H2 para Dados da NVD

### Passo 5.1 – Por que o cache é importante
- **O que explicar:**
  - Cache local do NVD melhora a performance do scan e evita downloads repetidos grandes.
- **Resultado esperado:**
  - Leitores percebem o benefício de dados NVD persistentes, especialmente em CI.

### Passo 5.2 – Configurar cache H2
- **O que descrever:**
  - Opções de configuração em alto nível no Dependency-Check para uso de H2 como cache.
- **Resultado esperado:**
  - Scans subsequentes rodam mais rápido após o cache estar populado.

---

## Seção 6 – Padrões de Integração em CI/CD (Gated Checks)

### Passo 6.1 – Adicionar SCA ao pipeline de build
- **O que mostrar:**
  - Exemplo de job de CI que executa `mvn dependency-check:check`.
- **Resultado esperado:**
  - Pipelines falham quando novas vulnerabilidades de alta severidade aparecem.

### Passo 6.2 – Como lidar com falhas de pipeline causadas por vulnerabilidades
- **O que descrever:**
  - Fluxo de triagem recomendado quando o pipeline falha (rever relatório, decidir corrigir vs. suprimir vs. aceitar risco).
- **Resultado esperado:**
  - Times têm um playbook claro de resposta.

---

## Seção 7 – Análise de Alcançabilidade com OWASP Dep-Scan

### Passo 7.1 – Objetivo da análise de alcançabilidade
- **O que explicar:**
  - Diferença entre vulnerabilidades "presentes" e de fato **alcançáveis/exploráveis**.
- **Resultado esperado:**
  - Leitores entendem por que alcançabilidade é importante para priorização.

### Passo 7.2 – Executar Dep-Scan (visão geral)
- **O que mostrar:**
  - Exemplo de execução do Dep-Scan (ou ferramenta similar) contra o projeto.
- **Resultado esperado:**
  - Relatório adicional identificando quais CVEs são provavelmente exploráveis.

---

## Seção 8 – Workflows de Artefatos de Conformidade

### Passo 8.1 – Gerar e usar SBOM
- **O que explicar:**
  - Como SBOMs se relacionam com visibilidade de supply chain.
- **Resultado esperado:**
  - Times sabem quando e como regenerar SBOMs (por exemplo, em cada release).

### Passo 8.2 – Criar documentos VDR e VEX
- **O que descrever:**
  - Como achados de vulnerabilidade viram VDR e são refinados por VEX para marcar problemas não exploráveis.
- **Resultado esperado:**
  - Fluxo claro de scan → relatório → decisão de risco → artefato documentado.

### Passo 8.3 – Exemplos reais de remediação de CVEs
- **O que fornecer:**
  - Pelo menos um exemplo em que uma dependência vulnerável foi:
    - Atualizada.
    - Suprimida com justificativa.
    - Mantida com forte justificativa e controles compensatórios.
- **Resultado esperado:**
  - Leitores veem padrões concretos e realistas de remediação.

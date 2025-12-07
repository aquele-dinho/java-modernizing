# **Relatório de Ações Prescritivas para Verificação de Vulnerabilidades de Dependências (SCA) Utilizando OWASP em Projetos de Modernização**

Este relatório técnico detalha a integração da Análise de Composição de Software (SCA), com foco nas ferramentas do ecossistema OWASP (Dependency-Check e Dep-Scan), em cada etapa crítica de um ciclo de modernização ou migração tecnológica. O objetivo é estabelecer uma metodologia DevSecOps robusta que não apenas detecte riscos herdados, mas também garanta que o processo de modernização eleve, e não comprometa, o perfil de segurança da aplicação.

## **I. Visão Estratégica: Fundamentos da Análise de Composição de Software (SCA) na Modernização**

A modernização de aplicações de missão crítica é frequentemente catalisada pela necessidade de mitigar riscos de segurança inerentes a plataformas que atingiram o Fim de Ciclo de Vida (EOL). A migração de dependências deve ser tratada como uma oportunidade obrigatória para saneamento do débito técnico de segurança.

### **1.1 O Imperativo de Segurança na Modernização de Plataformas**

O principal vetor de risco em projetos de modernização é o uso contínuo de componentes sem suporte. Um exemplo proeminente no ecossistema Java é o Java 11, cujo suporte oficial (Premier Support da Oracle) se encerrou, expondo sistemas a vulnerabilidades e falhas de compliance se não migrarem para versões Long-Term Support (LTS) mais recentes, como o JDK 17 ou 21\.1

A segurança da aplicação está intrinsecamente ligada à segurança do ferramental utilizado para verificá-la. A atualização da aplicação impõe a modernização da própria infraestrutura de segurança. Versões recentes da ferramenta primária de SCA da OWASP, o Dependency-Check (a partir da versão 11.0.0), exigem que o ambiente de execução utilize, no mínimo, Java 11\.2 A falha em sincronizar o upgrade do ambiente de build/CI, do código da aplicação e das ferramentas de segurança introduzirá falhas operacionais e lapsos de cobertura.

A verificação de dependências, ou SCA, é um pilar não negociável do desenvolvimento seguro. Esta prática é fundamental para atender aos requisitos de gerenciamento de componentes seguros, conforme especificado no OWASP Application Security Verification Standard (ASVS).3 O ASVS oferece uma base normalizada para testar controles de segurança técnica, garantindo um nível de rigor e cobertura no processo de verificação da segurança.3

### **1.2 O Ecossistema OWASP: Ferramentas de Detecção e Priorização**

A eficiência na gestão de vulnerabilidades de dependências durante uma migração de grande escala requer o uso coordenado de ferramentas especializadas da OWASP.

#### **OWASP Dependency-Check (SCA Clássico)**

O Dependency-Check é a ferramenta fundamental de SCA, atuando como um conjunto de ferramentas que identifica dependências de projetos e as verifica contra vulnerabilidades conhecidas e publicamente divulgadas (CVEs).4 O mecanismo central envolve a determinação de um identificador Common Platform Enumeration (CPE) para uma determinada dependência e, se encontrado, a geração de um relatório que vincula as entradas CVE associadas.4 É essencial para varreduras de *baseline* e cheques de qualidade (Gated Checks) no pipeline de CI/CD.

#### **OWASP Dep-Scan (Análise de Risco de Próxima Geração)**

Enquanto o Dependency-Check se concentra na detecção, o OWASP Dep-Scan se concentra na priorização e no compliance. O Dep-Scan é uma ferramenta de próxima geração que vai além do mapeamento simples de CVEs, oferecendo uma análise avançada de alcance (*reachability analysis*) para múltiplas linguagens.5 Em um projeto de modernização com milhares de dependências, a capacidade de priorizar vulnerabilidades que são de fato exploráveis — isto é, onde o fluxo de execução do código realmente atinge a função vulnerável na biblioteca — é crucial para mitigar o ruído dos falsos positivos 6 e focar a equipe de engenharia.

Além da priorização, o Dep-Scan é indispensável para a conformidade da cadeia de suprimentos de software (Supply Chain Security), permitindo a geração de artefatos essenciais como a Lista de Materiais de Software (SBOM), Relatórios de Divulgação de Vulnerabilidades (VDR) e documentos CSAF 2.0 VEX.5

### **1.3 Matriz de Ferramentas SCA da OWASP para Modernização**

A integração bem-sucedida da segurança na modernização depende da atribuição de papéis específicos a cada ferramenta em fases distintas do projeto.

Matriz de Ferramentas SCA da OWASP para Modernização

| Ferramenta | Foco Principal | Capacidades Chave | Integração na Migração |
| :---- | :---- | :---- | :---- |
| OWASP Dependency-Check | Detecção de CVEs em dependências (SCA Clássico) | CPE Matching, Geração de Relatórios HTML, Supressão XML.4 | Varredura de Baseline e Gated Check pós-upgrade. |
| OWASP Dep-Scan | Análise de Risco e Supply Chain Compliance | Reachability Analysis, Geração de SBOM/VDR/CSAF, Múltiplas Fontes de Dados (NVD, OSV, GitHub).5 | Análise de alcance na Execução; Geração de artefatos de compliance na Validação. |

## **II. Fase de Preparação e Avaliação de Risco (Pré-Migração)**

A fase de preparação é dedicada ao mapeamento preciso da dívida técnica de segurança existente e à otimização da infraestrutura de varredura.

### **2.1 Mapeamento e Varredura de Baseline**

Antes que qualquer alteração de código ou dependência seja iniciada, a equipe de projeto deve realizar uma varredura de *baseline* no código-fonte legado. Isso envolve a utilização do Dependency-Check para criar um inventário inicial de todas as dependências e o mapeamento de vulnerabilidades. A varredura de *baseline* quantifica o risco herdado, estabelecendo uma meta de segurança: o projeto de modernização deve resultar em um número de vulnerabilidades críticas igual ou inferior ao do *baseline*.

A geração do Software Bill-of-Materials (SBOM) inicial deve ser uma prioridade, idealmente utilizando o OWASP Dep-Scan, que suporta a geração de SBOMs e relatórios detalhados.5

### **2.2 Gestão de Infraestrutura e Performance da Varredura**

A precisão e a velocidade da SCA são diretamente dependentes da sua infraestrutura de dados.

#### **O Desafio da NVD API Key**

A partir da versão 9.0.0+, o OWASP Dependency-Check migrou do uso de *data feeds* para a NVD API do NIST. Por essa razão, a obtenção de uma NVD API Key (junto ao NIST) e sua configuração correta no Dependency-Check são **altamente recomendadas**.2 A não utilização de uma API Key resulta em atualizações de vulnerabilidades "extremamente lentas" 2, comprometendo a relevância dos relatórios e introduzindo atrasos inaceitáveis nos pipelines de CI/CD.

#### **Mitigação do Rate Limiting em CI/CD**

Em ambientes de Integração Contínua (CI) caracterizados por múltiplos *builds* paralelos (uma ocorrência comum durante a fase intensiva de refatoração da modernização), o uso de uma única NVD API Key pode facilmente exceder os limites de taxa impostos pela API, resultando em erros HTTP 403\.2 Este é um risco operacional de terceira ordem, pois um sistema de segurança que falha em seu próprio ambiente de execução paralisa o projeto de modernização.

A mitigação exige uma estratégia de infraestrutura de caching robusta. Em vez de permitir que cada agente de *build* baixe e atualize o banco de dados de vulnerabilidades separadamente, deve-se configurar um cache compartilhado para o banco de dados H2 local do Dependency-Check.2 É vital notar que versões recentes do Dependency-Check (11.0.0+) contêm *breaking changes* no uso do banco de dados H2, exigindo um *full download* dos dados NVD ou a execução de um comando de purge no banco de dados local para garantir a compatibilidade e a integridade da base de dados.2

### **2.3 Tratamento de Falsos Positivos e Política de Supressão**

O Dependency-Check, por utilizar a metodologia de *CPE matching*, é propenso a gerar falsos positivos.6 Ignorar esses alertas distrai a equipe; tratá-los no momento da execução do *build* interrompe o fluxo de trabalho.

A política prescritiva exige a criação de um arquivo XML de supressão de vulnerabilidades (*Suppression XML File*) nesta fase.6 Ao gerar o relatório HTML do *baseline*, a equipe de segurança deve revisar e suprimir os falsos positivos (por exemplo, clicando no botão de supressão ao lado de cada entrada CPE identificada).6 Este arquivo deve ser versionado no repositório de código, servindo como um registro formal e auditável de riscos aceitos e mitigados. Gerar este arquivo de supressão *antes* do início da migração garante que o *gated check* da Fase de Execução foque exclusivamente em novas vulnerabilidades introduzidas ou em riscos reais não suprimidos.

## **III. Fase de Execução e Refatoração (Ações Durante a Migração)**

A fase de execução transforma a verificação de vulnerabilidades em um componente ativo do pipeline de integração contínua (CI).

### **3.1 Gated Checks Pós-Upgrade Crítico**

Projetos de modernização Java, como a migração de Spring Boot 2.x para 3.x, exigem uma abordagem incremental: primeiro, a atualização para uma versão suportada (ex: Spring Boot 2.7.x) e, em seguida, o salto para a nova *major version* (Spring Boot 3.0), juntamente com a transição obrigatória de pacotes de javax para jakarta.7

A SCA deve ser configurada como um **Gated Check** imediatamente após cada ponto de ruptura tecnológica (Upgrade de JDK, Upgrade de Spring Framework). O Dependency-Check deve ser configurado para falhar o *build* se vulnerabilidades críticas ou de alta severidade forem detectadas.

* **Ponto Crítico de Verificação:** A migração de pacotes de javax para jakarta 8 é um momento de alto risco onde dependências transitivas vulneráveis, puxadas pela nova versão do *framework*, podem ser introduzidas sem conhecimento do desenvolvedor. A SCA é indispensável para detectar essas dependências transitivas, que, por padrão, também devem ser verificadas.9 Ferramentas como o spring-boot-properties-migrator 7 auxiliam na atualização de propriedades, mas não oferecem cobertura para a segurança das dependências resultantes.

### **3.2 Remediação Automatizada Através de Correção Programática**

Em um projeto de modernização de grande escala, o volume de dependências desatualizadas pode ser esmagador. O objetivo da SCA não é apenas detectar, mas permitir a correção em massa.

Ferramentas de correção programática, como o OpenRewrite, com sua receita DependencyVulnerabilityCheck, atuam como um *Software Composition Analysis* (SCA) ativo, detectando e realizando o *upgrade* de dependências com vulnerabilidades publicamente divulgadas.9 Esta abordagem automatizada é fundamental para manter o delta de segurança baixo.

#### **Controle de Risco no Upgrade (Maximum Upgrade Delta)**

A automação deve ser aplicada com cautela para evitar a introdução de *breaking changes*. O OpenRewrite (e ferramentas similares) permite o controle rigoroso sobre o delta de versão 9:

1. **Patch Upgrade (Padrão):** Por padrão, a receita DependencyVulnerabilityCheck prioriza *patch upgrades* (ex: 1.0.1 para 1.0.2). Esta é a opção mais segura, pois garante a compatibilidade total e minimiza o risco de refatoração manual.9  
2. **Minor/Major Upgrade:** Se a versão corrigida da dependência exigir um salto de versão minor ou major, o parâmetro maximumUpgradeDelta deve ser ajustado para sinalizar à equipe que a automação atingiu seu limite. *Major version upgrades* geralmente requerem refatoração de código, enquanto *minor version upgrades* podem introduzir novos recursos.9 O uso controlado deste parâmetro permite que os engenheiros foquem nas correções complexas (saltos major) enquanto a automação lida com o volume (saltos patch).

### **3.3 Verificação de Vulnerabilidades de Código (Taint Analysis)**

Embora a SCA resolva a segurança de componentes, a modernização e refatoração do código da aplicação podem inadvertidamente introduzir vulnerabilidades de código (SAST).

A transição de javax para jakarta e a refatoração de APIs de I/O são pontos de risco para a introdução de falhas como Cross-Site Scripting (XSS). Deve-se complementar a SCA com análise de fluxo de dados (*Taint Analysis*). Receitas como FindXssVulnerability detectam fluxos de dados não sanitizados de fontes (*sources*) controladas pelo usuário para métodos de saída (*sinks*).10 A execução dessas varreduras de SAST imediatamente após a refatoração de código garante que a aplicação moderna não herde ou introduza vetores de ataque de código.10

## **IV. Fase de Validação e Homologação (Pós-Migração)**

Esta fase garante a aceitação de segurança e a conformidade regulatória do novo ambiente.

### **4.1 Verificação Final de Conformidade e Cobertura Total**

Após a conclusão da refatoração e dos *upgrades*, uma varredura final de tolerância zero deve ser executada. Esta varredura não deve se limitar ao código-fonte, mas deve incluir uma **cobertura híbrida** de dependências e ambiente de execução.11

A SCA (Dependency-Check) verifica as bibliotecas do projeto, mas a segurança também depende da base onde o código executa. A modernização que utiliza imagens de contêiner deve incluir a varredura do Sistema Operacional (OS) e dos pacotes base, utilizando ferramentas como o Dep-Scan, que suporta a verificação de vulnerabilidades em distros Linux como RHEL/CentOS, Ubuntu, e Alpine.5 A verificação de dependências deve ser complementada pela varredura de contêineres e sistemas operacionais para garantir a máxima cobertura de risco.11

### **4.2 Auditoria e Purga de Supressões Obsoletas**

Um ponto de falha comum na governança de segurança pós-migração é a manutenção de arquivos de supressão de vulnerabilidades desatualizados. Se uma dependência foi atualizada ou removida, a entrada de supressão XML criada na Fase I 6 para essa dependência se torna obsoleta.12

A **Ação Crítica** nesta fase é a auditoria e purga do XML de supressão. Manter entradas obsoletas pode levar a confusão e, em casos raros, mascarar acidentalmente futuras vulnerabilidades em novos componentes que reutilizem nomes ou hashes de dependências. Ferramentas como o OpenRewrite oferecem receitas específicas, como IsOwaspSuppressionsFile, que podem auxiliar na remoção automática dessas entradas desatualizadas.12 Uma supressão limpa garante que o novo *baseline* de segurança opere sob a política de risco mais estrita.

### **4.3 Geração de Artefatos de Conformidade**

O produto final da SCA na modernização é a evidência auditável de diligência de segurança. O uso do OWASP Dep-Scan é recomendado para a geração dos seguintes artefatos:

1. **Software Bill-of-Materials (SBOM):** O SBOM final documenta todos os componentes, incluindo a versão corrigida da aplicação.5  
2. **VDR (Vulnerability Disclosure Report):** Um relatório detalhado de todas as vulnerabilidades.5  
3. **CSAF 2.0 VEX (Vulnerability Exploitability eXchange):** Crucialmente, o Dep-Scan pode gerar um documento VEX.5 Este artefato vai além do SBOM, comunicando o *status de explorabilidade* das vulnerabilidades detectadas, confirmando quais CVEs não representam um risco real para a aplicação final devido à análise de alcance. Este documento transforma um relatório de risco de segurança em uma declaração de conformidade proativa.

## **V. Governança e Operação Contínua (DevSecOps)**

A modernização só é bem-sucedida se a segurança for sustentável no ambiente operacional.

### **5.1 Integração em Pipeline e Varredura Contínua**

A estratégia de segurança deve operar em duas frentes:

1. **Shift Left (Gated Checks):** Manter o Dependency-Check e o Dep-Scan como *Gated Checks* rigorosos em cada *merge request* e *build* de produção.  
2. **Shift Right (Varredura Contínua):** Implementar a Varredura Contínua de Vulnerabilidades (Continuous Vulnerability Scanning).11 Esta varredura deve ocorrer fora do pipeline de *build* e visa detectar novas CVEs divulgadas *após* o *deploy* da aplicação. Uma vulnerabilidade pode ser divulgada hoje para uma dependência que foi considerada segura no momento do *build* há uma semana. A varredura contínua garante a vigilância proativa.11

#### **Resiliência do CI: Gestão de Cache e NVD API Key**

A sustentabilidade operacional do SCA depende da resiliência do CI/CD. O risco de *rate limiting* da NVD API 2 exige um monitoramento ativo do uso da chave e a manutenção estrita de um servidor de cache (como o banco de dados H2 compartilhado) para o Dependency-Check. A gestão do cache impede que múltiplos *builds* sobrecarreguem o serviço NVD, mantendo a performance da varredura.2

### **5.2 Política de Patching e Manutenção de LTS**

A repetição do risco de EOL, como o enfrentado pelo Java 11 1, deve ser evitada. Uma política de DevSecOps deve estabelecer um ciclo de vida de *patching* proativo para as novas versões LTS adotadas (ex: Java 17/21).

O débito técnico acumulado é o maior inimigo da segurança contínua. Para combatê-lo, deve-se agendar a execução recorrente (mensal ou trimestral) das receitas de atualização automatizada (como o OpenRewrite DependencyVulnerabilityCheck).9 Esta prática garante que o delta de vulnerabilidade seja constantemente minimizado, exigindo menos esforço manual de correção e evitando o retorno ao estado de vulnerabilidade massiva que impulsionou o projeto de modernização inicial.

## **Conclusões e Recomendações Táticas Finais**

A modernização tecnológica e a migração de plataformas, especialmente em ecossistemas legados, são exercícios de mitigação de risco de segurança. A integração bem-sucedida das ações de SCA da OWASP requer uma transição de uma mentalidade de "detecção" para uma mentalidade de "correção e compliance".

A tabela a seguir resume as ações táticas prescritivas, mapeando a integração da OWASP SCA em cada etapa do ciclo de vida de modernização.

Matriz de Ações de Verificação de Vulnerabilidade de Dependência (OWASP/SCA) por Fase de Modernização

| Fase de Modernização | Objetivo de Segurança | Ação OWASP Específica | Ferramentas/Requisitos |
| :---- | :---- | :---- | :---- |
| **I. Preparação (Baseline)** | Entender o risco herdado e configurar o ambiente. | 1\. Varredura de Baseline (Dependency-Check) para quantificar o débito técnico. 2\. Obtenção e configuração da NVD API Key; planejamento de caching para CI.2 3\. Criação e versionamento de XML de supressão inicial para falsos positivos herdados.6 | Dependency-Check, Java 11+ (obrigatório para tooling recente), Dep-Scan (para SBOM inicial).2 |
| **II. Execução (Upgrade & Refatoração)** | Garantir que os upgrades não introduzam ou reintroduzam CVEs e aplicar correções em massa. | 1\. **Gated Check** obrigatório após upgrades de versão principal (ex: Spring Boot 3).7 2\. Utilizar ferramentas de correção programática (como OpenRewrite) para aplicação automatizada de *patch upgrades*.9 3\. Varredura de código (SAST/Taint Analysis) para XSS e outras falhas de código induzidas por refatoração.10 | Dependency-Check (Maven/Gradle), OpenRewrite (com controle de maximumUpgradeDelta).9 |
| **III. Validação (Homologação)** | Assegurar conformidade, limpar o débito de segurança e documentar o estado final. | 1\. Varredura final de tolerância zero (código e container).11 2\. **Auditoria Crítica:** Remoção de supressões obsoletas no XML.12 3\. Geração de Artefatos de Conformidade: SBOM, VDR e CSAF 2.0 VEX (com análise de alcance).5 | Dep-Scan, OpenRewrite (IsOwaspSuppressionsFile), Varredura de Container.5 |
| **IV. Operação Contínua** | Manter o novo ambiente seguro contra novos CVEs e gerenciar o ciclo de vida (LTS). | 1\. Configurar Varredura Contínua (fora do pipeline de build).11 2\. Monitoramento de Limite de Taxa da NVD API Key para prevenir 403 errors.2 3\. Implementar política de upgrade e ciclos recorrentes de correção automatizada para manter o delta de vulnerabilidade baixo.1 | CI/CD Platform, Servidor de Cache SCA, Política de Manutenção LTS.1 |

**Recomendação Final:** A prioridade máxima durante a Fase I e V é a estabilidade da infraestrutura de segurança. A falha na gestão eficiente da NVD API Key e do cache de vulnerabilidades 2 pode paralisar os *builds* e comprometer a capacidade de resposta a novas vulnerabilidades, independentemente da modernização bem-sucedida do código da aplicação. A gestão de segurança de dependências em um ambiente moderno é uma responsabilidade de infraestrutura crítica.

#### **Works cited**

1. Java 11 End of Life: Dates, Risks, & How to Prepare \- TuxCare, accessed December 7, 2025, [https://tuxcare.com/blog/java-11-end-of-life/](https://tuxcare.com/blog/java-11-end-of-life/)  
2. dependency-check/DependencyCheck: OWASP dependency-check is a software composition analysis utility that detects publicly disclosed vulnerabilities in application dependencies. \- GitHub, accessed December 7, 2025, [https://github.com/dependency-check/DependencyCheck](https://github.com/dependency-check/DependencyCheck)  
3. OWASP Application Security Verification Standard (ASVS), accessed December 7, 2025, [https://owasp.org/www-project-application-security-verification-standard/](https://owasp.org/www-project-application-security-verification-standard/)  
4. OWASP Dependency-Check, accessed December 7, 2025, [https://owasp.org/www-project-dependency-check/](https://owasp.org/www-project-dependency-check/)  
5. OWASP dep-scan, accessed December 7, 2025, [https://owasp.org/www-project-dep-scan/](https://owasp.org/www-project-dep-scan/)  
6. How to Analyze the OWASP Dependency-Check? \- Aqua Security, accessed December 7, 2025, [https://www.aquasec.com/cloud-native-academy/supply-chain-security/owasp-dependency-check/](https://www.aquasec.com/cloud-native-academy/supply-chain-security/owasp-dependency-check/)  
7. Migrate Application From Spring Boot 2 to Spring Boot 3 | Baeldung, accessed December 7, 2025, [https://www.baeldung.com/spring-boot-3-migration](https://www.baeldung.com/spring-boot-3-migration)  
8. Spring Boot 3 Migration- Migrate Spring Boot 2 To Spring Boot 3 \- JavaTechOnline, accessed December 7, 2025, [https://javatechonline.com/spring-boot-3-migration-guide/](https://javatechonline.com/spring-boot-3-migration-guide/)  
9. Find and fix vulnerable dependencies | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/java/dependencies/dependencyvulnerabilitycheck](https://docs.openrewrite.org/recipes/java/dependencies/dependencyvulnerabilitycheck)  
10. Find XSS vulnerabilities | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/analysis/java/security/findxssvulnerability](https://docs.openrewrite.org/recipes/analysis/java/security/findxssvulnerability)  
11. Dependency scanning \- GitLab Docs, accessed December 7, 2025, [https://docs.gitlab.com/user/application\_security/dependency\_scanning/](https://docs.gitlab.com/user/application_security/dependency_scanning/)  
12. Find OWASP vulnerability suppression XML files | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/xml/security/isowaspsuppressionsfile](https://docs.openrewrite.org/recipes/xml/security/isowaspsuppressionsfile)
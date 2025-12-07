# **Modernização Estratégica de Aplicações Java e Spring Boot: Um Guia Exaustivo de Migração para as Versões LTS (11, 17, 21\) e Spring Boot 3.x Utilizando OpenRewrite**

O processo de modernização de um ecossistema de aplicações que utiliza Java 11 e Spring Boot 2.4 para as versões Long-Term Support (LTS) mais recentes (Java 17 e 21\) e o Spring Boot 3.x representa um imperativo técnico e estratégico. Esta transição, embora complexa, desbloqueia ganhos significativos em performance, segurança e produtividade. O desafio reside na gestão das múltiplas quebras de compatibilidade (breaking changes) que ocorrem simultaneamente tanto na plataforma Java quanto no *framework* Spring.

O presente relatório detalha as diferenças fundamentais no código e no *runtime* entre as versões Java 11, 17 e 21, analisa a ruptura arquitetônica imposta pelo Spring Boot 3.x, e estabelece uma metodologia rigorosa e faseada de migração utilizando o ecossistema de refatoração automatizada OpenRewrite.

## **Parte I: A Base da Plataforma: Diferenças Críticas no Código e Runtime do Java (JDK 11, 17, 21\)**

A evolução do Java, particularmente a partir do modelo de lançamento cadenciado a cada seis meses, concentrou inovações substanciais nas versões LTS (11, 17 e 21). A migração não é apenas uma atualização de versão, mas uma adoção de padrões de segurança e desempenho modernos. Java 17 e Java 21 são as versões LTS mais recomendadas, com a última oferecendo recursos de ponta.1

### **1\. Mudanças Estruturais e Quebras de API (Breaking Changes)**

A maior fonte de atrito na migração de Java 11 para Java 17 decorre da remoção de módulos e APIs legadas, que foram descontinuadas para limpar o *runtime* e promover substituições seguras.

#### **1.1. Remoção de Módulos Java EE e Ferramentas Legadas**

A principal quebra de compatibilidade que afeta aplicações legadas é a descontinuação e remoção de módulos Java EE que costumavam fazer parte do JDK padrão.

O Java 11 foi um marco ao remover módulos obsoletos como java.xml.ws.1 Essa remoção direta impacta aplicações que dependiam implicitamente de tecnologias Java XML Binding (JAXB) ou Java Web Services (JAX-WS) que estavam no *classpath* padrão. Para manter a funcionalidade, o código-fonte deve ser adaptado para utilizar artefatos do Jakarta EE correspondentes, que precisam ser adicionados explicitamente como dependências de *build* (por exemplo, jakarta.xml.bind-api).2 Embora a OpenRewrite ajude adicionando essas dependências, a reconfiguração de *plugins* de *build* que geram código a partir de arquivos WSDL ainda exige intervenção manual do arquiteto.2

Além dos módulos Java EE, outras ferramentas e APIs foram retiradas. O mecanismo de ativação de RMI (Remote Method Invocation Activation), já considerado obsoleto, foi removido em Java 17 (JEP 407), após ter sido opcional no Java 11\.3 De forma similar, as ferramentas Pack200 e suas APIs, utilizadas para otimizar arquivos JAR, foram removidas em Java 14 (JEP 367), tendo sido superadas pelos recursos de módulos e esquemas de compressão introduzidos no Java 9\.3

#### **1.2. Depreciação e Remoção de APIs Críticas e Internas**

A segurança e a integridade da plataforma foram reforçadas pela depreciação e remoção de APIs internas e potencialmente inseguras.

O Security Manager e suas APIs relacionadas foram formalmente deprecados para remoção futura em Java 17 (JEP 411).1 Qualquer aplicação que dependa dessa lógica de *sandboxing* deve iniciar a refatoração, pois seu uso é desencorajado.

O acesso direto a APIs internas, notavelmente o sun.misc.Unsafe, tem sido sistematicamente restringido. Métodos críticos foram removidos em etapas: Unsafe::defineClass foi removido no JDK 11, enquanto Unsafe::defineAnonymousClass foi removido no JDK 17, substituído pelo MethodHandles.Lookup::defineHiddenClass.5

A substituição dessas funcionalidades é feita por APIs padrão e seguras: a VarHandle API, introduzida no JDK 9, oferece manipulação de variáveis com segurança de tipo, e a Foreign Function & Memory API (FFM API), finalizada no JDK 21, permite interação segura e eficiente com memória nativa e funções externas.1

A dificuldade frequentemente relatada na migração de projetos de grande escala de Java 11 para Java 17 7 não advém primariamente do código da aplicação, mas sim da dependência transitiva de bibliotecas de terceiros (como *drivers* de cache ou utilitários de baixo nível) que utilizavam esses métodos internos (sun.misc.Unsafe). Essas bibliotecas exigem a atualização para versões compatíveis com o JDK 17 que já utilizam as novas APIs padrão.

#### **1.3. Modernização da Linguagem**

A adoção de novas versões de Java LTS permite que os desenvolvedores utilizem recursos de linguagem que aumentam a produtividade, a legibilidade e a segurança do código.

O Java 17 solidificou recursos como **Records** (para representação simples de dados, eliminando a verbosidade de *boilerplate* de POJOs) e **Sealed Classes** (para controle explícito sobre a herança e subtipos), ambos se tornando permanentes.1

Outra melhoria significativa é o **Pattern Matching**, finalizado para o operador instanceof (JDK 16\) e expandido para expressões e comandos switch (JDK 17 e posteriores).4 A migração permite a adoção de blocos de texto (text blocks) e a preferência por String.formatted(Object...).9

O OpenRewrite oferece *recipes* que automatizam a adoção desses recursos, como a alteração do código para usar o instanceof pattern matching do Java 17, permitindo que a equipe se concentre na lógica de negócios, e não na refatoração sintática.9

### **2\. Melhorias de Performance e Escalabilidade (Runtime)**

As melhorias de *runtime* são um motor primário para a migração, oferecendo ganhos significativos em eficiência operacional.

O gerenciamento de memória foi substancialmente aprimorado a partir do Java 11\. O Z Garbage Collector (ZGC) foi introduzido no Java 11, e o G1GC foi estabelecido como o Coletor de Lixo (GC) padrão para aplicações de baixa latência.1 O Java 17 e 21 trouxeram otimizações contínuas para esses coletores, reduzindo pausas de GC e melhorando a utilização de memória.1

O Java 21 se destaca pela introdução de **Virtual Threads** e **Structured Concurrency** (concluídas como *features* finais).1 As Virtual Threads são a mudança mais significativa na arquitetura da JVM em anos, oferecendo escalabilidade massiva para aplicações com uso intensivo de I/O, como a maioria dos microsserviços Spring Boot. A migração de Java 17 para Java 21 é notavelmente mais simples em termos de quebras de código do que o salto de 11 para 17 7, tornando o Java 21 o alvo estratégico ideal para maximizar o Retorno sobre o Investimento (ROI) em performance e escalabilidade.

## **Parte II: O Salto Arquitetônico: Spring Boot 2.x para Spring Boot 3.x**

A migração de Spring Boot 2.x (tipicamente acoplado a Java 8 ou Java 11\) para Spring Boot 3.x é uma transição arquitetônica que impõe uma reformulação do *framework* base, notavelmente através da adoção do Jakarta EE e do Spring Framework 6.0.

### **1\. Requisitos de Sistema e Cascade de Dependências**

O Spring Boot 3.0 estabelece requisitos mínimos que alinham a plataforma Java e o *framework*.

O requisito fundamental é que o Spring Boot 3.0 exige **Java 17** ou superior.8 Isso significa que a migração do código Java (descrita na Parte I) é um pré-requisito obrigatório. Adicionalmente, o Spring Boot 3.0 baseia-se no **Spring Framework 6.0**.10

Essa atualização de *framework* desencadeia uma cascata de requisitos de versão em todo o ecossistema de dependências. A migração pode exigir a atualização de bibliotecas como Spring Security 6.0, Hibernate 6.x, Apache HttpClient 5.x, Spring Kafka 3.0, e outras bibliotecas de dados e segurança.11 Gerenciar manualmente essa matriz de versões é complexo, reforçando a necessidade de ferramentas automatizadas.

### **2\. A Ruptura do Namespace: A Migração de Java EE para Jakarta EE**

O evento de maior volume na migração para Spring Boot 3.x é a mudança de *namespace* de javax.\* para jakarta.\*.13

O *rebranding* do Java EE para Jakarta EE exige que todas as importações em arquivos Java e as coordenadas de dependência nos arquivos de *build* (Maven ou Gradle) sejam atualizadas. Classes de Servlets, APIs de Transação, e validação de Beans, por exemplo, devem migrar (e.g., javax.transaction $\\rightarrow$ jakarta.transaction).15

Esta migração é crítica e não pode ser adiada, pois o Spring Boot 3.x e o Spring Framework 6.0 são nativos do Jakarta EE 10\. A comunidade Spring Boot confirmou que a transformação em tempo de execução (*runtime transformation*) de *namespaces* não é possível para arquivos JAR/WAR de aplicações Spring Boot com contêineres *embedded*, tornando o processo de migração do código-fonte ou do *build* essencial.16

Ferramentas como OpenRewrite são indispensáveis para lidar com essa mudança de alta frequência, utilizando *recipes* específicas (Migrate to Jakarta EE 10.0) para alterar automaticamente imports, dependências e arquivos de configuração (como web.xml).15

### **3\. Mudanças Críticas em APIs e Configurações de Código**

A transição para Spring Boot 3.x exige refatorações significativas em áreas de código onde a customização é frequente, como segurança e conectividade.

#### **3.1. Refatoração do Spring Security 6.0: Lambda DSL**

A configuração programática do Spring Security foi reestruturada para melhorar a clareza e imutabilidade, abandonando o padrão de métodos encadeados em favor do Domain-Specific Language (DSL) baseado em Lambdas.18

A substituição envolve a mudança de chamadas antigas como authorizeRequests() e antMatchers() para a nova sintaxe funcional: authorizeHttpRequests(authorize \-\> authorize.requestMatchers("/public/\*\*").permitAll()).18 Para aplicações com lógica de segurança complexa ou múltiplos *security matchers*, essa refatoração é obrigatória.

Além da sintaxe, o Spring Security 6.0 mudou seu comportamento padrão em aplicações Servlet, aplicando autorização a *todos* os tipos de *dispatch*.10 Isso pode ser ajustado com a propriedade spring.security.filter.dispatcher-types, mas requer uma revisão arquitetural.

A estratégia mais segura para migrar a segurança é uma transição em duas etapas: primeiro, atualizar a aplicação Spring Boot 2.x para o Spring Security 5.8 para resolver todas as depreciações existentes. Em seguida, aplicar a migração de 5.8 para 6.0, um caminho explicitamente recomendado pela equipe Spring Security para simplificar o processo.10 OpenRewrite oferece *recipes* para automatizar a conversão de chamadas encadeadas de ServerHttpSecurity para o DSL de Lambda.20

#### **3.2. Atualização do Client HTTP Stack**

O Spring Boot 3.x atualizou seu *stack* interno de cliente HTTP para o **Apache HttpClient 5.x**, substituindo as versões 4.x.13

Aplicações que personalizavam clientes HTTP, especialmente ao configurar RestTemplate usando HttpComponentsClientHttpRequestFactory, são as mais afetadas.13 A API 5.x do Apache HttpClient adota um design moderno baseado em *builder* e utiliza novos pacotes (org.apache.hc.client5.http.impl.classic e org.apache.hc.core5.ssl).21

A refatoração de customizações avançadas de RestTemplate (como configurações de *connection pooling*, SSL Contexts ou *TrustStores*) deve ser validada e ajustada manualmente, mesmo com o auxílio de automação. Por exemplo, a configuração de *TrustAllStrategy* e *HostnameVerifier* requer a importação das novas classes do httpclient5 e a adaptação à sintaxe de *builders*.21

#### **3.3. Propriedades de Configuração e Web**

Diversas propriedades de configuração foram renomeadas ou removidas no Spring Boot 3.0. Para auxiliar na migração, o módulo spring-boot-properties-migrator pode ser adicionado como dependência de *runtime*. Ele analisa o ambiente e migra temporariamente as propriedades durante a execução, além de imprimir diagnósticos de descontinuação.10 O OpenRewrite também fornece *recipes* para migrar propriedades (por exemplo, server.max.http.header.size para server.max-http-request-header-size).12

No nível da aplicação Web, a configuração de *trailing slash matching* no Spring MVC e WebFlux foi deprecada, com o valor padrão definido para false no Spring Framework 6.0.10 Isso significa que *endpoints* que antes correspondiam a URLs com ou sem barra final agora podem falhar. OpenRewrite pode ajudar a restaurar o comportamento antigo adicionando SetUseTrailingSlashMatch() na configuração, se necessário.23

## **Parte III: A Estratégia de Automação: Upgrades Faseados com OpenRewrite**

A complexidade das migrações de Java e Spring Boot em conjunto, envolvendo a reescrita de código sintático e semântico em larga escala, torna a automação via OpenRewrite uma necessidade arquitetônica para mitigar riscos e reduzir o tempo de projeto.

### **1\. Fundamentos e Mecanismo do OpenRewrite**

OpenRewrite é um ecossistema de refatoração automatizada que opera modificando a Lossless Semantic Tree (LST) do código-fonte.24 A LST é uma representação semântica rica que retém todos os detalhes de formatação e comentários originais.

O mecanismo de OpenRewrite baseia-se em **recipes** — conjuntos de instruções declarativas que definem regras de transformação.26 Ao rodar *recipes*, o motor realiza alterações minimamente invasivas que honram a formatação original do código.24 Isso é crucial para manter a rastreabilidade e a qualidade do código em repositórios que passarão por refatoração em massa.

Plugins para Maven e Gradle facilitam a aplicação dessas *recipes*.25 A funcionalidade mvn rewrite:dryRun permite que os arquitetos pré-visualizem as mudanças propostas antes de aplicá-las ao código-fonte, um passo essencial para validação e planejamento.27

### **2\. Metodologia de *Upgrade* Faseado**

A estratégia de migração deve ser implementada em fases discretas, usando *recipes* compostas que orquestram a sequência correta de mudanças. A migração de Spring Boot 2.x para 3.x (que envolve Java 17\) deve ser o foco inicial da automação.

A principal *recipe* composta para a transição é org.openrewrite.java.spring.boot3.UpgradeSpringBoot\_3\_0.12 Esta *recipe* encapsula toda a complexidade da mudança arquitetônica.

Tabela 2: Roteiro de Migração Faseada e Orquestração de Recipes OpenRewrite

| Fase | Objetivo Principal | Recipe Chave OpenRewrite | Versão de Target | Impacto e Racional Estratégico |
| :---- | :---- | :---- | :---- | :---- |
| **0\. Pré-Migração** | Limpeza de Depreciações e Estabilização | UpgradeSpringBoot\_2\_7 e *recipes* de *best practices* para SB 2.x | Spring Boot 2.7.x | Reduzir a superfície de código obsoleto e remover métodos deprecados que seriam removidos no SB 3.0.10 |
| **1\. Salto Principal (Jakarta EE/Java 17\)** | Migração para SB 3.0, SF 6.0 e Jakarta EE | UpgradeSpringBoot\_3\_0 | Spring Boot 3.0.x / Java 17 | Orquestrar as mudanças mais disruptivas, incluindo a transição Java 17, a refatoração do Spring Security 6.0 e o *namespace* Jakarta EE.12 |
| **2\. Estabilização e Melhorias Contínuas** | Adoção de Versões de Manutenção e Boas Práticas | UpgradeSpringBoot\_3\_2 (e subsequentes), SpringBoot33BestPractices | Spring Boot 3.2.x | Integrar melhorias de segurança (Spring Security 6.2) e adotar *best practices* do *framework*, como o uso de anotações @Valid.23 |
| **3\. Otimização da Plataforma** | Adoção do Java 21 | UpgradeToJava21 | Java 21 | Capitalizar os ganhos de escalabilidade do Virtual Threads e adotar novas APIs de linguagem, como Sequenced Collections, com baixo atrito de código.29 |

#### **2.1. Racional da Ordem de Execução**

A *recipe* composta UpgradeSpringBoot\_3\_0 é projetada para gerenciar a dependência entre plataforma e *framework*. Ela encadeia a migração para Java 17 (Migrate to Java 17\) dentro de sua execução.12 Isso é essencial porque o Spring Boot 3.0 *requer* o Java 17, e essa ordem garante que os arquivos de *build* sejam atualizados para o SB 3.0 e o SF 6.0 *antes* de aplicar refatorações de código específicas do Java 17 e Jakarta EE, garantindo um caminho de compilação mais limpo.

O UpgradeSpringBoot\_3\_0 abrange a migração de dependências críticas como: Spring Security 6.0, Spring Kafka 3.0, Spring Cloud 2022 e Hibernate 6.1.x, garantindo que todo o *stack* esteja coerente com a nova arquitetura.12

#### **2.2. Migração para Java 21**

A etapa final, a migração de Java 17 para Java 21, é de complexidade de refatoração significativamente menor em comparação com o salto 11 $\\rightarrow$ 17\.7 A *recipe* org.openrewrite.java.migrate.UpgradeToJava21 foca na migração para coleções sequenciadas e na consolidação de todas as mudanças do Java 17\.29 O principal benefício desta fase é puramente de *runtime*, permitindo a adoção das Virtual Threads no ambiente Spring Boot 3.x para otimização de escalabilidade.

### **3\. Limitações da Automação e Focos de Intervenção Manual**

Apesar da eficácia do OpenRewrite em lidar com refatorações mecânicas e mudanças de *namespace*, ele não pode inferir ou refatorar lógica de negócios complexa, nem pode lidar com todas as customizações altamente específicas do *framework*.30

Áreas críticas que exigem revisão e potencial intervenção manual incluem:

1. **Customizações de Spring Security:** O novo DSL de Lambdas do Spring Security 6.0, embora suportado pelo OpenRewrite, exige que arquitetos validem e, se necessário, refatorem customizações complexas, como a ordem de múltiplos SecurityFilterChain com securityMatcher e a adição de filtros customizados.19  
2. **Configurações Avançadas de Client HTTP:** A refatoração da lógica de inicialização de RestTemplate ou de clientes baseados em Apache HttpClient 5.x que envolvem a manipulação de SSL, *connection pooling* ou autenticação customizada deve ser revista para garantir que a transição para o modelo de *builder* do HttpClient 5.x seja funcionalmente equivalente à versão 4.x anterior.21  
3. **Dependências e *Build*:** Falhas na resolução de dependências durante a construção da LST podem resultar em refatorações incompletas (MavenMetadataFailures), exigindo que o diagnóstico de resolução de dependências seja executado para solucionar problemas de conectividade com repositórios.31  
4. **Plugins de Geração de Código:** A automação não cobre a atualização de *plugins* de *build* que geram código (ex.: geração de código a partir de WSDL), que devem ser ajustados manualmente para produzir código compatível com Jakarta EE e Java 17\.2

## **IV. Conclusão e Recomendações Prescritivas**

A modernização de aplicações Java 11/Spring Boot 2.4 para a arquitetura Java 21/Spring Boot 3.x é uma jornada técnica de alta complexidade. As diferenças críticas residem na necessidade de: 1\) Mitigar as rupturas de API do Java 17 (remoção de módulos Java EE e APIs internas sun.misc.Unsafe) e 2\) Gerenciar a transição arquitetônica do Spring Boot 3.x (obrigação de Java 17, adoção do Jakarta EE, e refatoração do Spring Security 6.0 e do *stack* HTTP).

Recomenda-se a adoção imediata do roteiro faseado utilizando OpenRewrite como o motor central da refatoração. O sucesso do projeto depende da priorização da Fase 1, onde a *recipe* composta UpgradeSpringBoot\_3\_0 deve ser aplicada para orquestrar a migração de *build*, Java 17 e Jakarta EE de maneira coesa, minimizando a superfície de erro.

O objetivo estratégico final deve ser a **migração para o Java 21**, aproveitando a baixa fricção de código na transição 17 $\\rightarrow$ 21 e capitalizando o benefício transformador das Virtual Threads para a escalabilidade da aplicação Spring Boot. A intervenção humana deve ser focada na validação pós-automação das áreas de alta customização, como segurança e conectividade de rede.

#### **Works cited**

1. Java 8 vs Java 11 vs Java 17 vs Java 21: A Comprehensive Comparison \- Medium, accessed December 7, 2025, [https://medium.com/@a.r.m.monesan\_9577/java-8-vs-java-11-vs-java-17-vs-java-21-a-comprehensive-comparison-aa4635f9c3fe](https://medium.com/@a.r.m.monesan_9577/java-8-vs-java-11-vs-java-17-vs-java-21-a-comprehensive-comparison-aa4635f9c3fe)  
2. Migrate to Java 17 | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-java-17](https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-java-17)  
3. Java 17 vs Java 11 \- Java PDF Blog | IDR Solutions, accessed December 7, 2025, [https://blog.idrsolutions.com/java-17-vs-java-11/](https://blog.idrsolutions.com/java-17-vs-java-11/)  
4. Significant Changes in JDK 17 Release \- Oracle Help Center, accessed December 7, 2025, [https://docs.oracle.com/en/java/javase/17/migrate/significant-changes-jdk-release.html](https://docs.oracle.com/en/java/javase/17/migrate/significant-changes-jdk-release.html)  
5. JEP 471: Deprecate the Memory-Access Methods in sun.misc.Unsafe for Removal, accessed December 7, 2025, [https://openjdk.org/jeps/471](https://openjdk.org/jeps/471)  
6. Java's Unsafe is Finally Going Away \- foojay, accessed December 7, 2025, [https://foojay.io/today/unsafe-is-finally-going-away-embracing-safer-memory-access-with-jep-471/](https://foojay.io/today/unsafe-is-finally-going-away-embracing-safer-memory-access-with-jep-471/)  
7. Are there breaking changes between 17 and 21? : r/java \- Reddit, accessed December 7, 2025, [https://www.reddit.com/r/java/comments/1e01kc6/are\_there\_breaking\_changes\_between\_17\_and\_21/](https://www.reddit.com/r/java/comments/1e01kc6/are_there_breaking_changes_between_17_and_21/)  
8. What's New in Spring Boot 3+: A Guide to the Latest Features and Improvements \- Medium, accessed December 7, 2025, [https://medium.com/@sunda.nitsri/whats-new-in-spring-boot-3-a-guide-to-the-latest-features-and-improvements-616b40f9ba3b](https://medium.com/@sunda.nitsri/whats-new-in-spring-boot-3-a-guide-to-the-latest-features-and-improvements-616b40f9ba3b)  
9. Migrate to Java 17 | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/java/migrate/upgradetojava17](https://docs.openrewrite.org/recipes/java/migrate/upgradetojava17)  
10. Spring Boot 3.0 Migration Guide \- GitHub, accessed December 7, 2025, [https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)  
11. Java 17 and Spring Boot 3: Upgrade Roadmap \- Atlantbh Sarajevo, accessed December 7, 2025, [https://www.atlantbh.com/java-17-and-spring-boot-3-upgrade-roadmap/](https://www.atlantbh.com/java-17-and-spring-boot-3-upgrade-roadmap/)  
12. Migrate to Spring Boot 3.0 | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/java/spring/boot3/upgradespringboot\_3\_0](https://docs.openrewrite.org/recipes/java/spring/boot3/upgradespringboot_3_0)  
13. Migrate Application From Spring Boot 2 to Spring Boot 3 | Baeldung, accessed December 7, 2025, [https://www.baeldung.com/spring-boot-3-migration](https://www.baeldung.com/spring-boot-3-migration)  
14. Migrating from the \`javax\` to \`jakarta\` namespace \- JetBrains Guide, accessed December 7, 2025, [https://www.jetbrains.com/guide/java/tutorials/migrating-javax-jakarta/](https://www.jetbrains.com/guide/java/tutorials/migrating-javax-jakarta/)  
15. Migrate deprecated javax.transaction packages to jakarta.transaction | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/java/migrate/jakarta/javaxtransactionmigrationtojakartatransaction](https://docs.openrewrite.org/recipes/java/migrate/jakarta/javaxtransactionmigrationtojakartatransaction)  
16. How to configure javax-to-jakarta transformation in spring boot 3 embedded tomcat?, accessed December 7, 2025, [https://stackoverflow.com/questions/78550035/how-to-configure-javax-to-jakarta-transformation-in-spring-boot-3-embedded-tomca](https://stackoverflow.com/questions/78550035/how-to-configure-javax-to-jakarta-transformation-in-spring-boot-3-embedded-tomca)  
17. Migrate to Jakarta EE 10.0 | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-jakarta-ee-10](https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-jakarta-ee-10)  
18. Migrating a Spring Boot Application from Spring Security 5 to Spring Security 6 \- GeeksforGeeks, accessed December 7, 2025, [https://www.geeksforgeeks.org/advance-java/migrating-a-spring-boot-application-from-spring-security-5-to-spring-security-6/](https://www.geeksforgeeks.org/advance-java/migrating-a-spring-boot-application-from-spring-security-5-to-spring-security-6/)  
19. Spring security migration to spring security 6 \- Stack Overflow, accessed December 7, 2025, [https://stackoverflow.com/questions/78592014/spring-security-migration-to-spring-security-6](https://stackoverflow.com/questions/78592014/spring-security-migration-to-spring-security-6)  
20. Spring Boot 2.x best practices | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/java/spring/boot2/springboot2bestpractices](https://docs.openrewrite.org/recipes/java/spring/boot2/springboot2bestpractices)  
21. I am migrating spring boot version 2.7.3 to spring-boot 3.0.0 so existing code is breaking related to HttpClients \- Stack Overflow, accessed December 7, 2025, [https://stackoverflow.com/questions/75242683/i-am-migrating-spring-boot-version-2-7-3-to-spring-boot-3-0-0-so-existing-code-i](https://stackoverflow.com/questions/75242683/i-am-migrating-spring-boot-version-2-7-3-to-spring-boot-3-0-0-so-existing-code-i)  
22. Apache HttpClient 5.x migration guide, accessed December 7, 2025, [https://hc.apache.org/httpcomponents-client-5.5.x/migration-guide/index.html](https://hc.apache.org/httpcomponents-client-5.5.x/migration-guide/index.html)  
23. Spring Boot 3.3 best practices | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/java/spring/boot3/springboot33bestpractices](https://docs.openrewrite.org/recipes/java/spring/boot3/springboot33bestpractices)  
24. Automate Your Java Upgrades: A Practical Case Study with OpenRewrite and GitHub Actions | by Daniil Roman | Berlin Tech Blog (by Kleinanzeigen and mobile.de) \- Medium, accessed December 7, 2025, [https://medium.com/berlin-tech-blog/automate-your-java-upgrades-a-practical-case-study-with-openrewrite-and-github-actions-44275f841082](https://medium.com/berlin-tech-blog/automate-your-java-upgrades-a-practical-case-study-with-openrewrite-and-github-actions-44275f841082)  
25. OpenRewrite by Moderne | Large Scale Automated Refactoring | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/](https://docs.openrewrite.org/)  
26. Automate Java/Springboot migration using OpenRewrite | by Arshdeep . | Medium, accessed December 7, 2025, [https://medium.com/@arshde3p/automating-java-springboot-migration-using-openrewrite-d6e63129102e](https://medium.com/@arshde3p/automating-java-springboot-migration-using-openrewrite-d6e63129102e)  
27. Simplify Java and SpringBoot migration with OpenRewrite \- DEV Community, accessed December 7, 2025, [https://dev.to/hgky95/simplify-java-and-springboot-migration-with-openrewrite-g3d](https://dev.to/hgky95/simplify-java-and-springboot-migration-with-openrewrite-g3d)  
28. Migrate to Spring Boot 3.2 | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/recipes/java/spring/boot3/upgradespringboot\_3\_2](https://docs.openrewrite.org/recipes/java/spring/boot3/upgradespringboot_3_2)  
29. OpenRewrite recipes for migrating to newer versions of Java. \- GitHub, accessed December 7, 2025, [https://github.com/openrewrite/rewrite-migrate-java](https://github.com/openrewrite/rewrite-migrate-java)  
30. Migrate to Spring Boot 3 from Spring Boot 2 | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-spring-3](https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-spring-3)  
31. Frequently asked questions (FAQ) | OpenRewrite Docs, accessed December 7, 2025, [https://docs.openrewrite.org/reference/faq](https://docs.openrewrite.org/reference/faq)
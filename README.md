# Análise de Malhas — Projeto em JavaFX

Este projeto foi desenvolvido para a disciplina de Circuitos I, com foco na implementação de um analisador de malhas elétricas utilizando o método da inspeção (também chamado de método reduzido). A aplicação permite inserir parâmetros do circuito e obter as correntes de malha calculadas de forma automatizada.

A interface gráfica foi construída utilizando **JavaFX 21**, a mesma versão da JDK utilizada no desenvolvimento.

---

## Tecnologias Utilizadas

- Java 21  
- JavaFX 21  
- Maven  
- FXML  
- Estrutura interna baseada em MVC simplificado  

---

## Funcionamento Geral

O sistema recebe valores de resistências, fontes e demais elementos do circuito. A partir desses dados, monta as equações de malha e calcula as correntes aplicando o método da inspeção. Os resultados são apresentados na interface gráfica de forma clara e organizada.

---

## Estrutura do Projeto

src/
├─ main/
│ ├─ java/
│ ├─ resources/
└─ test/

A pasta `resources` contém os arquivos FXML e demais elementos da interface.
A pasta `java` controladores e codigo fonte da aplicação

---

## Representação das Equações de Malha

O cálculo das correntes é baseado na formulação matricial obtida pela inspeção das malhas do circuito.  
Cada equação segue o formato:

(R_malha * I_malha) − (R_interface * I_interface) = Σ(Fontes)

Onde:

- **R_malha**: soma das resistências pertencentes à malha  
- **R_interface**: resistências compartilhadas entre malhas  
- **I_malha**: corrente desconhecida da malha atual  
- **I_interface**: corrente da malha adjacente  
- **Σ(Fontes)**: soma algébrica das fontes presentes na malha

A forma matricial gerada é:

A * I = B


Onde:

- **A** é a matriz dos coeficientes  
- **I** é o vetor das correntes de malha  
- **B** contém as contribuições das fontes  

---

## Método de Resolução Numérica

O sistema linear é resolvido através de **Eliminação de Gauss com pivotamento parcial**, garantindo maior estabilidade numérica.

A rotina segue os seguintes passos:

1. Seleção de pivôs adequados (pivotamento parcial)  
2. Eliminação progressiva dos termos abaixo da diagonal  
3. Substituição retroativa para obtenção das correntes  

O resultado final é exibido diretamente na interface gráfica.

---

## Geração do Executável (Fat JAR)

Para gerar um JAR executável contendo todas as dependências:

```terminal
mvn clean package
```
O arquivo final será criado em: target/<nome-do-projeto>-jar-with-dependencies.jar


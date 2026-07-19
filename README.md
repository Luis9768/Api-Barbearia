# 👑 Afro King's Barbearia - REST API

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

## 📖 Sobre o Projeto

A **Afro King's Barbearia API** é um sistema backend desenvolvido para gerenciar as operações de uma barbearia moderna. A aplicação oferece uma arquitetura robusta para o controle de agendamentos, clientes, serviços e autenticação, garantindo que regras de negócio complexas (como conflitos de horários e expedientes) sejam validadas no lado do servidor.

O sistema foi desenhado com separação clara de responsabilidades, utilizando o padrão **DTO (Data Transfer Object)** com `Records` do Java moderno e tratamento global de exceções, resultando em uma API previsível, limpa e segura.

## 🚀 Tecnologias Utilizadas

* **Java** (Desenvolvimento core)
* **Spring Boot 3.x** (Framework principal)
* **Spring Data JPA & Hibernate** (Persistência e mapeamento objeto-relacional)
* **Spring Security & JWT** (Autenticação, autorização e proteção de rotas)
* **MySQL** (Banco de dados relacional)
* **Jakarta Bean Validation** (Validação de dados de entrada)
* **Lombok** (Redução de boilerplate)

## ⚙️ Funcionalidades e Regras de Negócio (Core)

### 🔐 Segurança e Autenticação
* **Login via JWT (JSON Web Token):** Autenticação Stateless protegendo todas as rotas sensíveis.
* **Controle de Perfis (RBAC):** Restrição de acesso baseada em roles (`ROLE_ADMIN`, `ROLE_CLIENTE`).
* **Bloqueio de Inativos:** O Spring Security impede automaticamente a geração de tokens para usuários inativados ou demitidos.
* **Integração Front-end:** Configuração global de **CORS** habilitada para consumo seguro por aplicações SPA (React, Vue, etc).

### 📅 Gestão de Agendamentos (Engine)
* **A "Máquina do Tempo":** O sistema impede marcações e listagens de horários no passado.
* **Detecção de Conflitos:** Validação inteligente que impede sobreposição de horários para um mesmo barbeiro.
* **Expediente Dinâmico:** O sistema gera slots de horários com base na duração do serviço escolhido e no horário de funcionamento da barbearia.
* **Dias Especiais:** Suporte nativo para dias de folga, feriados ou horários de funcionamento atípicos.
* **Reagendamento:** Fluxo seguro para alteração de datas, validando novamente todas as regras de colisão e disponibilidade.

### 👥 Gestão de Usuários e Clientes
* **Arquitetura de Dados:** Separação estrutural entre `Usuario` (Credenciais de acesso/Login) e `Cliente` (Dados de perfil).
* **Soft Delete (Exclusão Lógica):** A exclusão de um usuário inativa em cascata tanto as credenciais quanto o perfil, mantendo o histórico de agendamentos no banco.
* **Prevenção de Duplicidade:** O banco e a aplicação blindam o cadastro de CPFs, Emails e Contatos duplicados.

## 🛠️ Arquitetura e Padrões Implementados

* **Controller Advice (`@RestControllerAdvice`):** Tratamento global de exceções. Erros de integridade de banco (como CPFs duplicados) ou quebras de regra de negócio (`IllegalArgumentException`) são interceptados e devolvidos ao Front-end como mensagens JSON limpas e amigáveis (Status 400), sem poluir o terminal do servidor.
* **DTO Pattern via Records:** Utilização intensiva de `records` para transferência de dados entre a camada de apresentação e a regra de negócio, garantindo imutabilidade e proteção de dados sensíveis (ex: senhas não são expostas na resposta HTTP).
* **Consultas Otimizadas:** Uso de JPQL e chaves estrangeiras virtuais para evitar o problema de N+1 e unir entidades separadas (como Usuário e Cliente) em uma única transação de abate lógico.

## 💻 Como executar o projeto localmente

### Pré-requisitos
* Java JDK 17 (ou superior) instalado.
* Maven instalado.
* MySQL Server rodando localmente (porta 3306).

### Passos para configuração
1. Clone este repositório:
   ```bash
   git clone [https://github.com/Luis9768/Api-Barbearia.git)

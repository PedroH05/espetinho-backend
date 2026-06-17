# Espetinho API

Backend REST da plataforma web para venda de espetinhos. Esta primeira entrega cria a base Spring Boot 3 com Java 17, Maven, PostgreSQL, Flyway, Swagger/OpenAPI, Spring Security e login tradicional com JWT.

## Dependencias iniciais

Dependencias escolhidas para o Spring Initializr:

- Spring Web: cria a API REST consumida pelo frontend Next.js.
- Spring Security: autenticação, autorização e proteção das rotas.
- Spring Data JPA: persistência com repositories e entidades.
- PostgreSQL Driver: conexão com o banco PostgreSQL.
- Validation: validação de DTOs de entrada com Bean Validation.
- Lombok: redução de boilerplate em entidades, services e configs.
- Spring Boot DevTools: produtividade em desenvolvimento.
- Spring Mail: envio futuro de validação de e-mail, recuperação de senha e recibos.
- OAuth2 Client: preparação para login com Google.
- WebSocket: preparação para chat em tempo real com STOMP.

Dependencias adicionadas manualmente no `pom.xml`:

- Springdoc OpenAPI: documentação Swagger em `/swagger-ui.html`.
- JJWT: geração e validação de tokens JWT.
- Flyway: migrations versionadas do banco de dados.

Dependencias previstas para etapas futuras:

- Cloudinary SDK: upload e gestão de imagens de produtos e avatares.
- Mercado Pago SDK: pagamentos PIX, crédito, débito e webhooks.
- Biblioteca de rate limiting, como Bucket4j: proteção de login e recuperação de senha.

## Requisitos locais

- Java 17
- Maven
- PostgreSQL

Variaveis principais:

```env
DB_URL=jdbc:postgresql://localhost:5432/espetinho
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=troque-por-uma-chave-segura-com-no-minimo-32-caracteres
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

## Executar

```bash
mvn spring-boot:run
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

## Deploy no Render com Docker

No Render, crie um `Web Service` usando o repositorio do GitHub e selecione `Docker` como runtime.

Variaveis de ambiente necessarias no Render:

```env
DB_URL=jdbc:postgresql://host-do-banco:5432/nome-do-banco
DB_USERNAME=usuario-do-banco
DB_PASSWORD=senha-do-banco
JWT_SECRET=troque-por-uma-chave-segura-com-no-minimo-32-caracteres
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

O Render define a variavel `PORT` automaticamente. A aplicacao ja esta preparada para usar essa porta em producao e continuar usando `8080` localmente.

## Endpoint implementado

### POST `/api/v1/auth/register`

Requisicao:

```json
{
  "name": "Pedro Henrique",
  "email": "usuario@email.com",
  "password": "123456"
}
```

Resposta de sucesso:

```json
{
  "success": true,
  "message": "Cadastro realizado com sucesso",
  "data": {
    "id": "uuid-do-usuario",
    "name": "Pedro Henrique",
    "email": "usuario@email.com",
    "role": "CLIENT",
    "emailVerified": true
  }
}
```

Observacao: nesta etapa de desenvolvimento o usuario nasce com `emailVerified=true` para facilitar os testes do frontend. Antes de producao, isso deve voltar para `false` junto com a implementacao da validacao de e-mail.

### POST `/api/v1/auth/login`

Requisicao:

```json
{
  "email": "usuario@email.com",
  "password": "123456"
}
```

Resposta de sucesso:

```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "data": {
    "token": "jwt_token_aqui",
    "user": {
      "id": "uuid-do-usuario",
      "name": "Pedro Henrique",
      "email": "usuario@email.com",
      "role": "CLIENT"
    }
  }
}
```

### GET `/api/v1/users/me`

Requisicao:

```text
Authorization: Bearer <token>
```

Resposta de sucesso:

```json
{
  "success": true,
  "message": "Usuario autenticado",
  "data": {
    "id": "uuid-do-usuario",
    "name": "Pedro Henrique",
    "email": "usuario@email.com",
    "role": "CLIENT"
  }
}
```

Resposta de credenciais invalidas:

```json
{
  "success": false,
  "message": "E-mail ou senha invalidos"
}
```

### POST `/api/v1/auth/forgot-password`

Solicita um codigo de recuperacao de senha.

Requisicao:

```json
{
  "email": "usuario@email.com"
}
```

Resposta:

```json
{
  "success": true,
  "message": "Se o e-mail existir, enviaremos um codigo de recuperacao"
}
```

Observacao: nesta etapa de desenvolvimento o codigo aparece no log da aplicacao. Exemplo:

```text
Codigo de recuperacao de senha para usuario@email.com: 483921
```

### POST `/api/v1/auth/verify-reset-code`

Valida se o codigo de recuperacao ainda esta correto e dentro do prazo.

Requisicao:

```json
{
  "email": "usuario@email.com",
  "code": "483921"
}
```

Resposta:

```json
{
  "success": true,
  "message": "Codigo valido"
}
```

### POST `/api/v1/auth/reset-password`

Define uma nova senha usando o codigo de recuperacao.

Requisicao:

```json
{
  "email": "usuario@email.com",
  "code": "483921",
  "newPassword": "novaSenha123"
}
```

Resposta:

```json
{
  "success": true,
  "message": "Senha alterada com sucesso"
}
```

Codigos de recuperacao expiram em 15 minutos, sao salvos com hash BCrypt e sao invalidados apos o uso.

## Contrato de seguranca atual

- `/api/v1/auth/login` esta publico.
- `/api/v1/admin/**` exige role `ADMIN`.
- `/api/v1/staff/**` aceita `STAFF` ou `ADMIN`.
- `GET /api/v1/products/**` fica publico para o cardapio.
- Demais rotas exigem JWT valido no header `Authorization: Bearer <token>`.

## Usuario para teste manual

Enquanto o cadastro ainda nao foi implementado, insira um usuario diretamente no banco usando uma senha BCrypt gerada pela aplicacao ou por uma ferramenta confiavel.

Exemplo de insert, substituindo `password_hash` por um hash BCrypt real:

```sql
INSERT INTO users (id, name, email, password_hash, role, active, email_verified)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Pedro Henrique',
    'usuario@email.com',
    '$2a$10$substitua_por_um_hash_bcrypt_real',
    'CLIENT',
    true,
    true
);
```

## Proximas etapas sugeridas

1. Cadastro tradicional com envio de codigo de verificacao por e-mail.
2. Refresh token ou estrategia de renovacao de sessao.
3. Recuperacao de senha.
4. Login com Google via OAuth2.
5. Rate limiting para autenticação.
6. Modulos de produtos, carrinho e pedidos.

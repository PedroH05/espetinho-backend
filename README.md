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
GOOGLE_CLIENT_ID=client-id-do-google
GOOGLE_CLIENT_SECRET=client-secret-do-google
FRONTEND_BASE_URL=http://localhost:3000
GOOGLE_SUCCESS_PATH=/auth/google/callback
GOOGLE_FAILURE_PATH=/login
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
GOOGLE_CLIENT_ID=client-id-do-google
GOOGLE_CLIENT_SECRET=client-secret-do-google
FRONTEND_BASE_URL=http://localhost:3000
GOOGLE_SUCCESS_PATH=/auth/google/callback
GOOGLE_FAILURE_PATH=/login
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
    "roles": ["CLIENT"],
    "emailVerified": true
  }
}
```

Observacao: nesta etapa de desenvolvimento o usuario nasce com `emailVerified=true` para facilitar os testes do frontend. Antes de producao, isso deve voltar para `false` junto com a implementacao da validacao de e-mail.

Observacao sobre permissoes: a API retorna `roles` como array porque um usuario pode ter mais de uma funcao no futuro. Exemplo de funcionario de caixa: `["STAFF", "CASHIER"]`.

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
      "roles": ["CLIENT"]
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
    "roles": ["CLIENT"]
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

Observacao: recuperacao de senha e permitida apenas para usuarios com `authProvider=LOCAL`. Usuarios criados por login Google terao autenticacao gerenciada pelo Google.

### GET `/api/v1/auth/google`

Inicia o login com Google. Esta rota deve ser aberta pelo navegador, nao pelo Axios.

Fluxo:

1. Frontend redireciona o usuario para:

```text
GET /api/v1/auth/google
```

2. Backend redireciona para o Google.
3. Google retorna para o callback tecnico do backend:

```text
/api/v1/auth/google/callback/google
```

4. Backend cria ou atualiza o usuario, gera o JWT e redireciona para o frontend:

```text
http://localhost:3000/auth/google/callback?token=jwt_aqui
```

Em producao, configure no Google Cloud o redirect URI:

```text
https://espetinho-backend.onrender.com/api/v1/auth/google/callback/google
```

Em desenvolvimento local:

```text
http://localhost:8080/api/v1/auth/google/callback/google
```

### GET `/api/v1/categories`

Lista categorias ativas para montar filtros no cardapio.

Resposta:

```json
{
  "success": true,
  "message": "Categorias consultadas com sucesso",
  "data": [
    {
      "id": "uuid-da-categoria",
      "name": "Espetinhos",
      "slug": "espetinhos",
      "displayOrder": 1
    }
  ]
}
```

### GET `/api/v1/products`

Lista o cardapio publico. Por padrao retorna apenas produtos ativos e disponiveis.

Parametros opcionais:

```text
search=carne
categoryId=uuid-da-categoria
available=true
```

Exemplos:

```text
GET /api/v1/products
GET /api/v1/products?search=carne
GET /api/v1/products?categoryId=uuid-da-categoria
GET /api/v1/products?search=carne&categoryId=uuid-da-categoria
```

Resposta:

```json
{
  "success": true,
  "message": "Cardapio consultado com sucesso",
  "data": {
    "categories": [
      {
        "id": "uuid-da-categoria",
        "name": "Espetinhos",
        "slug": "espetinhos",
        "displayOrder": 1
      }
    ],
    "products": [
      {
        "id": "uuid-do-produto",
        "name": "Espetinho de Carne",
        "description": "Espetinho de carne bovina temperada",
        "price": 9.90,
        "category": {
          "id": "uuid-da-categoria",
          "name": "Espetinhos",
          "slug": "espetinhos",
          "displayOrder": 1
        },
        "imageUrls": [],
        "available": true,
        "stockQuantity": 20
      }
    ]
  }
}
```

### GET `/api/v1/products/{id}`

Busca um produto ativo pelo ID.

### POST `/api/v1/products`

Cria um produto. Exige token de usuario `ADMIN`.

Requisicao:

```json
{
  "name": "Espetinho de Carne",
  "description": "Espetinho de carne bovina temperada",
  "price": 9.90,
  "categoryId": "uuid-da-categoria",
  "imageUrls": [],
  "available": true,
  "stockQuantity": 20
}
```

### PUT `/api/v1/products/{id}`

Atualiza um produto. Exige token de usuario `ADMIN`.

### PATCH `/api/v1/products/{id}/deactivate`

Marca o produto como indisponivel temporariamente (`available=false`). Exige token de usuario `ADMIN`.

### PATCH `/api/v1/products/{id}/activate`

Restaura o produto e marca como disponivel (`active=true` e `available=true`). Exige token de usuario `ADMIN`.

### DELETE `/api/v1/products/{id}`

Remove o produto usando soft delete (`active=false`). O registro continua no banco para permitir recuperacao. Exige token de usuario `ADMIN`.

### Carrinho `/api/v1/cart`

O carrinho funciona tanto para usuario logado quanto para visitante.

Para usuario logado:

```text
Authorization: Bearer <token>
```

Para visitante sem login, o frontend deve gerar um UUID e enviar em todas as chamadas:

```text
X-Guest-Id: 550e8400-e29b-41d4-a716-446655440000
```

Rotas:

```text
GET    /api/v1/cart
POST   /api/v1/cart/items
PUT    /api/v1/cart/items/{itemId}
DELETE /api/v1/cart/items/{itemId}
DELETE /api/v1/cart
```

Adicionar item:

```json
{
  "productId": "uuid-do-produto",
  "quantity": 2
}
```

Alterar quantidade:

```json
{
  "quantity": 3
}
```

Resposta:

```json
{
  "success": true,
  "message": "Carrinho consultado com sucesso",
  "data": {
    "id": "uuid-do-carrinho",
    "userId": null,
    "guestId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "id": "uuid-do-item",
        "productId": "uuid-do-produto",
        "productName": "Espetinho de Carne",
        "imageUrl": null,
        "unitPrice": 9.90,
        "quantity": 2,
        "subtotal": 19.80,
        "available": true
      }
    ],
    "totalItems": 2,
    "totalAmount": 19.80
  }
}
```

Se o produto ja existir no carrinho, `POST /api/v1/cart/items` soma a quantidade.

### Pedidos `/api/v1/orders`

Cria pedido a partir do carrinho ativo. Funciona com usuario logado ou visitante.

Para visitante, enviar o mesmo header usado no carrinho:

```text
X-Guest-Id: 550e8400-e29b-41d4-a716-446655440000
```

Criar pedido de retirada:

```text
POST /api/v1/orders
```

```json
{
  "type": "PICKUP",
  "customer": {
    "name": "Pedro Henrique",
    "phone": "12999999999",
    "email": "usuario@email.com"
  },
  "notes": "Sem farofa"
}
```

Criar pedido delivery:

```json
{
  "type": "DELIVERY",
  "customer": {
    "name": "Pedro Henrique",
    "phone": "12999999999",
    "email": "usuario@email.com"
  },
  "deliveryAddress": {
    "street": "Rua das Flores",
    "number": "123",
    "complement": "Casa",
    "neighborhood": "Centro"
  },
  "notes": "Entregar no portao"
}
```

Resposta:

```json
{
  "success": true,
  "message": "Pedido criado com sucesso",
  "data": {
    "id": "uuid-do-pedido",
    "userId": null,
    "guestId": "550e8400-e29b-41d4-a716-446655440000",
    "customer": {
      "name": "Pedro Henrique",
      "phone": "12999999999",
      "email": "usuario@email.com"
    },
    "type": "DELIVERY",
    "status": "AWAITING_PAYMENT",
    "deliveryAddress": {
      "street": "Rua das Flores",
      "number": "123",
      "complement": "Casa",
      "neighborhood": "Centro"
    },
    "items": [
      {
        "id": "uuid-do-item",
        "productId": "uuid-do-produto",
        "productName": "Espetinho de Carne",
        "unitPrice": 9.90,
        "quantity": 2,
        "subtotal": 19.80
      }
    ],
    "totalAmount": 19.80,
    "createdAt": "2026-07-02T17:00:00Z"
  }
}
```

Consultar pedido:

```text
GET /api/v1/orders/{id}
```

Visitante deve enviar o mesmo `X-Guest-Id` usado na compra. Usuario logado deve enviar o JWT. `ADMIN` e `STAFF` podem consultar pedidos.

Observacao: `DINE_IN` ja esta preparado no enum e no banco, mas ainda retorna erro porque mesas/QR Code serao implementados em uma etapa propria.

Categoria e produto de teste para inserir pelo DBeaver ou Neon:

```sql
INSERT INTO categories (id, name, slug, active, display_order)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    'Espetinhos',
    'espetinhos',
    true,
    1
);

INSERT INTO products (id, name, description, price, category_id, available, active, stock_quantity)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Espetinho de Carne',
    'Espetinho de carne bovina temperada',
    9.90,
    '22222222-2222-2222-2222-222222222222',
    true,
    true,
    20
);
```

## Contrato de seguranca atual

- `/api/v1/auth/login` esta publico.
- `/api/v1/auth/google` inicia o login com Google.
- `/api/v1/admin/**` exige `ADMIN` dentro de `roles`.
- `/api/v1/staff/**` aceita `STAFF` ou `ADMIN`.
- `GET /api/v1/categories/**` fica publico para filtros do cardapio.
- `GET /api/v1/products/**` fica publico para o cardapio.
- `/api/v1/cart/**` fica publico para permitir compra sem login. Visitantes devem enviar `X-Guest-Id`.
- `POST /api/v1/orders` fica publico para permitir compra sem login. Visitantes devem enviar `X-Guest-Id`.
- `GET /api/v1/orders/{id}` fica publico, mas o backend valida se o pedido pertence ao usuario logado ou ao `X-Guest-Id`.
- Escrita em `/api/v1/products/**` exige `ADMIN` dentro de `roles`.
- Demais rotas exigem JWT valido no header `Authorization: Bearer <token>`.

## Usuario para teste manual

Para criar um usuario manualmente no banco, use uma senha BCrypt gerada pela aplicacao ou por uma ferramenta confiavel.

Exemplo de insert, substituindo `password_hash` por um hash BCrypt real:

```sql
INSERT INTO users (id, name, email, password_hash, active, email_verified)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Pedro Henrique',
    'usuario@email.com',
    '$2a$10$substitua_por_um_hash_bcrypt_real',
    true,
    true
);

INSERT INTO user_roles (user_id, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'CLIENT');
```

Para transformar um usuario existente em administrador:

```sql
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE email = 'usuario@email.com'
ON CONFLICT (user_id, role) DO NOTHING;
```

## Proximas etapas sugeridas

1. Cadastro tradicional com envio de codigo de verificacao por e-mail.
2. Refresh token ou estrategia de renovacao de sessao.
3. Recuperacao de senha.
4. Login com Google via OAuth2.
5. Rate limiting para autenticação.
6. Modulos de produtos, carrinho e pedidos.

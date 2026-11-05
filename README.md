# 🛒 Cadastro de Produtos — Spring Boot + Thymeleaf

Aplicação web para **cadastrar, listar, filtrar e remover produtos**, construída com Java 17, Spring Boot 3, Thymeleaf, Spring Data JPA e banco H2 embarcado. Segue o padrão **MVC** e o padrão **Post/Redirect/Get (PRG)**.

## Funcionalidades

- Formulário de cadastro: nome, descrição, preço, estoque e categoria (select com opções fixas).
- Validação no servidor (preço < 0, nome vazio, estoque negativo etc.) com mensagens de erro no formulário.
- Listagem em tabela com contador total de produtos.
- Busca por nome e filtro por categoria.
- Botão **Remover** em cada linha.
- Persistência em banco H2 (arquivo `./data/produtosdb`), substituindo a lista estática da aula.
- Estilo com Bootstrap 4 + CSS próprio (`static/css/estilo.css`).

## Tecnologias

Java 17 · Spring Boot 3.3 · Spring Web · Thymeleaf · Bean Validation · Spring Data JPA · H2 · Maven

## Como executar

Pré-requisitos: JDK 17+ e Maven 3.8+.

```bash
mvn spring-boot:run
```

Acesse **http://localhost:8080** (redireciona para `/cadastrar`). Console do banco: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/produtosdb`, usuário `sa`, senha vazia). Se a porta 8080 estiver ocupada, descomente `server.port` em `application.properties`.

## Estrutura

```
src/main/java/com/example/cadastroprodutos/
├── CadastroProdutosApplication.java   # ponto de entrada
├── model/Produto.java                 # entidade + regras de validação
├── repository/ProdutoRepository.java  # acesso ao banco (Spring Data JPA)
├── service/ProdutoService.java        # regras de negócio / filtros
└── controller/ProdutoController.java  # rotas HTTP
src/main/resources/
├── application.properties
├── messages.properties                # mensagens de erro de conversão de tipo
├── static/css/estilo.css
└── templates/
    ├── cadastrar.html                 # formulário
    └── lista.html                     # tabela + busca
```

## Rotas

| Método | URL                     | O que faz                                              |
|--------|-------------------------|--------------------------------------------------------|
| GET    | `/`                     | Redireciona para `/cadastrar`                          |
| GET    | `/cadastrar`            | Exibe o formulário vazio                               |
| POST   | `/cadastrar`            | Valida e salva; redireciona para `/lista` (ou reexibe com erros) |
| GET    | `/lista`                | Lista produtos; aceita `?busca=` e `?categoria=`       |
| POST   | `/remover/{id}`         | Remove o produto e volta para `/lista`                 |

## Como o Thymeleaf funciona neste projeto

O Thymeleaf é um **motor de templates executado no servidor**. Os arquivos `.html` em `templates/` são HTML válido com atributos extras `th:*`. Quando o navegador pede uma página, o Spring executa o Controller, que entrega dados ao template; o Thymeleaf **processa os atributos `th:*`, gera o HTML final** e só então o envia ao navegador (o navegador nunca vê `th:`).

### Recuperando dados (Controller → página)

1. O navegador faz `GET /lista`.
2. O Controller consulta o Service/Repository e coloca o resultado no `Model`:
   ```java
   model.addAttribute("produtos", service.listar(busca, categoria));
   return "lista";   // renderiza templates/lista.html
   ```
3. No template, as expressões `${...}` leem esses atributos:
   - `th:each="produto, iterStat : ${produtos}"` repete a `<tr>` para cada produto;
   - `th:text="${produto.nome}"` escreve o valor (o Thymeleaf chama `getNome()`);
   - `th:if` / `th:unless` mostram a mensagem "nenhum produto" ou a tabela;
   - `#numbers.formatDecimal(...)` formata o preço como `49,90`;
   - `th:value`, `th:selected` repreenchem os campos de busca com o que o usuário digitou.

### Inserindo dados (página → Controller → banco)

1. `GET /cadastrar` cria um `Produto` vazio e o envia ao template:
   ```java
   model.addAttribute("produto", new Produto());
   ```
2. No formulário, `th:object="${produto}"` vincula o `<form>` a esse objeto e `th:field="*{nome}"` gera `id`, `name` e `value` de cada campo (o `*{}` acessa propriedades do objeto vinculado). `th:action="@{/cadastrar}"` define o destino do POST.
3. Ao enviar, o navegador faz `POST /cadastrar` com os campos (`nome=...&preco=...`).
4. O Spring (`@ModelAttribute Produto produto`) cria um `Produto` e preenche os atributos pelos **setters**. `@Valid` executa as validações do model e o resultado fica em `BindingResult`.
5. **Se houver erros**, o Controller retorna `"cadastrar"` de novo; o template mostra as mensagens com `#fields.hasErrors('preco')` e `th:errors="*{preco}"`, e os campos já vêm preenchidos com o que foi digitado.
6. **Se estiver válido**, `service.salvar(produto)` grava no H2 via JPA e o Controller devolve `redirect:/lista` (PRG), evitando reenvio duplicado ao atualizar a página. Uma mensagem de sucesso viaja por `RedirectAttributes.addFlashAttribute` e é exibida uma única vez na lista.

### Removendo

Cada linha tem um `<form th:action="@{/remover/{id}(id=${produto.id})}" method="post">`. O `@{...}` monta a URL com o id do produto; o Controller recebe `@PathVariable Long id`, chama `service.remover(id)` e redireciona.

### Fluxo completo

```
Navegador ──GET /cadastrar──▶ Controller ──Model(produto vazio)──▶ cadastrar.html ──▶ formulário
Navegador ──POST /cadastrar─▶ Controller ──@Valid──▶ erro? ──sim──▶ cadastrar.html (com mensagens)
                                         └─não─▶ Service ▶ Repository ▶ H2 ──redirect──▶ GET /lista
Navegador ──GET /lista──────▶ Controller ──Model(produtos)──▶ lista.html ──▶ tabela
```

## Diretivas Thymeleaf usadas

| Diretiva | Uso |
|---|---|
| `th:action`, `@{/rota}` | URL do formulário / links relativos à raiz da aplicação |
| `th:object`, `th:field`, `*{}` | Vincula formulário ao objeto e gera os campos |
| `th:text`, `th:value`, `th:selected` | Escreve valores e estado dos campos |
| `th:each` | Laço sobre listas (produtos, categorias) |
| `th:if`, `th:unless` | Exibição condicional |
| `th:classappend` | Adiciona classe CSS condicionalmente (erro, estoque baixo) |
| `th:errors`, `#fields.hasErrors()` | Mensagens de validação |

## Exercícios da aula implementados

| Nível | Exercício | Onde |
|---|---|---|
| 1 | Novos atributos (`descricao`, `estoque`) | `Produto`, templates |
| 1 | Estilo CSS | `static/css/estilo.css` + Bootstrap |
| 1 | Total de produtos abaixo da tabela | `lista.html` |
| 2 | Select de categoria com opções fixas | `cadastrar.html`, `categorias()` no Controller |
| 2 | Validação de preço < 0 com mensagem de erro | `@DecimalMin` + `th:errors` |
| 2 | Coluna "Ações" com botão Remover | `lista.html`, `POST /remover/{id}` |
| 3 | `@Service` separado do Controller | `ProdutoService` |
| 3 | Busca/filtro por nome e categoria | `GET /lista?busca=&categoria=` |
| 3 | Spring Data JPA + H2 | `ProdutoRepository`, `application.properties` |

## Testando

1. Acesse `/cadastrar`, cadastre "Camiseta", 49.90, estoque 10, "Vestuário" → vai para `/lista`.
2. Tente preço `-5` → o formulário reaparece com erro e nada é salvo.
3. Use a busca por nome e o filtro de categoria.
4. Remova um produto; reinicie o servidor e veja que os dados persistem (H2 em arquivo).

# Specification-Driven Design (SDD): PocketPDV Core & Engine

---

## 1. System Metadata & Execution Boundaries

* **System Identifier:** `pocket-pdv`
* **Target Container:** `nano-spring` (Minimalist IoC / DI / Web Router)
* **Target Runtime Platform:** Android 6.0.1 (API 23) / Android 7.1.1 (API 25)
* **Reference Hardware:** Motorola Moto G4 Play (MSM8916 Snapdragon 410, 4x Cortex-A53 @ 1.2 GHz, 2 GB LPDDR3, eMMC 4.5)
* **Execution Boundary:** Single-process hybrid application combining an embedded HTTP daemon in an Android `ForegroundService` with an in-process native Android UI.

---

## 2. Formal System Invariants & Constraints

```
[Invariant 1]  Heap(PocketPDV) <= 60 MB under sustained peak load
[Invariant 2]  ColdBoot(nano-spring) <= 500 ms (Context Init -> Socket Listen)
[Invariant 3]  Isolation(NativeUI) -> Uses Direct DI Context (Zero Loopback HTTP)
[Invariant 4]  Network IO -> Exclusively Port 8080 (Non-privileged, non-root)
[Invariant 5]  SQLite Concurrency -> WAL Mode (Readers never block Writers)

```

---

## 3. Component Interface Contracts

### 3.1 Container Lifecycle Contract (`nano-spring`)

```java
package com.nanospring.core;

public interface ApplicationContext {
    void start();
    void stop();
    <T> T getBean(Class<T> requiredType);
    boolean containsBean(Class<?> requiredType);
    void registerSingleton(Class<?> type, Object instance);
}

```

* **Pre-conditions for `start()`:** Storage directory initialized; `SQLiteOpenHelper` instance created.
* **Post-conditions for `start()`:** All `@Component`, `@Repository`, `@Service`, and `@Controller` singletons resolved and wired via constructor injection. No lazy circular references permitted.

### 3.2 Service Layer Contract (Domain Services)

```java
package com.nanospring.pocketpdv.domain.service;

import java.util.List;
import com.nanospring.pocketpdv.domain.model.*;

public interface EstoqueService {
    Produto obterPorCodigoBarras(String codigoBarras);
    List<Produto> listarTodos();
    void atualizarEstoque(long produtoId, int deltaQuantidade);
}

public interface VendaService {
    Venda criarVenda(List<ItemVendaComando> itens);
    void cancelarVenda(long vendaId);
}

public interface RelatorioService {
    RelatorioDiarioDTO obterMetricasHoje();
    List<AlertaEstoqueDTO> obterAlertasEstoque(int corteMinimo);
}

```

### 3.3 Security & Middleware Filter Contract

```java
package com.nanospring.web.filter;

import com.nanospring.web.http.HttpRequest;
import com.nanospring.web.http.HttpResponse;

public interface RequestFilter {
    /**
     * @return true se a requisicao deve prosseguir; false para interromper o pipeline.
     */
    boolean filter(HttpRequest request, HttpResponse response);
}

```

---

## 4. State & Persistence Specification

### 4.1 SQLite PRAGMA Protocol

Upon opening the database handle through `SQLiteOpenHelper`, the connection pool initialization must issue the following operational directives sequentially:

```sql
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;
PRAGMA foreign_keys = ON;
PRAGMA temp_store = MEMORY;
PRAGMA cache_size = -2000; -- Limita cache de paginas a ~2 MB

```

### 4.2 Entity Relational Model & Index Constraints

```
+-------------------------------------------------------------+
|                          produtos                           |
+----------------------+---------------+----------------------+
| id                   | INTEGER (PK)  | AUTOINCREMENT        |
| codigo_barras        | TEXT          | NOT NULL, UNIQUE     |
| nome                 | TEXT          | NOT NULL             |
| preco_centavos       | INTEGER       | NOT NULL             |
| estoque              | INTEGER       | NOT NULL, DEFAULT 0  |
+----------------------+---------------+----------------------+
INDEX: idx_produtos_codigo ON produtos(codigo_barras)

                              | 1
                              |
                              | N
+-----------------------------v-------------------------------+
|                        itens_venda                          |
+----------------------+---------------+----------------------+
| id                   | INTEGER (PK)  | AUTOINCREMENT        |
| venda_id             | INTEGER (FK)  | REFERENCES vendas(id)|
| produto_id           | INTEGER (FK)  | REFERENCES produtos  |
| quantidade           | INTEGER       | NOT NULL             |
| preco_unit_centavos  | INTEGER       | NOT NULL             |
+----------------------+---------------+----------------------+
INDEX: idx_itens_venda_venda_id ON itens_venda(venda_id)
INDEX: idx_itens_venda_produto_id ON itens_venda(produto_id)

                              ^ N
                              |
                              | 1
+-----------------------------+-------------------------------+
|                           vendas                            |
+----------------------+---------------+----------------------+
| id                   | INTEGER (PK)  | AUTOINCREMENT        |
| data_hora            | TEXT (ISO)    | NOT NULL             |
| total_centavos       | INTEGER       | NOT NULL             |
| status               | TEXT          | 'CONCLUIDA'/'CANCEL' |
+----------------------+---------------+----------------------+
INDEX: idx_vendas_data ON vendas(data_hora)

```

---

## 5. Web Interface & Hypermedia Protocol

### 5.1 Route Dispatch Matrix

| HTTP Verb | Path | Request Body | Required Auth | Response Type | Target Container Element (HTMX) |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/login` | N/A | None | `text/html` | Full Page Render |
| `POST` | `/login` | `form-urlencoded` | None | `302 / HX-Redirect` | Document Redirect |
| `POST` | `/logout` | N/A | Session Cookie (optional) | `302` + `Set-Cookie: Max-Age=0` | Invalida sessão server-side; GET não é aceito |
| `GET` | `/pdv` | N/A | Session Cookie | `text/html` | Full Page Shell |
| `POST` | `/pdv/carrinho/item` | `form-urlencoded` | Session Cookie | `text/html` fragment | `#cart-table-body`, `#cart-total` (OOB Swap) |
| `DELETE` | `/pdv/carrinho/item/{id}` | N/A | Session Cookie | `text/html` fragment | `#cart-table-body`, `#cart-total` (OOB Swap) |
| `POST` | `/pdv/carrinho/checkout` | N/A | Session Cookie | `text/html` fragment | `#modal-checkout-container` |
| `GET` | `/assets/{file}` | N/A | None | `text/css`, `application/javascript` | Static Asset Pipeline (RAM cached) |

### 5.2 OOB (Out-of-Band) Hypermedia Payload Spec

Exemplo de fragmento emitido por `POST /pdv/carrinho/item` para atualização simultânea do item e do totalizador sem re-renderizar a página completa:

```html
<!-- Fragmento Primario: Linha da Tabela -->
<tr id="item-row-102">
  <td class="px-4 py-2 font-mono">7891000100101</td>
  <td class="px-4 py-2">Cafe Torrado 500g</td>
  <td class="px-4 py-2 text-right">2x R$ 14,50</td>
  <td class="px-4 py-2 text-right font-bold">R$ 29,00</td>
  <td class="px-2 py-2 text-center">
    <button hx-delete="/pdv/carrinho/item/102" 
            hx-target="#cart-table-body" 
            class="text-red-600 hover:font-bold">×</button>
  </td>
</tr>

<!-- Atualizacao Out-of-Band do Total -->
<div id="cart-total" hx-swap-oob="true" class="text-2xl font-black text-emerald-600">
  R$ 29,00
</div>

```

---

## 6. Concurrency & Execution Blueprint

```
[Android OS Process: com.nanospring.pocketpdv]
 │
 ├── [Main UI Thread] (Prioridade: FOREGROUND)
 │    └── Activity / Views da Gerência
 │         └── Invocação direta: NanoSpring.getBean(RelatorioService.class)
 │
 └── [Foreground Service Thread Group] (Prioridade: BACKGROUND / THREAD_PRIORITY_BACKGROUND)
      │
      ├── [Thread: HttpAcceptor]
      │    └── loop { socket = serverSocket.accept(); threadPool.execute(worker); }
      │
      └── [Bounded ThreadPoolExecutor] (Pool: 4, Max: 8, Queue: ArrayBlockingQueue(32))
           ├── Worker 1: Parse HTTP -> nano-spring Router -> Controller -> Render HTML
           ├── Worker 2: Asset Streamer (RAM Buffer read)
           ├── Worker 3: Idle
           └── Worker 4: Idle

```

* **Executor Rejection Policy:** `ThreadPoolExecutor.AbortPolicy` (retorna imediatamente `HTTP 503 Service Unavailable` em vez de acumular memória na fila se houver sobrecarga de rede).
* **Power Management Constraints:** O serviço deve instanciar `PowerManager.WakeLock` sob `PARTIAL_WAKE_LOCK` e `WifiManager.WifiLock` sob `WIFI_MODE_FULL_HIGH_PERF`, retendo as instâncias estritamente até `onDestroy()` do serviço.

---

## 7. Quality Gates & Verification Protocols

```
                  +-----------------------------------+
                  |   Test Suite Verification Gate    |
                  +-----------------+-----------------+
                                    |
            +-----------------------+-----------------------+
            |                                               |
            v                                               v
[Static Profiling: Unit]                       [Hardware In-Situ: Moto G4]
 1. nano-spring IoC graph resolution            1. Cold-boot delta <= 500ms
 2. SecurityFilter 401 on missing cookie        2. Heap usage <= 60MB under 50 rps
 3. SQL syntax verification against SQLite DDL  3. Zero SQLiteLockedException with concurrent UI
                                                4. WakeLock ping validation (Screen off > 15m)

```

1. **Gate 1 (IoC Initialization Latency):**
* *Ação:* Medir via `System.nanoTime()` o intervalo entre a chamada `NanoSpring.init()` e a resolução completa do último Bean.
* *Aprovação:* $\Delta t \le 350\text{ ms}$ no hardware do Snapdragon 410.


2. **Gate 2 (Concorrência SQLite / WAL):**
* *Ação:* Executar script com 20 requisições sequenciais de inserção de vendas via HTTP enquanto a UI nativa executa 10 buscas de faturamento consecutivas na Main Thread.
* *Aprovação:* 0 ocorrências de `android.database.sqlite.SQLiteDatabaseLockedException`.


3. **Gate 3 (Memória e Garbage Collection):**
* *Ação:* Monitorar via Android Studio Profiler durante fluxo contínuo de 10 minutos adicionando e removendo itens do carrinho.
* *Aprovação:* Retenção estável do Heap sem curvas ascendentes monotônicas (indicativo de leak de listeners ou sockets abertos); teto absoluto $\le 60\text{ MB}$.
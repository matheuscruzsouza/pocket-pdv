package com.github.matheuscruzsouza.pocketpdv.domain.service;

import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Service;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemCarrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemVenda;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemVendaComando;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;
import com.github.matheuscruzsouza.pocketpdv.persistence.DatabaseHelper;
import com.github.matheuscruzsouza.pocketpdv.persistence.FuncionarioRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.ItemVendaRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.PagamentoRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.ProdutoRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.VendaRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class VendaServiceImpl implements VendaService {

    @Autowired
    private DatabaseHelper dbHelper;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    private static final Object DB_LOCK = new Object();

    public VendaServiceImpl() {
    }

    public VendaServiceImpl(DatabaseHelper dbHelper,
                            ProdutoRepository produtoRepository,
                            VendaRepository vendaRepository,
                            ItemVendaRepository itemVendaRepository) {
        this(dbHelper, produtoRepository, vendaRepository, itemVendaRepository, null);
    }

    public VendaServiceImpl(DatabaseHelper dbHelper,
                            ProdutoRepository produtoRepository,
                            VendaRepository vendaRepository,
                            ItemVendaRepository itemVendaRepository,
                            PagamentoRepository pagamentoRepository) {
        this.dbHelper = dbHelper;
        this.produtoRepository = produtoRepository;
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    private DatabaseHelper getDbHelper() {
        if (dbHelper != null) return dbHelper;
        if (PocketPdvService.getInstance() != null) {
            dbHelper = PocketPdvService.getInstance().getDbHelper();
        }
        return dbHelper;
    }

    private ProdutoRepository getProdutoRepository() {
        if (produtoRepository != null) return produtoRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            produtoRepository = (ProdutoRepository) PocketPdvService.getInstance().getServer().getBean(ProdutoRepository.class);
        }
        if (produtoRepository == null) {
            produtoRepository = new ProdutoRepository(getDbHelper());
        }
        return produtoRepository;
    }

    private VendaRepository getVendaRepository() {
        if (vendaRepository != null) return vendaRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            vendaRepository = (VendaRepository) PocketPdvService.getInstance().getServer().getBean(VendaRepository.class);
        }
        if (vendaRepository == null) {
            vendaRepository = new VendaRepository(getDbHelper());
        }
        return vendaRepository;
    }

    private ItemVendaRepository getItemVendaRepository() {
        if (itemVendaRepository != null) return itemVendaRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            itemVendaRepository = (ItemVendaRepository) PocketPdvService.getInstance().getServer().getBean(ItemVendaRepository.class);
        }
        if (itemVendaRepository == null) {
            itemVendaRepository = new ItemVendaRepository(getDbHelper());
        }
        return itemVendaRepository;
    }

    private FuncionarioRepository getFuncionarioRepository() {
        if (funcionarioRepository != null) return funcionarioRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            funcionarioRepository = (FuncionarioRepository) PocketPdvService.getInstance().getServer().getBean(FuncionarioRepository.class);
        }
        if (funcionarioRepository == null) {
            funcionarioRepository = new FuncionarioRepository(getDbHelper());
        }
        return funcionarioRepository;
    }

    private PagamentoRepository getPagamentoRepository() {
        if (pagamentoRepository != null) return pagamentoRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            pagamentoRepository = (PagamentoRepository) PocketPdvService.getInstance().getServer().getBean(PagamentoRepository.class);
        }
        if (pagamentoRepository == null) {
            pagamentoRepository = new PagamentoRepository(getDbHelper());
        }
        return pagamentoRepository;
    }

    @Override
    public Venda finalizarVenda(Carrinho carrinho) {
        return finalizarVenda(carrinho, 1);
    }

    @Override
    public Venda finalizarVenda(Carrinho carrinho, long funcionarioId) {
        return finalizarVenda(carrinho, funcionarioId, (List<com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento>) null);
    }

    @Override
    public Venda finalizarVenda(Carrinho carrinho, long funcionarioId, String formaPagamento, int valorRecebidoCentavos, int trocoCentavos) {
        int totalCentavos = (carrinho != null) ? carrinho.getTotalCentavos() : 0;
        int valorPagamento = totalCentavos;
        int valRec = valorRecebidoCentavos > 0 ? valorRecebidoCentavos : valorPagamento;
        int troco = Math.max(0, trocoCentavos);
        String tipo = (formaPagamento != null && !formaPagamento.trim().isEmpty()) ? formaPagamento : "DINHEIRO";

        List<com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento> pagamentos = new ArrayList<>();
        pagamentos.add(new com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento(tipo, valorPagamento, valRec, troco));
        return finalizarVenda(carrinho, funcionarioId, pagamentos);
    }

    @Override
    public Venda finalizarVenda(Carrinho carrinho, long funcionarioId, List<com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento> pagamentos) {
        if (carrinho == null || carrinho.getItens().isEmpty()) {
            throw new IllegalArgumentException("Carrinho vazio.");
        }

        List<ItemVendaComando> comandos = new ArrayList<>();
        for (ItemCarrinho ic : carrinho.getItens()) {
            comandos.add(new ItemVendaComando(
                    ic.getProduto().getId(),
                    ic.getQuantidade(),
                    ic.getPrecoUnitCentavos()
            ));
        }

        Venda venda = finalizarVenda(comandos, funcionarioId, pagamentos);
        carrinho.limpar();
        return venda;
    }

    @Override
    public Venda finalizarVenda(List<ItemVendaComando> itens) {
        return finalizarVenda(itens, 1);
    }

    @Override
    public Venda finalizarVenda(List<ItemVendaComando> itens, long funcionarioId) {
        return finalizarVenda(itens, funcionarioId, (List<com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento>) null);
    }

    @Override
    public Venda finalizarVenda(List<ItemVendaComando> itens, long funcionarioId, List<com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento> pagamentos) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Nenhum item informado para a venda.");
        }

        synchronized (DB_LOCK) {
            SQLiteDatabase db = getDbHelper().getWritableDatabase();
            db.beginTransaction();
            try {
                int totalCentavos = 0;
                List<ItemVenda> itensVenda = new ArrayList<>();

                for (ItemVendaComando cmd : itens) {
                    Produto prod = getProdutoRepository().buscarPorId(cmd.getProdutoId());
                    if (prod == null) {
                        throw new IllegalArgumentException("Produto id=" + cmd.getProdutoId() + " não encontrado.");
                    }

                    // Decremento com verificação atômica de estoque
                    boolean decrementou = getProdutoRepository().decrementarEstoque(
                            cmd.getProdutoId(), cmd.getQuantidade(), db);

                    if (!decrementou) {
                        throw new IllegalStateException("Estoque insuficiente para o produto: " + prod.getNome());
                    }

                    int precoUnitario = prod.getPrecoCentavos();
                    int subtotal = precoUnitario * cmd.getQuantidade();
                    totalCentavos += subtotal;

                    itensVenda.add(new ItemVenda(
                            0,
                            0, // preenchido apos inserir venda
                            cmd.getProdutoId(),
                            cmd.getQuantidade(),
                            precoUnitario
                    ));
                }

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                String dataHora = isoFormat.format(new Date());

                long finalFuncId = funcionarioId > 0 ? funcionarioId : 1;
                Venda venda = new Venda(0, dataHora, totalCentavos, "CONCLUIDA", finalFuncId);
                long vendaId = getVendaRepository().salvar(venda, db);

                for (ItemVenda iv : itensVenda) {
                    iv.setVendaId(vendaId);
                }

                getItemVendaRepository().salvarTodos(itensVenda, db);

                if (pagamentos != null && !pagamentos.isEmpty()) {
                    for (com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento pag : pagamentos) {
                        pag.setVendaId(vendaId);
                    }
                    getPagamentoRepository().salvarTodos(pagamentos, db);
                }

                db.setTransactionSuccessful();
                venda.setId(vendaId);

                // Notifica clientes SSE em tempo real sobre o novo saldo de estoque
                try {
                    for (ItemVendaComando cmd : itens) {
                        Produto pAtual = getProdutoRepository().buscarPorId(cmd.getProdutoId());
                        if (pAtual != null) {
                            com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub.getInstance()
                                    .notificarEstoque(pAtual.getId(), pAtual.getEstoque());
                        }
                    }
                } catch (Exception ignored) {}

                // Notifica conclusão de venda para o Customer Display e UI Android nativa
                try {
                    String operadorNome = "Operador de Caixa";
                    try {
                        Funcionario f = getFuncionarioRepository().buscarPorId(finalFuncId);
                        if (f != null && f.getNome() != null && !f.getNome().isEmpty()) {
                            operadorNome = f.getNome();
                        }
                    } catch (Exception ignored) {}
                    int qtdTotalItens = 0;
                    for (ItemVenda iv : itensVenda) {
                        qtdTotalItens += iv.getQuantidade();
                    }
                    com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub.getInstance()
                            .notificarVendaConcluida(vendaId, totalCentavos, operadorNome, qtdTotalItens);
                } catch (Exception ignored) {}

                return venda;
            } finally {
                db.endTransaction();
            }
        }
    }

    @Override
    public Venda buscarVenda(long id) {
        return getVendaRepository().buscarPorId(id);
    }

    @Override
    public List<Venda> listarVendasRecentes(int limit) {
        return getVendaRepository().listarRecentes(limit);
    }

    @Override
    public boolean estornarVenda(long vendaId) {
        if (vendaId <= 0) return false;
        synchronized (DB_LOCK) {
            SQLiteDatabase db = getDbHelper().getWritableDatabase();
            db.beginTransaction();
            try {
                Venda venda = getVendaRepository().buscarPorId(vendaId);
                if (venda == null || "CANCELADA".equalsIgnoreCase(venda.getStatus())) {
                    return false;
                }

                boolean cancelou = getVendaRepository().cancelarVenda(vendaId, db);
                if (!cancelou) return false;

                List<ItemVenda> itens = getItemVendaRepository().listarPorVendaId(vendaId);
                for (ItemVenda iv : itens) {
                    getProdutoRepository().incrementarEstoque(iv.getProdutoId(), iv.getQuantidade(), db);
                }

                db.setTransactionSuccessful();

                // Notifica clientes SSE sobre a recomposição do estoque
                try {
                    for (ItemVenda iv : itens) {
                        Produto p = getProdutoRepository().buscarPorId(iv.getProdutoId());
                        if (p != null) {
                            com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub.getInstance()
                                    .notificarEstoque(p.getId(), p.getEstoque());
                        }
                    }
                } catch (Exception ignored) {}

                return true;
            } finally {
                db.endTransaction();
            }
        }
    }
}

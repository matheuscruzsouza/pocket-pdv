package com.github.matheuscruzsouza.pocketpdv.domain.service;

import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Service;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemCarrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemVenda;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemVendaComando;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;
import com.github.matheuscruzsouza.pocketpdv.persistence.DatabaseHelper;
import com.github.matheuscruzsouza.pocketpdv.persistence.ItemVendaRepository;
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

    public VendaServiceImpl() {
    }

    public VendaServiceImpl(DatabaseHelper dbHelper,
                            ProdutoRepository produtoRepository,
                            VendaRepository vendaRepository,
                            ItemVendaRepository itemVendaRepository) {
        this.dbHelper = dbHelper;
        this.produtoRepository = produtoRepository;
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
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

    @Override
    public Venda finalizarVenda(Carrinho carrinho) {
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

        Venda venda = finalizarVenda(comandos);
        carrinho.limpar();
        return venda;
    }

    @Override
    public Venda finalizarVenda(List<ItemVendaComando> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Nenhum item informado para a venda.");
        }

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

            Venda venda = new Venda(0, dataHora, totalCentavos, "CONCLUIDA");
            long vendaId = getVendaRepository().salvar(venda, db);

            for (ItemVenda iv : itensVenda) {
                iv.setVendaId(vendaId);
            }

            getItemVendaRepository().salvarTodos(itensVenda, db);

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

            return venda;
        } finally {
            db.endTransaction();
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
}

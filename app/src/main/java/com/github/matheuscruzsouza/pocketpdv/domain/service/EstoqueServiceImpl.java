package com.github.matheuscruzsouza.pocketpdv.domain.service;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Service;
import com.github.matheuscruzsouza.pocketpdv.domain.model.AlertaEstoqueDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;
import com.github.matheuscruzsouza.pocketpdv.persistence.ProdutoRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;

import java.util.Collections;
import java.util.List;

@Service
public class EstoqueServiceImpl implements EstoqueService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public EstoqueServiceImpl() {
    }

    public EstoqueServiceImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    private ProdutoRepository getProdutoRepository() {
        if (produtoRepository != null) return produtoRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            produtoRepository = (ProdutoRepository) PocketPdvService.getInstance().getServer().getBean(ProdutoRepository.class);
        }
        if (produtoRepository == null && PocketPdvService.getInstance() != null) {
            produtoRepository = new ProdutoRepository(PocketPdvService.getInstance().getDbHelper());
        }
        return produtoRepository;
    }

    @Override
    public Produto buscarPorCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.trim().isEmpty() || getProdutoRepository() == null) {
            return null;
        }
        return getProdutoRepository().buscarPorCodigoBarras(codigoBarras.trim());
    }

    @Override
    public Produto buscarPorId(long id) {
        if (getProdutoRepository() == null) return null;
        return getProdutoRepository().buscarPorId(id);
    }

    @Override
    public List<Produto> listarCatalogo() {
        if (getProdutoRepository() == null) return Collections.emptyList();
        return getProdutoRepository().listarTodos();
    }

    @Override
    public List<AlertaEstoqueDTO> verificarAlertas(int limiteMinimo) {
        if (getProdutoRepository() == null) return Collections.emptyList();
        return getProdutoRepository().listarAlertasEstoque(limiteMinimo);
    }

    @Override
    public boolean ajustarEstoque(long produtoId, int novoEstoque) {
        if (getProdutoRepository() == null) return false;
        boolean ok = getProdutoRepository().ajustarEstoque(produtoId, novoEstoque);
        if (ok) {
            com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub.getInstance()
                    .notificarEstoque(produtoId, novoEstoque);
        }
        return ok;
    }

    @Override
    public long salvarProduto(Produto produto) {
        if (getProdutoRepository() == null || produto == null) return -1;
        long id = getProdutoRepository().salvar(produto);
        if (id > 0) {
            com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub.getInstance()
                    .notificarEstoque(produto.getId(), produto.getEstoque());
        }
        return id;
    }
}

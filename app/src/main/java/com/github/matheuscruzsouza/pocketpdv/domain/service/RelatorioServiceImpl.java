package com.github.matheuscruzsouza.pocketpdv.domain.service;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Service;
import com.github.matheuscruzsouza.pocketpdv.domain.model.AlertaEstoqueDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioDiarioDTO;
import com.github.matheuscruzsouza.pocketpdv.persistence.ProdutoRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.VendaRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class RelatorioServiceImpl implements RelatorioService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public RelatorioServiceImpl() {
    }

    public RelatorioServiceImpl(VendaRepository vendaRepository, ProdutoRepository produtoRepository) {
        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
    }

    private VendaRepository getVendaRepository() {
        if (vendaRepository != null) return vendaRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            vendaRepository = (VendaRepository) PocketPdvService.getInstance().getServer().getBean(VendaRepository.class);
        }
        if (vendaRepository == null && PocketPdvService.getInstance() != null) {
            vendaRepository = new VendaRepository(PocketPdvService.getInstance().getDbHelper());
        }
        return vendaRepository;
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
    public RelatorioDiarioDTO obterResumoDoDia(Date data) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dayStr = dayFormat.format(data != null ? data : new Date());
        String dataInicio = dayStr + "T00:00:00Z";
        String dataFim = dayStr + "T23:59:59Z";

        if (getVendaRepository() == null) {
            return new RelatorioDiarioDTO(0, 0);
        }
        return getVendaRepository().obterRelatorioDiario(dataInicio, dataFim);
    }

    @Override
    public List<AlertaEstoqueDTO> verificarAlertasCriticos(int limite) {
        if (getProdutoRepository() == null) {
            return Collections.emptyList();
        }
        return getProdutoRepository().listarAlertasEstoque(limite);
    }
}

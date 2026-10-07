package com.github.matheuscruzsouza.pocketpdv.domain.service;

import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Service;
import com.github.matheuscruzsouza.pocketpdv.domain.model.CaixaTurno;
import com.github.matheuscruzsouza.pocketpdv.domain.model.MovimentacaoCaixa;
import com.github.matheuscruzsouza.pocketpdv.persistence.CaixaRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.DatabaseHelper;
import com.github.matheuscruzsouza.pocketpdv.persistence.MovimentacaoRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class CaixaServiceImpl implements CaixaService {

    @Autowired
    private CaixaRepository caixaRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private DatabaseHelper dbHelper;

    public CaixaServiceImpl() {
    }

    public CaixaServiceImpl(CaixaRepository caixaRepository, MovimentacaoRepository movimentacaoRepository, DatabaseHelper dbHelper) {
        this.caixaRepository = caixaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.dbHelper = dbHelper;
    }

    private DatabaseHelper getDbHelper() {
        if (dbHelper != null) return dbHelper;
        if (PocketPdvService.getInstance() != null) {
            dbHelper = PocketPdvService.getInstance().getDbHelper();
        }
        return dbHelper;
    }

    private CaixaRepository getCaixaRepository() {
        if (caixaRepository != null) return caixaRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            caixaRepository = (CaixaRepository) PocketPdvService.getInstance().getServer().getBean(CaixaRepository.class);
        }
        if (caixaRepository == null) {
            caixaRepository = new CaixaRepository(getDbHelper());
        }
        return caixaRepository;
    }

    private MovimentacaoRepository getMovimentacaoRepository() {
        if (movimentacaoRepository != null) return movimentacaoRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            movimentacaoRepository = (MovimentacaoRepository) PocketPdvService.getInstance().getServer().getBean(MovimentacaoRepository.class);
        }
        if (movimentacaoRepository == null) {
            movimentacaoRepository = new MovimentacaoRepository(getDbHelper());
        }
        return movimentacaoRepository;
    }

    private String getIsoDate() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
    }

    @Override
    public CaixaTurno abrirCaixa(long funcionarioId, long trocoInicialCentavos) {
        CaixaTurno aberto = getCaixaRepository().buscarAbertoPorFuncionario(funcionarioId);
        if (aberto != null) {
            throw new IllegalStateException("O usuário já possui um caixa aberto.");
        }

        CaixaTurno novo = new CaixaTurno(0, funcionarioId, getIsoDate(), null, trocoInicialCentavos, null, "ABERTO");
        long id = getCaixaRepository().salvar(novo);
        novo.setId(id);
        return novo;
    }

    @Override
    public MovimentacaoCaixa registrarMovimentacao(long caixaTurnoId, String tipo, long valorCentavos, String descricao) {
        CaixaTurno caixa = getCaixaRepository().buscarPorId(caixaTurnoId);
        if (caixa == null || !"ABERTO".equals(caixa.getStatus())) {
            throw new IllegalStateException("Caixa não encontrado ou já está fechado.");
        }

        MovimentacaoCaixa mov = new MovimentacaoCaixa(0, caixaTurnoId, tipo, valorCentavos, descricao, getIsoDate());
        long id = getMovimentacaoRepository().salvar(mov);
        mov.setId(id);
        return mov;
    }

    @Override
    public CaixaTurno fecharCaixa(long caixaTurnoId, long valorDeclaradoCentavos) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        db.beginTransaction();
        try {
            CaixaTurno caixa = getCaixaRepository().buscarPorId(caixaTurnoId);
            if (caixa == null || !"ABERTO".equals(caixa.getStatus())) {
                throw new IllegalStateException("Caixa não encontrado ou já está fechado.");
            }

            caixa.setStatus("FECHADO");
            caixa.setDataFechamento(getIsoDate());
            caixa.setValorFechamentoDeclaradoCentavos(valorDeclaradoCentavos);

            getCaixaRepository().salvar(caixa, db);
            db.setTransactionSuccessful();
            return caixa;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public CaixaTurno obterCaixaAberto(long funcionarioId) {
        return getCaixaRepository().buscarAbertoPorFuncionario(funcionarioId);
    }

    @Override
    public List<CaixaTurno> listarCaixasRecentes(int limit) {
        return getCaixaRepository().listarRecentes(limit);
    }

    @Override
    public List<MovimentacaoCaixa> listarMovimentacoes(long caixaTurnoId) {
        return getMovimentacaoRepository().listarPorCaixaId(caixaTurnoId);
    }
}

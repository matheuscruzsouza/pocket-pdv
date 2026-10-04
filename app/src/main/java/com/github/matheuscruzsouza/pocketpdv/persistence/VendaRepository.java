package com.github.matheuscruzsouza.pocketpdv.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Repository;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioDiarioDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioVendaItemDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioVendasPorFuncionarioDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;

import java.util.ArrayList;
import java.util.List;

@Repository
public class VendaRepository {

    @Autowired
    private DatabaseHelper dbHelper;

    public VendaRepository() {
    }

    public VendaRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private DatabaseHelper getDbHelper() {
        if (dbHelper != null) return dbHelper;
        if (com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService.getInstance() != null) {
            dbHelper = com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService.getInstance().getDbHelper();
        }
        return dbHelper;
    }

    private SQLiteDatabase getReadableDb() {
        return getDbHelper().getReadableDatabase();
    }

    private SQLiteDatabase getWritableDb() {
        return getDbHelper().getWritableDatabase();
    }

    public long salvar(Venda venda, SQLiteDatabase dbExterno) {
        SQLiteDatabase db = (dbExterno != null) ? dbExterno : getWritableDb();
        ContentValues values = new ContentValues();
        values.put("data_hora", venda.getDataHora());
        values.put("total_centavos", venda.getTotalCentavos());
        values.put("status", venda.getStatus());
        values.put("funcionario_id", venda.getFuncionarioId() > 0 ? venda.getFuncionarioId() : 1);

        long id = db.insert("vendas", null, values);
        venda.setId(id);
        return id;
    }

    public boolean cancelarVenda(long vendaId, SQLiteDatabase dbExterno) {
        SQLiteDatabase db = (dbExterno != null) ? dbExterno : getWritableDb();
        ContentValues values = new ContentValues();
        values.put("status", "CANCELADA");
        int rows = db.update("vendas", values, "id = ? AND status != 'CANCELADA'", new String[]{String.valueOf(vendaId)});
        return rows > 0;
    }

    public Venda buscarPorId(long id) {
        SQLiteDatabase db = getReadableDb();
        try (Cursor cursor = db.query(
                "vendas",
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToVenda(cursor);
            }
        }
        return null;
    }

    public List<Venda> listarRecentes(int limit) {
        List<Venda> vendas = new ArrayList<>();
        SQLiteDatabase db = getReadableDb();
        try (Cursor cursor = db.query(
                "vendas",
                null,
                null,
                null,
                null,
                null,
                "id DESC",
                String.valueOf(limit))) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    vendas.add(mapCursorToVenda(cursor));
                } while (cursor.moveToNext());
            }
        }
        return vendas;
    }

    public List<RelatorioVendaItemDTO> listarRelatorioVendasDetalhadas(int limit) {
        List<RelatorioVendaItemDTO> itens = new ArrayList<>();
        SQLiteDatabase db = getReadableDb();

        String sql = "SELECT v.id, v.data_hora, v.total_centavos, v.status, " +
                     "COALESCE(v.funcionario_id, 1) AS funcionario_id, " +
                     "COALESCE(f.nome, 'Operador') AS funcionario_nome, " +
                     "COALESCE(SUM(iv.quantidade), 0) AS total_itens, " +
                     "COALESCE(GROUP_CONCAT(DISTINCT pg.tipo), 'NÃO INFORMADO') AS forma_pagamento " +
                     "FROM vendas v " +
                     "LEFT JOIN funcionarios f ON f.id = v.funcionario_id " +
                     "LEFT JOIN itens_venda iv ON iv.venda_id = v.id " +
                     "LEFT JOIN pagamentos pg ON pg.venda_id = v.id " +
                     "GROUP BY v.id, v.data_hora, v.total_centavos, v.status, v.funcionario_id, f.nome " +
                     "ORDER BY v.id DESC " +
                     "LIMIT ?";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(limit)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    String dataHora = cursor.getString(cursor.getColumnIndexOrThrow("data_hora"));
                    long totalCentavos = cursor.getLong(cursor.getColumnIndexOrThrow("total_centavos"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    long funcId = cursor.getLong(cursor.getColumnIndexOrThrow("funcionario_id"));
                    String funcNome = cursor.getString(cursor.getColumnIndexOrThrow("funcionario_nome"));
                    int totalItens = cursor.getInt(cursor.getColumnIndexOrThrow("total_itens"));
                    String formaPagamento = cursor.getString(cursor.getColumnIndexOrThrow("forma_pagamento"));

                    itens.add(new RelatorioVendaItemDTO(
                            id, dataHora, funcId, funcNome, totalItens, totalCentavos, status, formaPagamento
                    ));
                } while (cursor.moveToNext());
            }
        }
        return itens;
    }

    public List<RelatorioVendasPorFuncionarioDTO> obterDesempenhoPorFuncionario() {
        List<RelatorioVendasPorFuncionarioDTO> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDb();

        String sql = "SELECT f.id AS funcionario_id, " +
                     "f.nome AS funcionario_nome, " +
                     "COUNT(DISTINCT v.id) AS total_vendas, " +
                     "COALESCE(SUM(iv.quantidade), 0) AS total_itens, " +
                     "COALESCE(SUM(v.total_centavos), 0) AS faturamento_centavos " +
                     "FROM funcionarios f " +
                     "LEFT JOIN vendas v ON v.funcionario_id = f.id AND v.status = 'CONCLUIDA' " +
                     "LEFT JOIN itens_venda iv ON iv.venda_id = v.id " +
                     "GROUP BY f.id, f.nome " +
                     "ORDER BY faturamento_centavos DESC, total_vendas DESC";

        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long funcId = cursor.getLong(cursor.getColumnIndexOrThrow("funcionario_id"));
                    String funcNome = cursor.getString(cursor.getColumnIndexOrThrow("funcionario_nome"));
                    int totalVendas = cursor.getInt(cursor.getColumnIndexOrThrow("total_vendas"));
                    int totalItens = cursor.getInt(cursor.getColumnIndexOrThrow("total_itens"));
                    long faturamento = cursor.getLong(cursor.getColumnIndexOrThrow("faturamento_centavos"));

                    lista.add(new RelatorioVendasPorFuncionarioDTO(
                            funcId, funcNome, totalVendas, totalItens, faturamento
                    ));
                } while (cursor.moveToNext());
            }
        }
        return lista;
    }

    public RelatorioDiarioDTO obterRelatorioDiario(String dataInicioIso, String dataFimIso) {
        SQLiteDatabase db = getReadableDb();
        String dayPrefix = (dataInicioIso != null && dataInicioIso.length() >= 10) 
                ? dataInicioIso.substring(0, 10) + "%" 
                : "%";
        String sql = "SELECT COUNT(*) as qtd, COALESCE(SUM(total_centavos), 0) as total " +
                     "FROM vendas " +
                     "WHERE data_hora LIKE ? AND status = 'CONCLUIDA'";

        try (Cursor cursor = db.rawQuery(sql, new String[]{dayPrefix})) {
            if (cursor != null && cursor.moveToFirst()) {
                int qtd = cursor.getInt(cursor.getColumnIndexOrThrow("qtd"));
                long total = cursor.getLong(cursor.getColumnIndexOrThrow("total"));
                return new RelatorioDiarioDTO(qtd, total);
            }
        }
        return new RelatorioDiarioDTO(0, 0);
    }

    private Venda mapCursorToVenda(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        String dataHora = cursor.getString(cursor.getColumnIndexOrThrow("data_hora"));
        long totalCentavos = cursor.getLong(cursor.getColumnIndexOrThrow("total_centavos"));
        String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

        long funcId = 1;
        int funcColIdx = cursor.getColumnIndex("funcionario_id");
        if (funcColIdx >= 0 && !cursor.isNull(funcColIdx)) {
            funcId = cursor.getLong(funcColIdx);
        }

        return new Venda(id, dataHora, totalCentavos, status, funcId);
    }
}

package com.github.matheuscruzsouza.pocketpdv.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Repository;
import com.github.matheuscruzsouza.pocketpdv.domain.model.MovimentacaoCaixa;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MovimentacaoRepository {

    private final DatabaseHelper dbHelper;

    public MovimentacaoRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long salvar(MovimentacaoCaixa mov, SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("caixa_turno_id", mov.getCaixaTurnoId());
        cv.put("tipo", mov.getTipo());
        cv.put("valor_centavos", mov.getValorCentavos());
        cv.put("descricao", mov.getDescricao());
        cv.put("data_hora", mov.getDataHora());

        if (mov.getId() > 0) {
            db.update("movimentacao_caixa", cv, "id = ?", new String[]{String.valueOf(mov.getId())});
            return mov.getId();
        } else {
            return db.insert("movimentacao_caixa", null, cv);
        }
    }

    public long salvar(MovimentacaoCaixa mov) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return salvar(mov, db);
    }

    public List<MovimentacaoCaixa> listarPorCaixaId(long caixaTurnoId) {
        List<MovimentacaoCaixa> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("movimentacao_caixa", null, "caixa_turno_id = ?",
                new String[]{String.valueOf(caixaTurnoId)}, null, null, "data_hora ASC")) {
            if (c != null && c.moveToFirst()) {
                do {
                    lista.add(mapCursorToMovimentacao(c));
                } while (c.moveToNext());
            }
        }
        return lista;
    }

    private MovimentacaoCaixa mapCursorToMovimentacao(Cursor c) {
        MovimentacaoCaixa mov = new MovimentacaoCaixa();
        mov.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        mov.setCaixaTurnoId(c.getLong(c.getColumnIndexOrThrow("caixa_turno_id")));
        mov.setTipo(c.getString(c.getColumnIndexOrThrow("tipo")));
        mov.setValorCentavos(c.getLong(c.getColumnIndexOrThrow("valor_centavos")));
        mov.setDescricao(c.getString(c.getColumnIndexOrThrow("descricao")));
        mov.setDataHora(c.getString(c.getColumnIndexOrThrow("data_hora")));
        return mov;
    }
}

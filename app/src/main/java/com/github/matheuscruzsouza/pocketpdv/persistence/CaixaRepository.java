package com.github.matheuscruzsouza.pocketpdv.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Repository;
import com.github.matheuscruzsouza.pocketpdv.domain.model.CaixaTurno;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CaixaRepository {

    private final DatabaseHelper dbHelper;

    public CaixaRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long salvar(CaixaTurno caixa, SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("funcionario_id", caixa.getFuncionarioId());
        cv.put("data_abertura", caixa.getDataAbertura());
        cv.put("data_fechamento", caixa.getDataFechamento());
        cv.put("valor_abertura_centavos", caixa.getValorAberturaCentavos());
        if (caixa.getValorFechamentoDeclaradoCentavos() != null) {
            cv.put("valor_fechamento_declarado_centavos", caixa.getValorFechamentoDeclaradoCentavos());
        } else {
            cv.putNull("valor_fechamento_declarado_centavos");
        }
        cv.put("status", caixa.getStatus());

        if (caixa.getId() > 0) {
            db.update("caixa_turno", cv, "id = ?", new String[]{String.valueOf(caixa.getId())});
            return caixa.getId();
        } else {
            return db.insert("caixa_turno", null, cv);
        }
    }

    public long salvar(CaixaTurno caixa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return salvar(caixa, db);
    }

    public CaixaTurno buscarAbertoPorFuncionario(long funcionarioId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("caixa_turno", null, "funcionario_id = ? AND status = 'ABERTO'",
                new String[]{String.valueOf(funcionarioId)}, null, null, "id DESC", "1")) {
            if (c != null && c.moveToFirst()) {
                return mapCursorToCaixaTurno(c);
            }
        }
        return null;
    }

    public CaixaTurno buscarPorId(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("caixa_turno", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                return mapCursorToCaixaTurno(c);
            }
        }
        return null;
    }

    public List<CaixaTurno> listarRecentes(int limit) {
        List<CaixaTurno> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("caixa_turno", null, null, null, null, null, "id DESC", String.valueOf(limit))) {
            if (c != null && c.moveToFirst()) {
                do {
                    lista.add(mapCursorToCaixaTurno(c));
                } while (c.moveToNext());
            }
        }
        return lista;
    }

    private CaixaTurno mapCursorToCaixaTurno(Cursor c) {
        CaixaTurno ct = new CaixaTurno();
        ct.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        ct.setFuncionarioId(c.getLong(c.getColumnIndexOrThrow("funcionario_id")));
        ct.setDataAbertura(c.getString(c.getColumnIndexOrThrow("data_abertura")));
        ct.setDataFechamento(c.getString(c.getColumnIndexOrThrow("data_fechamento")));
        ct.setValorAberturaCentavos(c.getLong(c.getColumnIndexOrThrow("valor_abertura_centavos")));
        
        int idx = c.getColumnIndex("valor_fechamento_declarado_centavos");
        if (idx != -1 && !c.isNull(idx)) {
            ct.setValorFechamentoDeclaradoCentavos(c.getLong(idx));
        }
        
        ct.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
        return ct;
    }
}

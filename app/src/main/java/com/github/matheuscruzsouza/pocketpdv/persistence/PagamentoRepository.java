package com.github.matheuscruzsouza.pocketpdv.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Repository;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PagamentoRepository {

    @Autowired
    private DatabaseHelper dbHelper;

    public PagamentoRepository() {
    }

    public PagamentoRepository(DatabaseHelper dbHelper) {
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

    public long salvar(Pagamento pagamento, SQLiteDatabase dbExterno) {
        SQLiteDatabase db = (dbExterno != null) ? dbExterno : getWritableDb();
        ContentValues values = new ContentValues();
        values.put("venda_id", pagamento.getVendaId());
        values.put("tipo", pagamento.getTipo());
        values.put("valor_centavos", pagamento.getValorCentavos());
        values.put("valor_recebido_centavos", pagamento.getValorRecebidoCentavos());
        values.put("troco_centavos", pagamento.getTrocoCentavos());

        long id = db.insert("pagamentos", null, values);
        pagamento.setId(id);
        return id;
    }

    public void salvarTodos(List<Pagamento> pagamentos, SQLiteDatabase dbExterno) {
        if (pagamentos == null || pagamentos.isEmpty()) return;
        SQLiteDatabase db = (dbExterno != null) ? dbExterno : getWritableDb();
        for (Pagamento p : pagamentos) {
            salvar(p, db);
        }
    }

    public List<Pagamento> listarPorVendaId(long vendaId) {
        List<Pagamento> pagamentos = new ArrayList<>();
        SQLiteDatabase db = getReadableDb();
        try (Cursor cursor = db.query(
                "pagamentos",
                null,
                "venda_id = ?",
                new String[]{String.valueOf(vendaId)},
                null, null, "id ASC")) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    String tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));
                    long valorCentavos = cursor.getLong(cursor.getColumnIndexOrThrow("valor_centavos"));
                    long valorRecebidoCentavos = cursor.getLong(cursor.getColumnIndexOrThrow("valor_recebido_centavos"));
                    long trocoCentavos = cursor.getLong(cursor.getColumnIndexOrThrow("troco_centavos"));

                    pagamentos.add(new Pagamento(id, vendaId, tipo, valorCentavos, valorRecebidoCentavos, trocoCentavos));
                } while (cursor.moveToNext());
            }
        }
        return pagamentos;
    }
}

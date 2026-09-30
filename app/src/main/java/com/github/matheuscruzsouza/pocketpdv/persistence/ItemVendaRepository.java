package com.github.matheuscruzsouza.pocketpdv.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Repository;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemVenda;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ItemVendaRepository {

    @Autowired
    private DatabaseHelper dbHelper;

    public ItemVendaRepository() {
    }

    public ItemVendaRepository(DatabaseHelper dbHelper) {
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

    public void salvarTodos(List<ItemVenda> itens, SQLiteDatabase dbExterno) {
        SQLiteDatabase db = (dbExterno != null) ? dbExterno : getWritableDb();
        for (ItemVenda item : itens) {
            ContentValues values = new ContentValues();
            values.put("venda_id", item.getVendaId());
            values.put("produto_id", item.getProdutoId());
            values.put("quantidade", item.getQuantidade());
            values.put("preco_unit_centavos", item.getPrecoUnitCentavos());

            long id = db.insert("itens_venda", null, values);
            item.setId(id);
        }
    }

    public List<ItemVenda> listarPorVendaId(long vendaId) {
        List<ItemVenda> itens = new ArrayList<>();
        SQLiteDatabase db = getReadableDb();
        try (Cursor cursor = db.query(
                "itens_venda",
                null,
                "venda_id = ?",
                new String[]{String.valueOf(vendaId)},
                null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    long produtoId = cursor.getLong(cursor.getColumnIndexOrThrow("produto_id"));
                    int quantidade = cursor.getInt(cursor.getColumnIndexOrThrow("quantidade"));
                    int precoUnit = cursor.getInt(cursor.getColumnIndexOrThrow("preco_unit_centavos"));
                    itens.add(new ItemVenda(id, vendaId, produtoId, quantidade, precoUnit));
                } while (cursor.moveToNext());
            }
        }
        return itens;
    }
}

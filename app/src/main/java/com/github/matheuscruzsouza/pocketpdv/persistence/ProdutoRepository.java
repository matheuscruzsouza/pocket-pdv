package com.github.matheuscruzsouza.pocketpdv.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Repository;
import com.github.matheuscruzsouza.pocketpdv.domain.model.AlertaEstoqueDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProdutoRepository {

    @Autowired
    private DatabaseHelper dbHelper;

    public ProdutoRepository() {
    }

    public ProdutoRepository(DatabaseHelper dbHelper) {
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

    public Produto buscarPorCodigoBarras(String codigoBarras) {
        SQLiteDatabase db = getReadableDb();
        try (Cursor cursor = db.query(
                "produtos",
                null,
                "codigo_barras = ?",
                new String[]{codigoBarras},
                null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToProduto(cursor);
            }
        }
        return null;
    }

    public Produto buscarPorId(long id) {
        SQLiteDatabase db = getReadableDb();
        try (Cursor cursor = db.query(
                "produtos",
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToProduto(cursor);
            }
        }
        return null;
    }

    public List<Produto> listarTodos() {
        List<Produto> produtos = new ArrayList<>();
        SQLiteDatabase db = getReadableDb();
        try (Cursor cursor = db.query("produtos", null, null, null, null, null, "nome ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    produtos.add(mapCursorToProduto(cursor));
                } while (cursor.moveToNext());
            }
        }
        return produtos;
    }

    public List<AlertaEstoqueDTO> listarAlertasEstoque(int limite) {
        List<AlertaEstoqueDTO> alertas = new ArrayList<>();
        SQLiteDatabase db = getReadableDb();
        String sql = "SELECT id, nome, estoque FROM produtos WHERE estoque <= ? ORDER BY estoque ASC";
        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(limite)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                    int estoque = cursor.getInt(cursor.getColumnIndexOrThrow("estoque"));
                    alertas.add(new AlertaEstoqueDTO(id, nome, estoque, limite));
                } while (cursor.moveToNext());
            }
        }
        return alertas;
    }

    public boolean decrementarEstoque(long produtoId, int quantidade, SQLiteDatabase dbExterno) {
        SQLiteDatabase db = (dbExterno != null) ? dbExterno : getWritableDb();
        // Update condicional atômico: estoque >= quantidade
        String sql = "UPDATE produtos SET estoque = estoque - ? WHERE id = ? AND estoque >= ?";
        android.database.sqlite.SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, quantidade);
        stmt.bindLong(2, produtoId);
        stmt.bindLong(3, quantidade);
        int rowsAffected = stmt.executeUpdateDelete();
        return rowsAffected > 0;
    }

    public boolean ajustarEstoque(long produtoId, int novoEstoque) {
        if (novoEstoque < 0) novoEstoque = 0;
        SQLiteDatabase db = getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put("estoque", novoEstoque);
        int rows = db.update("produtos", cv, "id = ?", new String[]{String.valueOf(produtoId)});
        return rows > 0;
    }

    public long salvar(Produto produto) {
        SQLiteDatabase db = getWritableDb();
        ContentValues values = new ContentValues();
        values.put("codigo_barras", produto.getCodigoBarras());
        values.put("nome", produto.getNome());
        values.put("preco_centavos", produto.getPrecoCentavos());
        values.put("estoque", produto.getEstoque());

        if (produto.getId() > 0) {
            db.update("produtos", values, "id = ?", new String[]{String.valueOf(produto.getId())});
            return produto.getId();
        } else {
            long id = db.insert("produtos", null, values);
            produto.setId(id);
            return id;
        }
    }

    private Produto mapCursorToProduto(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        String codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo_barras"));
        String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
        int preco = cursor.getInt(cursor.getColumnIndexOrThrow("preco_centavos"));
        int estoque = cursor.getInt(cursor.getColumnIndexOrThrow("estoque"));
        return new Produto(id, codigo, nome, preco, estoque);
    }
}

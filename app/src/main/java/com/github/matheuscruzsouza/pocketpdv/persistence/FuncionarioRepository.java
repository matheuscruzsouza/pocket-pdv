package com.github.matheuscruzsouza.pocketpdv.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Repository;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FuncionarioRepository {

    @Autowired
    private DatabaseHelper dbHelper;

    public FuncionarioRepository() {
    }

    public FuncionarioRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private DatabaseHelper getDbHelper() {
        if (dbHelper != null) return dbHelper;
        if (PocketPdvService.getInstance() != null) {
            dbHelper = PocketPdvService.getInstance().getDbHelper();
        }
        return dbHelper;
    }

    private SQLiteDatabase getReadableDb() {
        return getDbHelper().getReadableDatabase();
    }

    private SQLiteDatabase getWritableDb() {
        return getDbHelper().getWritableDatabase();
    }

    public List<Funcionario> listarTodos() {
        List<Funcionario> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDb();
        try (Cursor c = db.query("funcionarios", null, null, null, null, null, "nome ASC")) {
            if (c != null && c.moveToFirst()) {
                do {
                    lista.add(mapCursorToFuncionario(c));
                } while (c.moveToNext());
            }
        }
        return lista;
    }

    public Funcionario buscarPorId(long id) {
        SQLiteDatabase db = getReadableDb();
        try (Cursor c = db.query("funcionarios", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                return mapCursorToFuncionario(c);
            }
        }
        return null;
    }

    public Funcionario buscarPorUsuario(String usuario) {
        if (usuario == null) return null;
        SQLiteDatabase db = getReadableDb();
        try (Cursor c = db.query("funcionarios", null, "usuario = ?", new String[]{usuario.trim()}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                return mapCursorToFuncionario(c);
            }
        }
        return null;
    }

    public boolean definirSenhaComCodigo(String usuario, String codigo, String novaSenha) {
        if (usuario == null || codigo == null || novaSenha == null) return false;
        Funcionario f = buscarPorUsuario(usuario);
        if (f == null || !f.isAtivo()) return false;

        // Validação case-insensitive do código de confirmação
        if (f.getCodigoConfirmacao() == null || !f.getCodigoConfirmacao().trim().equalsIgnoreCase(codigo.trim())) {
            return false;
        }

        SQLiteDatabase db = getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put("senha", novaSenha.trim());
        cv.put("senha_definida", 1);
        cv.putNull("codigo_confirmacao");

        int rows = db.update("funcionarios", cv, "id = ?", new String[]{String.valueOf(f.getId())});
        return rows > 0;
    }

    public long salvar(Funcionario f) {
        SQLiteDatabase db = getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put("nome", f.getNome());
        cv.put("cargo", f.getCargo());
        cv.put("usuario", f.getUsuario());
        cv.put("ativo", f.isAtivo() ? 1 : 0);
        cv.put("codigo_confirmacao", f.getCodigoConfirmacao());
        cv.put("senha", f.getSenha());
        cv.put("senha_definida", f.isSenhaDefinida() ? 1 : 0);

        if (f.getId() > 0) {
            db.update("funcionarios", cv, "id = ?", new String[]{String.valueOf(f.getId())});
            return f.getId();
        } else {
            long id = db.insertWithOnConflict("funcionarios", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            f.setId(id);
            return id;
        }
    }

    private Funcionario mapCursorToFuncionario(Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow("id"));
        String nome = c.getString(c.getColumnIndexOrThrow("nome"));
        String cargo = c.getString(c.getColumnIndexOrThrow("cargo"));
        String usuario = c.getString(c.getColumnIndexOrThrow("usuario"));
        boolean ativo = c.getInt(c.getColumnIndexOrThrow("ativo")) == 1;

        int idxCodigo = c.getColumnIndex("codigo_confirmacao");
        String codigo = (idxCodigo >= 0 && !c.isNull(idxCodigo)) ? c.getString(idxCodigo) : null;

        int idxSenha = c.getColumnIndex("senha");
        String senha = (idxSenha >= 0 && !c.isNull(idxSenha)) ? c.getString(idxSenha) : null;

        int idxSenhaDef = c.getColumnIndex("senha_definida");
        boolean senhaDefinida = (idxSenhaDef >= 0) && (c.getInt(idxSenhaDef) == 1);

        return new Funcionario(id, nome, cargo, usuario, ativo, codigo, senha, senhaDefinida);
    }
}

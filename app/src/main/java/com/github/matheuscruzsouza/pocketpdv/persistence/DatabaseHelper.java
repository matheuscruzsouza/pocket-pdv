package com.github.matheuscruzsouza.pocketpdv.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pocketpdv.db";
    public static final int DATABASE_VERSION = 3;

    // DDL Statements
    public static final String SQL_CREATE_PRODUTOS =
            "CREATE TABLE IF NOT EXISTS produtos (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  codigo_barras TEXT NOT NULL UNIQUE, " +
            "  nome TEXT NOT NULL, " +
            "  preco_centavos INTEGER NOT NULL CHECK (preco_centavos >= 0), " +
            "  estoque INTEGER NOT NULL DEFAULT 0 CHECK (estoque >= 0)" +
            ");";

    public static final String SQL_CREATE_IDX_PRODUTOS_CODIGO =
            "CREATE INDEX IF NOT EXISTS idx_produtos_codigo ON produtos(codigo_barras);";

    public static final String SQL_CREATE_VENDAS =
            "CREATE TABLE IF NOT EXISTS vendas (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  data_hora TEXT NOT NULL, " +
            "  total_centavos INTEGER NOT NULL CHECK (total_centavos >= 0), " +
            "  status TEXT NOT NULL, " +
            "  funcionario_id INTEGER DEFAULT 1 REFERENCES funcionarios(id)" +
            ");";

    public static final String SQL_CREATE_IDX_VENDAS_DATA =
            "CREATE INDEX IF NOT EXISTS idx_vendas_data ON vendas(data_hora);";

    public static final String SQL_CREATE_IDX_VENDAS_FUNC =
            "CREATE INDEX IF NOT EXISTS idx_vendas_funcionario_id ON vendas(funcionario_id);";

    public static final String SQL_CREATE_ITENS_VENDA =
            "CREATE TABLE IF NOT EXISTS itens_venda (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  venda_id INTEGER NOT NULL REFERENCES vendas(id), " +
            "  produto_id INTEGER NOT NULL REFERENCES produtos(id), " +
            "  quantidade INTEGER NOT NULL CHECK (quantidade > 0), " +
            "  preco_unit_centavos INTEGER NOT NULL CHECK (preco_unit_centavos >= 0)" +
            ");";

    public static final String SQL_CREATE_IDX_ITENS_VENDA_VENDA_ID =
            "CREATE INDEX IF NOT EXISTS idx_itens_venda_venda_id ON itens_venda(venda_id);";

    public static final String SQL_CREATE_IDX_ITENS_VENDA_PRODUTO_ID =
            "CREATE INDEX IF NOT EXISTS idx_itens_venda_produto_id ON itens_venda(produto_id);";

    public static final String SQL_CREATE_PAGAMENTOS =
            "CREATE TABLE IF NOT EXISTS pagamentos (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  venda_id INTEGER NOT NULL REFERENCES vendas(id), " +
            "  tipo TEXT NOT NULL, " +
            "  valor_centavos INTEGER NOT NULL CHECK (valor_centavos >= 0), " +
            "  valor_recebido_centavos INTEGER NOT NULL CHECK (valor_recebido_centavos >= 0), " +
            "  troco_centavos INTEGER NOT NULL DEFAULT 0 CHECK (troco_centavos >= 0)" +
            ");";

    public static final String SQL_CREATE_IDX_PAGAMENTOS_VENDA_ID =
            "CREATE INDEX IF NOT EXISTS idx_pagamentos_venda_id ON pagamentos(venda_id);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SDD Section 4.1: SQLite PRAGMA Protocol
        db.enableWriteAheadLogging();
        db.setForeignKeyConstraintsEnabled(true);
        executePragma(db, "PRAGMA busy_timeout = 5000;");
        executePragma(db, "PRAGMA journal_mode = WAL;");
        executePragma(db, "PRAGMA synchronous = NORMAL;");
        executePragma(db, "PRAGMA foreign_keys = ON;");
        executePragma(db, "PRAGMA temp_store = MEMORY;");
        executePragma(db, "PRAGMA cache_size = -2000;");
    }

    private void executePragma(SQLiteDatabase db, String pragma) {
        try (Cursor c = db.rawQuery(pragma, null)) {
            if (c != null) {
                c.moveToFirst();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        garantirUsuariosPadrao(db);
    }

    public static final String SQL_CREATE_FUNCIONARIOS =
            "CREATE TABLE IF NOT EXISTS funcionarios (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  nome TEXT NOT NULL, " +
            "  cargo TEXT NOT NULL, " +
            "  usuario TEXT NOT NULL UNIQUE, " +
            "  ativo INTEGER NOT NULL DEFAULT 1, " +
            "  codigo_confirmacao TEXT, " +
            "  senha TEXT, " +
            "  senha_definida INTEGER NOT NULL DEFAULT 0" +
            ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUTOS);
        db.execSQL(SQL_CREATE_IDX_PRODUTOS_CODIGO);
        db.execSQL(SQL_CREATE_VENDAS);
        db.execSQL(SQL_CREATE_IDX_VENDAS_DATA);
        db.execSQL(SQL_CREATE_IDX_VENDAS_FUNC);
        db.execSQL(SQL_CREATE_ITENS_VENDA);
        db.execSQL(SQL_CREATE_IDX_ITENS_VENDA_VENDA_ID);
        db.execSQL(SQL_CREATE_IDX_ITENS_VENDA_PRODUTO_ID);
        db.execSQL(SQL_CREATE_FUNCIONARIOS);
        db.execSQL(SQL_CREATE_PAGAMENTOS);
        db.execSQL(SQL_CREATE_IDX_PAGAMENTOS_VENDA_ID);

        seedInitialData(db);
        garantirUsuariosPadrao(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int v = oldVersion + 1; v <= newVersion; v++) {
            aplicarMigracao(db, v);
        }
    }

    private void aplicarMigracao(SQLiteDatabase db, int versao) {
        if (versao == 2) {
            migrarParaVersao2(db);
        } else if (versao == 3) {
            migrarParaVersao3(db);
        }
    }

    private void migrarParaVersao3(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PAGAMENTOS);
        db.execSQL(SQL_CREATE_IDX_PAGAMENTOS_VENDA_ID);
    }

    private void migrarParaVersao2(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FUNCIONARIOS);

        // Migração estrutural incremental para a tabela funcionarios
        if (!colunaExiste(db, "funcionarios", "codigo_confirmacao")) {
            db.execSQL("ALTER TABLE funcionarios ADD COLUMN codigo_confirmacao TEXT;");
        }
        if (!colunaExiste(db, "funcionarios", "senha")) {
            db.execSQL("ALTER TABLE funcionarios ADD COLUMN senha TEXT;");
        }
        if (!colunaExiste(db, "funcionarios", "senha_definida")) {
            db.execSQL("ALTER TABLE funcionarios ADD COLUMN senha_definida INTEGER NOT NULL DEFAULT 0;");
        }

        // Migração estrutural incremental para a tabela vendas
        if (!colunaExiste(db, "vendas", "funcionario_id")) {
            db.execSQL("ALTER TABLE vendas ADD COLUMN funcionario_id INTEGER DEFAULT 1;");
            db.execSQL("UPDATE vendas SET funcionario_id = 1 WHERE funcionario_id IS NULL;");
        }
        db.execSQL(SQL_CREATE_IDX_VENDAS_FUNC);

        garantirUsuariosPadrao(db);
    }

    private boolean colunaExiste(SQLiteDatabase db, String tabela, String nomeColuna) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tabela + ");", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex("name");
                do {
                    if (nameIndex != -1 && nomeColuna.equalsIgnoreCase(cursor.getString(nameIndex))) {
                        return true;
                    }
                } while (cursor.moveToNext());
            }
        }
        return false;
    }

    private void garantirUsuariosPadrao(SQLiteDatabase db) {
        String hashOperador = com.github.matheuscruzsouza.pocketpdv.security.PasswordHasher.hashPassword("1234");
        String hashAdmin = com.github.matheuscruzsouza.pocketpdv.security.PasswordHasher.hashPassword("admin");

        android.content.ContentValues cvOp = new android.content.ContentValues();
        cvOp.put("nome", "Carlos Silva");
        cvOp.put("cargo", "Operador de Caixa");
        cvOp.put("usuario", "operador");
        cvOp.put("ativo", 1);
        cvOp.put("senha", hashOperador);
        cvOp.put("senha_definida", 1);
        db.insertWithOnConflict("funcionarios", null, cvOp, SQLiteDatabase.CONFLICT_IGNORE);

        android.content.ContentValues cvAdmin = new android.content.ContentValues();
        cvAdmin.put("nome", "Mariana Costa");
        cvAdmin.put("cargo", "Gerente de Loja");
        cvAdmin.put("usuario", "admin");
        cvAdmin.put("ativo", 1);
        cvAdmin.put("senha", hashAdmin);
        cvAdmin.put("senha_definida", 1);
        db.insertWithOnConflict("funcionarios", null, cvAdmin, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void seedInitialData(SQLiteDatabase db) {
        db.execSQL("INSERT OR IGNORE INTO produtos (codigo_barras, nome, preco_centavos, estoque) " +
                "VALUES ('7891000100101', 'Cafe Torrado 500g', 1450, 45);");
        db.execSQL("INSERT OR IGNORE INTO produtos (codigo_barras, nome, preco_centavos, estoque) " +
                "VALUES ('7891000200202', 'Acucar Refinado 1kg', 480, 80);");
        db.execSQL("INSERT OR IGNORE INTO produtos (codigo_barras, nome, preco_centavos, estoque) " +
                "VALUES ('7891000300303', 'Leite Integral 1L', 520, 8);");
        db.execSQL("INSERT OR IGNORE INTO produtos (codigo_barras, nome, preco_centavos, estoque) " +
                "VALUES ('7891000400404', 'Biscoito Recheado 140g', 350, 3);");
    }
}

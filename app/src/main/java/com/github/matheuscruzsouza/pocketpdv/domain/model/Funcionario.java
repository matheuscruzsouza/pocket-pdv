package com.github.matheuscruzsouza.pocketpdv.domain.model;

import com.github.matheuscruzsouza.nanospring.validation.NotBlank;
import com.github.matheuscruzsouza.nanospring.validation.Size;

public class Funcionario {

    private long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    private String cargo;

    @NotBlank(message = "O usuário é obrigatório")
    @Size(min = 3, max = 30, message = "O usuário deve ter entre 3 e 30 caracteres")
    private String usuario;

    private boolean ativo;
    private String codigoConfirmacao;
    private String senha;
    private boolean senhaDefinida;

    public Funcionario() {
    }

    public Funcionario(long id, String nome, String cargo, String usuario, boolean ativo) {
        this(id, nome, cargo, usuario, ativo, null, null, false);
    }

    public Funcionario(long id, String nome, String cargo, String usuario, boolean ativo,
                       String codigoConfirmacao, String senha, boolean senhaDefinida) {
        this.id = id;
        this.nome = nome;
        this.cargo = cargo;
        this.usuario = usuario;
        this.ativo = ativo;
        this.codigoConfirmacao = codigoConfirmacao;
        this.senha = senha;
        this.senhaDefinida = senhaDefinida;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getCodigoConfirmacao() { return codigoConfirmacao; }
    public void setCodigoConfirmacao(String codigoConfirmacao) { this.codigoConfirmacao = codigoConfirmacao; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isSenhaDefinida() { return senhaDefinida; }
    public void setSenhaDefinida(boolean senhaDefinida) { this.senhaDefinida = senhaDefinida; }
}

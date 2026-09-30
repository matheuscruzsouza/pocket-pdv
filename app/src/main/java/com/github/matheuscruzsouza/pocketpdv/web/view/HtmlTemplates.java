package com.github.matheuscruzsouza.pocketpdv.web.view;

import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemCarrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioDiarioDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;

import java.util.List;
import java.util.Locale;

public class HtmlTemplates {

    public static String formatarDinheiro(int centavos) {
        return String.format(Locale.GERMANY, "R$ %.2f", centavos / 100.0);
    }

    public static String paginaLogin(String erro) {
        return paginaLogin(erro, null);
    }

    public static String paginaLogin(String erro, String sucesso) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"pt-BR\">\n<head>\n")
          .append("  <meta charset=\"UTF-8\">\n")
          .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("  <title>PocketPDV - Login</title>\n")
          .append("  <link rel=\"stylesheet\" href=\"/assets/style.css\">\n")
          .append("</head>\n<body>\n")
          .append("  <div class=\"login-box\">\n")
          .append("    <div class=\"login-header\">\n")
          .append("      <h1 class=\"brand\">PocketPDV</h1>\n")
          .append("      <p style=\"color: #6b7280; font-size: 0.875rem;\">Acesso ao Ponto de Venda</p>\n")
          .append("    </div>\n");

        if (erro != null && !erro.trim().isEmpty()) {
            sb.append("    <div style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem; font-size: 0.875rem;\">")
              .append(erro)
              .append("</div>\n");
        }
        if (sucesso != null && !sucesso.trim().isEmpty()) {
            sb.append("    <div style=\"background: #d1fae5; color: #065f46; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem; font-size: 0.875rem;\">")
              .append(sucesso)
              .append("</div>\n");
        }

        sb.append("    <form action=\"/login\" method=\"POST\">\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label class=\"form-label\" for=\"usuario\">Usuário</label>\n")
          .append("        <input class=\"form-input\" type=\"text\" id=\"usuario\" name=\"usuario\" required autofocus value=\"operador\">\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label class=\"form-label\" for=\"senha\">Senha</label>\n")
          .append("        <input class=\"form-input\" type=\"password\" id=\"senha\" name=\"senha\" required value=\"1234\">\n")
          .append("      </div>\n")
          .append("      <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; margin-top: 0.5rem;\">Entrar no PDV</button>\n")
          .append("    </form>\n")
          .append("    <div style=\"text-align: center; margin-top: 1.25rem; font-size: 0.875rem;\">\n")
          .append("      <a href=\"/primeiro-acesso\" style=\"color: #059669; font-weight: 600; text-decoration: none;\">🔑 Primeiro acesso? Defina sua senha com o código</a>\n")
          .append("    </div>\n")
          .append("  </div>\n")
          .append("</body>\n</html>");

        return sb.toString();
    }

    public static String paginaPrimeiroAcesso(String erro, String sucesso) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"pt-BR\">\n<head>\n")
          .append("  <meta charset=\"UTF-8\">\n")
          .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("  <title>PocketPDV - Primeiro Acesso</title>\n")
          .append("  <link rel=\"stylesheet\" href=\"/assets/style.css\">\n")
          .append("</head>\n<body>\n")
          .append("  <div class=\"login-box\" style=\"max-width: 420px;\">\n")
          .append("    <div class=\"login-header\">\n")
          .append("      <h1 class=\"brand\">PocketPDV</h1>\n")
          .append("      <p style=\"color: #6b7280; font-size: 0.875rem;\">Definição de Senha no Primeiro Login</p>\n")
          .append("    </div>\n");

        if (erro != null && !erro.trim().isEmpty()) {
            sb.append("    <div style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem; font-size: 0.875rem;\">")
              .append(erro)
              .append("</div>\n");
        }
        if (sucesso != null && !sucesso.trim().isEmpty()) {
            sb.append("    <div style=\"background: #d1fae5; color: #065f46; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem; font-size: 0.875rem;\">")
              .append(sucesso)
              .append("</div>\n");
        }

        sb.append("    <form action=\"/primeiro-acesso\" method=\"POST\">\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label class=\"form-label\" for=\"usuario\">Usuário</label>\n")
          .append("        <input class=\"form-input\" type=\"text\" id=\"usuario\" name=\"usuario\" required autofocus placeholder=\"Ex: mcosta\">\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label class=\"form-label\" for=\"codigo\">Código de Confirmação (6 dígitos)</label>\n")
          .append("        <input class=\"form-input font-mono\" type=\"text\" id=\"codigo\" name=\"codigo\" maxlength=\"6\" required placeholder=\"Ex: K9P2X4\" style=\"text-transform: uppercase; letter-spacing: 2px; font-weight: bold;\">\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label class=\"form-label\" for=\"senha\">Nova Senha</label>\n")
          .append("        <input class=\"form-input\" type=\"password\" id=\"senha\" name=\"senha\" required placeholder=\"Digite sua nova senha\">\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label class=\"form-label\" for=\"confirmaSenha\">Confirmar Nova Senha</label>\n")
          .append("        <input class=\"form-input\" type=\"password\" id=\"confirmaSenha\" name=\"confirmaSenha\" required placeholder=\"Repita a nova senha\">\n")
          .append("      </div>\n")
          .append("      <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; margin-top: 0.5rem;\">Criar Minha Senha</button>\n")
          .append("    </form>\n")
          .append("    <div style=\"text-align: center; margin-top: 1.25rem; font-size: 0.875rem;\">\n")
          .append("      <a href=\"/login\" style=\"color: #6b7280; text-decoration: none;\">&larr; Voltar para a tela de login</a>\n")
          .append("    </div>\n")
          .append("  </div>\n")
          .append("</body>\n</html>");

        return sb.toString();
    }

    public static String paginaPdv(List<Produto> catalogo, Carrinho carrinho, String msgSucesso, String msgErro) {
        StringBuilder sb = new StringBuilder();
        int totalCentavos = (carrinho != null) ? carrinho.getTotalCentavos() : 0;
        boolean carrinhoVazio = (carrinho == null || carrinho.getItens().isEmpty());

        sb.append("<!DOCTYPE html>\n<html lang=\"pt-BR\">\n<head>\n")
          .append("  <meta charset=\"UTF-8\">\n")
          .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("  <title>PocketPDV - Terminal</title>\n")
          .append("  <link rel=\"stylesheet\" href=\"/assets/style.css\">\n")
          .append("  <script src=\"/assets/htmx.min.js\"></script>\n")
          .append("</head>\n<body>\n")
          .append("  <div class=\"container\">\n")
          .append("    <header class=\"header\">\n")
          .append("      <div>\n")
          .append("        <span class=\"brand\">PocketPDV</span>\n")
          .append("        <span class=\"badge\" style=\"margin-left: 0.5rem;\">Terminal Ativo</span>\n")
          .append("      </div>\n")
          .append("      <div style=\"display: flex; gap: 0.75rem; align-items: center;\">\n")
          .append("        <button class=\"btn btn-secondary\" hx-get=\"/pdv/estoque\" hx-target=\"#modal-container\" style=\"font-size: 0.85rem; padding: 0.4rem 0.8rem;\">📦 Gestão de Estoque</button>\n")
          .append("        <button class=\"btn btn-secondary\" hx-get=\"/pdv/relatorio\" hx-target=\"#modal-container\" style=\"font-size: 0.85rem; padding: 0.4rem 0.8rem;\">📊 Vendas de Hoje</button>\n")
          .append("        <a href=\"/login\" style=\"color: #6b7280; font-size: 0.875rem; text-decoration: none;\">Encerrar Sessão</a>\n")
          .append("      </div>\n")
          .append("    </header>\n");

        // Barra de Leitura Rápida de Código de Barras (com autofocus)
        sb.append("    <div class=\"barcode-bar\">\n")
          .append("      <form hx-post=\"/pdv/carrinho/codigo\" hx-target=\"#cart-table-body\" style=\"display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;\">\n")
          .append("        <span style=\"font-weight: 700; color: #374151; white-space: nowrap;\">⚡ Código de Barras:</span>\n")
          .append("        <input type=\"text\" name=\"codigo\" id=\"input-codigo-barras\" class=\"form-input\" style=\"flex: 1; min-width: 220px; font-family: monospace; font-size: 1.1rem; font-weight: bold;\" placeholder=\"Bipe ou digite o código de barras (Enter)...\" autofocus autocomplete=\"off\">\n")
          .append("        <button type=\"submit\" class=\"btn btn-primary\" style=\"white-space: nowrap;\">+ Adicionar</button>\n")
          .append("      </form>\n")
          .append("    </div>\n");

        sb.append("    <div id=\"alerta-area\">\n");
        if (msgSucesso != null && !msgSucesso.isEmpty()) {
            sb.append("      <div style=\"background: #d1fae5; color: #065f46; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">")
              .append(msgSucesso).append("</div>\n");
        }
        if (msgErro != null && !msgErro.isEmpty()) {
            sb.append("      <div style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">")
              .append(msgErro).append("</div>\n");
        }
        sb.append("    </div>\n");

        sb.append("    <div class=\"grid\">\n")
          // Coluna Esquerda: Catálogo
          .append("      <div class=\"card\">\n")
          .append("        <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; flex-wrap: wrap; gap: 0.5rem;\">\n")
          .append("          <h2 class=\"card-title\" style=\"margin: 0;\">Catálogo de Produtos</h2>\n")
          .append("          <input type=\"text\" id=\"filtro-catalogo\" placeholder=\"🔍 Buscar produto...\" class=\"form-input\" style=\"width: 180px; padding: 0.35rem 0.6rem; font-size: 0.85rem;\" oninput=\"filtrarCatalogo(this.value)\">\n")
          .append("        </div>\n")
          .append("        <div class=\"table-container\">\n")
          .append("          <table id=\"tabela-catalogo\">\n")
          .append("            <thead>\n")
          .append("              <tr>\n")
          .append("                <th>Produto</th>\n")
          .append("                <th class=\"text-right\">Preço</th>\n")
          .append("                <th class=\"text-center\">Estoque</th>\n")
          .append("                <th class=\"text-center\">Ação</th>\n")
          .append("              </tr>\n")
          .append("            </thead>\n")
          .append("            <tbody>\n");

        if (catalogo != null) {
            for (Produto p : catalogo) {
                sb.append("              <tr class=\"item-catalogo-row\" id=\"row-prod-").append(p.getId()).append("\" data-busca=\"")
                  .append(p.getNome().toLowerCase()).append(" ").append(p.getCodigoBarras()).append("\">\n")
                  .append("                <td>\n")
                  .append("                  <div class=\"font-bold\">").append(p.getNome()).append("</div>\n")
                  .append("                  <div class=\"font-mono\" style=\"font-size: 0.75rem; color: #6b7280;\">").append(p.getCodigoBarras()).append("</div>\n")
                  .append("                </td>\n")
                  .append("                <td class=\"text-right font-bold\">").append(formatarDinheiro(p.getPrecoCentavos())).append("</td>\n")
                  .append("                <td class=\"text-center\">\n")
                  .append("                  <span id=\"estoque-prod-").append(p.getId()).append("\" style=\"padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: bold; background: ")
                  .append(p.getEstoque() <= 5 ? "#fee2e2; color: #b91c1c;" : "#e5e7eb; color: #374151;")
                  .append("\">").append(p.getEstoque()).append(" un</span>\n")
                  .append("                </td>\n")
                  .append("                <td class=\"text-center\">\n");

                if (p.getEstoque() > 0) {
                    sb.append("                  <button class=\"btn btn-primary\" style=\"padding: 0.35rem 0.75rem; font-size: 0.85rem;\" ")
                      .append("hx-post=\"/pdv/carrinho/item\" ")
                      .append("hx-vals='{\"produtoId\": ").append(p.getId()).append(", \"quantidade\": 1}' ")
                      .append("hx-target=\"#cart-table-body\">\n")
                      .append("                    + Adicionar\n")
                      .append("                  </button>\n");
                } else {
                    sb.append("                  <span style=\"color: #9ca3af; font-size: 0.75rem;\">Esgotado</span>\n");
                }

                sb.append("                </td>\n")
                  .append("              </tr>\n");
            }
        }

        sb.append("            </tbody>\n")
          .append("          </table>\n")
          .append("        </div>\n")
          .append("      </div>\n")

          // Coluna Direita: Carrinho
          .append("      <div class=\"card\">\n")
          .append("        <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;\">\n")
          .append("          <h2 class=\"card-title\" style=\"margin: 0;\">Carrinho de Compras</h2>\n")
          .append("          <button class=\"btn btn-secondary\" style=\"padding: 0.25rem 0.6rem; font-size: 0.75rem; color: #b91c1c;\" ")
          .append("hx-delete=\"/pdv/carrinho\" hx-confirm=\"Tem certeza que deseja limpar todo o carrinho?\" hx-target=\"#cart-table-body\">Esvaziar</button>\n")
          .append("        </div>\n")
          .append("        <div class=\"table-container\">\n")
          .append("          <table>\n")
          .append("            <thead>\n")
          .append("              <tr>\n")
          .append("                <th>Item</th>\n")
          .append("                <th class=\"text-center\">Qtd</th>\n")
          .append("                <th class=\"text-right\">Unitário</th>\n")
          .append("                <th class=\"text-right\">Subtotal</th>\n")
          .append("                <th class=\"text-center\">Remover</th>\n")
          .append("              </tr>\n")
          .append("            </thead>\n")
          .append("            <tbody id=\"cart-table-body\">\n")
          .append(fragmentoItensCarrinho(carrinho))
          .append("            </tbody>\n")
          .append("          </table>\n")
          .append("        </div>\n")

          // Total do Carrinho
          .append("        <div class=\"cart-summary\">\n")
          .append("          <div class=\"cart-total-label\">Total a Pagar</div>\n")
          .append("          <div class=\"cart-total-val\" id=\"cart-total\">")
          .append(formatarDinheiro(totalCentavos))
          .append("</div>\n")
          .append("        </div>\n")

          // Botão Finalizar Venda (abre modal de checkout)
          .append("        <button id=\"btn-checkout\" class=\"btn btn-checkout\" onclick=\"abrirCheckout()\" ")
          .append(carrinhoVazio ? "disabled style=\"opacity: 0.5; cursor: not-allowed;\"" : "")
          .append(">\n")
          .append("          Finalizar Venda\n")
          .append("        </button>\n")
          .append("      </div>\n")
          .append("    </div>\n")
          .append("  </div>\n");

        // Container global de modais
        sb.append("  <div id=\"modal-container\"></div>\n");

        // Scripts auxiliares (filtro em tempo real e modal de checkout)
        sb.append("  <script>\n")
          .append("    function filtrarCatalogo(termo) {\n")
          .append("      termo = (termo || '').toLowerCase().trim();\n")
          .append("      var rows = document.querySelectorAll('#tabela-catalogo .item-catalogo-row');\n")
          .append("      rows.forEach(function(row) {\n")
          .append("        var data = row.getAttribute('data-busca') || '';\n")
          .append("        row.style.display = data.indexOf(termo) !== -1 ? '' : 'none';\n")
          .append("      });\n")
          .append("    }\n")
          .append("    function fecharModal() {\n")
          .append("      var c = document.getElementById('modal-container');\n")
          .append("      if (c) c.innerHTML = '';\n")
          .append("    }\n")
          .append("    function abrirCheckout() {\n")
          .append("      var totalText = document.getElementById('cart-total').innerText;\n")
          .append("      var totalCentavos = Math.round(parseFloat(totalText.replace('R$', '').replace('.', '').replace(',', '.').trim()) * 100) || 0;\n")
          .append("      if (totalCentavos <= 0) return;\n")
          .append("      var html = '<div class=\"modal\" onclick=\"if(event.target===this) fecharModal()\">' +\n")
          .append("        '<div class=\"modal-content\">' +\n")
          .append("        '  <div class=\"modal-header\">' +\n")
          .append("        '    <h3 class=\"modal-title\">Finalizar Pagamento</h3>' +\n")
          .append("        '    <button class=\"modal-close\" onclick=\"fecharModal()\">&times;</button>' +\n")
          .append("        '  </div>' +\n")
          .append("        '  <div class=\"cart-summary\" style=\"margin-bottom: 1rem;\">' +\n")
          .append("        '    <div class=\"cart-total-label\">Total da Venda</div>' +\n")
          .append("        '    <div class=\"cart-total-val\">' + totalText + '</div>' +\n")
          .append("        '  </div>' +\n")
          .append("        '  <form hx-post=\"/pdv/carrinho/checkout\" hx-target=\"#alerta-area\" onsubmit=\"fecharModal()\">' +\n")
          .append("        '    <div class=\"form-group\">' +\n")
          .append("        '      <label class=\"form-label\">Forma de Pagamento</label>' +\n")
          .append("        '      <input type=\"hidden\" id=\"forma-pagamento-val\" name=\"formaPagamento\" value=\"DINHEIRO\">' +\n")
          .append("        '      <div class=\"payment-options\">' +\n")
          .append("        '        <div class=\"payment-btn active\" onclick=\"selecionarPagamento(this, \\'DINHEIRO\\')\">💵 Dinheiro</div>' +\n")
          .append("        '        <div class=\"payment-btn\" onclick=\"selecionarPagamento(this, \\'PIX\\')\">📱 PIX</div>' +\n")
          .append("        '        <div class=\"payment-btn\" onclick=\"selecionarPagamento(this, \\'DEBITO\\')\">💳 Débito</div>' +\n")
          .append("        '        <div class=\"payment-btn\" onclick=\"selecionarPagamento(this, \\'CREDITO\\')\">💳 Crédito</div>' +\n")
          .append("        '      </div>' +\n")
          .append("        '    </div>' +\n")
          .append("        '    <div id=\"troco-container\" class=\"troco-box\">' +\n")
          .append("        '      <label class=\"form-label\" style=\"margin-bottom: 0.35rem;\">Valor Recebido (R$)</label>' +\n")
          .append("        '      <input type=\"number\" step=\"0.01\" id=\"valor-recebido\" class=\"form-input\" placeholder=\"Ex: 50.00\" oninput=\"calcularTroco(' + totalCentavos + ')\">' +\n")
          .append("        '      <div style=\"display: flex; justify-content: space-between; align-items: center; margin-top: 0.75rem;\">' +\n")
          .append("        '        <span style=\"font-weight: 700; color: #4b5563;\">Troco a Devolver:</span>' +\n")
          .append("        '        <span id=\"label-troco\" class=\"troco-val\">R$ 0,00</span>' +\n")
          .append("        '      </div>' +\n")
          .append("        '    </div>' +\n")
          .append("        '    <button type=\"submit\" class=\"btn btn-checkout\">Confirmar e Finalizar Venda</button>' +\n")
          .append("        '  </form>' +\n")
          .append("        '</div>' +\n")
          .append("        '</div>';\n")
          .append("      document.getElementById('modal-container').innerHTML = html;\n")
          .append("      setTimeout(function(){ var inp = document.getElementById('valor-recebido'); if (inp) inp.focus(); }, 100);\n")
          .append("    }\n")
          .append("    function selecionarPagamento(btn, forma) {\n")
          .append("      document.querySelectorAll('.payment-btn').forEach(function(b){ b.classList.remove('active'); });\n")
          .append("      btn.classList.add('active');\n")
          .append("      document.getElementById('forma-pagamento-val').value = forma;\n")
          .append("      var trocoBox = document.getElementById('troco-container');\n")
          .append("      if (trocoBox) trocoBox.style.display = (forma === 'DINHEIRO') ? 'block' : 'none';\n")
          .append("    }\n")
          .append("    function calcularTroco(totalCentavos) {\n")
          .append("      var recVal = parseFloat(document.getElementById('valor-recebido').value) || 0;\n")
          .append("      var recCentavos = Math.round(recVal * 100);\n")
          .append("      var difCentavos = recCentavos - totalCentavos;\n")
          .append("      var labelTroco = document.getElementById('label-troco');\n")
          .append("      if (difCentavos >= 0) {\n")
          .append("        labelTroco.style.color = '#10b981';\n")
          .append("        labelTroco.innerText = 'R$ ' + (difCentavos / 100).toFixed(2).replace('.', ',');\n")
          .append("      } else {\n")
          .append("        labelTroco.style.color = '#ef4444';\n")
          .append("        labelTroco.innerText = 'Falta R$ ' + (Math.abs(difCentavos) / 100).toFixed(2).replace('.', ',');\n")
          .append("      }\n")
          .append("    }\n")
          .append("    if (window.EventSource) {\n")
          .append("      try {\n")
          .append("        var sse = new EventSource('/pdv/eventos/estoque');\n")
          .append("        sse.addEventListener('estoque-atualizado', function(e) {\n")
          .append("          try {\n")
          .append("            var data = JSON.parse(e.data);\n")
          .append("            var el = document.getElementById('estoque-prod-' + data.produtoId);\n")
          .append("            if (el) {\n")
          .append("              el.innerText = data.novoEstoque + ' un';\n")
          .append("              if (data.novoEstoque <= 5) {\n")
          .append("                el.style.background = '#fee2e2';\n")
          .append("                el.style.color = '#b91c1c';\n")
          .append("              } else {\n")
          .append("                el.style.background = '#e5e7eb';\n")
          .append("                el.style.color = '#374151';\n")
          .append("              }\n")
          .append("            }\n")
          .append("            var elModal = document.getElementById('modal-estoque-prod-' + data.produtoId);\n")
          .append("            if (elModal) {\n")
          .append("              elModal.innerText = data.novoEstoque + ' un';\n")
          .append("            }\n")
          .append("          } catch(err) {}\n")
          .append("        });\n")
          .append("      } catch(err) {}\n")
          .append("    }\n")
          .append("  </script>\n")
          .append("</body>\n</html>");

        return sb.toString();
    }

    public static String fragmentoItensCarrinho(Carrinho carrinho) {
        if (carrinho == null || carrinho.getItens().isEmpty()) {
            return "<tr><td colspan=\"5\" class=\"text-center\" style=\"color: #9ca3af; padding: 2rem;\">Carrinho vazio</td></tr>";
        }
        StringBuilder sb = new StringBuilder();
        for (ItemCarrinho ic : carrinho.getItens()) {
            long pId = ic.getProduto().getId();
            sb.append("<tr id=\"cart-row-").append(pId).append("\">\n")
              .append("  <td>\n")
              .append("    <div class=\"font-bold\">").append(ic.getProduto().getNome()).append("</div>\n")
              .append("  </td>\n")
              .append("  <td class=\"text-center\" style=\"white-space: nowrap;\">\n")
              .append("    <button class=\"btn-qty\" hx-post=\"/pdv/carrinho/item/diminuir\" ")
              .append("hx-vals='{\"produtoId\": ").append(pId).append(", \"quantidade\": 1}' ")
              .append("hx-target=\"#cart-table-body\">-</button>\n")
              .append("    <span class=\"font-mono font-bold\" style=\"margin: 0 0.4rem;\">").append(ic.getQuantidade()).append("</span>\n")
              .append("    <button class=\"btn-qty\" hx-post=\"/pdv/carrinho/item\" ")
              .append("hx-vals='{\"produtoId\": ").append(pId).append(", \"quantidade\": 1}' ")
              .append("hx-target=\"#cart-table-body\">+</button>\n")
              .append("  </td>\n")
              .append("  <td class=\"text-right\">").append(formatarDinheiro(ic.getPrecoUnitCentavos())).append("</td>\n")
              .append("  <td class=\"text-right font-bold\">").append(formatarDinheiro(ic.getSubtotalCentavos())).append("</td>\n")
              .append("  <td class=\"text-center\">\n")
              .append("    <button class=\"btn btn-danger\" ")
              .append("hx-delete=\"/pdv/carrinho/item/").append(pId).append("\" ")
              .append("hx-target=\"#cart-table-body\">\n")
              .append("      &times;\n")
              .append("    </button>\n")
              .append("  </td>\n")
              .append("</tr>\n");
        }
        return sb.toString();
    }

    public static String fragmentoCarrinhoAtualizado(Carrinho carrinho) {
        StringBuilder sb = new StringBuilder();
        // 1. Corpo da tabela do carrinho
        sb.append(fragmentoItensCarrinho(carrinho));

        // 2. Out-of-band swap para o valor total
        int totalCentavos = (carrinho != null) ? carrinho.getTotalCentavos() : 0;
        sb.append("<div id=\"cart-total\" hx-swap-oob=\"true\" class=\"cart-total-val\">")
          .append(formatarDinheiro(totalCentavos))
          .append("</div>\n");

        // 3. Out-of-band swap para o botao de checkout
        boolean vazio = (carrinho == null || carrinho.getItens().isEmpty());
        sb.append("<button id=\"btn-checkout\" hx-swap-oob=\"true\" class=\"btn btn-checkout\" onclick=\"abrirCheckout()\" ")
          .append(vazio ? "disabled style=\"opacity: 0.5; cursor: not-allowed;\"" : "")
          .append(">Finalizar Venda</button>\n");

        // 4. Out-of-band swap para limpar e focar campo de codigo de barras
        sb.append("<input type=\"text\" name=\"codigo\" id=\"input-codigo-barras\" hx-swap-oob=\"true\" class=\"form-input\" style=\"flex: 1; min-width: 220px; font-family: monospace; font-size: 1.1rem; font-weight: bold;\" placeholder=\"Bipe ou digite o código de barras (Enter)...\" autofocus autocomplete=\"off\" value=\"\">\n");

        return sb.toString();
    }

    public static String fragmentoCheckoutSucesso(Venda venda, String formaPagamento) {
        return "<div style=\"background: #d1fae5; color: #065f46; padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem; border: 1px solid #a7f3d0;\">\n" +
               "  <div class=\"font-bold\" style=\"font-size: 1.1rem;\">✅ Venda #" + venda.getId() + " finalizada com sucesso!</div>\n" +
               "  <div style=\"margin-top: 0.25rem;\">Total: <b>" + formatarDinheiro(venda.getTotalCentavos()) + "</b> &bull; Forma: <b>" + formaPagamento + "</b> &bull; Horário: " + venda.getDataHora() + "</div>\n" +
               "</div>\n" +
               "<script>setTimeout(function(){ window.location.reload(); }, 1200);</script>";
    }

    public static String fragmentoCheckoutErro(String erro) {
        return "<div style=\"background: #fee2e2; color: #b91c1c; padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem; border: 1px solid #fca5a5;\">\n" +
               "  <div class=\"font-bold\">❌ Erro ao finalizar venda</div>\n" +
               "  <div>" + erro + "</div>\n" +
               "</div>";
    }

    public static String fragmentoModalRelatorio(RelatorioDiarioDTO resumo, List<Venda> vendasRecentes) {
        StringBuilder sb = new StringBuilder();
        int qtdVendas = (resumo != null) ? resumo.getQuantidadeVendas() : 0;
        int totalCentavos = (resumo != null) ? resumo.getTotalCentavos() : 0;
        int ticketMedioCentavos = (resumo != null) ? resumo.getTicketMedioCentavos() : 0;

        sb.append("<div class=\"modal\" onclick=\"if(event.target===this) fecharModal()\">\n")
          .append("  <div class=\"modal-content\" style=\"max-width: 650px;\">\n")
          .append("    <div class=\"modal-header\">\n")
          .append("      <h3 class=\"modal-title\">📊 Resumo de Vendas do Dia</h3>\n")
          .append("      <button class=\"modal-close\" onclick=\"fecharModal()\">&times;</button>\n")
          .append("    </div>\n")
          .append("    <div style=\"display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.75rem; margin-bottom: 1.25rem;\">\n")
          .append("      <div class=\"card\" style=\"padding: 0.85rem; text-align: center; background: #ecfdf5; border-color: #a7f3d0;\">\n")
          .append("        <div style=\"font-size: 0.75rem; color: #065f46; font-weight: 700; text-transform: uppercase;\">Faturamento</div>\n")
          .append("        <div style=\"font-size: 1.4rem; font-weight: 900; color: #065f46; margin-top: 0.25rem;\">").append(formatarDinheiro(totalCentavos)).append("</div>\n")
          .append("      </div>\n")
          .append("      <div class=\"card\" style=\"padding: 0.85rem; text-align: center; background: #eff6ff; border-color: #bfdbfe;\">\n")
          .append("        <div style=\"font-size: 0.75rem; color: #1e40af; font-weight: 700; text-transform: uppercase;\">Total Vendas</div>\n")
          .append("        <div style=\"font-size: 1.4rem; font-weight: 900; color: #1e40af; margin-top: 0.25rem;\">").append(qtdVendas).append("</div>\n")
          .append("      </div>\n")
          .append("      <div class=\"card\" style=\"padding: 0.85rem; text-align: center; background: #fdf4ff; border-color: #f5d0fe;\">\n")
          .append("        <div style=\"font-size: 0.75rem; color: #86198f; font-weight: 700; text-transform: uppercase;\">Ticket Médio</div>\n")
          .append("        <div style=\"font-size: 1.4rem; font-weight: 900; color: #86198f; margin-top: 0.25rem;\">").append(formatarDinheiro(ticketMedioCentavos)).append("</div>\n")
          .append("      </div>\n")
          .append("    </div>\n")
          .append("    <h4 style=\"font-size: 0.95rem; font-weight: 700; margin-bottom: 0.5rem; color: #374151;\">Últimas Vendas Realizadas</h4>\n")
          .append("    <div class=\"table-container\" style=\"max-height: 250px; overflow-y: auto; margin-bottom: 1rem;\">\n")
          .append("      <table>\n")
          .append("        <thead>\n")
          .append("          <tr>\n")
          .append("            <th>ID</th>\n")
          .append("            <th>Data/Hora</th>\n")
          .append("            <th class=\"text-right\">Total</th>\n")
          .append("            <th class=\"text-center\">Status</th>\n")
          .append("          </tr>\n")
          .append("        </thead>\n")
          .append("        <tbody>\n");

        if (vendasRecentes == null || vendasRecentes.isEmpty()) {
            sb.append("          <tr><td colspan=\"4\" class=\"text-center\" style=\"color: #9ca3af; padding: 1.5rem;\">Nenhuma venda realizada ainda</td></tr>\n");
        } else {
            for (Venda v : vendasRecentes) {
                sb.append("          <tr>\n")
                  .append("            <td class=\"font-mono font-bold\">#").append(v.getId()).append("</td>\n")
                  .append("            <td style=\"font-size: 0.8rem; color: #4b5563;\">").append(v.getDataHora()).append("</td>\n")
                  .append("            <td class=\"text-right font-bold\" style=\"color: #065f46;\">").append(formatarDinheiro(v.getTotalCentavos())).append("</td>\n")
                  .append("            <td class=\"text-center\"><span class=\"badge\">").append(v.getStatus()).append("</span></td>\n")
                  .append("          </tr>\n");
            }
        }

        sb.append("        </tbody>\n")
          .append("      </table>\n")
          .append("    </div>\n")
          .append("    <div style=\"text-align: right;\">\n")
          .append("      <button class=\"btn btn-secondary\" onclick=\"fecharModal()\">Fechar</button>\n")
          .append("    </div>\n")
          .append("  </div>\n")
          .append("</div>\n");

        return sb.toString();
    }

    public static String fragmentoModalEstoque(List<Produto> catalogo, String alerta) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"modal\" onclick=\"if(event.target===this) fecharModal()\">\n")
          .append("  <div class=\"modal-content\" style=\"max-width: 750px;\">\n")
          .append("    <div class=\"modal-header\">\n")
          .append("      <h3 class=\"modal-title\">📦 Gestão de Estoque</h3>\n")
          .append("      <button class=\"modal-close\" onclick=\"fecharModal()\">&times;</button>\n")
          .append("    </div>\n");

        if (alerta != null && !alerta.trim().isEmpty()) {
            sb.append("    <div style=\"background: #d1fae5; color: #065f46; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">")
              .append(alerta).append("</div>\n");
        }

        sb.append("    <div class=\"table-container\" style=\"max-height: 280px; overflow-y: auto; margin-bottom: 1.25rem;\">\n")
          .append("      <table>\n")
          .append("        <thead>\n")
          .append("          <tr>\n")
          .append("            <th>Produto</th>\n")
          .append("            <th class=\"text-right\">Preço</th>\n")
          .append("            <th class=\"text-center\">Estoque Atual</th>\n")
          .append("            <th class=\"text-center\">Ajustar Estoque</th>\n")
          .append("          </tr>\n")
          .append("        </thead>\n")
          .append("        <tbody>\n");

        if (catalogo != null && !catalogo.isEmpty()) {
            for (Produto p : catalogo) {
                sb.append("          <tr>\n")
                  .append("            <td>\n")
                  .append("              <div class=\"font-bold\">").append(p.getNome()).append("</div>\n")
                  .append("              <div class=\"font-mono\" style=\"font-size: 0.75rem; color: #6b7280;\">").append(p.getCodigoBarras()).append("</div>\n")
                  .append("            </td>\n")
                  .append("            <td class=\"text-right font-bold\">").append(formatarDinheiro(p.getPrecoCentavos())).append("</td>\n")
                  .append("            <td class=\"text-center\">\n")
                  .append("              <span id=\"modal-estoque-prod-").append(p.getId()).append("\" style=\"padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.8rem; font-weight: bold; background: ")
                  .append(p.getEstoque() <= 5 ? "#fee2e2; color: #b91c1c;" : "#e5e7eb; color: #374151;")
                  .append("\">").append(p.getEstoque()).append(" un</span>\n")
                  .append("            </td>\n")
                  .append("            <td class=\"text-center\">\n")
                  .append("              <form hx-post=\"/pdv/estoque/ajuste\" hx-target=\"#modal-container\" style=\"display: inline-flex; gap: 4px; align-items: center;\">\n")
                  .append("                <input type=\"hidden\" name=\"produtoId\" value=\"").append(p.getId()).append("\">\n")
                  .append("                <input type=\"number\" name=\"novoEstoque\" value=\"").append(p.getEstoque()).append("\" min=\"0\" class=\"form-input\" style=\"width: 70px; padding: 0.25rem 0.4rem; font-size: 0.85rem; text-align: center;\">\n")
                  .append("                <button type=\"submit\" class=\"btn btn-primary\" style=\"padding: 0.25rem 0.6rem; font-size: 0.8rem;\">Salvar</button>\n")
                  .append("              </form>\n")
                  .append("            </td>\n")
                  .append("          </tr>\n");
            }
        } else {
            sb.append("          <tr><td colspan=\"4\" class=\"text-center\" style=\"color: #9ca3af; padding: 1.5rem;\">Nenhum produto cadastrado</td></tr>\n");
        }

        sb.append("        </tbody>\n")
          .append("      </table>\n")
          .append("    </div>\n")
          .append("    <div class=\"card\" style=\"background: #f9fafb; border: 1px solid #e5e7eb; padding: 1rem;\">\n")
          .append("      <h4 style=\"font-size: 0.95rem; font-weight: 700; margin-bottom: 0.75rem; color: #111827;\">➕ Cadastrar Novo Produto</h4>\n")
          .append("      <form hx-post=\"/pdv/estoque/novo\" hx-target=\"#modal-container\" style=\"display: grid; grid-template-columns: 2fr 1.5fr 1fr 1fr auto; gap: 0.5rem; align-items: end;\">\n")
          .append("        <div>\n")
          .append("          <label class=\"form-label\" style=\"font-size: 0.75rem;\">Nome</label>\n")
          .append("          <input type=\"text\" name=\"nome\" required class=\"form-input\" style=\"font-size: 0.85rem; padding: 0.35rem 0.5rem;\" placeholder=\"Ex: Suco Laranja 1L\">\n")
          .append("        </div>\n")
          .append("        <div>\n")
          .append("          <label class=\"form-label\" style=\"font-size: 0.75rem;\">Código Barras</label>\n")
          .append("          <input type=\"text\" name=\"codigoBarras\" required class=\"form-input font-mono\" style=\"font-size: 0.85rem; padding: 0.35rem 0.5rem;\" placeholder=\"Ex: 7891234567890\">\n")
          .append("        </div>\n")
          .append("        <div>\n")
          .append("          <label class=\"form-label\" style=\"font-size: 0.75rem;\">Preço (R$)</label>\n")
          .append("          <input type=\"number\" step=\"0.01\" name=\"preco\" required class=\"form-input\" style=\"font-size: 0.85rem; padding: 0.35rem 0.5rem;\" placeholder=\"8.50\">\n")
          .append("        </div>\n")
          .append("        <div>\n")
          .append("          <label class=\"form-label\" style=\"font-size: 0.75rem;\">Estoque</label>\n")
          .append("          <input type=\"number\" name=\"estoque\" required class=\"form-input\" style=\"font-size: 0.85rem; padding: 0.35rem 0.5rem;\" placeholder=\"20\" value=\"10\">\n")
          .append("        </div>\n")
          .append("        <button type=\"submit\" class=\"btn btn-primary\" style=\"font-size: 0.85rem; padding: 0.35rem 0.8rem; white-space: nowrap;\">+ Cadastrar</button>\n")
          .append("      </form>\n")
          .append("    </div>\n")
          .append("    <div style=\"text-align: right; margin-top: 1rem;\">\n")
          .append("      <button class=\"btn btn-secondary\" onclick=\"fecharModal()\">Fechar</button>\n")
          .append("    </div>\n")
          .append("  </div>\n")
          .append("</div>\n");

        return sb.toString();
    }
}

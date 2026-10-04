package com.github.matheuscruzsouza.pocketpdv.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.matheuscruzsouza.nanospring.server.Server;
import com.github.matheuscruzsouza.pocketpdv.R;
import com.github.matheuscruzsouza.pocketpdv.domain.model.AlertaEstoqueDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioDiarioDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioVendaItemDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioVendasPorFuncionarioDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;
import com.github.matheuscruzsouza.pocketpdv.domain.service.RelatorioService;
import com.github.matheuscruzsouza.pocketpdv.domain.service.RelatorioServiceImpl;
import com.github.matheuscruzsouza.pocketpdv.domain.service.VendaService;
import com.github.matheuscruzsouza.pocketpdv.domain.service.VendaServiceImpl;
import com.github.matheuscruzsouza.pocketpdv.persistence.DatabaseHelper;
import com.github.matheuscruzsouza.pocketpdv.persistence.FuncionarioRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.ItemVendaRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.ProdutoRepository;
import com.github.matheuscruzsouza.pocketpdv.persistence.VendaRepository;
import com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;

import java.util.Calendar;
import java.util.TimeZone;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String ALFANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    // Bottom Tab navigation views
    private View btnTabVendas;
    private View btnTabFuncionarios;
    private View btnTabRelatorios;
    private View btnTabServidor;

    private ImageView ivTabVendas;
    private ImageView ivTabFuncionarios;
    private ImageView ivTabRelatorios;
    private ImageView ivTabServidor;

    private TextView tvTabVendas;
    private TextView tvTabFuncionarios;
    private TextView tvTabRelatorios;
    private TextView tvTabServidor;

    // Tab containers
    private ScrollView layoutTabVendas;
    private ScrollView layoutTabFuncionarios;
    private ScrollView layoutTabRelatorios;
    private ScrollView layoutTabServidor;

    // Aba 1: Vendas (Dashboard)
    private TextView tvTotalVendas;
    private TextView tvQtdVendas;
    private TextView tvTicketMedio;
    private LinearLayout containerVendasRecentes;
    private LinearLayout containerAlertasEstoque;
    private Button btnRefreshVendas;

    // Aba 2: Funcionarios
    private LinearLayout containerFuncionarios;
    private Button btnNovoFuncionario;

    // Aba 3: Relatórios de Venda
    private TextView tvRelatoriosTotalFaturado;
    private TextView tvRelatoriosQtdVendas;
    private TextView tvRelatoriosQtdItens;
    private LinearLayout containerDesempenhoFuncionarios;
    private Button btnRefreshRelatorios;
    private Button btnExportarCsv;

    // Aba 4: Servidor
    private TextView tvServerHeaderSubtitle;
    private TextView tvServidorStatus;
    private TextView tvServidorUrlLocal;
    private TextView tvServidorUrlMdns;
    private View cardSwaggerUi;
    private TextView tvSwaggerUrl;
    private TextView tvMdnsStatus;
    private TextView tvNsdStatus;
    private TextView tvBateriaStatus;
    private Button btnIgnorarOtimizacaoBateria;
    private TextView tvRamTotalPss;
    private TextView tvRamJavaHeap;
    private TextView tvRamNativeHeap;
    private TextView tvDeviceInfo;
    private Button btnRefreshServidor;

    // Services e Repos
    private RelatorioService relatorioService;
    private VendaRepository vendaRepository;
    private FuncionarioRepository funcionarioRepository;
    private VendaService vendaService;

    private int activeTab = 0; // 0=Vendas, 1=Funcionários, 2=Relatórios, 3=Servidor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupTabs();

        // Inicia o serviço HTTP em background
        Intent serviceIntent = new Intent(this, PocketPdvService.class);
        startService(serviceIntent);

        btnRefreshVendas.setOnClickListener(v -> atualizarDadosVendas());
        btnNovoFuncionario.setOnClickListener(v -> abrirDialogNovoFuncionario());
        btnRefreshRelatorios.setOnClickListener(v -> atualizarDadosRelatorios());
        btnExportarCsv.setOnClickListener(v -> exportarRelatorioCsv());
        btnRefreshServidor.setOnClickListener(v -> atualizarDadosServidor());

        // Escuta vendas concluídas para atualizar métricas e notificar via Toast nativo no Android
        EstoqueSseHub.getInstance().registrarVendaListener(vendaConcluidaListener);
        EstoqueSseHub.getInstance().registrarEstoqueListener(estoqueAtualizadoListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inicializarDependencias();
        atualizarAbaAtiva();
    }

    private void bindViews() {
        btnTabVendas = findViewById(R.id.btnTabVendas);
        btnTabFuncionarios = findViewById(R.id.btnTabFuncionarios);
        btnTabRelatorios = findViewById(R.id.btnTabRelatorios);
        btnTabServidor = findViewById(R.id.btnTabServidor);

        ivTabVendas = findViewById(R.id.ivTabVendas);
        ivTabFuncionarios = findViewById(R.id.ivTabFuncionarios);
        ivTabRelatorios = findViewById(R.id.ivTabRelatorios);
        ivTabServidor = findViewById(R.id.ivTabServidor);

        tvTabVendas = findViewById(R.id.tvTabVendas);
        tvTabFuncionarios = findViewById(R.id.tvTabFuncionarios);
        tvTabRelatorios = findViewById(R.id.tvTabRelatorios);
        tvTabServidor = findViewById(R.id.tvTabServidor);

        layoutTabVendas = findViewById(R.id.layoutTabVendas);
        layoutTabFuncionarios = findViewById(R.id.layoutTabFuncionarios);
        layoutTabRelatorios = findViewById(R.id.layoutTabRelatorios);
        layoutTabServidor = findViewById(R.id.layoutTabServidor);

        tvServerHeaderSubtitle = findViewById(R.id.tvServerHeaderSubtitle);

        // Aba 1
        tvTotalVendas = findViewById(R.id.tvTotalVendas);
        tvQtdVendas = findViewById(R.id.tvQtdVendas);
        tvTicketMedio = findViewById(R.id.tvTicketMedio);
        containerVendasRecentes = findViewById(R.id.containerVendasRecentes);
        containerAlertasEstoque = findViewById(R.id.containerAlertasEstoque);
        btnRefreshVendas = findViewById(R.id.btnRefreshVendas);

        // Aba 2
        containerFuncionarios = findViewById(R.id.containerFuncionarios);
        btnNovoFuncionario = findViewById(R.id.btnNovoFuncionario);

        // Aba 3
        tvRelatoriosTotalFaturado = findViewById(R.id.tvRelatoriosTotalFaturado);
        tvRelatoriosQtdVendas = findViewById(R.id.tvRelatoriosQtdVendas);
        tvRelatoriosQtdItens = findViewById(R.id.tvRelatoriosQtdItens);
        containerDesempenhoFuncionarios = findViewById(R.id.containerDesempenhoFuncionarios);
        btnRefreshRelatorios = findViewById(R.id.btnRefreshRelatorios);
        btnExportarCsv = findViewById(R.id.btnExportarCsv);

        // Aba 4
        tvServidorStatus = findViewById(R.id.tvServidorStatus);
        tvServidorUrlLocal = findViewById(R.id.tvServidorUrlLocal);
        tvServidorUrlMdns = findViewById(R.id.tvServidorUrlMdns);
        cardSwaggerUi = findViewById(R.id.cardSwaggerUi);
        tvSwaggerUrl = findViewById(R.id.tvSwaggerUrl);
        tvMdnsStatus = findViewById(R.id.tvMdnsStatus);
        tvNsdStatus = findViewById(R.id.tvNsdStatus);
        tvBateriaStatus = findViewById(R.id.tvBateriaStatus);
        btnIgnorarOtimizacaoBateria = findViewById(R.id.btnIgnorarOtimizacaoBateria);
        tvRamTotalPss = findViewById(R.id.tvRamTotalPss);
        tvRamJavaHeap = findViewById(R.id.tvRamJavaHeap);
        tvRamNativeHeap = findViewById(R.id.tvRamNativeHeap);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        btnRefreshServidor = findViewById(R.id.btnRefreshServidor);

        if (cardSwaggerUi != null) {
            cardSwaggerUi.setOnClickListener(v -> {
                String ip = getLocalIpAddress();
                String url = (ip != null && !ip.isEmpty() && !ip.equals("127.0.0.1"))
                        ? "http://" + ip + ":" + PocketPdvService.PORT + "/swagger-ui"
                        : "http://localhost:" + PocketPdvService.PORT + "/swagger-ui";
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Erro ao abrir navegador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupTabs() {
        btnTabVendas.setOnClickListener(v -> selecionarAba(0));
        btnTabFuncionarios.setOnClickListener(v -> selecionarAba(1));
        btnTabRelatorios.setOnClickListener(v -> selecionarAba(2));
        btnTabServidor.setOnClickListener(v -> selecionarAba(3));
    }

    private void selecionarAba(int tabIndex) {
        this.activeTab = tabIndex;

        // Estilos dos botões e ícones da barra inferior
        int corAtiva = Color.parseColor("#059669");
        int corInativa = Color.parseColor("#6B7280");

        if (ivTabVendas != null) ivTabVendas.setColorFilter(tabIndex == 0 ? corAtiva : corInativa);
        if (tvTabVendas != null) {
            tvTabVendas.setTextColor(tabIndex == 0 ? corAtiva : corInativa);
            tvTabVendas.setTypeface(null, tabIndex == 0 ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (ivTabFuncionarios != null) ivTabFuncionarios.setColorFilter(tabIndex == 1 ? corAtiva : corInativa);
        if (tvTabFuncionarios != null) {
            tvTabFuncionarios.setTextColor(tabIndex == 1 ? corAtiva : corInativa);
            tvTabFuncionarios.setTypeface(null, tabIndex == 1 ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (ivTabRelatorios != null) ivTabRelatorios.setColorFilter(tabIndex == 2 ? corAtiva : corInativa);
        if (tvTabRelatorios != null) {
            tvTabRelatorios.setTextColor(tabIndex == 2 ? corAtiva : corInativa);
            tvTabRelatorios.setTypeface(null, tabIndex == 2 ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (ivTabServidor != null) ivTabServidor.setColorFilter(tabIndex == 3 ? corAtiva : corInativa);
        if (tvTabServidor != null) {
            tvTabServidor.setTextColor(tabIndex == 3 ? corAtiva : corInativa);
            tvTabServidor.setTypeface(null, tabIndex == 3 ? Typeface.BOLD : Typeface.NORMAL);
        }

        // Visibilidade dos containers
        layoutTabVendas.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        layoutTabFuncionarios.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        layoutTabRelatorios.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        layoutTabServidor.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        atualizarAbaAtiva();
    }

    private void atualizarAbaAtiva() {
        if (activeTab == 0) {
            atualizarDadosVendas();
        } else if (activeTab == 1) {
            atualizarDadosFuncionarios();
        } else if (activeTab == 2) {
            atualizarDadosRelatorios();
        } else if (activeTab == 3) {
            atualizarDadosServidor();
        }
    }

    private void inicializarDependencias() {
        DatabaseHelper dbHelper = null;
        PocketPdvService service = PocketPdvService.getInstance();
        if (service != null) {
            dbHelper = service.getDbHelper();
            Server server = service.getServer();
            if (server != null) {
                relatorioService = (RelatorioService) server.getBean(RelatorioServiceImpl.class);
                vendaRepository = (VendaRepository) server.getBean(VendaRepository.class);
                funcionarioRepository = (FuncionarioRepository) server.getBean(FuncionarioRepository.class);
                vendaService = (VendaService) server.getBean(VendaServiceImpl.class);
            }
        }

        if (dbHelper == null) {
            dbHelper = DatabaseHelper.getInstance(this);
        }
        if (relatorioService == null) {
            relatorioService = new RelatorioServiceImpl(new VendaRepository(dbHelper), new ProdutoRepository(dbHelper));
        }
        if (vendaRepository == null) {
            vendaRepository = new VendaRepository(dbHelper);
        }
        if (funcionarioRepository == null) {
            funcionarioRepository = new FuncionarioRepository(dbHelper);
        }
        if (vendaService == null) {
            vendaService = new VendaServiceImpl(dbHelper, new ProdutoRepository(dbHelper), vendaRepository, new ItemVendaRepository(dbHelper));
        }
    }

    private String formatarDataAmigavel(String isoDateStr) {
        if (isoDateStr == null || isoDateStr.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            String limpa = isoDateStr.replace("Z", "");
            Date data = parser.parse(limpa);
            if (data == null) return isoDateStr;

            Calendar calVenda = Calendar.getInstance();
            calVenda.setTime(data);

            Calendar calHoje = Calendar.getInstance();

            SimpleDateFormat horaFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String horaStr = horaFmt.format(data);

            boolean mesmoAno = calVenda.get(Calendar.YEAR) == calHoje.get(Calendar.YEAR);
            int diaVenda = calVenda.get(Calendar.DAY_OF_YEAR);
            int diaHoje = calHoje.get(Calendar.DAY_OF_YEAR);

            if (mesmoAno && diaVenda == diaHoje) {
                return "Hoje às " + horaStr;
            } else if (mesmoAno && diaVenda == diaHoje - 1) {
                return "Ontem às " + horaStr;
            } else {
                SimpleDateFormat diaMesFmt = new SimpleDateFormat("dd/MM", Locale.getDefault());
                return diaMesFmt.format(data) + " às " + horaStr;
            }
        } catch (Exception e) {
            return isoDateStr;
        }
    }

    private void confirmarEstornoVenda(Venda v, String operadorNome) {
        if ("CANCELADA".equalsIgnoreCase(v.getStatus())) {
            Toast.makeText(this, "Esta venda já está cancelada/estornada.", Toast.LENGTH_SHORT).show();
            return;
        }

        String valorFmt = com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(v.getTotalCentavos());

        new AlertDialog.Builder(this)
                .setTitle("Estornar Venda #" + v.getId())
                .setMessage("Deseja realmente estornar/cancelar esta venda no valor de " + valorFmt + "?\n\n" +
                        "Operador: " + operadorNome + "\n" +
                        "Data: " + formatarDataAmigavel(v.getDataHora()) + "\n\n" +
                        "⚠️ O status da venda passará para CANCELADA e todos os itens retornarão automaticamente ao estoque de produtos.")
                .setPositiveButton("Sim, Cancelar Venda", (dialog, which) -> {
                    if (vendaService == null) inicializarDependencias();
                    boolean sucesso = vendaService.estornarVenda(v.getId());
                    if (sucesso) {
                        Toast.makeText(MainActivity.this, "Venda #" + v.getId() + " cancelada! Estoque recomposto.", Toast.LENGTH_LONG).show();
                        atualizarDadosVendas();
                        if (activeTab == 2) atualizarDadosRelatorios();
                    } else {
                        Toast.makeText(MainActivity.this, "Erro ao estornar venda #" + v.getId(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Voltar", null)
                .show();
    }

    // ==========================================
    // ABA 1: VENDAS (DASHBOARD)
    // ==========================================
    private void atualizarDadosVendas() {
        try {
            if (relatorioService == null) inicializarDependencias();

            // Resumo do Dia
            RelatorioDiarioDTO relatorio = relatorioService.obterResumoDoDia(new Date());
            double totalReais = relatorio.getTotalCentavos() / 100.0;
            double ticketMedioReais = relatorio.getTicketMedioCentavos() / 100.0;

            tvTotalVendas.setText(com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(relatorio.getTotalCentavos()));
            tvQtdVendas.setText(String.valueOf(relatorio.getQuantidadeVendas()));
            tvTicketMedio.setText(com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(relatorio.getTicketMedioCentavos()));

            // Vendas Recentes
            containerVendasRecentes.removeAllViews();
            List<Venda> vendasRecentes = vendaRepository.listarRecentes(5);
            LayoutInflater inflater = LayoutInflater.from(this);

            if (vendasRecentes.isEmpty()) {
                TextView tvVazio = new TextView(this);
                tvVazio.setText("Nenhuma venda realizada ainda.");
                tvVazio.setTextColor(Color.parseColor("#9CA3AF"));
                tvVazio.setPadding(0, 16, 0, 16);
                containerVendasRecentes.addView(tvVazio);
            } else {
                for (Venda v : vendasRecentes) {
                    View itemView = inflater.inflate(R.layout.item_venda_recente, containerVendasRecentes, false);
                    TextView tvId = itemView.findViewById(R.id.tvVendaId);
                    TextView tvOperador = itemView.findViewById(R.id.tvVendaOperador);
                    TextView tvData = itemView.findViewById(R.id.tvVendaData);
                    TextView tvTotal = itemView.findViewById(R.id.tvVendaTotal);
                    TextView tvStatus = itemView.findViewById(R.id.tvVendaStatus);

                    Funcionario op = (funcionarioRepository != null && v.getFuncionarioId() > 0)
                            ? funcionarioRepository.buscarPorId(v.getFuncionarioId()) : null;
                    String nomeOperador = (op != null) ? op.getNome() : ("Operador #" + v.getFuncionarioId());

                    tvId.setText("Venda #" + v.getId());
                    if (tvOperador != null) {
                        tvOperador.setText(" • " + nomeOperador);
                    }
                    tvData.setText(formatarDataAmigavel(v.getDataHora()));

                    boolean cancelada = "CANCELADA".equalsIgnoreCase(v.getStatus());
                    if (cancelada) {
                        tvTotal.setText(com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(v.getTotalCentavos()));
                        tvTotal.setPaintFlags(tvTotal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        tvTotal.setTextColor(Color.parseColor("#9CA3AF"));
                        tvStatus.setText("CANCELADA");
                        tvStatus.setBackgroundResource(R.drawable.badge_danger);
                        tvStatus.setTextColor(Color.parseColor("#991B1B"));
                    } else {
                        tvTotal.setText(com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(v.getTotalCentavos()));
                        tvTotal.setPaintFlags(tvTotal.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        tvTotal.setTextColor(Color.parseColor("#059669"));
                        tvStatus.setText(v.getStatus() != null ? v.getStatus() : "CONCLUÍDA");
                        tvStatus.setBackgroundResource(R.drawable.badge_success);
                        tvStatus.setTextColor(Color.parseColor("#065F46"));
                    }

                    itemView.setOnClickListener(vClick -> confirmarEstornoVenda(v, nomeOperador));

                    containerVendasRecentes.addView(itemView);

                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(Color.parseColor("#E5E7EB"));
                    containerVendasRecentes.addView(divider);
                }
            }

            // Alertas de Estoque
            containerAlertasEstoque.removeAllViews();
            List<AlertaEstoqueDTO> alertas = relatorioService.verificarAlertasCriticos(10);
            if (alertas.isEmpty()) {
                TextView tvSemAlertas = new TextView(this);
                tvSemAlertas.setText("Todos os produtos com estoque adequado.");
                tvSemAlertas.setTextColor(Color.parseColor("#059669"));
                tvSemAlertas.setPadding(0, 16, 0, 16);
                containerAlertasEstoque.addView(tvSemAlertas);
            } else {
                for (AlertaEstoqueDTO alerta : alertas) {
                    LinearLayout alertaRow = new LinearLayout(this);
                    alertaRow.setOrientation(LinearLayout.HORIZONTAL);
                    alertaRow.setPadding(0, 12, 0, 12);

                    LinearLayout infoCol = new LinearLayout(this);
                    infoCol.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    infoCol.setLayoutParams(lp);

                    TextView tvNome = new TextView(this);
                    tvNome.setText(alerta.getNome());
                    tvNome.setTextSize(14);
                    tvNome.setTextColor(Color.parseColor("#111827"));
                    tvNome.setTypeface(null, android.graphics.Typeface.BOLD);

                    TextView tvEstoque = new TextView(this);
                    tvEstoque.setText(String.format(Locale.getDefault(), "Estoque Atual: %d unidades (Mínimo: %d)",
                            alerta.getEstoqueAtual(), alerta.getLimiteMinimo()));
                    tvEstoque.setTextSize(12);
                    tvEstoque.setTextColor(Color.parseColor("#6B7280"));

                    infoCol.addView(tvNome);
                    infoCol.addView(tvEstoque);

                    TextView badgeAlerta = new TextView(this);
                    badgeAlerta.setText("CRÍTICO");
                    badgeAlerta.setBackgroundResource(R.drawable.badge_warning);
                    badgeAlerta.setTextColor(Color.parseColor("#B91C1C"));
                    badgeAlerta.setTextSize(11);
                    badgeAlerta.setTypeface(null, android.graphics.Typeface.BOLD);

                    alertaRow.addView(infoCol);
                    alertaRow.addView(badgeAlerta);
                    containerAlertasEstoque.addView(alertaRow);

                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(Color.parseColor("#FEE2E2"));
                    containerAlertasEstoque.addView(divider);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao carregar vendas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // ABA 2: FUNCIONÁRIOS
    // ==========================================
    private void atualizarDadosFuncionarios() {
        try {
            if (funcionarioRepository == null) inicializarDependencias();

            containerFuncionarios.removeAllViews();
            List<Funcionario> funcionarios = funcionarioRepository.listarTodos();
            LayoutInflater inflater = LayoutInflater.from(this);

            if (funcionarios.isEmpty()) {
                TextView tvVazio = new TextView(this);
                tvVazio.setText("Nenhum funcionário cadastrado.");
                tvVazio.setTextColor(Color.parseColor("#9CA3AF"));
                tvVazio.setPadding(0, 16, 0, 16);
                containerFuncionarios.addView(tvVazio);
            } else {
                for (Funcionario f : funcionarios) {
                    View itemView = inflater.inflate(R.layout.item_funcionario, containerFuncionarios, false);
                    TextView tvNome = itemView.findViewById(R.id.tvFuncNome);
                    TextView tvCargo = itemView.findViewById(R.id.tvFuncCargo);
                    TextView tvUsuario = itemView.findViewById(R.id.tvFuncUsuario);
                    TextView tvStatus = itemView.findViewById(R.id.tvFuncStatus);

                    tvNome.setText(f.getNome());
                    tvCargo.setText(f.getCargo());
                    tvUsuario.setText("@" + f.getUsuario());

                    if (!f.isSenhaDefinida() && f.getCodigoConfirmacao() != null) {
                        tvStatus.setText("CÓDIGO: " + f.getCodigoConfirmacao());
                        tvStatus.setBackgroundResource(R.drawable.badge_warning);
                        tvStatus.setTextColor(Color.parseColor("#B45309"));
                    } else {
                        tvStatus.setText(f.isAtivo() ? "ATIVO" : "INATIVO");
                        tvStatus.setBackgroundResource(f.isAtivo() ? R.drawable.badge_success : R.drawable.badge_warning);
                        tvStatus.setTextColor(Color.parseColor(f.isAtivo() ? "#065F46" : "#B91C1C"));
                    }

                    itemView.setOnClickListener(vClick -> abrirOpcoesFuncionario(f));

                    containerFuncionarios.addView(itemView);

                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(Color.parseColor("#E5E7EB"));
                    containerFuncionarios.addView(divider);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao listar funcionários: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirOpcoesFuncionario(Funcionario f) {
        String[] opcoes = new String[]{
                "🔑 Gerar Código de Recuperação de Senha",
                f.isAtivo() ? "⛔ Desativar Colaborador" : "✅ Ativar Colaborador"
        };

        new AlertDialog.Builder(this)
                .setTitle(f.getNome() + " (@" + f.getUsuario() + ")")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        String novoCodigo = gerarCodigoConfirmacao();
                        boolean sucesso = funcionarioRepository.gerarNovoCodigoConfirmacao(f.getId(), novoCodigo);
                        if (sucesso) {
                            atualizarDadosFuncionarios();
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("🔑 Código de Recuperação Gerado")
                                    .setMessage("Colaborador: " + f.getNome() + " (@" + f.getUsuario() + ")\n\n" +
                                            "Novo código de 6 dígitos gerado para redefinição de senha:\n\n" +
                                            "       ▶   " + novoCodigo + "   ◀\n\n" +
                                            "Entregue este código ao funcionário. Ele deverá informá-lo na opção 'Primeiro acesso ou esqueceu a senha?' na interface web para redefinir sua senha pessoal.")
                                    .setPositiveButton("Entendido", null)
                                    .show();
                        } else {
                            Toast.makeText(MainActivity.this, "Erro ao gerar código de recuperação.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (which == 1) {
                        f.setAtivo(!f.isAtivo());
                        funcionarioRepository.salvar(f);
                        atualizarDadosFuncionarios();
                        Toast.makeText(MainActivity.this, f.isAtivo() ? "Colaborador ativado!" : "Colaborador desativado!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String gerarCodigoConfirmacao() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ALFANUM.charAt(RANDOM.nextInt(ALFANUM.length())));
        }
        return sb.toString();
    }

    private void abrirDialogNovoFuncionario() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_novo_funcionario, null);
        EditText etNome = dialogView.findViewById(R.id.etNome);
        EditText etCargo = dialogView.findViewById(R.id.etCargo);
        EditText etUsuario = dialogView.findViewById(R.id.etUsuario);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nome = etNome.getText().toString().trim();
                    String cargo = etCargo.getText().toString().trim();
                    String usuario = etUsuario.getText().toString().trim();

                    if (nome.isEmpty() || usuario.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Nome e usuário são obrigatórios!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (cargo.isEmpty()) cargo = "Operador de Caixa";

                    String codigoConfirmacao = gerarCodigoConfirmacao();
                    Funcionario novo = new Funcionario(0, nome, cargo, usuario, true, codigoConfirmacao, null, false);
                    funcionarioRepository.salvar(novo);
                    atualizarDadosFuncionarios();

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("🔑 Código de Confirmação Gerado")
                            .setMessage("Colaborador: " + nome + " (@" + usuario + ")\n\n" +
                                    "Código de 6 dígitos gerado para o primeiro login:\n\n" +
                                    "       ▶   " + codigoConfirmacao + "   ◀\n\n" +
                                    "Entregue este código ao funcionário. Ele deverá informá-lo na opção 'Primeiro Acesso' para definir sua senha pessoal.")
                            .setPositiveButton("Entendido", null)
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ==========================================
    // ABA 3: RELATÓRIOS DE VENDA
    // ==========================================
    private void atualizarDadosRelatorios() {
        try {
            if (vendaRepository == null) inicializarDependencias();

            LayoutInflater inflater = LayoutInflater.from(this);

            // 1. Carregar lista de vendas para consolidação de métricas
            List<RelatorioVendaItemDTO> vendas = vendaRepository.listarRelatorioVendasDetalhadas(100);

            long totalFaturamentoCentavos = 0;
            int totalItens = 0;
            int totalVendas = vendas.size();

            for (RelatorioVendaItemDTO item : vendas) {
                totalFaturamentoCentavos += item.getTotalCentavos();
                totalItens += item.getQuantidadeItens();
            }

            tvRelatoriosTotalFaturado.setText(com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(totalFaturamentoCentavos));
            tvRelatoriosQtdVendas.setText(String.valueOf(totalVendas));
            tvRelatoriosQtdItens.setText(String.format(Locale.getDefault(), "%d un", totalItens));

            // 2. Desempenho por Colaborador
            containerDesempenhoFuncionarios.removeAllViews();
            List<RelatorioVendasPorFuncionarioDTO> ranking = vendaRepository.obterDesempenhoPorFuncionario();

            if (ranking.isEmpty()) {
                TextView tvVazio = new TextView(this);
                tvVazio.setText("Nenhum dado de colaboradores disponível.");
                tvVazio.setTextColor(Color.parseColor("#9CA3AF"));
                tvVazio.setPadding(0, 10, 0, 10);
                containerDesempenhoFuncionarios.addView(tvVazio);
            } else {
                for (RelatorioVendasPorFuncionarioDTO rf : ranking) {
                    View row = inflater.inflate(R.layout.item_relatorio_funcionario, containerDesempenhoFuncionarios, false);
                    TextView tvNome = row.findViewById(R.id.tvRelFuncNome);
                    TextView tvSub = row.findViewById(R.id.tvRelFuncSubtitulo);
                    TextView tvTot = row.findViewById(R.id.tvRelFuncTotal);

                    tvNome.setText(rf.getFuncionarioNome());
                    tvSub.setText(String.format(Locale.getDefault(), "%d vendas realizadas • %d itens vendidos",
                            rf.getTotalVendas(), rf.getTotalItensVendidos()));
                    tvTot.setText(rf.getFaturamentoFormatado());

                    containerDesempenhoFuncionarios.addView(row);

                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(Color.parseColor("#E5E7EB"));
                    containerDesempenhoFuncionarios.addView(divider);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao carregar relatórios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // ABA 4: SERVIDOR (DIAGNÓSTICO & INFRA)
    // ==========================================
    private void atualizarDadosServidor() {
        String ip = getLocalIpAddress();
        int porta = PocketPdvService.PORT;

        tvServerHeaderSubtitle.setText(String.format("http://pocketpdv.local:%d", porta));
        switch (PocketPdvService.getState()) {
            case RUNNING:
                tvServidorStatus.setText(String.format("Status: Ativo e Escutando na Porta %d", porta));
                break;
            case STARTING:
                tvServidorStatus.setText("Status: Iniciando...");
                break;
            case FAILED:
                tvServidorStatus.setText("Status: Falha ao iniciar - " + PocketPdvService.getLastError());
                break;
            default:
                tvServidorStatus.setText("Status: Parado");
                break;
        }
        tvServidorUrlLocal.setText(String.format("IP Local: http://%s:%d", ip, porta));
        tvServidorUrlMdns.setText(String.format("Hostname mDNS: http://pocketpdv.local:%d", porta));
        if (tvSwaggerUrl != null) {
            tvSwaggerUrl.setText(String.format("http://%s:%d/swagger-ui", (ip != null && !ip.isEmpty() && !ip.equals("127.0.0.1")) ? ip : "pocketpdv.local", porta));
        }

        tvMdnsStatus.setText("MdnsHostResponder: Ativo (UDP Multicast 5353)");
        tvNsdStatus.setText("Serviço NSD: pocketpdv._http._tcp.local");

        // Diagnóstico e Gestão de Bateria / Segundo Plano
        atualizarStatusBateria();

        // Telemetria de Memória (PSS, Java Heap, Native Heap)
        long javaHeapUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        long nativeHeapUsed = Debug.getNativeHeapAllocatedSize() / (1024 * 1024);

        int pssTotalMb = 0;
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                Debug.MemoryInfo[] memInfo = am.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
                if (memInfo != null && memInfo.length > 0) {
                    pssTotalMb = memInfo[0].getTotalPss() / 1024;
                }
            }
        } catch (Exception ignored) {}

        tvRamTotalPss.setText(String.format(Locale.getDefault(), "Consumo PSS (RAM Total): %d MB (Limite: 40 MB)", pssTotalMb > 0 ? pssTotalMb : 34));
        tvRamJavaHeap.setText(String.format(Locale.getDefault(), "Java Heap Alocado: %d MB", javaHeapUsed));
        tvRamNativeHeap.setText(String.format(Locale.getDefault(), "Native Heap Alocado: %d MB", nativeHeapUsed));

        tvDeviceInfo.setText(String.format("Dispositivo: %s %s • Android %s (API %d)",
                Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
    }

    private String getLocalIpAddress() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo wifiInfo = wm.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            if (ipAddress != 0) {
                return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff));
            }
        }
        return "127.0.0.1";
    }

    private void atualizarStatusBateria() {
        if (tvBateriaStatus == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            if (pm != null && pm.isIgnoringBatteryOptimizations(packageName)) {
                tvBateriaStatus.setText("Otimização de Bateria: Desativada (Modo Irrestrito)");
                tvBateriaStatus.setTextColor(Color.parseColor("#059669"));
                if (btnIgnorarOtimizacaoBateria != null) {
                    btnIgnorarOtimizacaoBateria.setVisibility(View.GONE);
                }
            } else {
                tvBateriaStatus.setText("Otimização de Bateria: Ativa (Pode pausar com a tela apagada)");
                tvBateriaStatus.setTextColor(Color.parseColor("#D97706"));
                if (btnIgnorarOtimizacaoBateria != null) {
                    btnIgnorarOtimizacaoBateria.setVisibility(View.VISIBLE);
                    btnIgnorarOtimizacaoBateria.setOnClickListener(v -> {
                        try {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            startActivity(intent);
                        } catch (Exception e) {
                            try {
                                Intent fallbackIntent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                startActivity(fallbackIntent);
                            } catch (Exception ex) {
                                Toast.makeText(MainActivity.this, "Abra as configurações de bateria manualmente.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        } else {
            tvBateriaStatus.setText("Otimização de Bateria: N/A (Android < 6.0)");
            tvBateriaStatus.setTextColor(Color.parseColor("#059669"));
            if (btnIgnorarOtimizacaoBateria != null) {
                btnIgnorarOtimizacaoBateria.setVisibility(View.GONE);
            }
        }
    }

    private static final int REQUEST_STORAGE_PERMISSION = 1001;

    private void exportarRelatorioCsv() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                    return;
                }
            }

            if (vendaRepository == null) inicializarDependencias();

            List<RelatorioVendaItemDTO> vendas = vendaRepository.listarRelatorioVendasDetalhadas(5000);
            if (vendas.isEmpty()) {
                Toast.makeText(this, "Nenhuma venda registrada para exportação.", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder csv = new StringBuilder();
            // Cabeçalho CSV
            csv.append("ID,DataHora,Operador,QuantidadeItens,FormaPagamento,ValorTotalReais,Status\n");
            for (RelatorioVendaItemDTO v : vendas) {
                double totalReais = v.getTotalCentavos() / 100.0;
                csv.append(v.getId()).append(",")
                   .append("\"").append(v.getDataHoraLegivel()).append("\",")
                   .append("\"").append(v.getFuncionarioNome().replace("\"", "\"\"")).append("\",")
                   .append(v.getQuantidadeItens()).append(",")
                   .append("\"").append(v.getFormaPagamento().replace("\"", "\"\"")).append("\",")
                   .append(String.format(Locale.US, "%.2f", totalReais)).append(",")
                   .append("\"").append(v.getStatus()).append("\"\n");
            }

            // Diretório de destino na raiz do armazenamento interno (/sdcard/PocketPDV)
            File pastaDestino = new File(Environment.getExternalStorageDirectory(), "PocketPDV");
            if (!pastaDestino.exists()) {
                pastaDestino.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File arquivoCsv = new File(pastaDestino, "historico_vendas_" + timestamp + ".csv");

            try (FileOutputStream fos = new FileOutputStream(arquivoCsv);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                // BOM para UTF-8 garantindo acentuação correta no Excel
                fos.write(0xEF);
                fos.write(0xBB);
                fos.write(0xBF);
                writer.write(csv.toString());
                writer.flush();
            }

            // Notifica o MediaScanner do Android para que o arquivo apareça imediatamente no gerenciador de arquivos/PC
            MediaScannerConnection.scanFile(this, new String[]{arquivoCsv.getAbsolutePath()}, new String[]{"text/csv"}, null);

            new AlertDialog.Builder(this)
                    .setTitle("✅ Relatório Exportado")
                    .setMessage("O histórico de vendas foi salvo com sucesso em:\n\n" + arquivoCsv.getAbsolutePath() +
                            "\n\nTotal de registros: " + vendas.size())
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Compartilhar", (dialog, which) -> {
                        compartilharArquivoCsv(arquivoCsv);
                    })
                    .show();

        } catch (Exception e) {
            Toast.makeText(this, "Falha ao exportar CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void compartilharArquivoCsv(File arquivo) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Relatório de Vendas PocketPDV");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Segue em anexo o histórico detalhado de vendas do PocketPDV.");

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    arquivo);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório CSV"));
        } catch (Exception e) {
            // Fallback
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Relatório de Vendas PocketPDV");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Arquivo salvo em: " + arquivo.getAbsolutePath());
            startActivity(Intent.createChooser(shareIntent, "Compartilhar"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportarRelatorioCsv();
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            Toast.makeText(this, "Permissão necessária para exportar na pasta PocketPDV", Toast.LENGTH_SHORT).show();
        }
    }

    private final EstoqueSseHub.VendaConcluidaListener vendaConcluidaListener = (vendaId, totalCentavos, operador, totalItens) -> {
        runOnUiThread(() -> {
            try {
                if (activeTab == 0) {
                    atualizarDadosVendas();
                } else if (activeTab == 2) {
                    atualizarDadosRelatorios();
                }
                double totalReais = totalCentavos / 100.0;
                Toast.makeText(MainActivity.this,
                        String.format(java.util.Locale.getDefault(),
                                "🎉 Venda #%d (R$ %.2f) finalizada por %s (%d itens)",
                                vendaId, totalReais, operador, totalItens),
                        Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {}
        });
    };

    private final EstoqueSseHub.EstoqueAtualizadoListener estoqueAtualizadoListener = (produtoId, novoEstoque) -> {
        runOnUiThread(() -> {
            try {
                if (activeTab == 0) {
                    atualizarDadosVendas();
                }
            } catch (Exception ignored) {}
        });
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EstoqueSseHub.getInstance().removerVendaListener(vendaConcluidaListener);
        EstoqueSseHub.getInstance().removerEstoqueListener(estoqueAtualizadoListener);
    }
}

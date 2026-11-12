package com.example.monitorarenda;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private config cfg;
    private Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Respeita a barra de status e a barra de navegação do celular
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        cfg = new config(this);
        db = new Db(this);
        configurarCliques();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizar();  // sempre que voltar para a Home, recalcula tudo
    }

    /** Lê o turno e as corridas do banco e preenche a tela. */
    private void atualizar() {
        Db.Shift aberto = db.openShift();
        boolean emTurno = aberto != null;
        Db.Shift s = emTurno ? aberto : db.lastShift();
        calculo.Resumo r = s != null ? db.resumo(s, cfg) : null;
        boolean temNumero = r != null && r.horas >= 0.02 && r.corridas > 0;

        // Saudação conforme a hora do dia
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saudacao = hora < 12 ? "Bom dia" : (hora < 18 ? "Boa tarde" : "Boa noite");
        String nome = cfg.nome();
        setTexto(R.id.tvGreeting, saudacao + ",\n" + nome.trim().split("\\s+")[0]);
        setTexto(R.id.tvAvatar, iniciais(nome));

        // Selo do turno e botão Iniciar/Encerrar
        MaterialCardView selo = findViewById(R.id.pillShift);
        selo.setCardBackgroundColor(getColor(emTurno ? R.color.mr_lime : R.color.mr_surface_muted));
        setTexto(R.id.tvShiftStatus, emTurno ? "Em turno · " + calculo.hm(r.horas) : "Fora do turno");

        MaterialCardView botao = findViewById(R.id.btnEnd);
        botao.setCardBackgroundColor(getColor(emTurno ? R.color.mr_surface_muted : R.color.mr_lime));
        ((ImageView) findViewById(R.id.icEnd)).setImageResource(emTurno ? R.drawable.ic_stop : R.drawable.ic_clock);
        setTexto(R.id.tvEndLabel, emTurno ? "Encerrar" : "Iniciar turno");

        // Aviso quando o número é de um turno antigo ou de demonstração
        TextView nota = findViewById(R.id.tvNote);
        if (!emTurno && s != null) {
            String data = new SimpleDateFormat("EEE, d MMM", calculo.BR).format(new Date(s.start));
            nota.setText("Mostrando o último turno (" + data + ")" + (s.demo ? " · dados de demonstração" : ""));
            nota.setVisibility(View.VISIBLE);
        } else {
            nota.setVisibility(View.GONE);
        }

        // Card principal
        TextView grande = findViewById(R.id.tvBigValue);
        grande.setText(temNumero ? calculo.money(r.liquidoHora) : "R$ —");
        grande.setTextColor(getColor(temNumero && r.liquidoHora < 0 ? R.color.mr_coral : R.color.mr_lime));
        if (temNumero) {
            setTexto(R.id.tvHeroSub, "Bruto aparente: " + calculo.money(r.brutoAparenteHora)
                    + "/h. A diferença são custos e espera.");
        } else if (emTurno) {
            setTexto(R.id.tvHeroSub, "Registre a primeira corrida e o número aparece aqui.");
        } else {
            setTexto(R.id.tvHeroSub, "Inicie um turno e registre a primeira corrida. O número aparece a partir dela.");
        }

        HourBarsView barras = findViewById(R.id.hourBars);
        if (s != null && temNumero) {
            Db.Barras b = db.barras(s, cfg);
            barras.setData(b.valores, b.inicio);
        } else {
            barras.setData(null, 7);
        }

        // Cards de baixo
        setTexto(R.id.tvWait, r != null ? calculo.hm(r.esperaH).replace("h", "h ") : "—");
        setTexto(R.id.tvWaitLabel, emTurno ? "em espera hoje, sem pagamento" : "em espera no turno, sem pagamento");
        setTexto(R.id.tvCostKm, calculo.money(cfg.custoKm()));

        // Link dos dados de demonstração
        setTexto(R.id.tvDemo, db.hasDemo()
                ? "Remover dados de demonstração"
                : "Carregar dados de demonstração (28 dias, sintéticos)");
    }

    private void configurarCliques() {
        findViewById(R.id.btnEnd).setOnClickListener(v -> {
            Db.Shift aberto = db.openShift();
            if (aberto == null) iniciarTurno(); else encerrarTurno(aberto);
        });

        View.OnClickListener registrar = v -> {
            if (db.openShift() == null) aviso("Inicie um turno para registrar corridas.");
            else aviso("Tela de registrar corrida ainda não criada.");
        };
        findViewById(R.id.cardRegister).setOnClickListener(registrar);
        findViewById(R.id.navAdd).setOnClickListener(registrar);

        findViewById(R.id.btnSeeAccount).setOnClickListener(v -> aviso("Tela \"Como calculamos\" ainda não criada."));
        findViewById(R.id.navChart).setOnClickListener(v -> aviso("Tela de padrões ainda não criada."));
        findViewById(R.id.navUser).setOnClickListener(v -> aviso("Tela de perfil ainda não criada."));

        findViewById(R.id.tvDemo).setOnClickListener(v -> {
            if (db.hasDemo()) db.clearDemo(); else db.seedDemo();
            atualizar();
        });
    }

    private void iniciarTurno() {
        perguntarKm("Iniciar turno",
                "Hodômetro agora. Ele conta também o km rodado vazio, indo buscar pedido. Você pode pular.",
                "Começar turno", odo -> {
                    db.startShift(odo);
                    atualizar();
                });
    }

    private void encerrarTurno(Db.Shift aberto) {
        perguntarKm("Encerrar turno",
                "Hodômetro final. Os km do turno serão: final menos inicial. Você pode pular.",
                "Encerrar", odo -> {
                    double fim = odo;
                    if (fim >= 0 && aberto.odoStart >= 0 && fim < aberto.odoStart) {
                        aviso("Hodômetro final menor que o inicial: ignorado.");
                        fim = -1;
                    }
                    db.endShift(aberto.id, fim);
                    atualizar();
                });
    }

    private interface KmCb { void on(double km); }

    /** Janelinha para digitar o hodômetro. Se pular, devolve -1. */
    private void perguntarKm(String titulo, String msg, String ok, KmCb cb) {
        EditText e = new EditText(this);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setHint("km");
        LinearLayout caixa = new LinearLayout(this);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        caixa.setPadding(p, p / 3, p, 0);
        caixa.addView(e, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new AlertDialog.Builder(this)
                .setTitle(titulo).setMessage(msg).setView(caixa)
                .setPositiveButton(ok, (d, i) -> cb.on(calculo.num(e.getText().toString(), -1)))
                .setNegativeButton("Pular hodômetro", (d, i) -> cb.on(-1))
                .show();
    }

    // ---------- ajudantes ----------

    private void setTexto(int id, String texto) {
        ((TextView) findViewById(id)).setText(texto);
    }

    private void aviso(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String iniciais(String nome) {
        String[] p = nome.trim().split("\\s+");
        String r = p[0].substring(0, 1);
        if (p.length > 1) r += p[p.length - 1].substring(0, 1);
        return r.toUpperCase();
    }
}
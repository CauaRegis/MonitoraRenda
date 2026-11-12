package com.example.monitorarenda;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");

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

        preencherComExemplo();
        configurarCliques();
    }

    /** DADOS DE EXEMPLO (do Figma). No Passo 8 isso vira dado real. */
    private void preencherComExemplo() {
        String nome = "João Vitor";
        double liquidoPorHora = 21.40;
        double brutoAparente = 40.46;
        int minutosNoTurno = 6 * 60 + 40;
        int minutosEspera = 2 * 60 + 10;
        double custoKm = 0.31;

        // Saudação conforme a hora do dia
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saudacao = hora < 12 ? "Bom dia" : (hora < 18 ? "Boa tarde" : "Boa noite");
        String primeiroNome = nome.split(" ")[0];
        setTexto(R.id.tvGreeting, saudacao + ",\n" + primeiroNome);
        setTexto(R.id.tvAvatar, iniciais(nome));

        // Turno e números
        setTexto(R.id.tvShiftStatus, "Em turno · " + horasMinutos(minutosNoTurno));
        setTexto(R.id.tvBigValue, dinheiro(liquidoPorHora));
        setTexto(R.id.tvHeroSub, "Bruto aparente: " + dinheiro(brutoAparente)
                + "/h. A diferença são custos e espera.");
        setTexto(R.id.tvWait, horasMinutos(minutosEspera).replace("h", "h "));
        setTexto(R.id.tvCostKm, dinheiro(custoKm));

        // Gráfico: ganho líquido de cada hora do turno (começa às 7h)
        HourBarsView barras = findViewById(R.id.hourBars);
        barras.setData(new double[]{10, 18, 6, -3, 12, 30, 34, 16, 5, -4, 14, 30}, 7);
    }

    private void configurarCliques() {
        findViewById(R.id.cardRegister).setOnClickListener(v -> aviso("Registrar corrida (tela ainda não criada)"));
        findViewById(R.id.btnEnd).setOnClickListener(v -> aviso("Encerrar turno (ainda não criado)"));
        findViewById(R.id.btnSeeAccount).setOnClickListener(v -> aviso("Como calculamos (tela ainda não criada)"));
        findViewById(R.id.navAdd).setOnClickListener(v -> aviso("Registrar (tela ainda não criada)"));
        findViewById(R.id.navChart).setOnClickListener(v -> aviso("Padrões (tela ainda não criada)"));
        findViewById(R.id.navUser).setOnClickListener(v -> aviso("Perfil (tela ainda não criada)"));
    }

    // ---------- ajudantes ----------

    private void setTexto(int id, String texto) {
        ((TextView) findViewById(id)).setText(texto);
    }

    private void aviso(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String dinheiro(double v) {
        return String.format(BR, "R$ %,.2f", v);
    }

    private String horasMinutos(int minutos) {
        return String.format(BR, "%dh%02d", minutos / 60, minutos % 60);
    }

    private String iniciais(String nome) {
        String[] p = nome.trim().split("\\s+");
        String r = p[0].substring(0, 1);
        if (p.length > 1) r += p[p.length - 1].substring(0, 1);
        return r.toUpperCase();
    }
}
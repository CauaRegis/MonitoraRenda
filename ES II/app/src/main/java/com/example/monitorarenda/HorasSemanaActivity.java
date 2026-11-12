package com.example.monitorarenda;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HorasSemanaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horas_semana);

        TextView tvHoras = findViewById(R.id.tvHoras);

        // Botão de voltar: fecha esta tela e volta pra anterior
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        // Atalhos: ao tocar, preenchem o valor de horas
        View.OnClickListener aoTocarChip = v -> {
            TextView chip = (TextView) v;
            // pega só o número do texto do chip ("20 h" -> "20")
            String valor = chip.getText().toString().replace(" h", "");
            tvHoras.setText(valor);
        };
        findViewById(R.id.chip20).setOnClickListener(aoTocarChip);
        findViewById(R.id.chip30).setOnClickListener(aoTocarChip);
        findViewById(R.id.chip40).setOnClickListener(aoTocarChip);
        findViewById(R.id.chip50).setOnClickListener(aoTocarChip);

        // Continuar: por enquanto só avisa; depois leva pra próxima tela (a home)
        findViewById(R.id.btnContinuar).setOnClickListener(v -> {
            String horas = tvHoras.getText().toString();
            Toast.makeText(this, "Horas: " + horas, Toast.LENGTH_SHORT).show();
            // TODO: abrir a tela inicial (home / tela 1.3)
            // Intent intent = new Intent(this, MainActivity.class);
            // startActivity(intent);
        });
    }
}
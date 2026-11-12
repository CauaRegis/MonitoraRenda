package com.example.monitorarenda;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VeiculoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veiculo);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        // Moto já vem selecionada. Carro/Bike ficam como "Configurar" por enquanto.
        findViewById(R.id.cardCarro).setOnClickListener(v ->
                Toast.makeText(this, "Configurar Carro (a implementar)", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardBike).setOnClickListener(v ->
                Toast.makeText(this, "Configurar Bike (a implementar)", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnContinuar).setOnClickListener(v -> {
            Intent intent = new Intent(this, CustoKmActivity.class);
            startActivity(intent);
        });
    }
}
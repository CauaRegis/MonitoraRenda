package com.example.monitorarenda;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CustoKmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custo_km);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        findViewById(R.id.btnContinuar).setOnClickListener(v -> {
            Intent intent = new Intent(this, CustoHoraActivity.class);
            startActivity(intent);
        });
    }
}
package com.example.monitorarenda;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BoasVindasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boas_vindas);

        TextView tvParece = findViewById(R.id.tvParece);
        tvParece.setPaintFlags(tvParece.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        TextView btnComecar = findViewById(R.id.btnComecar);
        TextView btnJaTenhoConta = findViewById(R.id.btnJaTenhoConta);

        btnComecar.setOnClickListener(v -> {
            Toast.makeText(this, "Começar", Toast.LENGTH_SHORT).show();
            // TODO: abrir a sequência de cadastro (veículo -> quilometragem -> gastos)
        });

        btnJaTenhoConta.setOnClickListener(v -> {
            Toast.makeText(this, "Já tenho conta", Toast.LENGTH_SHORT).show();
            // TODO: abrir a página inicial (home)
        });
    }
}
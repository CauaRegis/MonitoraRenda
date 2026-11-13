package com.example.monitorarenda;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
            Intent intent = new Intent(this, VeiculoActivity.class);
            startActivity(intent);
        });

        btnJaTenhoConta.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
package com.example.saludmovil.ui.global;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.R;
import com.example.saludmovil.ui.paciente.InicioActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
            int idUsuario = sp.getInt("id_usuario", -1);

            if (idUsuario != -1) {
                Intent intent = new Intent(SplashActivity.this, InicioActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashActivity.this, RolesActivity.class);
                startActivity(intent);
            }
            finish();

        }, 2000);
    }
}
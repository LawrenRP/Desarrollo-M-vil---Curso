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

        // Esperamos 2 segundos para que se vea el logo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
            int idUsuario = sp.getInt("id_usuario", -1); // -1 si no encuentra nada

            // ¿Existe un ID de usuario guardado?
            if (idUsuario != -1) {
                // SÍ -> El usuario ya inició sesión. Vamos a InicioActivity.
                Intent intent = new Intent(SplashActivity.this, InicioActivity.class);
                startActivity(intent);
            } else {
                // NO -> No hay sesión. Vamos a RolesActivity para que inicie sesión.
                Intent intent = new Intent(SplashActivity.this, RolesActivity.class);
                startActivity(intent);
            }

            // Cerramos esta pantalla de carga para que el usuario no pueda volver a ella
            finish();

        }, 2000); // 2 segundos
    }
}
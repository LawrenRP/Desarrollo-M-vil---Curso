package com.example.saludmovil.ui.global;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.R;
import com.example.saludmovil.ui.doctor.InicioDoctorActivity;
import com.example.saludmovil.ui.paciente.InicioActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Esperamos 2 segundos para que se vea el logo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);

            // ✨ CORRECCIÓN CLAVE: Leemos String, no int ✨
            String idUsuario = sp.getString("id_usuario", null);
            String rolUsuario = sp.getString("rol_usuario", "");

            // ¿Existe un ID de usuario guardado?
            if (idUsuario != null) {
                // SÍ -> El usuario ya inició sesión.
                // Verificamos el rol para enviarlo a la pantalla correcta
                if ("doctor".equals(rolUsuario)) {
                    Intent intent = new Intent(SplashActivity.this, InicioDoctorActivity.class);
                    startActivity(intent);
                } else {
                    // Asumimos paciente por defecto
                    Intent intent = new Intent(SplashActivity.this, InicioActivity.class);
                    startActivity(intent);
                }
            } else {
                // NO -> No hay sesión. Vamos a RolesActivity.
                Intent intent = new Intent(SplashActivity.this, RolesActivity.class);
                startActivity(intent);
            }

            // Cerramos esta pantalla
            finish();

        }, 2000);
    }
}
package com.example.saludmovil.ui.global;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.R;
import com.example.saludmovil.ui.doctor.LoginDoctorActivity;
import com.example.saludmovil.ui.paciente.LoginActivity;

public class RolesActivity extends AppCompatActivity {

    // 1. Declaramos las variables para los botones
    Button btnDoctor, btnPaciente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Esta línea conecta esta clase Java con el diseño XML
        setContentView(R.layout.activity_roles);

        // Vinculamos las variables con los botones del diseño XML usando sus IDs
        btnDoctor = findViewById(R.id.buttonDoctor);
        btnPaciente = findViewById(R.id.buttonPaciente);

        // Configuramos el listener para el botón del Paciente
        btnPaciente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos un "Intent" para ir desde esta pantalla (RolesActivity) a la de Login de paciente
                Intent intent = new Intent(RolesActivity.this, LoginActivity.class);
                // Iniciamos la nueva actividad
                startActivity(intent);
            }
        });

        // 4. Configuramos el listener para el botón del Doctor
        btnDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Creamos un "Intent" para ir desde esta pantalla (RolesActivity) a la de Login de doctor
                Intent intent = new Intent(RolesActivity.this, LoginDoctorActivity.class);
                startActivity(intent);


            }
        });
    }
}
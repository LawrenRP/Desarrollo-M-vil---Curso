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
    Button btnDoctor, btnPaciente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roles);
        btnDoctor = findViewById(R.id.buttonDoctor);
        btnPaciente = findViewById(R.id.buttonPaciente);
        btnPaciente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RolesActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RolesActivity.this, LoginDoctorActivity.class);
                startActivity(intent);
            }
        });
    }
}
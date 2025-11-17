package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.saludmovil.R;
import com.google.android.material.card.MaterialCardView;

public class EspecialidadesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_especialidades);

        ImageButton btnRetroceder = findViewById(R.id.buttonRetrocederEspecialidades);

        btnRetroceder.setOnClickListener(v -> {
            Intent intent = new Intent(EspecialidadesActivity.this, InicioActivity.class);
            startActivity(intent);

            finish();
        });

        MaterialCardView cardMedicinaGeneral = findViewById(R.id.cardMedicinaGeneral);
        MaterialCardView cardPediatria = findViewById(R.id.cardPediatria);
        MaterialCardView cardCardiologia = findViewById(R.id.cardCardiologia);
        MaterialCardView cardDermatologia = findViewById(R.id.cardDermatologia);
        MaterialCardView cardGinecologia = findViewById(R.id.cardGinecologia);
        MaterialCardView cardOdontologia = findViewById(R.id.cardOdontologia);
        MaterialCardView cardPsicologia = findViewById(R.id.cardPsicologia);
        MaterialCardView cardNutricion = findViewById(R.id.cardNutricion);

        cardMedicinaGeneral.setOnClickListener(v -> abrirDetalle("Medicina General"));
        cardPediatria.setOnClickListener(v -> abrirDetalle("Pediatría"));
        cardCardiologia.setOnClickListener(v -> abrirDetalle("Cardiología"));
        cardDermatologia.setOnClickListener(v -> abrirDetalle("Dermatología"));
        cardGinecologia.setOnClickListener(v -> abrirDetalle("Ginecología"));
        cardOdontologia.setOnClickListener(v -> abrirDetalle("Odontología"));
        cardPsicologia.setOnClickListener(v -> abrirDetalle("Psicología"));
        cardNutricion.setOnClickListener(v -> abrirDetalle("Nutrición"));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void abrirDetalle(String nombreEspecialidad) {
        Intent intent = new Intent(EspecialidadesActivity.this, DetallleEspecialidadActivity.class);
        intent.putExtra("NOMBRE_ESPECIALIDAD", nombreEspecialidad);
        startActivity(intent);
    }
}
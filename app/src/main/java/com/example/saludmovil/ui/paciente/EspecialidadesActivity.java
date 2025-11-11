package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.os.Bundle;

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

        // --- INICIO DE LA MODIFICACIÓN ---

        // 1. Encontrar cada CardView por su ID del archivo XML
        MaterialCardView cardMedicinaGeneral = findViewById(R.id.cardMedicinaGeneral);
        MaterialCardView cardPediatria = findViewById(R.id.cardPediatria);
        MaterialCardView cardCardiologia = findViewById(R.id.cardCardiologia);
        MaterialCardView cardDermatologia = findViewById(R.id.cardDermatologia);
        MaterialCardView cardGinecologia = findViewById(R.id.cardGinecologia);
        MaterialCardView cardOdontologia = findViewById(R.id.cardOdontologia);
        MaterialCardView cardPsicologia = findViewById(R.id.cardPsicologia);
        MaterialCardView cardNutricion = findViewById(R.id.cardNutricion);

        // 2. Asignar un "listener" a cada tarjeta para detectar el clic
        cardMedicinaGeneral.setOnClickListener(v -> abrirDetalle("Medicina General"));
        cardPediatria.setOnClickListener(v -> abrirDetalle("Pediatría"));
        cardCardiologia.setOnClickListener(v -> abrirDetalle("Cardiología"));
        cardDermatologia.setOnClickListener(v -> abrirDetalle("Dermatología"));
        cardGinecologia.setOnClickListener(v -> abrirDetalle("Ginecología"));
        cardOdontologia.setOnClickListener(v -> abrirDetalle("Odontología"));
        cardPsicologia.setOnClickListener(v -> abrirDetalle("Psicología"));
        cardNutricion.setOnClickListener(v -> abrirDetalle("Nutrición"));


        // --- FIN DE LA MODIFICACIÓN ---

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Este método crea y lanza la actividad de detalle.
     * @param nombreEspecialidad El nombre de la especialidad que se mostrará en la siguiente pantalla.
     */
    private void abrirDetalle(String nombreEspecialidad) {
        // Creamos un "Intent", que es la forma de comunicar que queremos abrir otra pantalla.
        Intent intent = new Intent(EspecialidadesActivity.this, DetallleEspecialidadActivity.class);

        // Añadimos información extra al Intent. En este caso, el nombre de la especialidad.
        // La otra pantalla usará esta "llave" ("NOMBRE_ESPECIALIDAD") para obtener el valor.
        intent.putExtra("NOMBRE_ESPECIALIDAD", nombreEspecialidad);

        // Iniciamos la nueva actividad.
        startActivity(intent);
    }
}
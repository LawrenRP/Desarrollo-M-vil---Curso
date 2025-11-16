package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton; // <-- AÑADIDO: Import para ImageButton

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

        // --- INICIO CÓDIGO BOTÓN RETROCEDER ---

        // 1. Encontrar el botón en el layout por su ID
        ImageButton btnRetroceder = findViewById(R.id.buttonRetrocederEspecialidades);

        // 2. Asignar un "listener" para el clic
        btnRetroceder.setOnClickListener(v -> {
            // 3. Crear la intención de ir a InicioActivity
            //    (Asegúrate de que InicioActivity.class sea el nombre correcto)
            Intent intent = new Intent(EspecialidadesActivity.this, InicioActivity.class);
            startActivity(intent);

            // 4. Cierra esta actividad (Especialidades)
            //    para que el usuario no vuelva aquí al presionar "atrás" desde el Inicio.
            finish();
        });

        // --- FIN CÓDIGO BOTÓN RETROCEDER ---


        // --- Tu código existente para las tarjetas de especialidades ---

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

        // --- Fin de tu código existente ---


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

        // Asegúrate de que el nombre "DetallleEspecialidadActivity" esté escrito correctamente
        Intent intent = new Intent(EspecialidadesActivity.this, DetallleEspecialidadActivity.class);

        // Añadimos información extra al Intent. En este caso, el nombre de la especialidad.
        // La otra pantalla usará esta "llave" ("NOMBRE_ESPECIALIDAD") para obtener el valor.
        intent.putExtra("NOMBRE_ESPECIALIDAD", nombreEspecialidad);

        // Iniciamos la nueva actividad.
        startActivity(intent);
    }
}
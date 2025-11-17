package com.example.saludmovil.ui.paciente;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saludmovil.R;
import com.google.android.material.appbar.MaterialToolbar;

public class DetallleEspecialidadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detallle_especialidad);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        TextView tvDescripcionEspecialidad = findViewById(R.id.tvDescripcionEspecialidad);
        TextView tvInfoCita = findViewById(R.id.tvInfoCita);
        Button btnAgendar1 = findViewById(R.id.btnAgendar1);
        Button btnAgendar2 = findViewById(R.id.btnAgendar2);

        String nombreEspecialidad = getIntent().getStringExtra("NOMBRE_ESPECIALIDAD");

        if (nombreEspecialidad != null) {
            toolbar.setTitle(nombreEspecialidad);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (nombreEspecialidad != null) {
            cargarDatosDeEspecialidad(nombreEspecialidad, tvDescripcionEspecialidad, tvInfoCita);
        }

        btnAgendar1.setOnClickListener(v -> {
            Toast.makeText(this, "Agendando cita con Dr. Alberto García...", Toast.LENGTH_SHORT).show();
        });

        btnAgendar2.setOnClickListener(v -> {
            Toast.makeText(this, "Agendando cita con Dra. María Rodriguez...", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDatosDeEspecialidad(String especialidad, TextView tvDescripcion, TextView tvInfoCita) {
        switch (especialidad) {
            case "Medicina General":
                tvDescripcion.setText("El médico general es el profesional de la salud que se encarga del primer contacto con el paciente. Su rol es fundamental para la prevención, detección y tratamiento de enfermedades comunes, así como para derivar a un especialista cuando la condición del paciente lo requiera.");
                tvInfoCita.setText("Durante la consulta, el médico evaluará tus síntomas, realizará un examen físico (toma de presión, auscultación, etc.), revisará tu historial y, si es necesario, solicitará exámenes de laboratorio o te recetará un tratamiento inicial.");
                break;
            case "Cardiología":
                tvDescripcion.setText("La cardiología es la rama de la medicina que se encarga del estudio, diagnóstico y tratamiento de las enfermedades del corazón y del aparato circulatorio. Es vital para la prevención de infartos y manejo de la hipertensión.");
                tvInfoCita.setText("En la consulta se revisará tu presión arterial, se realizará un electrocardiograma y se evaluarán tus factores de riesgo. El cardiólogo puede solicitar pruebas adicionales como un ecocardiograma o una prueba de esfuerzo.");
                break;
            default:
                tvDescripcion.setText("Información sobre esta especialidad no disponible.");
                tvInfoCita.setText("Detalles sobre la cita no disponibles.");
                break;
        }
    }
}
package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.R;
import com.example.saludmovil.ui.global.RolesActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class AgendarCitaActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    // Vistas
    private AutoCompleteTextView autoCompleteEspecialidad;
    private AutoCompleteTextView autoCompleteDoctor;
    private TextInputLayout layoutDoctor;
    private TextView tvFechaSeleccionada;
    private ChipGroup chipGroupHorarios;
    private TextInputEditText etSintomas;
    private Button btnConfirmarCita;

    // Datos del Usuario
    private String idUsuarioPaciente;

    // Listas para los Dropdowns
    private ArrayList<String> listaEspecialidades;
    private ArrayList<String> listaDoctoresNombres;
    private ArrayList<String> listaDoctoresIDs; // IDs de Firestore (String)

    // Estado de la Selección
    private String idDoctorSeleccionado = "";
    private String fechaSeleccionadaString = "";
    private long fechaSeleccionadaTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_cita);

        db = FirebaseFirestore.getInstance();

        // --- 1. Vincular Vistas ---
        autoCompleteEspecialidad = findViewById(R.id.autoCompleteEspecialidad);
        autoCompleteDoctor = findViewById(R.id.autoCompleteDoctor);
        layoutDoctor = findViewById(R.id.layoutDoctor);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        etSintomas = findViewById(R.id.etSintomas);
        btnConfirmarCita = findViewById(R.id.btnConfirmarCita);
        ImageButton buttonRetroceder = findViewById(R.id.buttonRetrocederAgendar);
        chipGroupHorarios = findViewById(R.id.chipGroupHorarios);
        Button btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);

        buttonRetroceder.setOnClickListener(v -> finish());

        // --- 2. Obtener ID Paciente ---
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioPaciente = sp.getString("id_usuario", null);

        if (idUsuarioPaciente == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- 3. Cargar Datos Iniciales (Desde la Nube) ---
        cargarEspecialidadesDesdeNube();

        // --- 4. Configurar Listeners ---

        // Calendario
        btnSeleccionarFecha.setOnClickListener(v -> {
            long today = MaterialDatePicker.todayInUtcMilliseconds();
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(DateValidatorPointForward.from(today));

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecciona una fecha")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                fechaSeleccionadaTimestamp = selection;

                // Formato Visual
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                tvFechaSeleccionada.setText(sdf.format(selection));

                // Formato BD (YYYY-MM-DD)
                SimpleDateFormat sdfBD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdfBD.setTimeZone(TimeZone.getTimeZone("UTC"));
                fechaSeleccionadaString = sdfBD.format(selection);

                // ✨ Verificar Disponibilidad al cambiar fecha
                verificarDisponibilidad();
            });
        });

        // Dropdown Especialidad
        autoCompleteEspecialidad.setOnItemClickListener((parent, view, position, id) -> {
            String especialidad = parent.getItemAtPosition(position).toString();
            cargarDoctoresPorEspecialidad(especialidad);

            // Resetear selección de doctor
            autoCompleteDoctor.setText("");
            idDoctorSeleccionado = "";
            layoutDoctor.setEnabled(true);
            verificarDisponibilidad(); // Resetear horarios (se habilitarán todos hasta elegir doctor)
        });

        // Dropdown Doctor
        autoCompleteDoctor.setOnItemClickListener((parent, view, position, id) -> {
            idDoctorSeleccionado = listaDoctoresIDs.get(position);
            // ✨ Verificar Disponibilidad al cambiar doctor
            verificarDisponibilidad();
        });

        // Botón Confirmar
        btnConfirmarCita.setOnClickListener(v -> {
            if (validarCampos()) {
                confirmarCitaConSeguridad();
            }
        });
    }

    // --- MÉTODOS DE CARGA DE DATOS (FIRESTORE) ---

    private void cargarEspecialidadesDesdeNube() {
        listaEspecialidades = new ArrayList<>();
        // Consultamos la colección 'doctores' para ver qué especialidades existen realmente
        db.collection("doctores").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> setEspecialidades = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String esp = doc.getString("especialidad");
                        if (esp != null && !esp.isEmpty()) {
                            setEspecialidades.add(esp);
                        }
                    }
                    listaEspecialidades.addAll(setEspecialidades);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaEspecialidades);
                    autoCompleteEspecialidad.setAdapter(adapter);
                });
    }

    private void cargarDoctoresPorEspecialidad(String especialidad) {
        listaDoctoresNombres = new ArrayList<>();
        listaDoctoresIDs = new ArrayList<>();

        db.collection("doctores")
                .whereEqualTo("especialidad", especialidad)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombre_completo");
                        listaDoctoresNombres.add(nombre);
                        listaDoctoresIDs.add(doc.getId()); // Guardamos el ID real de Firestore
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaDoctoresNombres);
                    autoCompleteDoctor.setAdapter(adapter);
                });
    }

    // --- MÉTODOS DE DISPONIBILIDAD (LA MAGIA DEL CINE) ---

    private void verificarDisponibilidad() {
        // Resetear chips primero (habilitar todos o deshabilitar todos según prefieras)
        // Aquí habilitamos todos por defecto y luego bloqueamos los ocupados
        actualizarEstadoChips(new ArrayList<>());

        if (!idDoctorSeleccionado.isEmpty() && !fechaSeleccionadaString.isEmpty()) {
            // Consultar citas ocupadas
            db.collection("citas")
                    .whereEqualTo("id_doctor", idDoctorSeleccionado)
                    .whereEqualTo("fecha", fechaSeleccionadaString)
                    .whereEqualTo("estado", "agendada")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> horasOcupadas = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            horasOcupadas.add(doc.getString("hora"));
                        }
                        // Bloquear las horas encontradas
                        actualizarEstadoChips(horasOcupadas);
                    });
        }
    }

    private void actualizarEstadoChips(List<String> horasOcupadas) {
        for (int i = 0; i < chipGroupHorarios.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupHorarios.getChildAt(i);
            String horaChip = chip.getText().toString();

            if (horasOcupadas.contains(horaChip)) {
                // 🚫 OCUPADO
                chip.setEnabled(false);
                chip.setChecked(false);
                chip.setAlpha(0.4f); // Visualmente deshabilitado
            } else {
                // ✅ LIBRE
                chip.setEnabled(true);
                chip.setAlpha(1.0f);
            }
        }
    }

    // --- VALIDACIÓN Y GUARDADO ---

    private boolean validarCampos() {
        if (idDoctorSeleccionado.isEmpty()) {
            autoCompleteDoctor.setError("Selecciona un doctor");
            return false;
        }
        if (fechaSeleccionadaString.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (chipGroupHorarios.getCheckedChipId() == -1) {
            Toast.makeText(this, "Selecciona un horario", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void confirmarCitaConSeguridad() {
        // 1. Obtener datos
        int idChip = chipGroupHorarios.getCheckedChipId();
        Chip chip = findViewById(idChip);
        String horaSeleccionada = chip.getText().toString();
        String sintomas = etSintomas.getText().toString().trim();
        String nombreDoctor = autoCompleteDoctor.getText().toString();
        String especialidad = autoCompleteEspecialidad.getText().toString();

        // 2. ÚLTIMA VERIFICACIÓN (Anti-colisión)
        // Antes de guardar, preguntamos una vez más si sigue libre
        db.collection("citas")
                .whereEqualTo("id_doctor", idDoctorSeleccionado)
                .whereEqualTo("fecha", fechaSeleccionadaString)
                .whereEqualTo("hora", horaSeleccionada)
                .whereEqualTo("estado", "agendada")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        // ❌ ALGUIEN GANÓ EL LUGAR
                        Toast.makeText(this, "¡Lo sentimos! Ese horario acaba de ser reservado.", Toast.LENGTH_LONG).show();
                        verificarDisponibilidad(); // Refrescar la vista
                    } else {
                        // ✅ ESTÁ LIBRE, PROCEDEMOS A GUARDAR
                        guardarCitaFinal(horaSeleccionada, sintomas, nombreDoctor, especialidad);
                    }
                });
    }

    private void guardarCitaFinal(String hora, String sintomas, String nombreDoc, String especialidad) {
        // Buscar nombre del paciente
        db.collection("pacientes").document(idUsuarioPaciente).get()
                .addOnSuccessListener(doc -> {
                    String nombrePac = "Paciente";
                    if (doc.exists()) {
                        nombrePac = doc.getString("nombre") + " " + doc.getString("apellido");
                    }

                    Map<String, Object> cita = new HashMap<>();
                    cita.put("id_paciente", idUsuarioPaciente);
                    cita.put("id_doctor", idDoctorSeleccionado);
                    cita.put("fecha", fechaSeleccionadaString);
                    cita.put("hora", hora);
                    cita.put("motivo", sintomas);
                    cita.put("estado", "agendada");

                    // Datos desnormalizados
                    cita.put("nombre_doctor_temp", nombreDoc);
                    cita.put("nombre_paciente_temp", nombrePac);
                    cita.put("especialidad", especialidad);

                    db.collection("citas").add(cita)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this, "¡Cita Agendada con Éxito!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(this, com.example.saludmovil.ui.paciente.InicioActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }
}
package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.database.BaseDeDatos;
import com.example.saludmovil.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip; // ✨ Importado
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; // ✨ Importado

import java.text.SimpleDateFormat;
import java.util.ArrayList; // ✨ Importado
import java.util.Locale;
import java.util.TimeZone;

public class AgendarCitaActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteEspecialidad;
    private AutoCompleteTextView autoCompleteDoctor; // ✨ Nuevo
    private TextInputLayout layoutDoctor; // ✨ Nuevo
    private TextView tvFechaSeleccionada;
    private ChipGroup chipGroupHorarios;
    private TextInputEditText etSintomas;

    private long fechaSeleccionadaTimestamp = 0;
    private int idUsuarioPaciente;

    // Listas para manejar los datos de la BD
    private ArrayList<String> listaEspecialidades;
    private ArrayList<Integer> listaEspecialidadIDs;
    private ArrayList<String> listaDoctores;
    private ArrayList<Integer> listaDoctorIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_cita);

        // --- Inicialización de Vistas ---
        autoCompleteEspecialidad = findViewById(R.id.autoCompleteEspecialidad);
        autoCompleteDoctor = findViewById(R.id.autoCompleteDoctor); // ✨ Nuevo
        layoutDoctor = findViewById(R.id.layoutDoctor); // ✨ Nuevo
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        chipGroupHorarios = findViewById(R.id.chipGroupHorarios);
        etSintomas = findViewById(R.id.etSintomas);
        Button btnConfirmarCita = findViewById(R.id.btnConfirmarCita);
        Button btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAgendar);

        // --- Configuración de la Toolbar ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- Obtenemos el ID del Paciente que está agendando ---
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioPaciente = sp.getInt("id_usuario", -1);
        if (idUsuarioPaciente == -1) {
            Toast.makeText(this, "Error de sesión de usuario.", Toast.LENGTH_SHORT).show();
            finish(); // Si no hay usuario, no puede agendar
            return;
        }

        // --- Configuración del Dropdown de Especialidades (AHORA DESDE LA BD) ---
        cargarEspecialidades();

        // --- Configuración del Selector de Fecha (Sin cambios) ---
        btnSeleccionarFecha.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText("Selecciona una fecha");
            final MaterialDatePicker<Long> datePicker = builder.build();
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                fechaSeleccionadaTimestamp = selection;
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String fechaFormateada = sdf.format(selection);
                tvFechaSeleccionada.setText(fechaFormateada);
            });
        });

        // --- Configuración del botón de Confirmación con Validación ---
        btnConfirmarCita.setOnClickListener(v -> {
            if (validarCampos()) {
                confirmarCita();
            }
        });

        // --- Configuración de Listeners de los Dropdowns ---
        autoCompleteEspecialidad.setOnItemClickListener((parent, view, position, id) -> {
            // Cuando el usuario selecciona una especialidad, cargamos los doctores
            int especialidadId = listaEspecialidadIDs.get(position);
            cargarDoctores(especialidadId);
            autoCompleteDoctor.setText(""); // Limpiamos la selección anterior de doctor
        });
    }

    /**
     * Carga las especialidades desde la Base de Datos y las pone en el dropdown
     */
    private void cargarEspecialidades() {
        listaEspecialidades = new ArrayList<>();
        listaEspecialidadIDs = new ArrayList<>();
        BaseDeDatos bd = new BaseDeDatos(this);
        Cursor cursor = bd.getEspecialidades(); // Asumimos que este método existe en tu BD

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id");
                int nombreIndex = cursor.getColumnIndex("nombre");
                if (idIndex != -1 && nombreIndex != -1) {
                    listaEspecialidades.add(cursor.getString(nombreIndex));
                    listaEspecialidadIDs.add(cursor.getInt(idIndex));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        bd.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaEspecialidades);
        autoCompleteEspecialidad.setAdapter(adapter);
    }

    /**
     * Carga los doctores de una especialidad específica desde la BD y los pone en su dropdown
     * @param especialidadId El ID de la especialidad seleccionada
     */
    private void cargarDoctores(int especialidadId) {
        listaDoctores = new ArrayList<>();
        listaDoctorIDs = new ArrayList<>();
        BaseDeDatos bd = new BaseDeDatos(this);
        Cursor cursor = bd.getDoctoresPorEspecialidad(especialidadId); // Usamos el método nuevo

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id_usuario");
                int nombreIndex = cursor.getColumnIndex("nombre_completo");
                if (idIndex != -1 && nombreIndex != -1) {
                    listaDoctores.add(cursor.getString(nombreIndex));
                    listaDoctorIDs.add(cursor.getInt(idIndex));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        bd.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaDoctores);
        autoCompleteDoctor.setAdapter(adapter);
        layoutDoctor.setEnabled(true); // Habilitamos el dropdown de doctores
    }

    /**
     * Este método verifica que todos los campos necesarios estén llenos.
     */
    private boolean validarCampos() {
        // 1. Validar Especialidad
        if (TextUtils.isEmpty(autoCompleteEspecialidad.getText().toString())) {
            Toast.makeText(this, "Por favor, selecciona una especialidad", Toast.LENGTH_SHORT).show();
            autoCompleteEspecialidad.setError("Campo requerido");
            return false;
        }

        // ✨ 2. Validar Doctor ✨
        if (TextUtils.isEmpty(autoCompleteDoctor.getText().toString())) {
            Toast.makeText(this, "Por favor, selecciona un doctor", Toast.LENGTH_SHORT).show();
            autoCompleteDoctor.setError("Campo requerido");
            return false;
        }

        // 3. Validar Fecha
        if (fechaSeleccionadaTimestamp == 0) {
            Toast.makeText(this, "Por favor, selecciona una fecha para tu cita", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 4. Validar Horario
        if (chipGroupHorarios.getCheckedChipId() == -1) {
            Toast.makeText(this, "Por favor, elige un horario disponible", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 5. Validar Síntomas
        if (TextUtils.isEmpty(etSintomas.getText().toString().trim())) {
            Toast.makeText(this, "Por favor, describe tus síntomas", Toast.LENGTH_SHORT).show();
            etSintomas.setError("Campo requerido");
            return false;
        }
        return true;
    }

    /**
     * Este método se ejecuta cuando la cita es válida y se va a registrar.
     */
    private void confirmarCita() {
        // --- Recolectamos toda la información ---
        String especialidad = autoCompleteEspecialidad.getText().toString();

        // Obtenemos el ID del doctor seleccionado
        int posDoctor = listaDoctores.indexOf(autoCompleteDoctor.getText().toString());
        int idDoctor = listaDoctorIDs.get(posDoctor);

        // Formateamos la fecha a un string "YYYY-MM-DD" para la BD
        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdfDB.setTimeZone(TimeZone.getTimeZone("UTC"));
        String fechaParaBD = sdfDB.format(fechaSeleccionadaTimestamp);

        // Obtenemos la hora del Chip seleccionado
        Chip chipSeleccionado = findViewById(chipGroupHorarios.getCheckedChipId());
        String hora = chipSeleccionado.getText().toString();

        String sintomas = etSintomas.getText().toString().trim();

        // --- Guardamos en la Base de Datos ---
        BaseDeDatos bd = new BaseDeDatos(this);
        boolean exito = bd.agendarCita(idUsuarioPaciente, idDoctor, fechaParaBD, hora, sintomas);
        bd.close();

        if (exito) {
            Toast.makeText(this, "Su cita médica se ha registrado con éxito", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AgendarCitaActivity.this, InicioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar la cita. Intente de nuevo.", Toast.LENGTH_LONG).show();
        }
    }
}
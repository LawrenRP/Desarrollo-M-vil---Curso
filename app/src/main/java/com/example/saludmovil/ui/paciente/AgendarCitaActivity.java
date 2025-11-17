package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.database.BaseDeDatos;
import com.example.saludmovil.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AgendarCitaActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteEspecialidad;
    private AutoCompleteTextView autoCompleteDoctor;
    private TextInputLayout layoutDoctor;
    private TextView tvFechaSeleccionada;
    private ChipGroup chipGroupManana, chipGroupTarde, chipGroupNoche;

    private TextInputEditText etSintomas;

    private long fechaSeleccionadaTimestamp = 0;
    private int idUsuarioPaciente;
    private ArrayList<String> listaEspecialidades;
    private ArrayList<Integer> listaEspecialidadIDs;
    private ArrayList<String> listaDoctores;
    private ArrayList<Integer> listaDoctorIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_cita);
        autoCompleteEspecialidad = findViewById(R.id.autoCompleteEspecialidad);
        autoCompleteDoctor = findViewById(R.id.autoCompleteDoctor);
        layoutDoctor = findViewById(R.id.layoutDoctor);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        etSintomas = findViewById(R.id.etSintomas);
        Button btnConfirmarCita = findViewById(R.id.btnConfirmarCita);
        Button btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        ImageButton buttonRetroceder = findViewById(R.id.buttonRetrocederAgendar);

        chipGroupManana = findViewById(R.id.chipGroupManana);
        chipGroupTarde = findViewById(R.id.chipGroupTarde);
        chipGroupNoche = findViewById(R.id.chipGroupNoche);
        buttonRetroceder.setOnClickListener(v -> onBackPressed());
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioPaciente = sp.getInt("id_usuario", -1);
        if (idUsuarioPaciente == -1) {
            Toast.makeText(this, "Error de sesión de usuario.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarEspecialidades();

        btnSeleccionarFecha.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText("Selecciona una fecha");

            long today = MaterialDatePicker.todayInUtcMilliseconds();
            Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utc.setTimeInMillis(today);
            utc.set(Calendar.DAY_OF_MONTH, 1);
            long startMonth = utc.getTimeInMillis();
            utc.add(Calendar.MONTH, 2);
            long endMonth = utc.getTimeInMillis();
            CalendarConstraints.DateValidator validatorPast = DateValidatorPointForward.from(today);
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(validatorPast);
            constraintsBuilder.setStart(startMonth);
            constraintsBuilder.setEnd(endMonth);
            constraintsBuilder.setOpenAt(today);

            builder.setCalendarConstraints(constraintsBuilder.build());

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
        btnConfirmarCita.setOnClickListener(v -> {
            if (validarCampos()) {
                confirmarCita();
            }
        });
        autoCompleteEspecialidad.setOnItemClickListener((parent, view, position, id) -> {
            int especialidadId = listaEspecialidadIDs.get(position);
            cargarDoctores(especialidadId);
            autoCompleteDoctor.setText("");
        });
        configurarListenersDeChips();
    }

    private void configurarListenersDeChips() {
        chipGroupManana.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                chipGroupTarde.clearCheck();
                chipGroupNoche.clearCheck();
            }
        });

        chipGroupTarde.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                chipGroupManana.clearCheck();
                chipGroupNoche.clearCheck();
            }
        });

        chipGroupNoche.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                chipGroupManana.clearCheck();
                chipGroupTarde.clearCheck();
            }
        });
    }

    private void cargarEspecialidades() {
        listaEspecialidades = new ArrayList<>();
        listaEspecialidadIDs = new ArrayList<>();
        BaseDeDatos bd = new BaseDeDatos(this);
        Cursor cursor = bd.getEspecialidades();

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
        private void cargarDoctores(int especialidadId) {
        listaDoctores = new ArrayList<>();
        listaDoctorIDs = new ArrayList<>();
        BaseDeDatos bd = new BaseDeDatos(this);
        Cursor cursor = bd.getDoctoresPorEspecialidad(especialidadId);

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
        layoutDoctor.setEnabled(true);
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(autoCompleteEspecialidad.getText().toString())) {
            Toast.makeText(this, "Por favor, selecciona una especialidad", Toast.LENGTH_SHORT).show();
            autoCompleteEspecialidad.setError("Campo requerido");
            return false;
        }
        if (TextUtils.isEmpty(autoCompleteDoctor.getText().toString())) {
            Toast.makeText(this, "Por favor, selecciona un doctor", Toast.LENGTH_SHORT).show();
            autoCompleteDoctor.setError("Campo requerido");
            return false;
        }
        if (fechaSeleccionadaTimestamp == 0) {
            Toast.makeText(this, "Por favor, selecciona una fecha para tu cita", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (chipGroupManana.getCheckedChipId() == -1 &&
                chipGroupTarde.getCheckedChipId() == -1 &&
                chipGroupNoche.getCheckedChipId() == -1) {
            Toast.makeText(this, "Por favor, elige un horario disponible", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(etSintomas.getText().toString().trim())) {
            Toast.makeText(this, "Por favor, describe tus síntomas", Toast.LENGTH_SHORT).show();
            etSintomas.setError("Campo requerido");
            return false;
        }
        return true;
    }

    private void confirmarCita() {
        String especialidad = autoCompleteEspecialidad.getText().toString();
        int posDoctor = listaDoctores.indexOf(autoCompleteDoctor.getText().toString());
        int idDoctor = listaDoctorIDs.get(posDoctor);
        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdfDB.setTimeZone(TimeZone.getTimeZone("UTC"));
        String fechaParaBD = sdfDB.format(fechaSeleccionadaTimestamp);

        int checkedId = chipGroupManana.getCheckedChipId();
        if (checkedId == -1) {
            checkedId = chipGroupTarde.getCheckedChipId();
        }
        if (checkedId == -1) {
            checkedId = chipGroupNoche.getCheckedChipId();
        }

        Chip chipSeleccionado = findViewById(checkedId);
        String hora = chipSeleccionado.getText().toString();

        String sintomas = etSintomas.getText().toString().trim();

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
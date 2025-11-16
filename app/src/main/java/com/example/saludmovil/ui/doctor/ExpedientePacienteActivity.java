package com.example.saludmovil.ui.doctor;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.HistorialCitasAdapter;
import com.example.saludmovil.data.CitaHistorial;
import com.example.saludmovil.database.BaseDeDatos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ExpedientePacienteActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvNombre, tvDNI, tvAlergias, tvEnfermedades;
    private Button btnAgendarCita, btnCrearReceta;
    private RecyclerView rvHistorialCitas;

    private TextView tvExpedienteFechaNac, tvExpedienteEdad, tvExpedienteEstaturaPeso;
    private TextView tvExpedienteTipoSangre, tvExpedienteMedicamentos, tvExpedienteContacto;
    private TextView tvExpedienteSexo;

    private BaseDeDatos bd;
    private HistorialCitasAdapter adapter;
    private int idUsuarioDoctor;
    private int idUsuarioPaciente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expediente_paciente);

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getInt("id_usuario", -1);
        idUsuarioPaciente = getIntent().getIntExtra("id_paciente", -1);

        if (idUsuarioDoctor == -1 || idUsuarioPaciente == -1) {
            Toast.makeText(this, "Error: No se pudo cargar el expediente", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbarExpediente);
        tvNombre = findViewById(R.id.tvExpedienteNombre);
        tvDNI = findViewById(R.id.tvExpedienteDNI);
        tvAlergias = findViewById(R.id.tvExpedienteAlergias);
        tvEnfermedades = findViewById(R.id.tvExpedienteEnfermedades);
        btnAgendarCita = findViewById(R.id.btnExpedienteAgendarCita);
        btnCrearReceta = findViewById(R.id.btnExpedienteCrearReceta);
        rvHistorialCitas = findViewById(R.id.rvExpedienteCitas);

        tvExpedienteFechaNac = findViewById(R.id.tvExpedienteFechaNac);
        tvExpedienteEdad = findViewById(R.id.tvExpedienteEdad);
        tvExpedienteEstaturaPeso = findViewById(R.id.tvExpedienteEstaturaPeso);
        tvExpedienteTipoSangre = findViewById(R.id.tvExpedienteTipoSangre);
        tvExpedienteMedicamentos = findViewById(R.id.tvExpedienteMedicamentos);
        tvExpedienteContacto = findViewById(R.id.tvExpedienteContacto);
        tvExpedienteSexo = findViewById(R.id.tvExpedienteSexo);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        bd = new BaseDeDatos(this);
        adapter = new HistorialCitasAdapter(this);
        rvHistorialCitas.setAdapter(adapter);
        rvHistorialCitas.setNestedScrollingEnabled(false);

        cargarDatosPaciente();
        cargarHistorialCitas();

        btnAgendarCita.setOnClickListener(v -> Toast.makeText(this, "Función: Abrir 'Agendar Cita' para este paciente", Toast.LENGTH_SHORT).show());
        btnCrearReceta.setOnClickListener(v -> Toast.makeText(this, "Función: Abrir 'Crear Receta' para este paciente", Toast.LENGTH_SHORT).show());
    }

    private void cargarDatosPaciente() {
        Cursor cursor = bd.getPerfilPacientePorId(idUsuarioPaciente);
        if (cursor != null && cursor.moveToFirst()) {

            String nombre = getSafeStringFromCursor(cursor, "nombre", R.string.expediente_no_disponible);
            String apellido = getSafeStringFromCursor(cursor, "apellido", R.string.expediente_no_disponible);
            String dni = getSafeStringFromCursor(cursor, "dni", R.string.expediente_no_disponible);
            String fechaNac = getSafeStringFromCursor(cursor, "fecha_nacimiento", R.string.expediente_no_disponible);
            String estatura = getSafeStringFromCursor(cursor, "estatura", R.string.expediente_no_disponible);
            String peso = getSafeStringFromCursor(cursor, "peso", R.string.expediente_no_disponible);
            String sangre = getSafeStringFromCursor(cursor, "tipo_sangre", R.string.expediente_no_disponible);
            String sexo = getSafeStringFromCursor(cursor, "sexo", R.string.expediente_no_disponible);
            String alergias = getSafeStringFromCursor(cursor, "alergias", R.string.expediente_no_registrado);
            String enfermedades = getSafeStringFromCursor(cursor, "enfermedades_cronicas", R.string.expediente_no_registrado);
            String medicamentos = getSafeStringFromCursor(cursor, "medicamentos_actuales", R.string.expediente_sin_medicamentos_valor);
            String contactoNombre = getSafeStringFromCursor(cursor, "nombre_contacto_emergencia", R.string.expediente_no_disponible);
            String contactoCelular = getSafeStringFromCursor(cursor, "celular_contacto_emergencia", R.string.expediente_no_disponible);

            int edad = calcularEdad(fechaNac.equals(getString(R.string.expediente_no_disponible)) ? "" : fechaNac);

            tvNombre.setText(getString(R.string.expediente_nombre_completo, nombre, apellido));
            tvDNI.setText(getString(R.string.expediente_dni, dni));
            tvExpedienteFechaNac.setText(getString(R.string.expediente_nacimiento, fechaNac));
            tvExpedienteEdad.setText(edad == -1
                    ? getString(R.string.expediente_edad_na)
                    : getString(R.string.expediente_edad, edad));
            tvExpedienteEstaturaPeso.setText(getString(R.string.expediente_estatura_peso, estatura, peso));
            tvExpedienteTipoSangre.setText(getString(R.string.expediente_tipo_sangre, sangre));
            tvExpedienteSexo.setText(getString(R.string.expediente_sexo, sexo));
            tvAlergias.setText(getString(R.string.expediente_alergias, alergias));
            tvEnfermedades.setText(getString(R.string.expediente_enfermedades, enfermedades));
            tvExpedienteMedicamentos.setText(getString(R.string.expediente_medicamentos, medicamentos));
            tvExpedienteContacto.setText(getString(R.string.expediente_contacto, contactoNombre, contactoCelular));

            cursor.close();
        }
    }

    private int calcularEdad(String fechaNacimientoStr) {
        if (fechaNacimientoStr == null || fechaNacimientoStr.isEmpty()) {
            return -1;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar fechaNacimiento = Calendar.getInstance();
            fechaNacimiento.setTime(formatter.parse(fechaNacimientoStr));
            Calendar hoy = Calendar.getInstance();

            int edad = hoy.get(Calendar.YEAR) - fechaNacimiento.get(Calendar.YEAR);
            if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--;
            }
            return Math.max(edad, 0);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void cargarHistorialCitas() {
        ArrayList<CitaHistorial> listaCitas = new ArrayList<>();
        Cursor cursor = bd.getHistorialDeCitas(idUsuarioDoctor, idUsuarioPaciente);

        if (cursor != null && cursor.moveToFirst()) {
            int fechaIndex = cursor.getColumnIndex("fecha");
            int horaIndex = cursor.getColumnIndex("hora");
            int estadoIndex = cursor.getColumnIndex("estado");
            int motivoIndex = cursor.getColumnIndex("motivo");

            do {
                if (fechaIndex != -1 && horaIndex != -1 && estadoIndex != -1 && motivoIndex != -1) {
                    String fecha = cursor.getString(fechaIndex);
                    String hora = cursor.getString(horaIndex);
                    String estado = cursor.getString(estadoIndex);
                    String motivo = cursor.getString(motivoIndex);
                    listaCitas.add(new CitaHistorial(fecha, hora, estado, motivo));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.submitList(listaCitas);
    }

    private String getSafeStringFromCursor(Cursor cursor, String columnName, int defaultStringResId) {
        int index = cursor.getColumnIndex(columnName);
        String valor = "";

        if (index != -1 && !cursor.isNull(index)) {
            valor = cursor.getString(index);
        }

        if (TextUtils.isEmpty(valor)) {
            return getString(defaultStringResId);
        }

        return valor;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bd != null) {
            bd.close();
        }
    }
}

package com.example.saludmovil.ui.doctor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.HistorialCitasAdapter;
import com.example.saludmovil.data.Cita;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ExpedientePacienteActivity extends AppCompatActivity {

    private static final String TAG = "ExpedienteDebug";

    // Vistas
    private Toolbar toolbar;
    private TextView tvNombre, tvDNI, tvAlergias, tvEnfermedades;
    private TextView tvExpedienteFechaNac, tvExpedienteEdad, tvExpedienteEstaturaPeso;
    private TextView tvExpedienteTipoSangre, tvExpedienteMedicamentos, tvExpedienteContacto;
    private TextView tvExpedienteSexo;
    private RecyclerView rvHistorialCitas;
    private TextView tvSinCitasHistorial; // ✨ TextView para lista vacía

    // Lógica
    private FirebaseFirestore db;
    private HistorialCitasAdapter adapter;
    private String idUsuarioDoctor;
    private String idUsuarioPaciente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expediente_paciente);

        db = FirebaseFirestore.getInstance();

        // 1. Obtener IDs
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getString("id_usuario", null);

        if (getIntent().hasExtra("id_paciente")) {
            idUsuarioPaciente = getIntent().getStringExtra("id_paciente");
            if (idUsuarioPaciente == null) {
                // Fallback por si llega como int
                int idInt = getIntent().getIntExtra("id_paciente", -1);
                if (idInt != -1) idUsuarioPaciente = String.valueOf(idInt);
            }
        }

        if (idUsuarioDoctor == null || idUsuarioPaciente == null) {
            Toast.makeText(this, "Error: No se pudo cargar el expediente", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();

        // Configurar RecyclerView con LayoutManager
        adapter = new HistorialCitasAdapter(this);
        rvHistorialCitas.setLayoutManager(new LinearLayoutManager(this)); // ✨ ESTO ES ESENCIAL
        rvHistorialCitas.setAdapter(adapter);
        rvHistorialCitas.setNestedScrollingEnabled(false);

        // Cargar Datos
        cargarDatosPacienteDeFirestore();
        cargarHistorialCitasDeFirestore();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarExpediente);
        tvNombre = findViewById(R.id.tvExpedienteNombre);
        tvDNI = findViewById(R.id.tvExpedienteDNI);
        tvAlergias = findViewById(R.id.tvExpedienteAlergias);
        tvEnfermedades = findViewById(R.id.tvExpedienteEnfermedades);
        rvHistorialCitas = findViewById(R.id.rvExpedienteCitas);
        tvSinCitasHistorial = findViewById(R.id.tvSinCitasHistorial); // ✨ Vincular vista vacía

        tvExpedienteFechaNac = findViewById(R.id.tvExpedienteFechaNac);
        tvExpedienteEdad = findViewById(R.id.tvExpedienteEdad);
        tvExpedienteEstaturaPeso = findViewById(R.id.tvExpedienteEstaturaPeso);
        tvExpedienteTipoSangre = findViewById(R.id.tvExpedienteTipoSangre);
        tvExpedienteMedicamentos = findViewById(R.id.tvExpedienteMedicamentos);
        tvExpedienteContacto = findViewById(R.id.tvExpedienteContacto);
        tvExpedienteSexo = findViewById(R.id.tvExpedienteSexo);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void cargarDatosPacienteDeFirestore() {
        db.collection("pacientes").document(idUsuarioPaciente).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String nombre = document.getString("nombre");
                        String apellido = document.getString("apellido");
                        String dni = document.getString("dni");
                        String fechaNac = document.getString("fecha_nacimiento");

                        tvNombre.setText(getString(R.string.expediente_nombre_completo, nombre, apellido));
                        tvDNI.setText(getString(R.string.expediente_dni, dni));
                        tvExpedienteFechaNac.setText(getString(R.string.expediente_nacimiento, fechaNac));

                        String estatura = document.getString("estatura");
                        String peso = document.getString("peso");
                        String sangre = document.getString("tipo_sangre");
                        String alergias = document.getString("alergias");
                        String cronicas = document.getString("enfermedades_cronicas");
                        String medicamentos = document.getString("medicamentos_actuales");
                        String contacto = document.getString("nombre_contacto_emergencia");
                        String telefono = document.getString("celular_contacto_emergencia");
                        String sexo = document.getString("sexo");

                        tvExpedienteEstaturaPeso.setText(getString(R.string.expediente_estatura_peso,
                                estatura != null ? estatura : "N/A",
                                peso != null ? peso : "N/A"));
                        tvExpedienteTipoSangre.setText(getString(R.string.expediente_tipo_sangre, sangre != null ? sangre : "N/A"));
                        tvAlergias.setText(getString(R.string.expediente_alergias, alergias != null && !alergias.isEmpty() ? alergias : "No registradas"));
                        tvEnfermedades.setText(getString(R.string.expediente_enfermedades, cronicas != null && !cronicas.isEmpty() ? cronicas : "No registradas"));
                        tvExpedienteMedicamentos.setText(getString(R.string.expediente_medicamentos, medicamentos != null && !medicamentos.isEmpty() ? medicamentos : "Ninguno"));
                        tvExpedienteContacto.setText(getString(R.string.expediente_contacto, contacto != null ? contacto : "N/A", telefono != null ? telefono : ""));

                        if (tvExpedienteSexo != null) {
                            tvExpedienteSexo.setText("Sexo: " + (sexo != null ? sexo : "N/A"));
                        }

                        int edad = calcularEdad(fechaNac);
                        tvExpedienteEdad.setText(edad == -1 ? getString(R.string.expediente_edad_na) : getString(R.string.expediente_edad, edad));
                    } else {
                        Log.e(TAG, "El documento del paciente no existe");
                        Toast.makeText(this, "Error: Paciente no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show());
    }

    private void cargarHistorialCitasDeFirestore() {
        Log.d(TAG, "🔍 Consultando citas para: Paciente=" + idUsuarioPaciente + ", Doctor=" + idUsuarioDoctor);

        db.collection("citas")
                .whereEqualTo("id_paciente", idUsuarioPaciente)
                .whereEqualTo("id_doctor", idUsuarioDoctor)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "✅ Consulta exitosa. Citas encontradas: " + queryDocumentSnapshots.size());

                    ArrayList<Cita> listaCitas = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String idCita = doc.getId();
                        String fecha = doc.getString("fecha");
                        String hora = doc.getString("hora");
                        String estado = doc.getString("estado");
                        String motivo = doc.getString("motivo");

                        Log.d(TAG, "📋 Cita: " + fecha + " " + hora + " - Estado: " + estado);

                        // Creamos la Cita usando el constructor seguro de Strings
                        listaCitas.add(new Cita(
                                0, // ID local
                                idUsuarioPaciente,
                                idUsuarioDoctor,
                                idCita, // ID Firestore
                                fecha,
                                hora,
                                estado,
                                motivo,
                                tvNombre.getText().toString(),
                                "Yo"
                        ));
                    }

                    Log.d(TAG, "📤 Enviando " + listaCitas.size() + " citas al adapter");
                    adapter.submitList(listaCitas);

                    // ✨ Mostrar/ocultar mensaje de lista vacía
                    if (listaCitas.isEmpty()) {
                        Log.w(TAG, "⚠️ No hay citas para mostrar");
                        rvHistorialCitas.setVisibility(View.GONE);
                        tvSinCitasHistorial.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "✅ Mostrando " + listaCitas.size() + " citas en el RecyclerView");
                        rvHistorialCitas.setVisibility(View.VISIBLE);
                        tvSinCitasHistorial.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando historial de citas", e);
                    Toast.makeText(this, "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Mostrar mensaje de error
                    rvHistorialCitas.setVisibility(View.GONE);
                    tvSinCitasHistorial.setVisibility(View.VISIBLE);
                    tvSinCitasHistorial.setText("Error al cargar el historial de citas");
                });
    }

    private int calcularEdad(String fechaNacimientoStr) {
        if (fechaNacimientoStr == null || fechaNacimientoStr.isEmpty()) return -1;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar fechaNacimiento = Calendar.getInstance();
            fechaNacimiento.setTime(formatter.parse(fechaNacimientoStr));
            Calendar hoy = Calendar.getInstance();
            int edad = hoy.get(Calendar.YEAR) - fechaNacimiento.get(Calendar.YEAR);
            if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacimiento.get(Calendar.DAY_OF_YEAR)) edad--;
            return Math.max(edad, 0);
        } catch (ParseException e) { return -1; }
    }
}
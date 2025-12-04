package com.example.saludmovil.ui.doctor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Para ver logs si algo falla
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.MedicamentoResumenAdapter;
import com.example.saludmovil.data.MedicamentoRecetado;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConsultaPacienteActivity extends AppCompatActivity implements MedicamentoResumenAdapter.OnItemClickListener {

    // Vistas
    // ✨ Añadimos tvDni a las variables
    private TextView tvNombre, tvDni, tvEstatura, tvPeso, tvSangre, tvAlergias, tvCronicas, tvMedicamentos, tvContactoNombre, tvContactoTel;
    private TextView tvCodigoConsulta, tvFechaHora, tvMotivoConsulta;

    private TextInputEditText etDiagnostico;
    private RecyclerView rvMedicamentos;
    private Button btnRecetar, btnCompletar, btnCancelar;

    // Datos
    private FirebaseFirestore db;
    private String idCitaFirebase;
    private String idPaciente;

    private ArrayList<MedicamentoRecetado> listaMedicamentosRecetados;
    private MedicamentoResumenAdapter adapter;

    private final ActivityResultLauncher<Intent> selectorMedicamentosLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<MedicamentoRecetado> nuevos = (ArrayList<MedicamentoRecetado>) result.getData().getSerializableExtra("medicamentos_seleccionados");
                    if (nuevos != null) {
                        listaMedicamentosRecetados.addAll(nuevos);
                        adapter.actualizarLista(listaMedicamentosRecetados);
                        rvMedicamentos.setVisibility(View.VISIBLE);

                        View emptyView = findViewById(R.id.tvListaVacia);
                        if (emptyView != null) emptyView.setVisibility(View.GONE);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_paciente);

        // 1. Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        idCitaFirebase = getIntent().getStringExtra("id_cita_firebase");
        idPaciente = getIntent().getStringExtra("id_paciente");

        Log.d("DEBUG_CITA", "IDs recibidos -> Cita: " + idCitaFirebase + ", Paciente: " + idPaciente);

        if (idCitaFirebase == null || idPaciente == null) {
            Toast.makeText(this, "Error: Datos insuficientes.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        listaMedicamentosRecetados = new ArrayList<>();
        initViews();

        cargarDatosGenerales();
        cargarDatosDeFirestore();

        setupRecyclerView();
        setupButtons();
    }

    private void initViews() {
        tvNombre = findViewById(R.id.tvNombrePaciente);
        tvDni = findViewById(R.id.tvDniPaciente); // ✨ Vinculamos el nuevo ID
        tvEstatura = findViewById(R.id.tvEstatura);
        tvPeso = findViewById(R.id.tvPeso);
        tvSangre = findViewById(R.id.tvTipoSangre);
        tvAlergias = findViewById(R.id.tvAlergias);
        tvCronicas = findViewById(R.id.tvEnfermedadesCronicas);
        tvMedicamentos = findViewById(R.id.tvMedicamentos);
        tvContactoNombre = findViewById(R.id.tvContactoNombre);
        tvContactoTel = findViewById(R.id.tvContactoTelefono);

        tvCodigoConsulta = findViewById(R.id.tvCodigoConsulta);
        tvFechaHora = findViewById(R.id.tvFechaHoraConsulta);
        tvMotivoConsulta = findViewById(R.id.tvMotivoConsulta);

        etDiagnostico = findViewById(R.id.inputDiagnostico);
        rvMedicamentos = findViewById(R.id.rvMedicamentosRecetados);
        btnRecetar = findViewById(R.id.btnRecetarMedicamentos);
        btnCompletar = findViewById(R.id.btnCompletarConsulta);
        btnCancelar = findViewById(R.id.btnCancelarConsulta);

        findViewById(R.id.buttonAtras).setOnClickListener(v -> finish());
    }

    private void cargarDatosGenerales() {
        String folioVisual = idCitaFirebase.substring(0, Math.min(idCitaFirebase.length(), 8)).toUpperCase();
        tvCodigoConsulta.setText("Consulta Ref: " + folioVisual);
        String fechaHora = new SimpleDateFormat("dd 'de' MMM - hh:mm a", Locale.getDefault()).format(new Date());
        tvFechaHora.setText(fechaHora);
    }

    private void cargarDatosDeFirestore() {
        // A. Cargar Perfil del Paciente (COMPLETO)
        db.collection("pacientes").document(idPaciente).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Datos Personales
                        String nombre = doc.getString("nombre");
                        String apellido = doc.getString("apellido");
                        String dni = doc.getString("dni");

                        tvNombre.setText(nombre + " " + apellido);
                        tvDni.setText("DNI: " + (dni != null ? dni : "N/A")); // ✨ Mostramos DNI

                        // Datos Físicos
                        String estatura = doc.getString("estatura");
                        String peso = doc.getString("peso");
                        String sangre = doc.getString("tipo_sangre");

                        tvEstatura.setText("Estatura: " + (estatura != null && !estatura.isEmpty() ? estatura + "cm" : "N/A"));
                        tvPeso.setText("Peso: " + (peso != null && !peso.isEmpty() ? peso + "kg" : "N/A"));
                        tvSangre.setText("Sangre: " + (sangre != null && !sangre.isEmpty() ? sangre : "N/A"));

                        // ✨ Historial Médico (Ahora sí cargamos todo) ✨
                        String alergias = doc.getString("alergias");
                        tvAlergias.setText("Alergias: " + (alergias != null && !alergias.isEmpty() ? alergias : "Ninguna"));

                        String cronicas = doc.getString("enfermedades_cronicas");
                        tvCronicas.setText("Crónicas: " + (cronicas != null && !cronicas.isEmpty() ? cronicas : "Ninguna"));

                        String medsActuales = doc.getString("medicamentos_actuales");
                        tvMedicamentos.setText("Medicamentos: " + (medsActuales != null && !medsActuales.isEmpty() ? medsActuales : "Ninguno"));

                        // ✨ Contacto de Emergencia ✨
                        String conNombre = doc.getString("nombre_contacto_emergencia");
                        tvContactoNombre.setText("Contacto: " + (conNombre != null && !conNombre.isEmpty() ? conNombre : "N/A"));

                        String conTel = doc.getString("celular_contacto_emergencia");
                        tvContactoTel.setText("Teléfono: " + (conTel != null && !conTel.isEmpty() ? conTel : "N/A"));
                    }
                });

        // B. Cargar Motivo de la Cita
        db.collection("citas").document(idCitaFirebase).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String motivo = doc.getString("motivo");
                        tvMotivoConsulta.setText(motivo != null ? motivo : "Sin motivo registrado");
                    }
                });
    }

    private void setupRecyclerView() {
        rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicamentoResumenAdapter(this, listaMedicamentosRecetados, this);
        rvMedicamentos.setAdapter(adapter);
    }

    private void setupButtons() {
        btnRecetar.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeleccionarMedicamentoActivity.class);
            selectorMedicamentosLauncher.launch(intent);
        });

        btnCancelar.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cancelar Consulta")
                    .setMessage("¿Seguro que deseas salir? No se guardará nada.")
                    .setPositiveButton("Salir", (dialog, which) -> finish())
                    .setNegativeButton("Quedarse", null)
                    .show();
        });

        btnCompletar.setOnClickListener(v -> guardarConsultaEstructurada());
    }

    // 1. Método principal de guardado
    private void guardarConsultaEstructurada() {
        String diagnostico = etDiagnostico.getText().toString().trim();

        if (TextUtils.isEmpty(diagnostico)) {
            etDiagnostico.setError("El diagnóstico es obligatorio");
            return;
        }

        // Actualizar estado de la Cita
        Map<String, Object> actualizacionesCita = new HashMap<>();
        actualizacionesCita.put("estado", "Completada");
        actualizacionesCita.put("diagnostico", diagnostico);

        db.collection("citas").document(idCitaFirebase)
                .update(actualizacionesCita)
                .addOnSuccessListener(aVoid -> {
                    // Si hay medicamentos, guardamos la receta COMPLETA
                    if (!listaMedicamentosRecetados.isEmpty()) {
                        guardarRecetaEnNube(diagnostico);
                    } else {
                        finalizarProceso();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show());
    }

    private void guardarRecetaEnNube(String diagnostico) {
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        String idDoctorActual = sp.getString("id_usuario", null);

        if (idDoctorActual == null) {
            Toast.makeText(this, "Error: Doctor no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("doctores").document(idDoctorActual).get()
                .addOnSuccessListener(docSnapshot -> {
                    String nombreDoc = docSnapshot.getString("nombre_completo");
                    String especialidad = docSnapshot.getString("especialidad");
                    String cmp = docSnapshot.getString("cmp");

                    // Obtenemos datos limpios de la UI
                    String nombrePac = tvNombre.getText().toString();
                    String dniPac = tvDni.getText().toString().replace("DNI: ", ""); // Limpiamos el prefijo

                    String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    Map<String, Object> receta = new HashMap<>();
                    receta.put("id_cita", idCitaFirebase);
                    receta.put("fecha_emision", fechaHoy);
                    receta.put("id_paciente", idPaciente);
                    receta.put("id_doctor", idDoctorActual);

                    receta.put("nombre_doctor_temp", nombreDoc);
                    receta.put("especialidad", especialidad);
                    receta.put("cmp_doctor", cmp);
                    receta.put("nombre_paciente_temp", nombrePac);
                    receta.put("dni_paciente_temp", dniPac);
                    receta.put("diagnostico", diagnostico);

                    receta.put("medicamentos", listaMedicamentosRecetados);

                    db.collection("recetas").add(receta)
                            .addOnSuccessListener(docRef -> finalizarProceso())
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar receta", Toast.LENGTH_SHORT).show());
                });
    }

    private void finalizarProceso() {
        Toast.makeText(this, "Consulta finalizada correctamente", Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onEliminarClick(int position) {
        listaMedicamentosRecetados.remove(position);
        adapter.actualizarLista(listaMedicamentosRecetados);
        if (listaMedicamentosRecetados.isEmpty()) {
            rvMedicamentos.setVisibility(View.GONE);
            View emptyView = findViewById(R.id.tvListaVacia);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
        }
    }
}
package com.example.saludmovil.ui.paciente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.MedicamentoLecturaAdapter;
import com.example.saludmovil.data.MedicamentoRecetado;
import com.example.saludmovil.utils.PdfGenerator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecetaDetalleActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String idReceta;

    // Vistas
    private TextView tvFolio, tvFecha, tvPaciente, tvDni, tvDoctor, tvEspecialidad, tvCmp, tvDiagnostico;
    private RecyclerView rvDetalleMedicamentos;
    private Button btnDescargar;

    // Lista para el PDF y el Adapter
    private ArrayList<MedicamentoRecetado> listaMedicamentosParaPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receta_detalle);

        idReceta = getIntent().getStringExtra("id_receta");
        if (idReceta == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        listaMedicamentosParaPdf = new ArrayList<>();

        initViews();
        cargarDetalleReceta();
    }

    private void initViews() {
        tvFolio = findViewById(R.id.tvFolioReceta);
        tvFecha = findViewById(R.id.tvFechaEmision);
        tvPaciente = findViewById(R.id.tvNombrePaciente);
        tvDni = findViewById(R.id.tvDniPaciente);
        tvDoctor = findViewById(R.id.tvDoctorDetalle);
        tvEspecialidad = findViewById(R.id.tvEspecialidadDetalle);
        tvCmp = findViewById(R.id.tvCmpDoctor);
        tvDiagnostico = findViewById(R.id.tvDiagnostico);

        rvDetalleMedicamentos = findViewById(R.id.rvDetalleMedicamentos);
        rvDetalleMedicamentos.setLayoutManager(new LinearLayoutManager(this));

        btnDescargar = findViewById(R.id.btnDescargarReceta);
        btnDescargar.setOnClickListener(v -> descargarPdf());

        findViewById(R.id.buttonRetrocederDetalle).setOnClickListener(v -> finish());
    }

    private void cargarDetalleReceta() {
        android.util.Log.d("DEBUG_RECETA", "Solicitando receta con ID: " + idReceta);

        db.collection("recetas").document(idReceta).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // 🔍 RADIOGRAFÍA: Imprimimos todo el documento en el Logcat
                        android.util.Log.d("DEBUG_RECETA", "Documento encontrado. Datos: " + document.getData());

                        // --- Cabecera ---
                        String folio = document.getId().substring(0, Math.min(document.getId().length(), 8)).toUpperCase();
                        tvFolio.setText("Folio N°: " + folio);

                        // Verificar campos específicos
                        String fecha = document.getString("fecha_emision");
                        android.util.Log.d("DEBUG_RECETA", "Fecha: " + fecha);
                        tvFecha.setText("Emitida el: " + String.valueOf(fecha));

                        // Doctor
                        String docName = document.getString("nombre_doctor_temp");
                        android.util.Log.d("DEBUG_RECETA", "Doctor: " + docName);
                        tvDoctor.setText(docName != null ? docName : "Dr. Asignado");

                        String especialidad = document.getString("especialidad");
                        tvEspecialidad.setText(especialidad != null ? especialidad : "General");

                        String cmp = document.getString("cmp_doctor");
                        tvCmp.setText("CMP: " + (cmp != null ? cmp : "N/A"));

                        // Paciente
                        String pacName = document.getString("nombre_paciente_temp");
                        String dni = document.getString("dni_paciente_temp");
                        android.util.Log.d("DEBUG_RECETA", "Paciente: " + pacName + " - DNI: " + dni);

                        tvPaciente.setText(pacName != null ? pacName : "Paciente");
                        tvDni.setText("DNI/Asegurado: " + (dni != null ? dni : "N/A"));

                        // Diagnóstico
                        String dx = document.getString("diagnostico");
                        android.util.Log.d("DEBUG_RECETA", "Diagnóstico: " + dx);
                        tvDiagnostico.setText(dx != null ? dx : "Sin diagnóstico detallado");

                        // Medicamentos
                        Object rawMedicamentos = document.get("medicamentos");
                        if (rawMedicamentos instanceof List) {
                            android.util.Log.d("DEBUG_RECETA", "Lista de medicamentos encontrada.");
                            List<Map<String, Object>> listaMapas = (List<Map<String, Object>>) rawMedicamentos;
                            cargarListaMedicamentosSegura(listaMapas);
                        } else {
                            android.util.Log.e("DEBUG_RECETA", "No se encontró lista de medicamentos o formato incorrecto.");
                        }
                    } else {
                        android.util.Log.e("DEBUG_RECETA", "El documento no existe en Firestore.");
                        Toast.makeText(this, "Error: La receta no existe.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("DEBUG_RECETA", "Error de conexión: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ✨ MÉTODO DE LECTURA SEGURA ✨
    private void cargarListaMedicamentosSegura(List<Map<String, Object>> lista) {
        listaMedicamentosParaPdf.clear();

        for (Map<String, Object> map : lista) {
            try {
                // Leemos cada campo como Object y lo convertimos a String de forma segura
                String nombre = String.valueOf(map.get("nombre"));
                String cantidad = String.valueOf(map.get("cantidad"));
                String indicaciones = String.valueOf(map.get("indicaciones"));

                // El ID puede venir como Long (Firestore) o String. Lo manejamos.
                int idMed = 0;
                Object idObj = map.get("idMedicamento");
                if (idObj instanceof Number) {
                    idMed = ((Number) idObj).intValue();
                }

                // Añadimos a la lista
                listaMedicamentosParaPdf.add(new MedicamentoRecetado(idMed, nombre, cantidad, indicaciones));

            } catch (Exception e) {
                e.printStackTrace(); // Si un item falla, lo saltamos pero mostramos los demás
            }
        }

        // Actualizamos el RecyclerView
        MedicamentoLecturaAdapter adapter = new MedicamentoLecturaAdapter(this, listaMedicamentosParaPdf);
        rvDetalleMedicamentos.setAdapter(adapter);
    }

    private void descargarPdf() {
        if (listaMedicamentosParaPdf.isEmpty()) {
            Toast.makeText(this, "Espere a que carguen los datos...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recolectar textos de la UI
        String folio = tvFolio.getText().toString();
        String fecha = tvFecha.getText().toString();
        String doctor = tvDoctor.getText().toString();
        String especialidad = tvEspecialidad.getText().toString();
        String cmp = tvCmp.getText().toString();
        String paciente = tvPaciente.getText().toString(); // Ojo: Esto estará vacío si no lo llenamos
        String dni = tvDni.getText().toString();
        String diagnostico = tvDiagnostico.getText().toString();

        PdfGenerator.generarPdfReceta(
                this, folio, fecha, doctor, especialidad, cmp, paciente, dni, diagnostico, listaMedicamentosParaPdf
        );
    }
}
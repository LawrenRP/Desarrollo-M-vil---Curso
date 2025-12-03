package com.example.saludmovil.ui.doctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.database.BaseDeDatos;
import com.example.saludmovil.data.Cita;
import com.example.saludmovil.adapters.CitasDoctorAdapter;
import com.example.saludmovil.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MisCitasDoctorActivity extends AppCompatActivity implements CitasDoctorAdapter.OnCitaClickListener {

    private RecyclerView recyclerViewCitas;
    private CitasDoctorAdapter adapter;
    private BaseDeDatos bd;
    private FirebaseFirestore db;
    private String idUsuarioDoctor;
    private ChipGroup chipGroupFiltroDoctor;

    private List<Cita> listaMaestraCitas;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas_doctor);
        ImageButton btnAtras = findViewById(R.id.buttonAtras);
        if (btnAtras != null) {
            btnAtras.setOnClickListener(v -> finish());
        }

        // ✨ Leemos el ID como String
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getString("id_usuario", null);

        if (idUsuarioDoctor == null) {
            Toast.makeText(this, "Error de sesión de doctor.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bd = new BaseDeDatos(this);
        db = FirebaseFirestore.getInstance();
        listaMaestraCitas = new ArrayList<>();

        recyclerViewCitas = findViewById(R.id.recyclerViewCitasDoctor);
        adapter = new CitasDoctorAdapter(this, new ArrayList<>(), this);
        recyclerViewCitas.setAdapter(adapter);
        chipGroupFiltroDoctor = findViewById(R.id.chipGroupFiltroDoctor);
        chipGroupFiltroDoctor.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()){
                filtrarLista("Todos");
                return;
            }
            int idDelChip = checkedIds.get(0);
            Chip chipSeleccionado = group.findViewById(idDelChip);
            if (chipSeleccionado != null){
                filtrarLista(chipSeleccionado.getText().toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        escucharCitasEnTiempoReal();
    }

    private void escucharCitasEnTiempoReal() {
        listenerRegistration = db.collection("citas")
                .whereEqualTo("id_doctor", idUsuarioDoctor) // ✨ Ya funciona con String
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        listaMaestraCitas.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Cita cita = convertirDocumentoACita(doc);
                            listaMaestraCitas.add(cita);
                        }
                        aplicarFiltroActual();
                    }
                });
    }

    // ✨ Método con lectura compatible de IDs (String o Number)
    private Cita convertirDocumentoACita(QueryDocumentSnapshot doc) {
        String idFirestore = doc.getId();

        // ID local (hash para compatibilidad)
        int idCita = idFirestore.hashCode();

        // ✨ CORRECCIÓN CLAVE: Lectura segura del ID de paciente ✨
        String idPaciente = null;

        // 1. Intentamos leerlo como String (formato nuevo)
        try {
            idPaciente = doc.getString("id_paciente");
        } catch (Exception e) {}

        // 2. Si falló o es nulo, intentamos leerlo como Número (formato viejo)
        if (idPaciente == null) {
            Long idLong = doc.getLong("id_paciente");
            if (idLong != null) {
                idPaciente = String.valueOf(idLong);
            } else {
                idPaciente = ""; // Fallback final si no hay ID
            }
        }

        // Hacemos lo mismo para el doctor, por si acaso
        String idDoctor = null;
        try { idDoctor = doc.getString("id_doctor"); } catch(Exception e) {}
        if (idDoctor == null) {
            Long idDocLong = doc.getLong("id_doctor");
            if (idDocLong != null) idDoctor = String.valueOf(idDocLong);
            else idDoctor = "";
        }

        String fecha = doc.getString("fecha");
        String hora = doc.getString("hora");
        String estado = doc.getString("estado");
        String motivo = doc.getString("motivo");

        String nombrePaciente = doc.getString("nombre_paciente_temp");
        if (nombrePaciente == null) nombrePaciente = "Paciente Desconocido";

        // Usamos el nuevo constructor
        return new Cita(idCita, idPaciente, idDoctor, idFirestore, fecha, hora, estado, motivo, nombrePaciente, "Yo");
    }

    private void aplicarFiltroActual() {
        String filtro = "Todos";
        if (!chipGroupFiltroDoctor.getCheckedChipIds().isEmpty()) {
            Chip chip = findViewById(chipGroupFiltroDoctor.getCheckedChipIds().get(0));
            if (chip != null) filtro = chip.getText().toString();
        }
        filtrarLista(filtro);
    }

    private void filtrarLista(String estadoFiltro) {
        ArrayList<Cita> listaFiltrada = new ArrayList<>();

        if (estadoFiltro.equalsIgnoreCase("Todos")) {
            listaFiltrada.addAll(listaMaestraCitas);
        } else {
            for (Cita cita : listaMaestraCitas) {
                if (cita.getEstado() != null && cita.getEstado().equalsIgnoreCase(estadoFiltro)) {
                    listaFiltrada.add(cita);
                }
            }
        }
        adapter.setCitas(listaFiltrada);
    }

    @Override
    public void onCitaClick(Cita cita) {
        if (cita.getEstado().equalsIgnoreCase("agendada")) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Gestionar Cita")
                    .setMessage("Paciente: " + cita.getNombrePaciente() + "\nFecha: " + cita.getFecha())
                .setPositiveButton("Atender Cita", (dialog, which) -> {
                    // --- 🔍 LOGS DE DEPURACIÓN (SALIDA) ---
                    android.util.Log.d("DEBUG_CITA", "--- Intentando abrir Consulta ---");
                    android.util.Log.d("DEBUG_CITA", "ID Cita Firebase: " + cita.getIdFirestore());
                    android.util.Log.d("DEBUG_CITA", "ID Paciente: " + cita.getIdPaciente());

                    Intent intent = new Intent(MisCitasDoctorActivity.this, ConsultaPacienteActivity.class);
                    intent.putExtra("id_paciente", cita.getIdPaciente());
                    intent.putExtra("id_cita_firebase", cita.getIdFirestore());
                    startActivity(intent);
                })
                    .setNegativeButton("Cancelar Cita", (dialog, which) -> {
                        mostrarDialogoConfirmacion(cita.getIdFirestore(), "cancelada");
                    })
                    .setNeutralButton("Volver", null)
                    .show();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Detalle (" + cita.getEstado() + ")")
                    .setMessage("Paciente: " + cita.getNombrePaciente() + "\nMotivo: " + cita.getMotivo())
                    .setPositiveButton("Cerrar", null)
                    .show();
        }
    }

    private void mostrarDialogoConfirmacion(String docId, String nuevoEstado) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Estás seguro?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, confirmar", (dialog, which) -> {
                    actualizarEstadoEnFirebase(docId, nuevoEstado);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void actualizarEstadoEnFirebase(String docId, String nuevoEstado) {
        db.collection("citas").document(docId)
                .update("estado", nuevoEstado)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cita actualizada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listenerRegistration != null) listenerRegistration.remove();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bd != null) bd.close();
    }
}

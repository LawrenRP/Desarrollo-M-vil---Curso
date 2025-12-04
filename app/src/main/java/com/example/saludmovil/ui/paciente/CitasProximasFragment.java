package com.example.saludmovil.ui.paciente;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.CitasPacienteAdapter;
import com.example.saludmovil.data.Cita;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CitasProximasFragment extends Fragment implements CitasPacienteAdapter.OnCitaClickListener {

    private RecyclerView recyclerView;
    private LinearLayout layoutVacio;
    private CitasPacienteAdapter adapter;
    private FirebaseFirestore db;
    private String idUsuarioPaciente; // ✨ AHORA ES STRING
    private ListenerRegistration listenerRegistration;

    public CitasProximasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_citas_lista, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences("datos_usuario", Context.MODE_PRIVATE);
        idUsuarioPaciente = sp.getString("id_usuario", null); // ✨ LEER STRING

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewCitasFragment);
        layoutVacio = view.findViewById(R.id.layoutSinCitasFragment); // Asegúrate que existe este ID o quítalo

        adapter = new CitasPacienteAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void escucharCitasEnTiempoReal() {
        if (idUsuarioPaciente == null) return;

        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        listenerRegistration = db.collection("citas")
                .whereEqualTo("id_paciente", idUsuarioPaciente)
                .whereEqualTo("estado", "agendada")
                .whereGreaterThanOrEqualTo("fecha", hoy)
                .orderBy("fecha", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        // Si falla, revisa el Logcat por el índice
                        return;
                    }

                    if (snapshots != null) {
                        List<Cita> lista = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            lista.add(convertirDocumentoACita(doc));
                        }
                        adapter.submitList(lista);

                        // Manejo de vista vacía (opcional)
                        if (lista.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
                        }
                    }
                });
    }

    // Método auxiliar para convertir
    private Cita convertirDocumentoACita(QueryDocumentSnapshot doc) {
        String idFirestore = doc.getId();

        // IDs numéricos (legacy)
        Long idCitaLong = doc.getLong("id_cita_sqlite");
        int idCita = idCitaLong != null ? idCitaLong.intValue() : 0;

        // Intentamos leer IDs numéricos por si acaso, aunque usamos Strings
        int idPacienteInt = 0;
        int idDoctorInt = 0;

        String fecha = doc.getString("fecha");
        String hora = doc.getString("hora");
        String estado = doc.getString("estado");
        String motivo = doc.getString("motivo");
        String nombreDoctor = doc.getString("nombre_doctor_temp");
        if (nombreDoctor == null) nombreDoctor = "Dr. Asignado";

        // ✨ Constructor actualizado (10 argumentos)
        return new Cita(
                idCita,
                String.valueOf(idPacienteInt), // int -> String
                String.valueOf(idDoctorInt),   // int -> String
                idFirestore,
                fecha,
                hora,
                estado,
                motivo,
                "Yo",
                nombreDoctor
        );
    }

    @Override
    public void onCitaClick(Cita cita) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Cancelar Cita")
                .setMessage("¿Deseas cancelar tu cita con el " + cita.getNombreDoctor() + "?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    cancelarCita(cita.getIdFirestore());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelarCita(String documentId) {
        db.collection("citas").document(documentId)
                .update("estado", "cancelada")
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Cita cancelada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cancelar", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResume() {
        super.onResume();
        escucharCitasEnTiempoReal();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
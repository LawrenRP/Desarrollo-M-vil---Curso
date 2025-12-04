package com.example.saludmovil.ui.paciente;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CitasPasadasFragment extends Fragment implements CitasPacienteAdapter.OnCitaClickListener {

    private RecyclerView recyclerView;
    private LinearLayout layoutVacio;
    private CitasPacienteAdapter adapter;
    private FirebaseFirestore db;
    private String idUsuarioPaciente; // ✨ STRING
    private ListenerRegistration listenerRegistration;

    public CitasPasadasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_citas_lista, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences("datos_usuario", Context.MODE_PRIVATE);
        idUsuarioPaciente = sp.getString("id_usuario", null); // ✨ STRING

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewCitasFragment);
        layoutVacio = view.findViewById(R.id.layoutSinCitasFragment);

        adapter = new CitasPacienteAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void escucharHistorialEnTiempoReal() {
        if (idUsuarioPaciente == null) return;

        listenerRegistration = db.collection("citas")
                .whereEqualTo("id_paciente", idUsuarioPaciente)
                .whereIn("estado", Arrays.asList("completada", "cancelada", "Completada", "Cancelada")) // Aceptamos Mayús/Minús por seguridad
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    if (snapshots != null) {
                        List<Cita> lista = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            lista.add(convertirDocumentoACita(doc));
                        }
                        adapter.submitList(lista);

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

    private Cita convertirDocumentoACita(QueryDocumentSnapshot doc) {
        String idFirestore = doc.getId();

        Long idCitaLong = doc.getLong("id_cita_sqlite");
        int idCita = idCitaLong != null ? idCitaLong.intValue() : 0;

        // ✨ CORRECCIÓN: Definimos las variables que faltaban ✨
        int idPacienteInt = 0;
        int idDoctorInt = 0;

        String fecha = doc.getString("fecha");
        String hora = doc.getString("hora");
        String estado = doc.getString("estado");
        String motivo = doc.getString("motivo");
        String nombreDoctor = doc.getString("nombre_doctor_temp");
        if (nombreDoctor == null) nombreDoctor = "Dr. Asignado";

        return new Cita(
                idCita,
                String.valueOf(idPacienteInt), // Ahora sí existen
                String.valueOf(idDoctorInt),   // Ahora sí existen
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
                .setTitle("Detalle de Cita")
                .setMessage("Doctor: " + cita.getNombreDoctor() + "\n" +
                        "Fecha: " + cita.getFecha() + " - " + cita.getHora() + "\n" +
                        "Estado: " + cita.getEstado())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        escucharHistorialEnTiempoReal();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
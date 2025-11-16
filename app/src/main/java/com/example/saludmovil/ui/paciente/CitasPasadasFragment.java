package com.example.saludmovil.ui.paciente;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.CitasPacienteAdapter;
import com.example.saludmovil.data.CitaParaPaciente;
import com.example.saludmovil.database.BaseDeDatos;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class CitasPasadasFragment extends Fragment implements CitasPacienteAdapter.OnCitaClickListener {

    private static final String TAG = "MiAppDebug";
    private RecyclerView recyclerView;
    private CitasPacienteAdapter adapter;
    private ArrayList<CitaParaPaciente> listaDeCitas;
    private BaseDeDatos bd;
    private int idUsuarioPaciente;

    public CitasPasadasFragment() {
        Log.d(TAG, "CitasPasadasFragment: Constructor llamado.");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "CitasPasadasFragment: onCreateView - VISTA INFLADA.");

        View view = inflater.inflate(R.layout.fragment_citas_lista, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences("datos_usuario", Context.MODE_PRIVATE);
        idUsuarioPaciente = sp.getInt("id_usuario", -1);

        bd = new BaseDeDatos(getContext());
        listaDeCitas = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewCitasFragment);
        adapter = new CitasPacienteAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void cargarCitas() {
        if (bd == null) {
            bd = new BaseDeDatos(getContext());
        }
        listaDeCitas.clear();
        Cursor cursor = bd.getPasadasCitasPaciente(idUsuarioPaciente);

        if (cursor != null && cursor.moveToFirst()) {
            int idCitaIndex = cursor.getColumnIndex("id");
            int fechaIndex = cursor.getColumnIndex("fecha");
            int horaIndex = cursor.getColumnIndex("hora");
            int estadoIndex = cursor.getColumnIndex("estado");
            int motivoIndex = cursor.getColumnIndex("motivo");
            int doctorIndex = cursor.getColumnIndex("nombre_completo");

            do {
                if (idCitaIndex != -1 && fechaIndex != -1 && horaIndex != -1 && estadoIndex != -1 &&
                        motivoIndex != -1 && doctorIndex != -1) {

                    int idCita = cursor.getInt(idCitaIndex);
                    String fecha = cursor.getString(fechaIndex);
                    String hora = cursor.getString(horaIndex);
                    String estado = cursor.getString(estadoIndex);
                    String motivo = cursor.getString(motivoIndex);
                    String nombreDoctor = cursor.getString(doctorIndex);

                    listaDeCitas.add(new CitaParaPaciente(idCita, fecha, hora, estado, motivo, nombreDoctor));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.submitList(listaDeCitas);
        Log.d(TAG, "Citas 'Pasadas' encontradas: " + listaDeCitas.size());
    }
    @Override
    public void onCitaClick(CitaParaPaciente cita) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Detalle de Cita (" + cita.getEstado() + ")")
                .setMessage("Doctor: " + cita.getNombreDoctor() +
                        "\nFecha: " + cita.getFecha() + " - " + cita.getHora() +
                        "\n\nMotivo: " + cita.getMotivo())
                .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "CitasPasadasFragment: onResume - llamando a cargarCitas().");
        cargarCitas();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bd != null) {
            bd.close();
        }
    }
}
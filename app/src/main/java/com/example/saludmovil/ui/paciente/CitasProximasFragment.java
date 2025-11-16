package com.example.saludmovil.ui.paciente;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
public class CitasProximasFragment extends Fragment implements CitasPacienteAdapter.OnCitaClickListener {

    private static final String TAG = "MiAppDebug";
    private RecyclerView recyclerView;
    private CitasPacienteAdapter adapter;
    private ArrayList<CitaParaPaciente> listaDeCitas;
    private BaseDeDatos bd;
    private int idUsuarioPaciente;
    public CitasProximasFragment() {
        Log.d(TAG, "CitasProximasFragment: Constructor llamado.");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "CitasProximasFragment: onCreateView - VISTA INFLADA.");
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

        Cursor cursor = bd.getProximasCitasPaciente(idUsuarioPaciente);

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
        Log.d(TAG, "Citas 'Próximas' encontradas: " + listaDeCitas.size());
    }

    @Override
    public void onCitaClick(CitaParaPaciente cita) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Cancelar Cita")
                .setMessage("¿Estás seguro de que deseas cancelar tu cita con el " + cita.getNombreDoctor() + "?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    actualizarCita(cita.getIdCita(), "Cancelada");
                })
                .setNegativeButton("No, volver", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void actualizarCita(int idCita, String nuevoEstado) {
        if (bd == null) {
            bd = new BaseDeDatos(getContext());
        }
        boolean exito = bd.actualizarEstadoCita(idCita, nuevoEstado);
        if (exito) {
            Toast.makeText(getContext(), "Cita actualizada a: " + nuevoEstado, Toast.LENGTH_SHORT).show();
            cargarCitas();
        } else {
            Toast.makeText(getContext(), "Error al actualizar la cita", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "CitasProximasFragment: onResume - llamando a cargarCitas().");
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
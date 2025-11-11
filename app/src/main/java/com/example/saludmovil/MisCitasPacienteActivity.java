package com.example.saludmovil;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class MisCitasPacienteActivity extends AppCompatActivity implements CitasPacienteAdapter.OnCitaClickListener {

    private RecyclerView recyclerViewCitas;
    private CitasPacienteAdapter adapter;
    private ArrayList<CitaParaPaciente> listaDeCitas;
    private BaseDeDatos bd;
    private int idUsuarioPaciente;

    private ChipGroup chipGroupFiltroPaciente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas_paciente);

        Toolbar toolbar = findViewById(R.id.toolbarMisCitasPaciente);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioPaciente = sp.getInt("id_usuario", -1);
        if (idUsuarioPaciente == -1) { /* ... (código de error) ... */ }

        recyclerViewCitas = findViewById(R.id.recyclerViewCitasPaciente);
        listaDeCitas = new ArrayList<>();
        bd = new BaseDeDatos(this);

        adapter = new CitasPacienteAdapter(this, listaDeCitas, this);
        recyclerViewCitas.setAdapter(adapter);
        chipGroupFiltroPaciente = findViewById(R.id.chipGroupFiltroPaciente);
        chipGroupFiltroPaciente.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                adapter.filtrarPorEstado("Todos");
                return;
            }
            int idDelChip = checkedIds.get(0);
            Chip chipSeleccionado = group.findViewById(idDelChip);
            if (chipSeleccionado != null) {
                adapter.filtrarPorEstado(chipSeleccionado.getText().toString());
            }
        });
        cargarCitasDelPaciente();
    }

    private void cargarCitasDelPaciente() {
        listaDeCitas.clear();
        Cursor cursor = bd.getTodasCitasPaciente(idUsuarioPaciente);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idCita = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"));
                String hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"));
                String estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"));
                String motivo = cursor.getString(cursor.getColumnIndexOrThrow("motivo"));
                String nombreDoctor = cursor.getString(cursor.getColumnIndexOrThrow("nombre_completo"));

                listaDeCitas.add(new CitaParaPaciente(idCita, fecha, hora, estado, motivo, nombreDoctor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setCitas(listaDeCitas);
        List<Integer> checkedIds = chipGroupFiltroPaciente.getCheckedChipIds();
        String filtroActual = "Todos";
        if (!checkedIds.isEmpty()) {
            Chip chipSeleccionado = findViewById(checkedIds.get(0));
            if (chipSeleccionado != null) {
                filtroActual = chipSeleccionado.getText().toString();
            }
        }
        adapter.filtrarPorEstado(filtroActual);
    }

    @Override
    public void onCitaClick(CitaParaPaciente cita) {
        if (cita.getEstado().equalsIgnoreCase("agendada")) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cancelar Cita")
                    .setMessage("¿Estás seguro de que deseas cancelar tu cita con el " + cita.getNombreDoctor() + "?")
                    .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                        actualizarCita(cita.getIdCita(), "Cancelada");
                    })
                    .setNegativeButton("No, volver", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            Toast.makeText(this, "Esta cita ya está " + cita.getEstado().toLowerCase(), Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarCita(int idCita, String nuevoEstado) {
        boolean exito = bd.actualizarEstadoCita(idCita, nuevoEstado);
        if (exito) {
            Toast.makeText(this, "Cita actualizada a: " + nuevoEstado, Toast.LENGTH_SHORT).show();
            cargarCitasDelPaciente(); // Refrescamos la lista para ver el cambio
        } else {
            Toast.makeText(this, "Error al actualizar la cita", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bd != null) {
            bd.close();
        }
    }
}
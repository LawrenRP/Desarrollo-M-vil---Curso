package com.example.saludmovil.ui.doctor;

import android.content.SharedPreferences;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.List;

public class MisCitasDoctorActivity extends AppCompatActivity implements CitasDoctorAdapter.OnCitaClickListener {

    private RecyclerView recyclerViewCitas;
    private CitasDoctorAdapter adapter;
    private ArrayList<Cita> listaDeCitas;
    private BaseDeDatos bd;
    private int idUsuarioDoctor;
    private ChipGroup chipGroupFiltroDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas_doctor);

        ImageButton btnAtras = findViewById(R.id.buttonAtras);
        btnAtras.setOnClickListener(v -> finish());


        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getInt("id_usuario", -1);
        if (idUsuarioDoctor == -1) {
            Toast.makeText(this, "Error de sesión de doctor.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        recyclerViewCitas = findViewById(R.id.recyclerViewCitasDoctor);
        listaDeCitas = new ArrayList<>();
        bd = new BaseDeDatos(this);
        adapter = new CitasDoctorAdapter(this, listaDeCitas, this);
        recyclerViewCitas.setAdapter(adapter);

        chipGroupFiltroDoctor = findViewById(R.id.chipGroupFiltroDoctor);
        chipGroupFiltroDoctor.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()){
                adapter.filtrarPorEstado("Todos");
                return;
            }
            int idDelChip = checkedIds.get(0);

            Chip chipSeleccionado = group.findViewById(idDelChip);
            if (chipSeleccionado != null){
                String estado = chipSeleccionado.getText().toString();
                adapter.filtrarPorEstado(estado);
            }
        });
        cargarCitasDelDoctor();
    }

    private void cargarCitasDelDoctor() {
        listaDeCitas.clear();
        Cursor cursor = bd.getTodasCitasDoctor(idUsuarioDoctor);

        if (cursor != null && cursor.moveToFirst()) {
            int idCitaIndex = cursor.getColumnIndex("id");
            int fechaIndex = cursor.getColumnIndex("fecha");
            int horaIndex = cursor.getColumnIndex("hora");
            int estadoIndex = cursor.getColumnIndex("estado");
            int motivoIndex = cursor.getColumnIndex("motivo");
            int nombreIndex = cursor.getColumnIndex("nombre");
            int apellidoIndex = cursor.getColumnIndex("apellido");

            do {
                if (idCitaIndex != -1 && fechaIndex != -1 && horaIndex != -1 && estadoIndex != -1 &&
                        motivoIndex != -1 && nombreIndex != -1 && apellidoIndex != -1) {

                    int idCita = cursor.getInt(idCitaIndex);
                    String fecha = cursor.getString(fechaIndex);
                    String hora = cursor.getString(horaIndex);
                    String estado = cursor.getString(estadoIndex);
                    String motivo = cursor.getString(motivoIndex);
                    String nombrePaciente = cursor.getString(nombreIndex) + " " + cursor.getString(apellidoIndex);

                    listaDeCitas.add(new Cita(idCita, fecha, hora, estado, motivo, nombrePaciente));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setCitas(listaDeCitas);
        List<Integer> checkedIds = chipGroupFiltroDoctor.getCheckedChipIds();
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
    public void onCitaClick(Cita cita) {
        if (cita.getEstado().equalsIgnoreCase("agendada")) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Gestionar Cita")
                    .setMessage("Paciente: " + cita.getNombrePaciente() + "\nFecha: " + cita.getFecha() + " - " + cita.getHora())
                    .setPositiveButton("Completada", (dialog, which) -> {
                        mostrarDialogoConfirmacion(cita.getIdCita(), "Completada");
                    })
                    .setNegativeButton("Cancelar Cita", (dialog, which) -> {
                        mostrarDialogoConfirmacion(cita.getIdCita(), "Cancelada");
                    })
                    .setNeutralButton("Volver", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();

        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Detalle de Cita (" + cita.getEstado() + ")")
                    .setMessage("Paciente: " + cita.getNombrePaciente() +
                            "\nFecha: " + cita.getFecha() + " - " + cita.getHora() +
                            "\n\nMotivo: " + cita.getMotivo())
                    .setPositiveButton("Entendido", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    private void mostrarDialogoConfirmacion(int idCita, String nuevoEstado) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Estás seguro?")
                .setMessage("Vas a marcar esta cita como '" + nuevoEstado + "'. Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, confirmar", (dialog, which) -> {
                    actualizarCita(idCita, nuevoEstado);
                })
                .setNegativeButton("No, volver", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void actualizarCita(int idCita, String nuevoEstado) {
        boolean exito = bd.actualizarEstadoCita(idCita, nuevoEstado);
        if (exito) {
            Toast.makeText(this, "Cita actualizada a: " + nuevoEstado, Toast.LENGTH_SHORT).show();
            cargarCitasDelDoctor();

            List<Integer> checkedIds = chipGroupFiltroDoctor.getCheckedChipIds();
            String filtroActual = "Todos";
            if (!checkedIds.isEmpty()) {
                Chip chipSeleccionado = findViewById(checkedIds.get(0));
                if (chipSeleccionado != null) {
                    filtroActual = chipSeleccionado.getText().toString();
                }
            }
            adapter.filtrarPorEstado(filtroActual);
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
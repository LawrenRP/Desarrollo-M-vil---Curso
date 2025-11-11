package com.example.saludmovil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.data.CitaParaPaciente;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class CitasPacienteAdapter extends RecyclerView.Adapter<CitasPacienteAdapter.CitaPacienteViewHolder> {

    private ArrayList<CitaParaPaciente> listaCitasCompleta;
    private ArrayList<CitaParaPaciente> listaCitasFiltrada;
    private Context context;
    private OnCitaClickListener listener;
    public interface OnCitaClickListener {
        void onCitaClick(CitaParaPaciente cita);
    }
    public CitasPacienteAdapter(Context context, ArrayList<CitaParaPaciente> listaCitas, OnCitaClickListener listener) {
        this.context = context;
        this.listaCitasCompleta = listaCitas;
        this.listaCitasFiltrada = new ArrayList<>(listaCitasCompleta);
        this.listener = listener;
    }
    public class CitaPacienteViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombreDoctor, tvFechaHora, tvMotivo;
        Chip chipEstado;

        public CitaPacienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreDoctor = itemView.findViewById(R.id.tvNombreDoctorCita);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHoraCita);
            tvMotivo = itemView.findViewById(R.id.tvMotivoCita);
            chipEstado = itemView.findViewById(R.id.chipEstadoCita);
        }
        void bind(CitaParaPaciente cita) {
            tvNombreDoctor.setText(cita.getNombreDoctor());
            tvFechaHora.setText(cita.getFecha() + " - " + cita.getHora());
            tvMotivo.setText(cita.getMotivo());
            chipEstado.setText(cita.getEstado());

            if ("Completada".equalsIgnoreCase(cita.getEstado())) {

            } else if ("Cancelada".equalsIgnoreCase(cita.getEstado())) {

            } else {

            }
            itemView.setOnClickListener(v -> listener.onCitaClick(cita));
        }
    }
    @NonNull
    @Override
    public CitaPacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_paciente, parent, false);
        return new CitaPacienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaPacienteViewHolder holder, int position) {
        holder.bind(listaCitasFiltrada.get(position));
    }
    @Override
    public int getItemCount() {
        return listaCitasFiltrada.size();
    }

    public void filtrarPorEstado(String estado) {
        listaCitasFiltrada.clear();

        if (estado.equalsIgnoreCase("Todos")) {
            listaCitasFiltrada.addAll(listaCitasCompleta);
        } else {
            for (CitaParaPaciente cita : listaCitasCompleta) {
                if (cita.getEstado().equalsIgnoreCase(estado)) {
                    listaCitasFiltrada.add(cita);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setCitas(ArrayList<CitaParaPaciente> nuevasCitas) {
        this.listaCitasCompleta = nuevasCitas;
        // Reseteamos el filtro cada vez que se carga la lista
        filtrarPorEstado("Todos");
    }
}
package com.example.saludmovil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.data.Cita;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class CitasDoctorAdapter extends RecyclerView.Adapter<CitasDoctorAdapter.CitaViewHolder>{
    private ArrayList<Cita> listaCitasCompleta;
    private ArrayList<Cita> listaCitasFiltrada;
    private Context context;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }
    public CitasDoctorAdapter(Context context, ArrayList<Cita> listaCitas, OnCitaClickListener listener) {
        this.context = context;
        this.listaCitasCompleta = listaCitas;
        this.listaCitasFiltrada = new ArrayList<>(listaCitas);
        this.listener = listener;
    }
    public class CitaViewHolder extends RecyclerView.ViewHolder{
        TextView tvNombrePaciente, tvFechaHora, tvMotivo;
        Chip chipEstado;
        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePaciente = itemView.findViewById(R.id.tvNombrePacienteCita);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHoraCita);
            tvMotivo = itemView.findViewById(R.id.tvMotivoCita);
            chipEstado = itemView.findViewById(R.id.chipEstadoCita);
        }
        void bind(Cita cita) {
            tvNombrePaciente.setText(cita.getNombrePaciente());
            tvFechaHora.setText(cita.getFecha() + " - " + cita.getHora());
            tvMotivo.setText(cita.getMotivo());
            chipEstado.setText(cita.getEstado());
            if ("Completada".equalsIgnoreCase(cita.getEstado())) {
                chipEstado.setChipBackgroundColorResource(R.color.estado_completada_fondo);
                chipEstado.setTextColor(ContextCompat.getColor(context, R.color.estado_completada_texto));
            } else if ("Cancelada".equalsIgnoreCase(cita.getEstado())) {
                chipEstado.setChipBackgroundColorResource(R.color.estado_cancelada_fondo);
                chipEstado.setTextColor(ContextCompat.getColor(context, R.color.estado_cancelada_texto));
            } else {
                chipEstado.setChipBackgroundColorResource(R.color.estado_agendada_fondo);
                chipEstado.setTextColor(ContextCompat.getColor(context, R.color.estado_agendada_texto));
            }

            itemView.setOnClickListener(v -> listener.onCitaClick(cita));
        }
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_doctor, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = listaCitasFiltrada.get(position);
        holder.bind(cita);
    }

    @Override
    public int getItemCount() {
        return listaCitasFiltrada.size();
    }
    public void setCitas(ArrayList<Cita> nuevasCitas) {
        this.listaCitasCompleta = nuevasCitas;
        this.listaCitasFiltrada = new ArrayList<>(nuevasCitas);
        notifyDataSetChanged();
    }

    public void filtrarPorEstado(String estado){
        listaCitasFiltrada.clear();
        if(estado.equalsIgnoreCase("Todos")){
            listaCitasFiltrada.addAll(listaCitasCompleta);
        } else {
            for (Cita cita : listaCitasCompleta){
                if(cita.getEstado().equalsIgnoreCase(estado)){
                    listaCitasFiltrada.add(cita);
                }
            }
        }

        notifyDataSetChanged();
    }

}

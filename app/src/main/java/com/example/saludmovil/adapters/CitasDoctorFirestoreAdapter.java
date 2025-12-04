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
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CitasDoctorFirestoreAdapter extends RecyclerView.Adapter<CitasDoctorFirestoreAdapter.ViewHolder> {

    private Context context;
    private List<QueryDocumentSnapshot> listaCitas;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(QueryDocumentSnapshot doc);
    }

    public CitasDoctorFirestoreAdapter(Context context, OnCitaClickListener listener) {
        this.context = context;
        this.listaCitas = new ArrayList<>();
        this.listener = listener;
    }

    public void setCitas(List<QueryDocumentSnapshot> nuevasCitas) {
        this.listaCitas = nuevasCitas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(listaCitas.get(position));
    }

    @Override
    public int getItemCount() {
        return listaCitas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePaciente, tvFechaHora, tvMotivo;
        Chip chipEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePaciente = itemView.findViewById(R.id.tvNombrePacienteCita);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHoraCita);
            tvMotivo = itemView.findViewById(R.id.tvMotivoCita);
            chipEstado = itemView.findViewById(R.id.chipEstadoCita);
        }

        void bind(QueryDocumentSnapshot doc) {
            String paciente = doc.getString("nombre_paciente_temp");
            String fecha = doc.getString("fecha");
            String hora = doc.getString("hora");
            String motivo = doc.getString("motivo");
            String estado = doc.getString("estado");

            tvNombrePaciente.setText(paciente != null ? paciente : "Paciente Desconocido");
            tvFechaHora.setText(fecha + " - " + hora);
            tvMotivo.setText(motivo != null ? motivo : "Sin motivo");

            if (estado != null) {
                chipEstado.setText(estado.substring(0, 1).toUpperCase() + estado.substring(1));
                configurarColorChip(estado);
            }

            itemView.setOnClickListener(v -> listener.onCitaClick(doc));
        }

        private void configurarColorChip(String estado) {
            int colorFondo, colorTexto;
            if ("completada".equalsIgnoreCase(estado)) {
                colorFondo = R.color.estado_completada_fondo;
                colorTexto = R.color.estado_completada_texto;
            } else if ("cancelada".equalsIgnoreCase(estado)) {
                colorFondo = R.color.estado_cancelada_fondo;
                colorTexto = R.color.estado_cancelada_texto;
            } else {
                colorFondo = R.color.estado_agendada_fondo;
                colorTexto = R.color.estado_agendada_texto;
            }
            try {
                chipEstado.setChipBackgroundColorResource(colorFondo);
                chipEstado.setTextColor(ContextCompat.getColor(context, colorTexto));
            } catch (Exception e) {}
        }
    }
}
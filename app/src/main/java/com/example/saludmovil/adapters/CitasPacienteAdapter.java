package com.example.saludmovil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludmovil.R;
import com.example.saludmovil.data.Cita;
import com.google.android.material.chip.Chip;

public class CitasPacienteAdapter extends ListAdapter<Cita, CitasPacienteAdapter.CitaPacienteViewHolder> {

    private Context context;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }

    public CitasPacienteAdapter(Context context, OnCitaClickListener listener) {
        super(CITA_COMPARATOR);
        this.context = context;
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

        void bind(Cita cita) {
            // ✨ Usamos getNombreDoctor()
            tvNombreDoctor.setText("Dr. " + cita.getNombreDoctor());
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
    public CitaPacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_paciente, parent, false);
        return new CitaPacienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaPacienteViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
    private static final DiffUtil.ItemCallback<Cita> CITA_COMPARATOR = new DiffUtil.ItemCallback<Cita>() {
        @Override
        public boolean areItemsTheSame(@NonNull Cita oldItem, @NonNull Cita newItem) {
            // Comparamos por ID de Firestore si existe, o ID local
            if (oldItem.getIdFirestore() != null && newItem.getIdFirestore() != null) {
                return oldItem.getIdFirestore().equals(newItem.getIdFirestore());
            }
            return oldItem.getIdCita() == newItem.getIdCita();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Cita oldItem, @NonNull Cita newItem) {
            return oldItem.getEstado().equals(newItem.getEstado());
        }
    };
}
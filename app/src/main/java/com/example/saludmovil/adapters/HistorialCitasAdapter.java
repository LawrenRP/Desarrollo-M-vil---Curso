package com.example.saludmovil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludmovil.R;
import com.example.saludmovil.data.Cita;
import com.google.android.material.chip.Chip;

public class HistorialCitasAdapter extends ListAdapter<Cita, HistorialCitasAdapter.HistorialViewHolder> {

    private Context context;

    public HistorialCitasAdapter(Context context) {
        super(CITA_COMPARATOR);
        this.context = context;
    }

    public class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvFechaHora, tvMotivo;
        Chip chipEstado;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaHora = itemView.findViewById(R.id.tvHistorialFechaHora);
            tvMotivo = itemView.findViewById(R.id.tvHistorialMotivo);
            chipEstado = itemView.findViewById(R.id.chipHistorialEstado);
        }

        void bind(Cita cita) {
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
        }
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    private static final DiffUtil.ItemCallback<Cita> CITA_COMPARATOR = new DiffUtil.ItemCallback<Cita>() {
        @Override
        public boolean areItemsTheSame(@NonNull Cita oldItem, @NonNull Cita newItem) {
            if (oldItem.getIdFirestore() != null) return oldItem.getIdFirestore().equals(newItem.getIdFirestore());
            return oldItem.getIdCita() == newItem.getIdCita();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Cita oldItem, @NonNull Cita newItem) {
            return oldItem.getEstado().equals(newItem.getEstado());
        }
    };
}
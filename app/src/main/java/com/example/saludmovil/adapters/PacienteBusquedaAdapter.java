package com.example.saludmovil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.data.Paciente;
import com.example.saludmovil.utils.Validaciones;

public class PacienteBusquedaAdapter extends ListAdapter<Paciente, PacienteBusquedaAdapter.PacienteViewHolder> {

    private OnPacienteClickListener listener;
    public interface OnPacienteClickListener {
        void onPacienteClick(Paciente paciente);
    }
    public PacienteBusquedaAdapter(OnPacienteClickListener listener) {
        super(PACIENTE_COMPARATOR);
        this.listener = listener;
    }
    public class PacienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDni;

        public PacienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombrePacienteBusqueda);
            tvDni = itemView.findViewById(R.id.tvDniPacienteBusqueda);
        }

        void bind(Paciente paciente) {
            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
            tvNombre.setText(Validaciones.capitalizarPalabras(nombreCompleto));
            tvDni.setText("DNI: " + paciente.getDni());
            itemView.setOnClickListener(v -> listener.onPacienteClick(paciente));
        }
    }
    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paciente_busqueda, parent, false);
        return new PacienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        Paciente paciente = getItem(position);
        holder.bind(paciente);
    }

    private static final DiffUtil.ItemCallback<Paciente> PACIENTE_COMPARATOR = new DiffUtil.ItemCallback<Paciente>() {
        @Override
        public boolean areItemsTheSame(@NonNull Paciente oldItem, @NonNull Paciente newItem) {
            return oldItem.getIdUsuario() == newItem.getIdUsuario();
        }
        @Override
        public boolean areContentsTheSame(@NonNull Paciente oldItem, @NonNull Paciente newItem) {
            return oldItem.getIdUsuario() == newItem.getIdUsuario() &&
                    oldItem.getDni().equals(newItem.getDni());
        }
    };
}
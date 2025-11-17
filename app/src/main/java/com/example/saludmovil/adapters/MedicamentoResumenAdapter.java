package com.example.saludmovil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.data.MedicamentoRecetado;

import java.util.ArrayList;

public class MedicamentoResumenAdapter extends RecyclerView.Adapter<MedicamentoResumenAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MedicamentoRecetado> listaMedicamentos;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onEliminarClick(int position);
    }

    public MedicamentoResumenAdapter(Context context, ArrayList<MedicamentoRecetado> listaMedicamentos, OnItemClickListener listener) {
        this.context = context;
        this.listaMedicamentos = listaMedicamentos;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCantidad, tvIndicaciones;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMedicamentoRecetado);
            tvCantidad = itemView.findViewById(R.id.tvCantidadRecetada);
            tvIndicaciones = itemView.findViewById(R.id.tvIndicacionesRecetadas);
            btnEliminar = itemView.findViewById(R.id.btnEliminarMedicamento);
        }

        void bind(MedicamentoRecetado medicamento, int position) {
            tvNombre.setText(medicamento.getNombre());
            tvCantidad.setText(medicamento.getCantidad());
            tvIndicaciones.setText("Indicaciones: " + medicamento.getIndicaciones());

            btnEliminar.setOnClickListener(v -> listener.onEliminarClick(position));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicamento_recetado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(listaMedicamentos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos.size();
    }
    public void actualizarLista(ArrayList<MedicamentoRecetado> nuevaLista) {
        this.listaMedicamentos = nuevaLista;
        notifyDataSetChanged();
    }
}
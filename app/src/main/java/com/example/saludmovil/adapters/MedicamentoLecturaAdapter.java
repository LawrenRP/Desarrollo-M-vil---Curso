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

public class MedicamentoLecturaAdapter extends RecyclerView.Adapter<MedicamentoLecturaAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MedicamentoRecetado> listaMedicamentos;

    public MedicamentoLecturaAdapter(Context context, ArrayList<MedicamentoRecetado> listaMedicamentos) {
        this.context = context;
        this.listaMedicamentos = listaMedicamentos;
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

        void bind(MedicamentoRecetado medicamento) {
            tvNombre.setText(medicamento.getNombre());
            tvCantidad.setText(medicamento.getCantidad());
            tvIndicaciones.setText("Indicaciones: " + medicamento.getIndicaciones());
            btnEliminar.setVisibility(View.GONE);
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
        holder.bind(listaMedicamentos.get(position));
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos.size();
    }
}
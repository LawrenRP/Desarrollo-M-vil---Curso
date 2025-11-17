package com.example.saludmovil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludmovil.R;
import com.example.saludmovil.data.Receta;
import java.util.ArrayList;

public class RecetasAdapter extends RecyclerView.Adapter<RecetasAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Receta> listaRecetas;
    private OnRecetaClickListener listener;

    public interface OnRecetaClickListener {
        void onRecetaClick(int idReceta);
    }

    public RecetasAdapter(Context context, ArrayList<Receta> listaRecetas, OnRecetaClickListener listener) {
        this.context = context;
        this.listaRecetas = listaRecetas;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvDoctor, tvEspecialidad;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFechaReceta);
            tvDoctor = itemView.findViewById(R.id.tvDoctorReceta);
            tvEspecialidad = itemView.findViewById(R.id.tvEspecialidadReceta);
        }

        void bind(Receta receta) {
            tvFecha.setText(receta.getFechaEmision());
            tvDoctor.setText("Dr. " + receta.getNombreDoctor());
            tvEspecialidad.setText(receta.getEspecialidad());

            itemView.setOnClickListener(v -> listener.onRecetaClick(receta.getId()));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_receta_resumen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(listaRecetas.get(position));
    }

    @Override
    public int getItemCount() { return listaRecetas.size(); }
}
package com.example.saludmovil.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludmovil.R;
import com.example.saludmovil.data.Medicamento;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class MedicamentoSeleccionAdapter extends RecyclerView.Adapter<MedicamentoSeleccionAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Medicamento> listaMedicamentos;

    public MedicamentoSeleccionAdapter(Context context, ArrayList<Medicamento> listaMedicamentos) {
        this.context = context;
        this.listaMedicamentos = listaMedicamentos;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvStock;
        CheckBox checkBox;
        LinearLayout layoutCampos;
        TextInputEditText inputCantidad, inputIndicaciones;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMedicamento);
            tvStock = itemView.findViewById(R.id.tvStock);
            checkBox = itemView.findViewById(R.id.checkboxMedicamento);
            layoutCampos = itemView.findViewById(R.id.layoutCamposAdicionales);
            inputCantidad = itemView.findViewById(R.id.inputCantidad);
            inputIndicaciones = itemView.findViewById(R.id.inputIndicaciones);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicamento_seleccionable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicamento medicamento = listaMedicamentos.get(position);
        holder.tvNombre.setText(medicamento.getNombre() + " " + medicamento.getPresentacion());
        holder.tvStock.setText("Stock: " + medicamento.getStock() + " unidades");
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(medicamento.isSeleccionado());
        holder.layoutCampos.setVisibility(medicamento.isSeleccionado() ? View.VISIBLE : View.GONE);
        holder.inputCantidad.setText(medicamento.getCantidadUsuario());
        holder.inputIndicaciones.setText(medicamento.getIndicacionesUsuario());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            medicamento.setSeleccionado(isChecked);
            holder.layoutCampos.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        if (holder.inputCantidad.getTag() instanceof TextWatcher) {
            holder.inputCantidad.removeTextChangedListener((TextWatcher) holder.inputCantidad.getTag());
        }
        TextWatcher cantidadWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                medicamento.setCantidadUsuario(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        holder.inputCantidad.addTextChangedListener(cantidadWatcher);
        holder.inputCantidad.setTag(cantidadWatcher);


        if (holder.inputIndicaciones.getTag() instanceof TextWatcher) {
            holder.inputIndicaciones.removeTextChangedListener((TextWatcher) holder.inputIndicaciones.getTag());
        }
        TextWatcher indicacionesWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                medicamento.setIndicacionesUsuario(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        holder.inputIndicaciones.addTextChangedListener(indicacionesWatcher);
        holder.inputIndicaciones.setTag(indicacionesWatcher);
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos.size();
    }

    public void filtrarLista(ArrayList<Medicamento> listaFiltrada) {
        this.listaMedicamentos = listaFiltrada;
        notifyDataSetChanged();
    }
    public ArrayList<Medicamento> getListaActual() {
        return listaMedicamentos;
    }
}
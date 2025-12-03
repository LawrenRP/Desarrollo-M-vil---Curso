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
import com.example.saludmovil.data.Cita; // ✨ Importamos nuestro modelo
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CitasFirestoreAdapter extends RecyclerView.Adapter<CitasFirestoreAdapter.ViewHolder> {

    private Context context;
    private List<Cita> listaCitas; // ✨ CAMBIO: Ahora usamos objetos Cita
    private OnCitaClickListener listener;

    // ✨ CAMBIO: La interfaz ahora devuelve un objeto Cita
    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }

    public CitasFirestoreAdapter(Context context, OnCitaClickListener listener) {
        this.context = context;
        this.listaCitas = new ArrayList<>();
        this.listener = listener;
    }

    // ✨ CAMBIO: Recibe lista de Citas
    public void setCitas(List<Cita> nuevasCitas) {
        this.listaCitas = nuevasCitas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_paciente, parent, false);
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
        TextView tvNombreDoctor, tvFechaHora, tvMotivo;
        Chip chipEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreDoctor = itemView.findViewById(R.id.tvNombreDoctorCita);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHoraCita);
            tvMotivo = itemView.findViewById(R.id.tvMotivoCita);
            chipEstado = itemView.findViewById(R.id.chipEstadoCita);
        }

        void bind(Cita cita) { // ✨ CAMBIO: Recibe un objeto Cita

            // Usamos los datos ya procesados del objeto Cita
            String doctorName = cita.getNombreDoctor();
            String fecha = cita.getFecha();
            String hora = cita.getHora();
            String motivo = cita.getMotivo();
            String estado = cita.getEstado();

            tvNombreDoctor.setText("Dr. " + (doctorName != null ? doctorName : "Sin asignar"));

            // Formateo de fecha (opcional, si ya viene formateada mejor)
            tvFechaHora.setText(formatearFechaBonita(fecha) + " - " + hora);

            tvMotivo.setText(motivo != null ? motivo : "Sin motivo");

            if (estado != null) {
                chipEstado.setText(estado.substring(0, 1).toUpperCase() + estado.substring(1));
                configurarColorChip(estado);
            }

            itemView.setOnClickListener(v -> listener.onCitaClick(cita));
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
            } catch (Exception e) {
                // Fallback seguro
            }
        }

        private String formatearFechaBonita(String fechaDB) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
                Date d = input.parse(fechaDB);
                return output.format(d);
            } catch (Exception e) { return fechaDB; }
        }
    }
}
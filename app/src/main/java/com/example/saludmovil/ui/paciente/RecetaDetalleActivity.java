package com.example.saludmovil.ui.paciente;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.saludmovil.R;
import com.example.saludmovil.database.BaseDeDatos;

public class RecetaDetalleActivity extends AppCompatActivity {

    private BaseDeDatos bd;
    private int idReceta;
    private TextView tvFolio, tvFecha, tvPaciente, tvDni, tvDoctor, tvEspecialidad, tvCmp, tvDiagnostico;
    private LinearLayout layoutListaMedicamentos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receta_detalle);

        idReceta = getIntent().getIntExtra("id_receta", -1);
        if (idReceta == -1) {
            finish();
            return;
        }

        bd = new BaseDeDatos(this);
        initViews();
        cargarDatosGenerales();
        cargarMedicamentos();
    }

    private void initViews() {
        tvFolio = findViewById(R.id.tvFolioReceta);
        tvFecha = findViewById(R.id.tvFechaEmision);
        tvPaciente = findViewById(R.id.tvNombrePaciente);
        tvDni = findViewById(R.id.tvDniPaciente);
        tvDoctor = findViewById(R.id.tvDoctorDetalle);
        tvEspecialidad = findViewById(R.id.tvEspecialidadDetalle);
        tvCmp = findViewById(R.id.tvCmpDoctor);
        tvDiagnostico = findViewById(R.id.tvDiagnostico);
        layoutListaMedicamentos = findViewById(R.id.layoutListaMedicamentos);

        findViewById(R.id.buttonRetrocederDetalle).setOnClickListener(v -> finish());
    }

    private void cargarDatosGenerales() {
        Cursor c = bd.getCabeceraReceta(idReceta);
        if (c != null && c.moveToFirst()) {
            tvFolio.setText("Folio N°: " + c.getInt(c.getColumnIndexOrThrow("id")));
            tvFecha.setText("Emitida el: " + c.getString(c.getColumnIndexOrThrow("fecha_emision")));

            String nombrePac = c.getString(c.getColumnIndexOrThrow("pac_nombre")) + " " + c.getString(c.getColumnIndexOrThrow("pac_apellido"));
            tvPaciente.setText(nombrePac);
            tvDni.setText("DNI: " + c.getString(c.getColumnIndexOrThrow("pac_dni")));

            tvDoctor.setText("Dr. " + c.getString(c.getColumnIndexOrThrow("nombre_completo")));
            tvEspecialidad.setText(c.getString(c.getColumnIndexOrThrow("especialidad")));
            tvCmp.setText("CMP: " + c.getString(c.getColumnIndexOrThrow("numero_colegiatura")));

            tvDiagnostico.setText(c.getString(c.getColumnIndexOrThrow("diagnostico")));
            c.close();
        }
    }

    private void cargarMedicamentos() {
        Cursor c = bd.getMedicamentosDeReceta(idReceta);
        LayoutInflater inflater = LayoutInflater.from(this);

        if (c != null && c.moveToFirst()) {
            do {
                String nombre = c.getString(c.getColumnIndexOrThrow("nombre"));
                String presentacion = c.getString(c.getColumnIndexOrThrow("presentacion"));
                String cantidad = c.getString(c.getColumnIndexOrThrow("cantidad"));
                String indicaciones = c.getString(c.getColumnIndexOrThrow("indicaciones"));

                View cardView = inflater.inflate(R.layout.item_medicamento_recetado, layoutListaMedicamentos, false);
                TextView tvNombre = cardView.findViewById(R.id.tvNombreMedicamentoRecetado);
                TextView tvCant = cardView.findViewById(R.id.tvCantidadRecetada);
                TextView tvInd = cardView.findViewById(R.id.tvIndicacionesRecetadas);
                ImageButton btnEliminar = cardView.findViewById(R.id.btnEliminarMedicamento);

                tvNombre.setText(nombre + " " + presentacion);
                tvCant.setText(cantidad);
                tvInd.setText("Indicaciones: " + indicaciones);
                btnEliminar.setVisibility(View.GONE);
                layoutListaMedicamentos.addView(cardView);

            } while (c.moveToNext());
            c.close();
        }
    }
}
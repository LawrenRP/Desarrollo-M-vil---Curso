package com.example.saludmovil.ui.paciente;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.MedicamentoLecturaAdapter;
import com.example.saludmovil.data.MedicamentoRecetado;
import com.example.saludmovil.database.BaseDeDatos;

import java.util.ArrayList;

public class RecetaDetalleActivity extends AppCompatActivity {

    private BaseDeDatos bd;
    private int idReceta;

    private TextView tvFolio, tvFecha, tvPaciente, tvDni, tvDoctor, tvEspecialidad, tvCmp, tvDiagnostico;
    private RecyclerView rvDetalleMedicamentos;

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
        rvDetalleMedicamentos = findViewById(R.id.rvDetalleMedicamentos);
        rvDetalleMedicamentos.setLayoutManager(new LinearLayoutManager(this));

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
        ArrayList<MedicamentoRecetado> lista = new ArrayList<>();
        Cursor c = bd.getMedicamentosDeReceta(idReceta);

        if (c != null && c.moveToFirst()) {
            do {
                String nombre = c.getString(c.getColumnIndexOrThrow("nombre"));
                String presentacion = c.getString(c.getColumnIndexOrThrow("presentacion"));
                String cantidad = c.getString(c.getColumnIndexOrThrow("cantidad"));
                String indicaciones = c.getString(c.getColumnIndexOrThrow("indicaciones"));
                lista.add(new MedicamentoRecetado(0, nombre + " " + presentacion, cantidad, indicaciones));

            } while (c.moveToNext());
            c.close();
        }
        MedicamentoLecturaAdapter adapter = new MedicamentoLecturaAdapter(this, lista);
        rvDetalleMedicamentos.setAdapter(adapter);
    }
}
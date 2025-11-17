package com.example.saludmovil.ui.paciente;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.MedicamentoLecturaAdapter;
import com.example.saludmovil.data.MedicamentoRecetado;
import com.example.saludmovil.database.BaseDeDatos;
import com.example.saludmovil.utils.PdfGenerator;

import java.util.ArrayList;
import java.util.List;

public class RecetaDetalleActivity extends AppCompatActivity {

    private BaseDeDatos bd;
    private int idReceta;

    private TextView tvFolio, tvFecha, tvPaciente, tvDni, tvDoctor, tvEspecialidad, tvCmp, tvDiagnostico;
    private RecyclerView rvDetalleMedicamentos;
    private Button btnDescargar;
    private final List<MedicamentoRecetado> listaMedicamentosParaPdf = new ArrayList<>();

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
        btnDescargar = findViewById(R.id.btnDescargarReceta);
        btnDescargar.setOnClickListener(v -> descargarPdf());

        findViewById(R.id.buttonRetrocederDetalle).setOnClickListener(v -> finish());
    }

    private void cargarDatosGenerales() {
        Cursor c = bd.getCabeceraReceta(idReceta);
        if (c != null && c.moveToFirst()) {
            tvFolio.setText(getString(R.string.receta_folio_format, c.getInt(c.getColumnIndexOrThrow("id"))));
            tvFecha.setText(getString(R.string.receta_emitida_el_format, c.getString(c.getColumnIndexOrThrow("fecha_emision"))));

            tvPaciente.setText(getString(
                    R.string.receta_nombre_paciente_format,
                    c.getString(c.getColumnIndexOrThrow("pac_nombre")),
                    c.getString(c.getColumnIndexOrThrow("pac_apellido"))
            ));
            tvDni.setText(getString(R.string.receta_dni_format, c.getString(c.getColumnIndexOrThrow("pac_dni"))));

            tvDoctor.setText(getString(R.string.receta_doctor_format, c.getString(c.getColumnIndexOrThrow("nombre_completo"))));
            tvEspecialidad.setText(c.getString(c.getColumnIndexOrThrow("especialidad")));
            tvCmp.setText(getString(R.string.receta_cmp_format, c.getString(c.getColumnIndexOrThrow("numero_colegiatura"))));

            tvDiagnostico.setText(c.getString(c.getColumnIndexOrThrow("diagnostico")));
            c.close();
        }
    }

    private void descargarPdf() {
        String folio = tvFolio.getText().toString().replace("Folio N°: ", "");
        String fecha = tvFecha.getText().toString().replace("Emitida el: ", "");
        String paciente = tvPaciente.getText().toString();
        String dni = tvDni.getText().toString().replace("DNI: ", "");
        String doctor = tvDoctor.getText().toString().replace("Dr. ", "");
        String especialidad = tvEspecialidad.getText().toString();
        String cmp = tvCmp.getText().toString().replace("CMP: ", "");
        String diagnostico = tvDiagnostico.getText().toString();

        if (!listaMedicamentosParaPdf.isEmpty()) {
            PdfGenerator.generarPdfReceta(
                    this,
                    folio,
                    fecha,
                    doctor,
                    especialidad,
                    cmp,
                    paciente,
                    dni,
                    diagnostico,
                    new ArrayList<>(listaMedicamentosParaPdf)
            );
        } else {
            Toast.makeText(this, "Espera a que carguen los medicamentos", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarMedicamentos() {
        listaMedicamentosParaPdf.clear();
        Cursor c = bd.getMedicamentosDeReceta(idReceta);

        if (c != null && c.moveToFirst()) {
            do {
                String nombre = c.getString(c.getColumnIndexOrThrow("nombre"));
                String presentacion = c.getString(c.getColumnIndexOrThrow("presentacion"));
                String cantidad = c.getString(c.getColumnIndexOrThrow("cantidad"));
                String indicaciones = c.getString(c.getColumnIndexOrThrow("indicaciones"));
                listaMedicamentosParaPdf.add(
                        new MedicamentoRecetado(0, nombre + " " + presentacion, cantidad, indicaciones)
                );
            } while (c.moveToNext());
            c.close();
        }

        rvDetalleMedicamentos.setAdapter(new MedicamentoLecturaAdapter(this, new ArrayList<>(listaMedicamentosParaPdf)));
    }
}
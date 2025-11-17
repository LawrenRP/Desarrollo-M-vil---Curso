package com.example.saludmovil.ui.doctor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.MedicamentoResumenAdapter;
import com.example.saludmovil.data.MedicamentoRecetado;
import com.example.saludmovil.database.BaseDeDatos;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ConsultaPacienteActivity extends AppCompatActivity implements MedicamentoResumenAdapter.OnItemClickListener {

    private TextView tvNombre, tvEstatura, tvPeso, tvSangre, tvAlergias, tvCronicas, tvMedicamentos, tvContactoNombre, tvContactoTel;
    private TextView tvCodigoConsulta, tvFechaHora, tvMotivoConsulta;
    private TextInputEditText etDiagnostico;
    private RecyclerView rvMedicamentos;
    private Button btnRecetar, btnCompletar, btnCancelar;

    private BaseDeDatos bd;
    private int idCita;
    private int idPaciente;
    private ArrayList<MedicamentoRecetado> listaMedicamentosRecetados;
    private MedicamentoResumenAdapter adapter;

    private final ActivityResultLauncher<Intent> selectorMedicamentosLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<MedicamentoRecetado> nuevos = (ArrayList<MedicamentoRecetado>) result.getData().getSerializableExtra("medicamentos_seleccionados");
                    if (nuevos != null) {
                        listaMedicamentosRecetados.addAll(nuevos);
                        adapter.actualizarLista(listaMedicamentosRecetados);
                        rvMedicamentos.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_paciente);
        idCita = getIntent().getIntExtra("id_cita", -1);
        idPaciente = getIntent().getIntExtra("id_paciente", -1);

        if (idCita == -1 || idPaciente == -1) {
            Toast.makeText(this, "Error al cargar la consulta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bd = new BaseDeDatos(this);
        listaMedicamentosRecetados = new ArrayList<>();

        initViews();
        cargarDatosConsulta();
        cargarMotivoCita();
        cargarPerfilPaciente();
        setupRecyclerView();
        setupButtons();
    }

    private void initViews() {
        tvNombre = findViewById(R.id.tvNombrePaciente);
        tvEstatura = findViewById(R.id.tvEstatura);
        tvPeso = findViewById(R.id.tvPeso);
        tvSangre = findViewById(R.id.tvTipoSangre);
        tvAlergias = findViewById(R.id.tvAlergias);
        tvCronicas = findViewById(R.id.tvEnfermedadesCronicas);
        tvMedicamentos = findViewById(R.id.tvMedicamentos);
        tvContactoNombre = findViewById(R.id.tvContactoNombre);
        tvContactoTel = findViewById(R.id.tvContactoTelefono);

        tvCodigoConsulta = findViewById(R.id.tvCodigoConsulta);
        tvFechaHora = findViewById(R.id.tvFechaHoraConsulta);
        tvMotivoConsulta = findViewById(R.id.tvMotivoConsulta);
        etDiagnostico = findViewById(R.id.inputDiagnostico);
        rvMedicamentos = findViewById(R.id.rvMedicamentosRecetados);

        btnRecetar = findViewById(R.id.btnRecetarMedicamentos);
        btnCompletar = findViewById(R.id.btnCompletarConsulta);
        btnCancelar = findViewById(R.id.btnCancelarConsulta);

        findViewById(R.id.buttonAtras).setOnClickListener(v -> finish());
    }

    private void cargarDatosConsulta() {
        tvCodigoConsulta.setText("Consulta N°: C-00" + idCita);
        String fechaHora = new SimpleDateFormat("dd 'de' MMM - hh:mm a", Locale.getDefault()).format(new Date());
        tvFechaHora.setText(fechaHora);
    }

    private void cargarPerfilPaciente() {
        Cursor cursor = bd.getPerfilPacientePorId(idPaciente);
        if (cursor != null && cursor.moveToFirst()) {
            int nombreIndex = cursor.getColumnIndex("nombre");
            int apellidoIndex = cursor.getColumnIndex("apellido");

            String nombre = (nombreIndex != -1) ? cursor.getString(nombreIndex) : "";
            String apellido = (apellidoIndex != -1) ? cursor.getString(apellidoIndex) : "";

            tvNombre.setText(nombre + " " + apellido);
            llenarTextView(tvEstatura, cursor, "estatura", "Estatura: ", "cm");
            llenarTextView(tvPeso, cursor, "peso", "Peso: ", "kg");
            llenarTextView(tvSangre, cursor, "tipo_sangre", "Sangre: ", "");
            llenarTextView(tvAlergias, cursor, "alergias", "Alergias: ", "");
            llenarTextView(tvCronicas, cursor, "enfermedades_cronicas", "Crónicas: ", "");
            llenarTextView(tvMedicamentos, cursor, "medicamentos_actuales", "Medicamentos: ", "");

            int contactoNombreIndex = cursor.getColumnIndex("nombre_contacto_emergencia");
            int contactoTelIndex = cursor.getColumnIndex("celular_contacto_emergencia");

            String cNombre = (contactoNombreIndex != -1) ? cursor.getString(contactoNombreIndex) : "N/A";
            String cTel = (contactoTelIndex != -1) ? cursor.getString(contactoTelIndex) : "";

            tvContactoNombre.setText("Contacto: " + (cNombre != null ? cNombre : "N/A"));
            tvContactoTel.setText("Teléfono: " + (cTel != null ? cTel : "N/A"));

            cursor.close();
        }
    }

    private void llenarTextView(TextView tv, Cursor c, String columna, String prefijo, String sufijo) {
        int idx = c.getColumnIndex(columna);
        String valor = (idx != -1) ? c.getString(idx) : null;
        if (valor == null || valor.isEmpty()) valor = "N/A";
        tv.setText(prefijo + valor + sufijo);
    }

    private void setupRecyclerView() {
        rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicamentoResumenAdapter(this, listaMedicamentosRecetados, this);
        rvMedicamentos.setAdapter(adapter);
    }

    private void setupButtons() {
        btnRecetar.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeleccionarMedicamentoActivity.class);
            selectorMedicamentosLauncher.launch(intent);
        });

        btnCancelar.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cancelar Consulta")
                    .setMessage("¿Seguro que deseas cancelar? Se perderán los datos ingresados.")
                    .setPositiveButton("Sí, Cancelar", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        });

        btnCompletar.setOnClickListener(v -> guardarConsulta());
    }

    private void guardarConsulta() {
        String diagnostico = etDiagnostico.getText().toString().trim();

        if (TextUtils.isEmpty(diagnostico)) {
            etDiagnostico.setError("El diagnóstico es obligatorio");
            return;
        }

        boolean exitoHistorial = bd.addHistorialClinico(idCita, "Notas de consulta general", diagnostico);

        if (exitoHistorial) {
            if (!listaMedicamentosRecetados.isEmpty()) {
                String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                long idReceta = bd.crearRecetaCabecera(idCita, fechaHoy);

                for (MedicamentoRecetado med : listaMedicamentosRecetados) {
                    bd.agregarDetalleReceta(idReceta, med.getIdMedicamento(), med.getCantidad(), med.getIndicaciones());
                }
            }
            bd.actualizarEstadoCita(idCita, "Completada");

            Toast.makeText(this, "Consulta finalizada con éxito", Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_OK);
            finish();

        } else {
            Toast.makeText(this, "Error al guardar el historial", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarMotivoCita() {
        Cursor cursor = bd.getDetalleCita(idCita);
        if (cursor != null && cursor.moveToFirst()) {
            int motivoIndex = cursor.getColumnIndex("motivo");

            if (motivoIndex != -1) {
                String motivo = cursor.getString(motivoIndex);
                tvMotivoConsulta.setText(motivo != null && !motivo.isEmpty() ? motivo : "Sin motivo registrado");
            }
            cursor.close();
        }
    }
    @Override
    public void onEliminarClick(int position) {
        listaMedicamentosRecetados.remove(position);
        adapter.notifyItemRemoved(position);
        if (listaMedicamentosRecetados.isEmpty()) {
            rvMedicamentos.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bd != null) {
            bd.close();
        }
    }
}
package com.example.saludmovil.ui.doctor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RegistrarDoctorPaso2Activity extends AppCompatActivity {

    TextInputEditText edCMP;
    AutoCompleteTextView autoCompleteEspecialidad;
    Button btnAdjuntarTitulo, btnFinalizar;
    TextView tvArchivoSeleccionado;
    MaterialToolbar toolbar;
    private boolean hayCambiosSinGuardar = false;

    private String nombrePaso1, dniPaso1, fechaNacPaso1, telefonoPaso1, correoPaso1, clavePaso1;

    private ActivityResultLauncher<String> filePickerLauncher;
    private String rutaTituloGuardado = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_doctor_paso2);

        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbarRegDoc2);
        edCMP = findViewById(R.id.editTextRegDocCMP);
        autoCompleteEspecialidad = findViewById(R.id.autoCompleteEspecialidad);
        btnAdjuntarTitulo = findViewById(R.id.btnAdjuntarTitulo);
        tvArchivoSeleccionado = findViewById(R.id.tvArchivoSeleccionado);
        btnFinalizar = findViewById(R.id.buttonFinalizarRegistro);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recuperarDatosDelPaso1();
        setupBackButton();
        setupChangeListeners();
        setupEspecialidades();

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String cmp = edCMP.getText().toString().trim();
                        if (cmp.isEmpty()) {
                            Toast.makeText(this, "Por favor, ingresa tu CMP antes de subir el título", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String nombreArchivo = "titulo_cmp_" + cmp + ".pdf";
                        if (copiarArchivoPrivado(uri, nombreArchivo)) {
                            rutaTituloGuardado = nombreArchivo;
                            tvArchivoSeleccionado.setText(nombreArchivo);
                            hayCambiosSinGuardar = true;
                            Toast.makeText(this, "Título adjuntado con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnAdjuntarTitulo.setOnClickListener(v -> filePickerLauncher.launch("application/pdf"));
        btnFinalizar.setOnClickListener(v -> registrarDoctorEnFirestore());
    }

    private void registrarDoctorEnFirestore() {
        String cmp = edCMP.getText().toString().trim();
        String especialidad = autoCompleteEspecialidad.getText().toString().trim();

        if (cmp.isEmpty() || especialidad.isEmpty() || rutaTituloGuardado.isEmpty()){
            Toast.makeText(this, "Por favor, complete todos los campos y adjunte su título", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("doctores")
                .whereEqualTo("cmp", cmp)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Toast.makeText(this, "Este CMP ya está registrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    verificarCorreoYGuardar(cmp, especialidad);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al verificar CMP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void verificarCorreoYGuardar(String cmp, String especialidad) {
        db.collection("doctores")
                .whereEqualTo("correo", correoPaso1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Toast.makeText(this, "Este correo ya está registrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    guardarDoctor(cmp, especialidad);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al verificar correo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void guardarDoctor(String cmp, String especialidad) {
        Map<String, Object> doctor = new HashMap<>();

        doctor.put("nombre_completo", nombrePaso1);
        doctor.put("dni", dniPaso1);
        doctor.put("fecha_nacimiento", fechaNacPaso1);
        doctor.put("celular", telefonoPaso1);
        doctor.put("correo", correoPaso1);
        doctor.put("contrasena", clavePaso1);
        doctor.put("cmp", cmp);
        doctor.put("especialidad", especialidad);
        doctor.put("ruta_titulo_universitario", rutaTituloGuardado);
        doctor.put("rol", "doctor");

        db.collection("doctores")
                .add(doctor)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Registro médico exitoso!", Toast.LENGTH_LONG).show();
                    hayCambiosSinGuardar = false;

                    String idFirebase = documentReference.getId();
                    guardarSesion(idFirebase, "doctor");

                    Intent intent = new Intent(RegistrarDoctorPaso2Activity.this, InicioDoctorActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void guardarSesion(String id, String rol) {
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("id_usuario", id);
        editor.putString("rol_usuario", rol);
        editor.apply();
    }

    private boolean copiarArchivoPrivado(Uri uri, String nombreArchivo) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = openFileOutput(nombreArchivo, Context.MODE_PRIVATE)) {
            if (inputStream == null) return false;
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void recuperarDatosDelPaso1() {
        Intent intent = getIntent();
        nombrePaso1 = intent.getStringExtra("NOMBRE");
        dniPaso1 = intent.getStringExtra("DNI");
        fechaNacPaso1 = intent.getStringExtra("FECHA_NACIMIENTO");
        telefonoPaso1 = intent.getStringExtra("TELEFONO");
        correoPaso1 = intent.getStringExtra("CORREO");
        clavePaso1 = intent.getStringExtra("CLAVE");
    }

    private void setupEspecialidades() {
        String[] especialidades = {
                "Medicina General",
                "Pediatría",
                "Cardiología",
                "Dermatología",
                "Ginecología",
                "Neurología",
                "Psicología",
                "Nutrición"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, especialidades
        );
        autoCompleteEspecialidad.setAdapter(adapter);
    }

    private void setupChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { hayCambiosSinGuardar = true; }
            @Override public void afterTextChanged(Editable s) {}
        };
        edCMP.addTextChangedListener(textWatcher);
        autoCompleteEspecialidad.addTextChangedListener(textWatcher);
    }

    private void setupBackButton() {
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hayCambiosSinGuardar) {
                    new AlertDialog.Builder(RegistrarDoctorPaso2Activity.this)
                            .setTitle("Descartar Cambios")
                            .setMessage("¿Estás seguro de que quieres salir? Los cambios no se guardarán.")
                            .setPositiveButton("Salir", (dialog, which) -> finish())
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}

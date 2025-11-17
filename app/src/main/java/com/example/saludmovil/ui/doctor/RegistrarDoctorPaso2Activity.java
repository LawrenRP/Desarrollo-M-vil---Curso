package com.example.saludmovil.ui.doctor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import com.example.saludmovil.database.BaseDeDatos;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_doctor_paso2);

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
                        boolean exito = copiarArchivoPrivado(uri, nombreArchivo);

                        if (exito) {
                            rutaTituloGuardado = nombreArchivo;
                            tvArchivoSeleccionado.setText(nombreArchivo);
                            hayCambiosSinGuardar = true;
                            Toast.makeText(this, "Título adjuntado con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        btnAdjuntarTitulo.setOnClickListener(v -> {
            try {
                filePickerLauncher.launch("application/pdf");
            } catch (Exception e) {
                Toast.makeText(this, "No se encontró una aplicación para seleccionar archivos", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            String cmp = edCMP.getText().toString().trim();
            String especialidad = autoCompleteEspecialidad.getText().toString().trim();
            if (cmp.isEmpty() || especialidad.isEmpty() || rutaTituloGuardado.isEmpty()){
                Toast.makeText(this, "Por favor, complete todos los campos y adjunte su título", Toast.LENGTH_SHORT).show();
                return;
            }

            BaseDeDatos bd = new BaseDeDatos(getApplicationContext());
            long idUsuario = bd.registrarUsuario(correoPaso1, clavePaso1, "doctor");

            if (idUsuario != -1) {
                bd.registrarDoctorPaso1(idUsuario, nombrePaso1, dniPaso1, fechaNacPaso1, telefonoPaso1, correoPaso1);
                int idEspecialidad = bd.getIdEspecialidad(especialidad);

                bd.registrarDoctorPaso2(idUsuario, cmp, idEspecialidad, rutaTituloGuardado);

                hayCambiosSinGuardar = false;
                Toast.makeText(getApplicationContext(), "¡Registro de doctor completado con éxito!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(RegistrarDoctorPaso2Activity.this, LoginDoctorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(getApplicationContext(), "Error: El correo electrónico ya está en uso.", Toast.LENGTH_LONG).show();
            }
        });
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
        BaseDeDatos bd = new BaseDeDatos(this);
        Cursor cursor = bd.getEspecialidades();
        ArrayList<String> listaEspecialidades = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int nombreIndex = cursor.getColumnIndex("nombre");
                if (nombreIndex != -1)
                    listaEspecialidades.add(cursor.getString(nombreIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();
        bd.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, listaEspecialidades
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
                            .setMessage("¿Estás seguro de que quieres salir? La información profesional se perderá.")
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
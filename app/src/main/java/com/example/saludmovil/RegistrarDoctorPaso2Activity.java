package com.example.saludmovil;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class RegistrarDoctorPaso2Activity extends AppCompatActivity {

    TextInputEditText edCMP;
    AutoCompleteTextView autoCompleteEspecialidad;
    Button btnAdjuntarTitulo, btnFinalizar;
    TextView tvArchivoSeleccionado;
    MaterialToolbar toolbar;
    private boolean hayCambiosSinGuardar = false;


    private String nombrePaso1, dniPaso1, fechaNacPaso1, telefonoPaso1, correoPaso1, clavePaso1;

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
        setupEspecialidades(); // Lo llamamos después de recuperar los datos

        btnAdjuntarTitulo.setOnClickListener(v -> {
            tvArchivoSeleccionado.setText("titulo_medico.pdf");
            hayCambiosSinGuardar = true;
            Toast.makeText(this, "Archivo seleccionado (simulación)", Toast.LENGTH_SHORT).show();
        });


        btnFinalizar.setOnClickListener(v -> {
            String cmp = edCMP.getText().toString().trim();
            String especialidad = autoCompleteEspecialidad.getText().toString().trim();
            String archivo = tvArchivoSeleccionado.getText().toString();

            if (cmp.isEmpty() || especialidad.isEmpty() || archivo.equals("Ningún archivo seleccionado")){
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }


            BaseDeDatos bd = new BaseDeDatos(getApplicationContext());

            // 1. Intentamos registrar el usuario (correo y clave)
            long idUsuario = bd.registrarUsuario(correoPaso1, clavePaso1, "doctor");

            if (idUsuario != -1) {
                // Si el usuario se creó con éxito...
                // 2. Registramos sus datos personales en la tabla de doctores
                bd.registrarDoctorPaso1(idUsuario, nombrePaso1, dniPaso1, fechaNacPaso1, telefonoPaso1);

                // 3. Obtenemos el ID de la especialidad (tu método lo crea si no existe)
                int idEspecialidad = bd.getIdEspecialidad(especialidad);
                String rutaTituloSimulada = "path/to/" + archivo; // Ruta simulada del archivo

                // 4. Actualizamos el registro del doctor con sus datos profesionales
                bd.registrarDoctorPaso2(idUsuario, cmp, idEspecialidad, rutaTituloSimulada);

                hayCambiosSinGuardar = false;
                Toast.makeText(getApplicationContext(), "¡Registro de doctor completado con éxito!", Toast.LENGTH_LONG).show();

                // Llevamos al usuario al Login principal, que ya sabe manejar doctores
                Intent intent = new Intent(RegistrarDoctorPaso2Activity.this, LoginDoctorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                // Si idUsuario es -1, significa que el correo ya existe
                Toast.makeText(getApplicationContext(), "Error: El correo electrónico ya está en uso.", Toast.LENGTH_LONG).show();

            }
        });
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
        // MEJORA: Cargamos las especialidades desde la base de datos
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
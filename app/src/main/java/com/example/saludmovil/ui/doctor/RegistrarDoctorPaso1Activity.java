package com.example.saludmovil.ui.doctor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.saludmovil.R;
import com.example.saludmovil.utils.Validaciones; // ✨ Usamos tus validaciones
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.firestore.FirebaseFirestore; // ✨ Firestore

import org.json.JSONException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegistrarDoctorPaso1Activity extends AppCompatActivity {

    // Vistas
    TextInputEditText edNombre, edDNI, edFechaNacimiento, edTelefono, edCorreo, edClave, edConfirmarClave;
    Button btnSiguiente;
    MaterialButton btnVerificarDNI;
    MaterialToolbar toolbar;
    TextInputLayout layoutNombre, layoutFecha, layoutTelefono, layoutCorreo, layoutClave, layoutConfirmar;

    private RequestQueue colaPeticiones;
    private boolean hayCambiosSinGuardar = false;
    private FirebaseFirestore db; // ✨

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_doctor_paso1);

        db = FirebaseFirestore.getInstance(); // Inicializamos

        vincularVistas();
        colaPeticiones = Volley.newRequestQueue(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        configurarEstadoInicialFormulario();
        setupClickListeners();
        setupChangeListeners();
        setupBackButton();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarRegDoc1);
        edNombre = findViewById(R.id.editTextRegDocNombre);
        edDNI = findViewById(R.id.editTextRegDocDNI);
        edFechaNacimiento = findViewById(R.id.editTextRegDocFechaNacimiento);
        edTelefono = findViewById(R.id.editTextRegDocTelefono);
        edCorreo = findViewById(R.id.editTextRegDocCorreo);
        edClave = findViewById(R.id.editTextRegDocClave);
        edConfirmarClave = findViewById(R.id.editTextRegDocConfirmarClave);
        btnSiguiente = findViewById(R.id.buttonRegDocSiguiente);
        btnVerificarDNI = findViewById(R.id.buttonVerificarDNIDoctor);

        layoutNombre = findViewById(R.id.textInputLayoutRegDocNombre);
        layoutFecha = findViewById(R.id.textInputLayoutRegDocFecha);
        layoutTelefono = findViewById(R.id.textInputLayoutRegDocTelefono);
        layoutCorreo = findViewById(R.id.textInputLayoutRegDocCorreo);
        layoutClave = findViewById(R.id.textInputLayoutRegDocClave);
        layoutConfirmar = findViewById(R.id.textInputLayoutRegDocConfirmarClave);
    }

    private void configurarEstadoInicialFormulario() {
        layoutNombre.setEnabled(false);
        layoutFecha.setEnabled(false);
        layoutTelefono.setEnabled(false);
        layoutCorreo.setEnabled(false);
        layoutClave.setEnabled(false);
        layoutConfirmar.setEnabled(false);
        btnSiguiente.setEnabled(false);
    }

    private void habilitarFormularioPostVerificacion() {
        layoutFecha.setEnabled(true);
        layoutTelefono.setEnabled(true);
        layoutCorreo.setEnabled(true);
        layoutClave.setEnabled(true);
        layoutConfirmar.setEnabled(true);
        btnSiguiente.setEnabled(true);

        edDNI.setEnabled(false);
        btnVerificarDNI.setEnabled(false);
        // El nombre se queda deshabilitado si la API lo encontró
    }

    private void setupClickListeners() {
        edFechaNacimiento.setOnClickListener(v -> mostrarCalendario());

        btnVerificarDNI.setOnClickListener(v -> {
            String dni = edDNI.getText().toString().trim();
            if (dni.length() == 8) {
                verificarDNIconAPI(dni);
            } else {
                Toast.makeText(this, "Por favor, ingrese un DNI de 8 dígitos.", Toast.LENGTH_SHORT).show();
            }
        });

        btnSiguiente.setOnClickListener(v -> validarYPasarAlSiguientePaso());
    }

    private void validarYPasarAlSiguientePaso() {
        String nombre = edNombre.getText().toString().trim();
        String dni = edDNI.getText().toString().trim();
        String fechaNacimiento = edFechaNacimiento.getText().toString().trim();
        String telefono = edTelefono.getText().toString().trim();
        String correo = edCorreo.getText().toString().trim();
        String clave = edClave.getText().toString().trim();
        String confirmarClave = edConfirmarClave.getText().toString().trim();

        // 1. Validaciones Locales
        if (nombre.isEmpty() || fechaNacimiento.isEmpty() || telefono.isEmpty() || correo.isEmpty() || clave.isEmpty() || confirmarClave.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Por favor, llene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!clave.equals(confirmarClave)) {
            Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✨ USAMOS TU CLASE VALIDACIONES ✨
        if (!Validaciones.esValido(clave)) {
            Toast.makeText(this, "La contraseña no cumple los requisitos de seguridad.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!Validaciones.esFechaNacimientoValida(fechaNacimiento)) {
            Toast.makeText(this, "La fecha de nacimiento no es válida. Debes ser mayor de 18 años.", Toast.LENGTH_LONG).show();
            return;
        }

        // ✨ 2. Verificar duplicados en Firestore (DNI) ✨
        db.collection("doctores")
                .whereEqualTo("dni", dni)
                .get()
                .addOnSuccessListener(snapshotDni -> {
                    if (!snapshotDni.isEmpty()) {
                        Toast.makeText(this, "Este DNI ya está registrado como doctor.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Si el DNI está libre, chequeamos el correo
                    checkCorreoYAvanzar(nombre, dni, fechaNacimiento, telefono, correo, clave);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al verificar DNI: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void checkCorreoYAvanzar(String nombre, String dni, String fecha, String tel, String correo, String clave) {
        // ✨ 3. Verificar duplicados en Firestore (Correo) ✨
        db.collection("doctores")
                .whereEqualTo("correo", correo)
                .get()
                .addOnSuccessListener(snapshotCorreo -> {
                    if (!snapshotCorreo.isEmpty()) {
                        Toast.makeText(this, "Este correo ya está registrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✨ 4. Si todo está libre, pasamos al Paso 2 ✨
                    hayCambiosSinGuardar = false;
                    Intent intent = new Intent(RegistrarDoctorPaso1Activity.this, RegistrarDoctorPaso2Activity.class);
                    intent.putExtra("NOMBRE", nombre);
                    intent.putExtra("DNI", dni);
                    intent.putExtra("FECHA_NACIMIENTO", fecha);
                    intent.putExtra("TELEFONO", tel);
                    intent.putExtra("CORREO", correo);
                    intent.putExtra("CLAVE", clave);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al verificar correo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void verificarDNIconAPI(String dni) {
        Toast.makeText(this, "Verificando DNI...", Toast.LENGTH_SHORT).show();
        btnVerificarDNI.setEnabled(false);

        String url = "https://api.decolecta.com/v1/reniec/dni?numero=" + dni;
        // ✨ Asegúrate de que este sea tu token válido ✨
        final String token = "Bearer sk_11710.H4Eh0Rb9Z4GxyXToTjrPAWTuQO3ppNSc";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String nombres = response.optString("first_name");
                        String apellidoPaterno = response.optString("first_last_name");
                        String apellidoMaterno = response.optString("second_last_name");

                        String nombreCompleto = nombres + " " + apellidoPaterno + " " + apellidoMaterno;
                        edNombre.setText(Validaciones.capitalizarPalabras(nombreCompleto));

                        habilitarFormularioPostVerificacion();
                        Toast.makeText(this, "DNI verificado.", Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        manejarErrorAPI("Error al procesar la respuesta.");
                    }
                },
                error -> manejarErrorAPI("DNI no encontrado. Ingrese sus datos manualmente.")
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };
        colaPeticiones.add(request);
    }

    private void manejarErrorAPI(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        btnVerificarDNI.setEnabled(true);
        layoutNombre.setEnabled(true); // Habilitamos para escribir manual
        habilitarFormularioPostVerificacion();
    }

    private void mostrarCalendario() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegistrarDoctorPaso1Activity.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String fechaFormateada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year);
                    edFechaNacimiento.setText(fechaFormateada);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { hayCambiosSinGuardar = true; }
            @Override public void afterTextChanged(Editable s) {}
        };
        edDNI.addTextChangedListener(textWatcher);
    }

    private void setupBackButton() {
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hayCambiosSinGuardar) {
                    new AlertDialog.Builder(RegistrarDoctorPaso1Activity.this)
                            .setTitle("Descartar Cambios")
                            .setMessage("¿Estás seguro de que quieres salir?")
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
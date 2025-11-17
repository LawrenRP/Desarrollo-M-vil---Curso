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
import com.example.saludmovil.utils.Validaciones;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import java.util.Calendar;
import java.util.Locale;

public class RegistrarDoctorPaso1Activity extends AppCompatActivity {

    TextInputEditText edNombre, edDNI, edFechaNacimiento, edTelefono, edCorreo, edClave, edConfirmarClave;
    Button btnSiguiente;
    MaterialButton btnVerificarDNI;
    MaterialToolbar toolbar;
    TextInputLayout layoutNombre, layoutFecha, layoutTelefono, layoutCorreo, layoutClave, layoutConfirmar;

    private RequestQueue colaPeticiones;
    private boolean hayCambiosSinGuardar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_doctor_paso1);

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
        layoutNombre.setEnabled(false);
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

        btnSiguiente.setOnClickListener(v -> {
            String nombre = edNombre.getText().toString().trim();
            String dni = edDNI.getText().toString().trim();
            String fechaNacimiento = edFechaNacimiento.getText().toString().trim();
            String telefono = edTelefono.getText().toString().trim();
            String correo = edCorreo.getText().toString().trim();
            String clave = edClave.getText().toString().trim();
            String confirmarClave = edConfirmarClave.getText().toString().trim();

            if (nombre.isEmpty() || fechaNacimiento.isEmpty() || telefono.isEmpty() || correo.isEmpty() || clave.isEmpty() || confirmarClave.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Por favor, llene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!clave.equals(confirmarClave)) {
                Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Validaciones.esValido(clave)) {
                Toast.makeText(this, "La contraseña no cumple los requisitos de seguridad.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!Validaciones.esFechaNacimientoValida(fechaNacimiento)) {
                Toast.makeText(this, "La fecha de nacimiento no es válida. Debes ser mayor de 18 años.", Toast.LENGTH_LONG).show();
                return;
            }

            hayCambiosSinGuardar = false;
            Intent intent = new Intent(RegistrarDoctorPaso1Activity.this, RegistrarDoctorPaso2Activity.class);
            intent.putExtra("NOMBRE", nombre);
            intent.putExtra("DNI", dni);
            intent.putExtra("FECHA_NACIMIENTO", fechaNacimiento);
            intent.putExtra("TELEFONO", telefono);
            intent.putExtra("CORREO", correo);
            intent.putExtra("CLAVE", clave);
            startActivity(intent);
        });
    }

    private void verificarDNIconAPI(String dni) {
        Toast.makeText(this, "Verificando DNI...", Toast.LENGTH_SHORT).show();
        btnVerificarDNI.setEnabled(false);

        String url = "https://api.decolecta.com/v1/reniec/dni?numero=" + dni;


        final String token = "Bearer sk_11710.H4Eh0Rb9Z4GxyXToTjrPAWTuQO3ppNSc";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String nombres = response.getString("first_name");
                        String apellidoPaterno = response.getString("first_last_name");
                        String apellidoMaterno = response.getString("second_last_name");


                        String nombreCompleto = nombres + " " + apellidoPaterno + " " + apellidoMaterno;
                        edNombre.setText(Validaciones.capitalizarPalabras(nombreCompleto));

                        habilitarFormularioPostVerificacion();
                        Toast.makeText(this, "DNI verificado. Complete el resto de datos.", Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la respuesta. Ingrese sus datos manualmente.", Toast.LENGTH_LONG).show();
                        layoutNombre.setEnabled(true);
                        habilitarFormularioPostVerificacion();
                    }
                },
                error -> {
                    Toast.makeText(this, "DNI no encontrado. Ingrese sus datos manualmente.", Toast.LENGTH_LONG).show();
                    layoutNombre.setEnabled(true);
                    habilitarFormularioPostVerificacion();
                }
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
        edNombre.addTextChangedListener(textWatcher);
        edFechaNacimiento.addTextChangedListener(textWatcher);
        edTelefono.addTextChangedListener(textWatcher);
        edCorreo.addTextChangedListener(textWatcher);
        edClave.addTextChangedListener(textWatcher);
        edConfirmarClave.addTextChangedListener(textWatcher);
    }

    private void setupBackButton() {
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hayCambiosSinGuardar) {
                    new AlertDialog.Builder(RegistrarDoctorPaso1Activity.this)
                            .setTitle("Descartar Cambios")
                            .setMessage("¿Estás seguro de que quieres salir? Los datos ingresados se perderán.")
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
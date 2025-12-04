package com.example.saludmovil.ui.paciente;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.saludmovil.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PerfilPacienteActivity extends AppCompatActivity {
    TextInputEditText edEstatura, edPeso, edAlergias, edEnfermedades, edMedicamentos, edContactoNombre, edContactoTelefono;
    AutoCompleteTextView autoCompleteSexo, autoCompleteSangre;
    Button btnGuardar;
    MaterialToolbar toolbar;

    private String idUsuario;
    private boolean hayCambiosSinGuardar = false;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_paciente);

        db = FirebaseFirestore.getInstance();

        vincularVistas();
        configurarMenusDesplegables();
        configurarToolbarYRetroceso();

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuario = sp.getString("id_usuario", "");

        if (idUsuario.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cargarDatosDelPerfil();
        configurarListenersDeCambios();
        configurarBotonGuardar();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarPerfil);
        edEstatura = findViewById(R.id.editTextPerfilEstatura);
        edPeso = findViewById(R.id.editTextPerfilPeso);
        autoCompleteSangre = findViewById(R.id.autoCompletePerfilSangre);
        autoCompleteSexo = findViewById(R.id.autoCompletePerfilSexo);
        edAlergias = findViewById(R.id.editTextPerfilAlergias);
        edEnfermedades = findViewById(R.id.editTextPerfilEnfermedades);
        edMedicamentos = findViewById(R.id.editTextPerfilMedicamentos);
        edContactoNombre = findViewById(R.id.editTextPerfilContactoNombre);
        edContactoTelefono = findViewById(R.id.editTextPerfilContactoTelefono);
        btnGuardar = findViewById(R.id.buttonGuardarPerfil);
    }

    private void configurarMenusDesplegables() {
        String[] opcionesSexo = getResources().getStringArray(R.array.opciones_sexo);
        String[] opcionesSangre = getResources().getStringArray(R.array.opciones_tipo_sangre);

        ArrayAdapter<String> adapterSexo = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesSexo);
        ArrayAdapter<String> adapterSangre = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesSangre);

        autoCompleteSexo.setAdapter(adapterSexo);
        autoCompleteSangre.setAdapter(adapterSangre);
    }

    private void configurarToolbarYRetroceso() {
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hayCambiosSinGuardar) {
                    new AlertDialog.Builder(PerfilPacienteActivity.this)
                            .setTitle("Descartar Cambios")
                            .setMessage("¿Estás seguro de que quieres salir? Los cambios no se guardarán.")
                            .setPositiveButton("Descartar", (dialog, which) -> finish())
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void configurarBotonGuardar() {
        btnGuardar.setOnClickListener(v -> {
            String estatura = edEstatura.getText().toString().trim();
            String peso = edPeso.getText().toString().trim();
            String sangre = autoCompleteSangre.getText().toString().trim();
            String sexo = autoCompleteSexo.getText().toString().trim();
            String alergias = edAlergias.getText().toString().trim();
            String enfermedades = edEnfermedades.getText().toString().trim();
            String medicamentos = edMedicamentos.getText().toString().trim();
            String contactoNombre = edContactoNombre.getText().toString().trim();
            String contactoTelefono = edContactoTelefono.getText().toString().trim();

            if (estatura.isEmpty() || peso.isEmpty() || sangre.isEmpty() || sexo.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Por favor, complete los campos obligatorios", Toast.LENGTH_LONG).show();
                return;
            }

            guardarCambiosEnFirestore(estatura, peso, sangre, sexo, alergias, enfermedades, medicamentos, contactoNombre, contactoTelefono);
        });
    }

    private void cargarDatosDelPerfil() {
        db.collection("pacientes")
                .document(idUsuario)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Datos médicos editables
                        setTextIfNotNull(edEstatura, documentSnapshot.getString("estatura"));
                        setTextIfNotNull(edPeso, documentSnapshot.getString("peso"));
                        setAutoCompleteTextIfNotNull(autoCompleteSangre, documentSnapshot.getString("tipo_sangre"));
                        setAutoCompleteTextIfNotNull(autoCompleteSexo, documentSnapshot.getString("sexo"));
                        setTextIfNotNull(edAlergias, documentSnapshot.getString("alergias"));
                        setTextIfNotNull(edEnfermedades, documentSnapshot.getString("enfermedades_cronicas"));
                        setTextIfNotNull(edMedicamentos, documentSnapshot.getString("medicamentos_actuales"));
                        setTextIfNotNull(edContactoNombre, documentSnapshot.getString("nombre_contacto_emergencia"));
                        setTextIfNotNull(edContactoTelefono, documentSnapshot.getString("celular_contacto_emergencia"));

                        hayCambiosSinGuardar = false;
                    } else {
                        Toast.makeText(this, "No se encontró el perfil del paciente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setTextIfNotNull(TextInputEditText editText, String value) {
        if (value != null && !value.isEmpty()) {
            editText.setText(value);
        }
    }

    private void setAutoCompleteTextIfNotNull(AutoCompleteTextView editText, String value) {
        if (value != null && !value.isEmpty()) {
            editText.setText(value, false);
        }
    }

    private void guardarCambiosEnFirestore(String estatura, String peso, String sangre, String sexo,
                                           String alergias, String enfermedades, String medicamentos,
                                           String contactoNombre, String contactoTelefono) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estatura", estatura);
        updates.put("peso", peso);
        updates.put("tipo_sangre", sangre);
        updates.put("sexo", sexo);
        updates.put("alergias", alergias);
        updates.put("enfermedades_cronicas", enfermedades);
        updates.put("medicamentos_actuales", medicamentos);
        updates.put("nombre_contacto_emergencia", contactoNombre);
        updates.put("celular_contacto_emergencia", contactoTelefono);

        db.collection("pacientes")
                .document(idUsuario)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    hayCambiosSinGuardar = false;
                    Toast.makeText(getApplicationContext(), "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void configurarListenersDeCambios() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hayCambiosSinGuardar = true;
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        edEstatura.addTextChangedListener(textWatcher);
        edPeso.addTextChangedListener(textWatcher);
        autoCompleteSangre.addTextChangedListener(textWatcher);
        autoCompleteSexo.addTextChangedListener(textWatcher);
        edAlergias.addTextChangedListener(textWatcher);
        edEnfermedades.addTextChangedListener(textWatcher);
        edMedicamentos.addTextChangedListener(textWatcher);
        edContactoNombre.addTextChangedListener(textWatcher);
        edContactoTelefono.addTextChangedListener(textWatcher);
    }
}

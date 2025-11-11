package com.example.saludmovil.ui.paciente;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.saludmovil.database.BaseDeDatos;
import com.example.saludmovil.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class PerfilPacienteActivity extends AppCompatActivity {

    // CAMBIO: Declaramos los componentes correctos del nuevo diseño
    TextInputEditText edEstatura, edPeso, edAlergias, edEnfermedades, edMedicamentos, edContactoNombre, edContactoTelefono;
    AutoCompleteTextView autoCompleteSexo, autoCompleteSangre;
    Button btnGuardar;
    MaterialToolbar toolbar;

    private int idUsuario;
    private boolean hayCambiosSinGuardar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_paciente);

        vincularVistas();
        configurarMenusDesplegables();
        configurarToolbarYRetroceso();

        idUsuario = getIntent().getIntExtra("id_usuario", -1);
        if (idUsuario == -1) {
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
        // CAMBIO: Usamos los nuevos IDs y variables
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
            String estatura = edEstatura.getText().toString();
            String peso = edPeso.getText().toString();
            String sangre = autoCompleteSangre.getText().toString();
            String sexo = autoCompleteSexo.getText().toString();
            String alergias = edAlergias.getText().toString();
            String enfermedades = edEnfermedades.getText().toString();
            String medicamentos = edMedicamentos.getText().toString();
            String contactoNombre = edContactoNombre.getText().toString();
            String contactoTelefono = edContactoTelefono.getText().toString();

            if (estatura.isEmpty() || peso.isEmpty() || sangre.isEmpty() || sexo.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Por favor, complete los campos obligatorios", Toast.LENGTH_LONG).show();
                return;
            }

            BaseDeDatos bd = new BaseDeDatos(getApplicationContext());
            bd.actualizarPerfilPacientePorId(idUsuario, estatura, peso, sangre, sexo, alergias, enfermedades, medicamentos, contactoNombre, contactoTelefono);

            hayCambiosSinGuardar = false;
            Toast.makeText(getApplicationContext(), "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void cargarDatosDelPerfil() {
        BaseDeDatos bd = new BaseDeDatos(getApplicationContext());
        Cursor cursor = bd.getPerfilPacientePorId(idUsuario);

        if (cursor != null && cursor.moveToFirst()) {
            setTextFromCursor(edEstatura, cursor, "estatura");
            setTextFromCursor(edPeso, cursor, "peso");
            setTextFromCursor(autoCompleteSangre, cursor, "tipo_sangre");
            setTextFromCursor(autoCompleteSexo, cursor, "sexo");
            setTextFromCursor(edAlergias, cursor, "alergias");
            setTextFromCursor(edEnfermedades, cursor, "enfermedades_cronicas");
            setTextFromCursor(edMedicamentos, cursor, "medicamentos_actuales");
            setTextFromCursor(edContactoNombre, cursor, "nombre_contacto_emergencia");
            setTextFromCursor(edContactoTelefono, cursor, "telefono_contacto_emergencia");
            cursor.close();
            hayCambiosSinGuardar = false;
        }
    }

    // Métodos auxiliares para limpiar el código
    private void setTextFromCursor(TextInputEditText editText, Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex != -1 && !cursor.isNull(columnIndex)) {
            editText.setText(cursor.getString(columnIndex));
        }
    }

    private void setTextFromCursor(AutoCompleteTextView editText, Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex != -1 && !cursor.isNull(columnIndex)) {
            editText.setText(cursor.getString(columnIndex), false); // el 'false' evita que se muestre el dropdown al cargar
        }
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
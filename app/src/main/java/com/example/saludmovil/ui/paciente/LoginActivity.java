package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludmovil.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText edDni, edClave;
    MaterialButton btnLogin, btnNuevoUsuario;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        edDni = findViewById(R.id.editTextLoginDni);
        edClave = findViewById(R.id.editTextLoginClave);
        btnLogin = findViewById(R.id.buttonLogin);
        btnNuevoUsuario = findViewById(R.id.textViewNuevoUsuario);

        btnLogin.setOnClickListener(view -> {
            String dni = edDni.getText().toString().trim();
            String clave = edClave.getText().toString().trim();

            if (dni.isEmpty() || clave.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Por favor, llene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            loginPacienteEnFirestore(dni, clave);
        });

        btnNuevoUsuario.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegistrarActivity.class));
        });
    }

    private void loginPacienteEnFirestore(String dni, String clave) {
        db.collection("pacientes")
                .whereEqualTo("dni", dni)
                .whereEqualTo("contrasena", clave)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        // ✨ CAMBIO: Usamos el ID real de Firestore (String)
                        String idFirestore = document.getId();
                        String rol = document.getString("rol");

                        // Guardamos el ID como String
                        guardarSesion(idFirestore, rol != null ? rol : "paciente");

                        Toast.makeText(getApplicationContext(), "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        irAlInicio();
                    } else {
                        Toast.makeText(getApplicationContext(), "DNI o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ✨ CAMBIO: El ID ahora es String
    private void guardarSesion(String id, String rol) {
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("id_usuario", id); // Usamos putString
        editor.putString("rol_usuario", rol);
        editor.apply();
    }

    private void irAlInicio() {
        startActivity(new Intent(LoginActivity.this, InicioActivity.class));
        finish();
    }
}
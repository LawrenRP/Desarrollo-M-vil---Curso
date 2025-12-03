package com.example.saludmovil.ui.doctor;

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

public class LoginDoctorActivity extends AppCompatActivity {

    TextInputEditText edCmp, edClave;
    MaterialButton btnLogin, btnRegistrar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_doctor);

        db = FirebaseFirestore.getInstance();

        edCmp = findViewById(R.id.editTextDoctorCodigo);
        edClave = findViewById(R.id.editTextDoctorClave);
        btnLogin = findViewById(R.id.buttonLoginDoctor);
        btnRegistrar = findViewById(R.id.textViewNuevoDoctor);

        btnRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginDoctorActivity.this, RegistrarDoctorPaso1Activity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String cmp = edCmp.getText().toString().trim();
            String clave = edClave.getText().toString().trim();

            if (cmp.isEmpty() || clave.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Llene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            loginDoctorEnFirestore(cmp, clave);
        });
    }

    private void loginDoctorEnFirestore(String cmp, String clave) {
        db.collection("doctores")
                .whereEqualTo("cmp", cmp)
                .whereEqualTo("contrasena", clave)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        // ✨ CAMBIO: Usamos el ID real de Firestore (String)
                        String idFirestore = document.getId();
                        String rol = "doctor";

                        guardarSesion(idFirestore, rol);

                        Toast.makeText(getApplicationContext(), "Bienvenido, Doctor(a)", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginDoctorActivity.this, InicioDoctorActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "CMP o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
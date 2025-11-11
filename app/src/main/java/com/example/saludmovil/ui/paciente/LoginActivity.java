package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.database.BaseDeDatos;
import com.example.saludmovil.ui.doctor.InicioDoctorActivity;
import com.example.saludmovil.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText edDni, edClave; // Esto ya lo habías cambiado, ¡perfecto!
    MaterialButton btnLogin, btnNuevoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edDni = findViewById(R.id.editTextLoginDni);
        edClave = findViewById(R.id.editTextLoginClave);
        btnLogin = findViewById(R.id.buttonLogin);
        btnNuevoUsuario = findViewById(R.id.textViewNuevoUsuario);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. Renombramos la variable local para mayor claridad
                String dni = edDni.getText().toString().trim();
                String clave = edClave.getText().toString().trim();
                BaseDeDatos bd = new BaseDeDatos(getApplicationContext());

                if (dni.isEmpty() || clave.isEmpty()) { // Usamos isEmpty() que es un poco más estándar
                    Toast.makeText(getApplicationContext(), "Por favor, llene todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cursor cursor = bd.loginPacientePorDNI(dni, clave);

                if (cursor != null && cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex("id");
                    int rolIndex = cursor.getColumnIndex("rol");

                    int usuarioId = cursor.getInt(idIndex);
                    String rol = cursor.getString(rolIndex);
                    cursor.close();

                    SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("id_usuario", usuarioId);
                    editor.putString("rol_usuario", rol);
                    editor.apply();

                    Toast.makeText(getApplicationContext(), "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                    if ("paciente".equals(rol)) {
                        startActivity(new Intent(LoginActivity.this, InicioActivity.class));
                    } else if ("doctor".equals(rol)) {
                        startActivity(new Intent(LoginActivity.this, InicioDoctorActivity.class));
                    }
                    finish();

                } else {
                    if(cursor != null) {
                        cursor.close();
                    }
                    // 3. Actualizamos el mensaje de error
                    Toast.makeText(getApplicationContext(), "DNI o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnNuevoUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegistrarActivity.class));
            }
        });
    }
}
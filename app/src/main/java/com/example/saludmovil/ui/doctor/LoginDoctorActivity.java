package com.example.saludmovil.ui.doctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludmovil.database.BaseDeDatos;
import com.example.saludmovil.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginDoctorActivity extends AppCompatActivity {

    TextInputEditText edCmp, edClave; // ¡Perfecto que ya lo hayas cambiado!
    MaterialButton btnLogin, btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_doctor);

        edCmp = findViewById(R.id.editTextDoctorCodigo);
        edClave = findViewById(R.id.editTextDoctorClave);
        btnLogin = findViewById(R.id.buttonLoginDoctor);
        btnRegistrar = findViewById(R.id.textViewNuevoDoctor);

        btnRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginDoctorActivity.this, RegistrarDoctorPaso1Activity.class);
            startActivity(intent);
        });


        btnLogin.setOnClickListener(v -> {
            // 1. Nombramos la variable local para que sea 'cmp'
            String cmp = edCmp.getText().toString().trim();
            String clave = edClave.getText().toString().trim();
            BaseDeDatos bd = new BaseDeDatos(getApplicationContext());

            if (cmp.isEmpty() || clave.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Llene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2 ¡EL CAMBIO MÁS IMPORTANTE! Usamos el nuevo método por CMP
            Cursor cursor = bd.loginDoctorPorCMP(cmp, clave);

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                int rolIndex = cursor.getColumnIndex("rol");
                int usuarioId = cursor.getInt(idIndex);
                String rol = cursor.getString(rolIndex);

                if ("doctor".equals(rol)) {
                    cursor.close();

                    SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("id_usuario", usuarioId);
                    editor.putString("rol_usuario", rol);
                    editor.apply();

                    Toast.makeText(getApplicationContext(), "Bienvenido, Doctor(a)", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginDoctorActivity.this, InicioDoctorActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    cursor.close();
                    Toast.makeText(getApplicationContext(), "Esta cuenta no pertenece a un doctor.", Toast.LENGTH_SHORT).show();
                }

            } else {
                if(cursor != null) {
                    cursor.close();
                }
                // 3. Actualizamos el mensaje de error
                Toast.makeText(getApplicationContext(), "CMP o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
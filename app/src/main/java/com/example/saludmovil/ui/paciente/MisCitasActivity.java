package com.example.saludmovil.ui.paciente;

import android.os.Bundle;
import android.view.View; // <-- 1. Importar View
import android.widget.ImageButton; // <-- 2. Importar ImageButton

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.saludmovil.R;

public class MisCitasActivity extends AppCompatActivity {

    // --- ✨ CÓDIGO AÑADIDO (Declaración) ---
    ImageButton btnRetroceder;
    // --- FIN DEL CÓDIGO AÑADIDO ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mis_citas);

        // --- ✨ CÓDIGO AÑADIDO (Conexión y Lógica) ---

        // 1. Conectamos la variable con el ID del XML
        btnRetroceder = findViewById(R.id.buttonRetrocederMisCitas);

        // 2. Le damos la acción al hacer clic
        btnRetroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. finish() cierra esta pantalla y regresa a la anterior (InicioActivity)
                finish();
            }
        });
        // --- FIN DEL CÓDIGO AÑADIDO ---


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
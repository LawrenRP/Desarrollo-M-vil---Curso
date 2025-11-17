package com.example.saludmovil.ui.doctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.PacienteBusquedaAdapter;
import com.example.saludmovil.data.Paciente;
import com.example.saludmovil.database.BaseDeDatos;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class BuscarPacienteActivity extends AppCompatActivity implements PacienteBusquedaAdapter.OnPacienteClickListener {

    private Toolbar toolbar;
    private TextInputEditText etBuscarPaciente;
    private RecyclerView recyclerViewBusqueda;

    private BaseDeDatos bd;
    private PacienteBusquedaAdapter adapter;
    private int idUsuarioDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_paciente);

        toolbar = findViewById(R.id.toolbarBuscarPaciente);
        etBuscarPaciente = findViewById(R.id.etBuscarPaciente);
        recyclerViewBusqueda = findViewById(R.id.recyclerViewBusqueda);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getInt("id_usuario", -1);
        if (idUsuarioDoctor == -1) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bd = new BaseDeDatos(this);
        adapter = new PacienteBusquedaAdapter(this);
        recyclerViewBusqueda.setAdapter(adapter);

        etBuscarPaciente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarPacientes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buscarPacientes("");
    }

    private void buscarPacientes(String terminoBusqueda) {
        ArrayList<Paciente> pacientesEncontrados = new ArrayList<>();
        Cursor cursor = bd.buscarPacientesAtendidos(idUsuarioDoctor, terminoBusqueda);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id_usuario");
            int dniIndex = cursor.getColumnIndex("dni");
            int nombreIndex = cursor.getColumnIndex("nombre");
            int apellidoIndex = cursor.getColumnIndex("apellido");

            do {
                if (idIndex != -1 && dniIndex != -1 && nombreIndex != -1 && apellidoIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String dni = cursor.getString(dniIndex);
                    String nombre = cursor.getString(nombreIndex);
                    String apellido = cursor.getString(apellidoIndex);
                    pacientesEncontrados.add(new Paciente(id, dni, nombre, apellido));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.submitList(pacientesEncontrados);
    }

    @Override
    public void onPacienteClick(Paciente paciente) {
        Toast.makeText(this, "Viendo expediente de: " + paciente.getNombre(), Toast.LENGTH_SHORT).show();


         Intent intent = new Intent(this, ExpedientePacienteActivity.class);
         intent.putExtra("id_paciente", paciente.getIdUsuario());
         startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bd != null) {
            bd.close();
        }
    }
}
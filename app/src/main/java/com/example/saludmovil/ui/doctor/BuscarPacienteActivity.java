package com.example.saludmovil.ui.doctor;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuscarPacienteActivity extends AppCompatActivity implements PacienteBusquedaAdapter.OnPacienteClickListener {

    private Toolbar toolbar;
    private TextInputEditText etBuscarPaciente;
    private RecyclerView recyclerViewBusqueda;

    private FirebaseFirestore db;
    private PacienteBusquedaAdapter adapter;
    private List<Paciente> listaMaestraPacientes;
    private String idUsuarioDoctor; // ✨ Necesitamos el ID del doctor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_paciente);

        db = FirebaseFirestore.getInstance();
        listaMaestraPacientes = new ArrayList<>();

        // Obtener ID del Doctor
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getString("id_usuario", null);
        if (idUsuarioDoctor == null) {
            finish(); // Seguridad
            return;
        }

        toolbar = findViewById(R.id.toolbarBuscarPaciente);
        etBuscarPaciente = findViewById(R.id.etBuscarPaciente);
        recyclerViewBusqueda = findViewById(R.id.recyclerViewBusqueda);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new PacienteBusquedaAdapter(this);
        recyclerViewBusqueda.setAdapter(adapter);

        // Cargar SOLO mis pacientes
        cargarMisPacientesDeFirestore();

        etBuscarPaciente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPacientes(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarMisPacientesDeFirestore() {
        // 1. Buscar todas mis citas para obtener los IDs de mis pacientes
        db.collection("citas")
                .whereEqualTo("id_doctor", idUsuarioDoctor)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> idsPacientesUnicos = new HashSet<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String idPac = doc.getString("id_paciente");
                        if (idPac != null) {
                            idsPacientesUnicos.add(idPac);
                        }
                    }

                    if (idsPacientesUnicos.isEmpty()) {
                        // No tienes pacientes aún
                        Toast.makeText(this, "Aún no has atendido a ningún paciente.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2. Descargar los datos de ESOS pacientes
                    cargarDetallesPacientes(new ArrayList<>(idsPacientesUnicos));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar citas: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cargarDetallesPacientes(List<String> ids) {
        // Firestore permite buscar por 'IN' (hasta 10 items, si son más hay que hacer lotes)
        // Para ser robustos y simples, haremos una consulta por cada ID (en paralelo es rápido)
        // O podemos descargar todos y filtrar localmente si la BD no es gigante.

        // Opción Híbrida: Descargar todos y filtrar por ID (Más fácil de implementar ahora)
        db.collection("pacientes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaMaestraPacientes.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // ¿Este paciente está en mi lista de IDs?
                        if (ids.contains(doc.getId())) {
                            String id = doc.getId();
                            String dni = doc.getString("dni");
                            String nombre = doc.getString("nombre");
                            String apellido = doc.getString("apellido");

                            if (nombre == null) nombre = "";
                            if (apellido == null) apellido = "";
                            if (dni == null) dni = "";

                            listaMaestraPacientes.add(new Paciente(id, dni, nombre, apellido));
                        }
                    }
                    // Mostrar la lista
                    adapter.submitList(new ArrayList<>(listaMaestraPacientes));
                });
    }

    private void filtrarPacientes(String texto) {
        if (texto.isEmpty()) {
            adapter.submitList(new ArrayList<>(listaMaestraPacientes));
            return;
        }
        List<Paciente> filtrada = new ArrayList<>();
        String busqueda = texto.toLowerCase();
        for (Paciente p : listaMaestraPacientes) {
            if (p.getNombre().toLowerCase().contains(busqueda) ||
                    p.getApellido().toLowerCase().contains(busqueda) ||
                    p.getDni().contains(busqueda)) {
                filtrada.add(p);
            }
        }
        adapter.submitList(filtrada);
    }

    @Override
    public void onPacienteClick(Paciente paciente) {
        Intent intent = new Intent(BuscarPacienteActivity.this, ExpedientePacienteActivity.class);
        intent.putExtra("id_paciente", paciente.getIdUsuario());
        startActivity(intent);
    }
}
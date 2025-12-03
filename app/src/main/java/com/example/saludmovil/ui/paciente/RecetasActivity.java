package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.RecetasAdapter;
import com.example.saludmovil.data.Receta;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class RecetasActivity extends AppCompatActivity implements RecetasAdapter.OnRecetaClickListener {

    private RecyclerView recyclerView;
    private TextView tvSinRecetas;
    private AutoCompleteTextView autoCompleteEspecialidad;
    private FirebaseFirestore db;
    private String idUsuario;
    private RecetasAdapter adapter;
    private ArrayList<Receta> listaCompleta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recetas);

        db = FirebaseFirestore.getInstance();

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuario = sp.getString("id_usuario", null);

        if (idUsuario == null) {
            finish();
            return;
        }

        initViews();
        listaCompleta = new ArrayList<>();
        setupRecyclerView();
        cargarRecetasDeFirestore();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewRecetas);
        tvSinRecetas = findViewById(R.id.tvSinRecetas);
        autoCompleteEspecialidad = findViewById(R.id.autoCompleteEspecialidadFiltro);
        findViewById(R.id.buttonRetrocederRecetas).setOnClickListener(v -> finish());

        // Dropdown simple (puedes mejorarlo luego leyendo de BD)
        String[] opciones = {"Todas", "Medicina General", "Cardiología"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opciones);
        autoCompleteEspecialidad.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        adapter = new RecetasAdapter(this, listaCompleta, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void cargarRecetasDeFirestore() {
        db.collection("recetas")
                .whereEqualTo("id_paciente", idUsuario)
                // .orderBy("fecha_emision", Query.Direction.DESCENDING) // Comenta esto si no tienes el índice aún
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaCompleta.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();

                        // Lectura segura de campos (evita nulos)
                        String fecha = doc.getString("fecha_emision");
                        if (fecha == null) fecha = "Fecha desconocida";

                        String doctor = doc.getString("nombre_doctor_temp");
                        if (doctor == null) doctor = "Dr. Asignado";

                        // La especialidad a veces no la guardamos, ponemos default
                        String especialidad = "Medicina General";
                        if (doc.contains("especialidad")) {
                            especialidad = doc.getString("especialidad");
                        }

                        listaCompleta.add(new Receta(id, fecha, doctor, especialidad));
                    }
                    adapter.actualizarLista(listaCompleta);

                    if (listaCompleta.isEmpty()) {
                        tvSinRecetas.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvSinRecetas.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRecetaClick(String idReceta) {
        Intent intent = new Intent(this, RecetaDetalleActivity.class);
        intent.putExtra("id_receta", idReceta);
        startActivity(intent);
    }
}
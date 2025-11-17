package com.example.saludmovil.ui.paciente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.RecetasAdapter;
import com.example.saludmovil.data.Receta;
import com.example.saludmovil.database.BaseDeDatos;

import java.util.ArrayList;

public class RecetasActivity extends AppCompatActivity implements RecetasAdapter.OnRecetaClickListener {

    private RecyclerView recyclerView;
    private TextView tvSinRecetas;
    private BaseDeDatos bd;
    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recetas);

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuario = sp.getInt("id_usuario", -1);

        recyclerView = findViewById(R.id.recyclerViewRecetas);
        tvSinRecetas = findViewById(R.id.tvSinRecetas);
        ImageButton btnAtras = findViewById(R.id.buttonRetrocederRecetas);

        btnAtras.setOnClickListener(v -> finish());

        bd = new BaseDeDatos(this);
        cargarRecetas();
    }

    private void cargarRecetas() {
        ArrayList<Receta> lista = new ArrayList<>();
        Cursor cursor = bd.getRecetasDelPaciente(idUsuario);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha_emision"));
                String doctor = cursor.getString(cursor.getColumnIndexOrThrow("nombre_completo"));
                String especialidad = cursor.getString(cursor.getColumnIndexOrThrow("especialidad"));
                lista.add(new Receta(id, 0, fecha, doctor, especialidad));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (lista.isEmpty()) {
            tvSinRecetas.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvSinRecetas.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            RecetasAdapter adapter = new RecetasAdapter(this, lista, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onRecetaClick(int idReceta) {
        Intent intent = new Intent(this, RecetaDetalleActivity.class);
        intent.putExtra("id_receta", idReceta);
        startActivity(intent);
    }
}
package com.example.saludmovil.ui.paciente;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.RecetasAdapter;
import com.example.saludmovil.data.Receta;
import com.example.saludmovil.database.BaseDeDatos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RecetasActivity extends AppCompatActivity implements RecetasAdapter.OnRecetaClickListener {

    private RecyclerView recyclerView;
    private TextView tvSinRecetas;
    private AutoCompleteTextView autoCompleteEspecialidad;
    private Button btnFiltrarFecha;

    private BaseDeDatos bd;
    private int idUsuario;
    private RecetasAdapter adapter;
    private ArrayList<Receta> listaCompleta;
    private ArrayList<Receta> listaFiltrada;
    private String filtroEspecialidad = "";
    private String filtroFecha = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recetas);

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuario = sp.getInt("id_usuario", -1);

        initViews();
        bd = new BaseDeDatos(this);

        listaCompleta = new ArrayList<>();
        listaFiltrada = new ArrayList<>();

        configurarFiltros();
        cargarRecetas();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewRecetas);
        tvSinRecetas = findViewById(R.id.tvSinRecetas);
        autoCompleteEspecialidad = findViewById(R.id.autoCompleteEspecialidadFiltro);
        btnFiltrarFecha = findViewById(R.id.btnFiltrarFecha);
        ImageButton btnAtras = findViewById(R.id.buttonRetrocederRecetas);

        btnAtras.setOnClickListener(v -> finish());
    }

    private void configurarFiltros() {
        cargarEspecialidadesDropdown();

        autoCompleteEspecialidad.setOnItemClickListener((parent, view, position, id) -> {
            filtroEspecialidad = parent.getItemAtPosition(position).toString();
            aplicarFiltros();
        });

        autoCompleteEspecialidad.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    filtroEspecialidad = "";
                    aplicarFiltros();
                }
            }
        });
        btnFiltrarFecha.setOnClickListener(v -> mostrarCalendario());
        btnFiltrarFecha.setOnLongClickListener(v -> {
            filtroFecha = "";
            btnFiltrarFecha.setText("Seleccionar Fecha");
            aplicarFiltros();
            Toast.makeText(this, "Filtro de fecha borrado", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void cargarEspecialidadesDropdown() {
        ArrayList<String> especialidades = new ArrayList<>();
        especialidades.add("Todas");

        Cursor c = bd.getEspecialidades();
        if (c != null && c.moveToFirst()) {
            do {
                especialidades.add(c.getString(c.getColumnIndexOrThrow("nombre")));
            } while (c.moveToNext());
            c.close();
        }

        ArrayAdapter<String> adapterEsp = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, especialidades);
        autoCompleteEspecialidad.setAdapter(adapterEsp);
    }

    private void mostrarCalendario() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar fechaSeleccionada = Calendar.getInstance();
                    fechaSeleccionada.set(year, month, dayOfMonth);
                    SimpleDateFormat sdfBD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    filtroFecha = sdfBD.format(fechaSeleccionada.getTime());
                    SimpleDateFormat sdfBtn = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    btnFiltrarFecha.setText(sdfBtn.format(fechaSeleccionada.getTime()));
                    aplicarFiltros();
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    private void cargarRecetas() {
        listaCompleta.clear();
        Cursor cursor = bd.getRecetasDelPaciente(idUsuario);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha_emision"));
                String doctor = cursor.getString(cursor.getColumnIndexOrThrow("nombre_completo"));
                String especialidad = cursor.getString(cursor.getColumnIndexOrThrow("especialidad"));

                listaCompleta.add(new Receta(id, 0, fecha, doctor, especialidad));
            } while (cursor.moveToNext());
            cursor.close();
        }
        listaFiltrada.addAll(listaCompleta);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        adapter = new RecetasAdapter(this, listaFiltrada, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        actualizarEstadoVacio();
    }
    private void aplicarFiltros() {
        listaFiltrada.clear();

        for (Receta receta : listaCompleta) {
            boolean coincideEspecialidad = true;
            boolean coincideFecha = true;
            if (!filtroEspecialidad.isEmpty() && !filtroEspecialidad.equals("Todas")) {
                if (!receta.getEspecialidad().equalsIgnoreCase(filtroEspecialidad)) {
                    coincideEspecialidad = false;
                }
            }
            if (!filtroFecha.isEmpty()) {
                if (!receta.getFechaEmision().equals(filtroFecha)) {
                    coincideFecha = false;
                }
            }
            if (coincideEspecialidad && coincideFecha) {
                listaFiltrada.add(receta);
            }
        }

        adapter.actualizarLista(listaFiltrada);
        actualizarEstadoVacio();
    }

    private void actualizarEstadoVacio() {
        if (listaFiltrada.isEmpty()) {
            tvSinRecetas.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvSinRecetas.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRecetaClick(int idReceta) {
        Intent intent = new Intent(this, RecetaDetalleActivity.class);
        intent.putExtra("id_receta", idReceta);
        startActivity(intent);
    }
}
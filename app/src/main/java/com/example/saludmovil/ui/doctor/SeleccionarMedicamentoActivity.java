package com.example.saludmovil.ui.doctor;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludmovil.R;
import com.example.saludmovil.adapters.MedicamentoSeleccionAdapter;
import com.example.saludmovil.data.Medicamento;
import com.example.saludmovil.data.MedicamentoRecetado;
import com.example.saludmovil.database.BaseDeDatos;
import java.util.ArrayList;

public class SeleccionarMedicamentoActivity extends AppCompatActivity {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private Button btnConfirmar;
    private ImageButton btnAtras;

    private BaseDeDatos bd;
    private ArrayList<Medicamento> listaCompletaMedicamentos;
    private MedicamentoSeleccionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_medicamento);

        searchView = findViewById(R.id.searchViewMedicamentos);
        recyclerView = findViewById(R.id.rvListaMedicamentos);
        btnConfirmar = findViewById(R.id.btnConfirmarSeleccion);
        btnAtras = findViewById(R.id.buttonAtras);

        bd = new BaseDeDatos(this);
        listaCompletaMedicamentos = new ArrayList<>();

        cargarMedicamentos("");
        adapter = new MedicamentoSeleccionAdapter(this, listaCompletaMedicamentos);
        recyclerView.setAdapter(adapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrar(newText);
                return true;
            }
        });

        btnAtras.setOnClickListener(v -> finish());
        btnConfirmar.setOnClickListener(v -> confirmarSeleccion());
    }

    private void cargarMedicamentos(String busqueda) {
        listaCompletaMedicamentos.clear();
        Cursor cursor = bd.buscarMedicamentos(busqueda);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int nombreIndex = cursor.getColumnIndex("nombre");
            int presIndex = cursor.getColumnIndex("presentacion");
            int stockIndex = cursor.getColumnIndex("stock");

            do {
                int id = cursor.getInt(idIndex);
                String nombre = cursor.getString(nombreIndex);
                String presentacion = cursor.getString(presIndex);
                int stock = cursor.getInt(stockIndex);

                listaCompletaMedicamentos.add(new Medicamento(id, nombre, presentacion, stock));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
    private void filtrar(String texto) {
        ArrayList<Medicamento> listaFiltrada = new ArrayList<>();
        for (Medicamento item : listaCompletaMedicamentos) {
            if (item.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                listaFiltrada.add(item);
            }
        }
        adapter.filtrarLista(listaFiltrada);
    }

    private void confirmarSeleccion() {
        ArrayList<MedicamentoRecetado> seleccionadosFinales = new ArrayList<>();
        for (Medicamento med : listaCompletaMedicamentos) {
            if (med.isSeleccionado()) {
                if (med.getCantidadUsuario().isEmpty()) {
                    Toast.makeText(this, "Falta la cantidad para: " + med.getNombre(), Toast.LENGTH_SHORT).show();
                    return;
                }
                String nombreCompleto = med.getNombre() + " " + med.getPresentacion();
                String indicaciones = med.getIndicacionesUsuario().isEmpty() ? "Sin indicaciones específicas" : med.getIndicacionesUsuario();

                seleccionadosFinales.add(new MedicamentoRecetado(
                        med.getId(),
                        nombreCompleto,
                        med.getCantidadUsuario(),
                        indicaciones
                ));
            }
        }

        if (seleccionadosFinales.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un medicamento", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra("medicamentos_seleccionados", seleccionadosFinales);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
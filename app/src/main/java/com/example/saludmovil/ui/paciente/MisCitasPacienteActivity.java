package com.example.saludmovil.ui.paciente;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.saludmovil.R;
import com.example.saludmovil.adapters.CitasViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MisCitasPacienteActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private CitasViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas_paciente);
        toolbar = findViewById(R.id.toolbarMisCitasPaciente);
        tabLayout = findViewById(R.id.tabLayoutCitas);
        viewPager = findViewById(R.id.viewPagerCitas);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        viewPagerAdapter = new CitasViewPagerAdapter(this);

        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Próximas");
                    break;
                case 1:
                    tab.setText("Pasadas");
                    break;
            }
        }).attach();
    }
}
package com.example.saludmovil.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.saludmovil.ui.paciente.CitasPasadasFragment;
import com.example.saludmovil.ui.paciente.CitasProximasFragment;

public class CitasViewPagerAdapter extends FragmentStateAdapter {

    public CitasViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CitasProximasFragment();
            case 1:
                return new CitasPasadasFragment();
            default:
                return new CitasProximasFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
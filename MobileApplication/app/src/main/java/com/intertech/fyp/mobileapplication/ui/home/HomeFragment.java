package com.intertech.fyp.mobileapplication.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.intertech.fyp.mobileapplication.R;
import com.intertech.fyp.mobileapplication.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private View root;
    Button transactionButton, productButton, deviceButton, profileButton;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        productButton = root.findViewById(R.id.homeProducts);
        productButton.setOnClickListener(this::toPages);

        transactionButton = root.findViewById(R.id.homeTransactions);
        transactionButton.setOnClickListener(this::toPages);

        deviceButton = root.findViewById(R.id.homeDevices);
        deviceButton.setOnClickListener(this::toPages);

        profileButton = root.findViewById(R.id.homeProfile);
        profileButton.setOnClickListener(this::toPages);

        return root;
    }

    private void toPages(View view)
    {
        switch (view.getId())
        {
            case R.id.homeProducts:
                Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_products);
                break;

            case R.id.homeTransactions:
                Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_transactions);
                break;

            case R.id.homeDevices:
                Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_devices);
                break;

            case R.id.homeProfile:
                Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_profile);
                break;

            default:

                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onResume() {
        super.onResume();
    }
}
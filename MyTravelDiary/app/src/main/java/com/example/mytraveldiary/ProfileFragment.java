package com.example.mytraveldiary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private EditText editName, editEmail, editFavorites;
    private Button btnEdit, btnLogout, btnDelete;
    private TextView textName, textEmail;
    private boolean isEditing = false;
    private AppData appData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        appData = AppData.getInstance();
        textName = root.findViewById(R.id.textName);
        textEmail = root.findViewById(R.id.textEmail);
        editName = root.findViewById(R.id.editName);
        editEmail = root.findViewById(R.id.editEmail);
        editFavorites = root.findViewById(R.id.editFavorites);
        btnEdit = root.findViewById(R.id.btnEdit);
        btnLogout = root.findViewById(R.id.btnLogout);
        btnDelete = root.findViewById(R.id.btnDelete);

        loadProfile();
        setEditingEnabled(false);

        btnEdit.setOnClickListener(v -> toggleEditMode());
        btnLogout.setOnClickListener(v -> appData.logout(requireContext()));
        btnDelete.setOnClickListener(v -> confirmDelete());

        return root;
    }

    private void loadProfile() {
        UserProfile profile = appData.getProfile();
        if (profile == null) return;

        textName.setText(profile.getName());
        textEmail.setText(profile.getEmail());
        editName.setText(profile.getName());
        editEmail.setText(profile.getEmail());
        editFavorites.setText(profile.getFavoriteDestinations());
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        setEditingEnabled(isEditing);

        if (!isEditing) {
            // Save profile
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String favorites = editFavorites.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            UserProfile updated = new UserProfile(name, email, favorites);
            appData.updateProfile(updated);
            loadProfile();

            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            btnEdit.setText("Edit Profile");
        } else {
            btnEdit.setText("Save Changes");
        }
    }

    private void setEditingEnabled(boolean enabled) {
        editName.setEnabled(enabled);
        editEmail.setEnabled(enabled);
        editFavorites.setEnabled(enabled);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    appData.deleteAccount(requireContext());
                    loadProfile();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

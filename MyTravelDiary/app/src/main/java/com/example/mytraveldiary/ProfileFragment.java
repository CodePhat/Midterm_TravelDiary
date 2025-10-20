package com.example.mytraveldiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    private EditText editName, editEmail, editFavorites;
    private Button btnEdit, btnLogout, btnDelete, btnClearCache;
    private TextView textName, textEmail;
    private ImageView imageProfile;
    private SwitchCompat switchDarkMode;
    private boolean isEditing = false;
    private AppData appData;
    private ActivityResultLauncher<String> profileImagePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileImagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    AppData data = AppData.getInstance();
                    UserProfile profile = data.getProfile();
                    if (profile != null) {
                        profile.setProfileImageUri(uri.toString());
                        loadProfileImage();
                        Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        appData = AppData.getInstance();
        textName = root.findViewById(R.id.textName);
        textEmail = root.findViewById(R.id.textEmail);
        imageProfile = root.findViewById(R.id.imageProfile);
        editName = root.findViewById(R.id.editName);
        editEmail = root.findViewById(R.id.editEmail);
        editFavorites = root.findViewById(R.id.editFavorites);
        btnEdit = root.findViewById(R.id.btnEdit);
        btnLogout = root.findViewById(R.id.btnLogout);
        btnDelete = root.findViewById(R.id.btnDelete);
        btnClearCache = root.findViewById(R.id.btnClearCache);
        switchDarkMode = root.findViewById(R.id.switchDarkMode);

        loadProfile();
        setEditingEnabled(false);
        updateProfileImageClickable(false);

        // Click profile image to change it (only when editing)
        imageProfile.setOnClickListener(v -> {
            if (isEditing) {
                profileImagePicker.launch("image/*");
            }
        });

        btnEdit.setOnClickListener(v -> toggleEditMode());
        btnLogout.setOnClickListener(v -> logoutAndReturnToLogin());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnClearCache.setOnClickListener(v -> confirmClearCache());

        // Initialize dark mode switch state
        switchDarkMode.setChecked(ThemeManager.isDarkMode(requireContext()));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(requireContext(), isChecked);
            Toast.makeText(requireContext(),
                isChecked ? "Dark mode enabled" : "Light mode enabled",
                Toast.LENGTH_SHORT).show();
        });

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
        loadProfileImage();
    }

    private void loadProfileImage() {
        UserProfile profile = appData.getProfile();
        if (profile != null && profile.getProfileImageUri() != null) {
            Glide.with(this)
                .load(Uri.parse(profile.getProfileImageUri()))
                .circleCrop()
                .placeholder(R.drawable.img)
                .into(imageProfile);
        } else {
            imageProfile.setImageResource(R.drawable.img);
        }
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        setEditingEnabled(isEditing);
        updateProfileImageClickable(isEditing);

        if (!isEditing) {
            // Save profile
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String favorites = editFavorites.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Preserve the profile image URI
            UserProfile currentProfile = appData.getProfile();
            UserProfile updated = new UserProfile(name, email, favorites);
            if (currentProfile != null && currentProfile.getProfileImageUri() != null) {
                updated.setProfileImageUri(currentProfile.getProfileImageUri());
            }

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

    private void updateProfileImageClickable(boolean clickable) {
        imageProfile.setClickable(clickable);
        imageProfile.setAlpha(clickable ? 1.0f : 0.7f);
    }

    // Log out user and go back to LoginActivity
    private void logoutAndReturnToLogin() {
        appData.logout(requireContext());
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Go to LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // Confirm delete, then go back to LoginActivity
    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    appData.deleteAccount(requireContext());
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Confirm clear cache
    private void confirmClearCache() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear Cache")
                .setMessage("This will delete all your trips, expenses, itineraries, and diary entries. Your account will remain but all data will be cleared.\n\nAre you sure?")
                .setPositiveButton("Clear Cache", (dialog, which) -> {
                    appData.clearCache(requireContext());

                    // Refresh profile UI to show cleared data
                    loadProfile();

                    // Show confirmation
                    Toast.makeText(requireContext(), "All data cleared successfully", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

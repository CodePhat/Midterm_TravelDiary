package com.example.mytraveldiary.ui.fragments;

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

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.data.models.UserProfile;
import com.example.mytraveldiary.ui.activities.LoginActivity;
import com.example.mytraveldiary.utils.helpers.ThemeManager;
import com.example.mytraveldiary.services.sync.DataSyncService;

public class ProfileFragment extends Fragment {

    private EditText editName, editEmail, editFavorites;
    private Button btnEdit, btnLogout, btnDelete, btnClearCache, btnLanguage, btnSyncCloud;
    private TextView textName, textEmail;
    private ImageView imageProfile;
    private SwitchCompat switchDarkMode;
    private boolean isEditing = false;
    private AppData appData;
    private ActivityResultLauncher<String> profileImagePicker;
    private android.os.Handler syncHandler;  // MEMORY LEAK FIX: Proper Handler cleanup

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileImagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        // Take persistent URI permission so we can access it later
                        requireContext().getContentResolver().takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        AppData data = AppData.getInstance();
                        UserProfile profile = data.getProfile();
                        if (profile != null) {
                            profile.setProfileImageUri(uri.toString());
                            loadProfileImage();

                            // Trigger save to persist changes
                            data.triggerSave();

                            Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();

                            // Notify other fragments to refresh (e.g., dashboard)
                            Intent broadcastIntent = new Intent("com.example.mytraveldiary.PROFILE_UPDATED");
                            requireContext().sendBroadcast(broadcastIntent);
                        }
                    } catch (Exception e) {
                        // If taking persistent permission fails, still use the URI
                        AppData data = AppData.getInstance();
                        UserProfile profile = data.getProfile();
                        if (profile != null) {
                            profile.setProfileImageUri(uri.toString());
                            loadProfileImage();
                            data.triggerSave();
                            Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();

                            // Notify other fragments
                            Intent broadcastIntent = new Intent("com.example.mytraveldiary.PROFILE_UPDATED");
                            requireContext().sendBroadcast(broadcastIntent);
                        }
                        android.util.Log.w("ProfileFragment", "Could not take persistent URI permission", e);
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        appData = AppData.getInstance();
        syncHandler = new android.os.Handler(android.os.Looper.getMainLooper());  // Initialize Handler
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
        btnLanguage = root.findViewById(R.id.btnLanguage);
        btnSyncCloud = root.findViewById(R.id.btnSyncCloud);
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
        btnLanguage.setOnClickListener(v -> showLanguageDialog());
        btnSyncCloud.setOnClickListener(v -> syncToCloud());

        // Initialize dark mode switch state
        switchDarkMode.setChecked(ThemeManager.isDarkMode(requireContext()));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TRANSLATION FIX: Apply theme change which will recreate activity
            // The MainActivity.attachBaseContext() will automatically preserve the locale
            ThemeManager.setDarkMode(requireContext(), isChecked);

            // TRANSLATION FIX: Use string resources for toast
            Toast.makeText(requireContext(),
                isChecked ? R.string.msg_dark_mode_enabled : R.string.msg_light_mode_enabled,
                Toast.LENGTH_SHORT).show();
        });

        // IMPROVEMENT: Update language button to show current language
        updateLanguageButtonText();

        // Update UI labels with machine translation
        updateUI();

        return root;
    }

    /**
     * Update language button text to show current language
     */
    private void updateLanguageButtonText() {
        String currentLang = com.example.mytraveldiary.utils.LocaleHelper.getLanguage(requireContext());
        String langDisplay = currentLang.equals("vi") ? getString(R.string.lang_vi) : getString(R.string.lang_en);
        btnLanguage.setText(getString(R.string.language) + ": " + langDisplay);
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
        if (profile != null && profile.getProfileImageUri() != null && !profile.getProfileImageUri().isEmpty()) {
            try {
                if (getContext() != null && isAdded()) {
                    Glide.with(requireContext().getApplicationContext())
                        .load(Uri.parse(profile.getProfileImageUri()))
                        .circleCrop()
                        .placeholder(R.drawable.img)
                        .error(R.drawable.img)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC)
                        .into(imageProfile);
                }
            } catch (Exception e) {
                imageProfile.setImageResource(R.drawable.img);
            }
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

            Toast.makeText(getContext(), R.string.msg_profile_updated, Toast.LENGTH_SHORT).show();
            // TRANSLATION FIX: Use string resource
            btnEdit.setText(R.string.btn_edit_profile);
        } else {
            // TRANSLATION FIX: Use string resource
            btnEdit.setText(R.string.btn_save_changes);
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
        // FIX: Use requireActivity() for proper locale context
        new AlertDialog.Builder(requireActivity())
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
        // FIX: Use requireActivity() for proper locale context
        new AlertDialog.Builder(requireActivity())
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

    // Show language selection dialog
    private void showLanguageDialog() {
        String[] languages = {getString(R.string.lang_en), getString(R.string.lang_vi)};
        String[] languageCodes = {"en", "vi"};

        // IMPROVEMENT: Get current language and highlight it
        String currentLang = com.example.mytraveldiary.utils.LocaleHelper.getLanguage(requireContext());
        int currentIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                currentIndex = i;
                break;
            }
        }

        // FIX: Use requireActivity() instead of requireContext() to get Activity's wrapped context with correct locale
        // IMPROVEMENT: Use setSingleChoiceItems to show current selection with radio button
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.change_language)
                .setSingleChoiceItems(languages, currentIndex, (dialog, which) -> {
                    String selectedLang = languageCodes[which];

                    // Don't do anything if same language is selected
                    if (selectedLang.equals(currentLang)) {
                        dialog.dismiss();
                        return;
                    }

                    // Start LoadingActivity to apply language change
                    Intent intent = new Intent(requireContext(), com.example.mytraveldiary.ui.activities.LoadingActivity.class);
                    intent.putExtra(com.example.mytraveldiary.ui.activities.LoadingActivity.EXTRA_LANG, selectedLang);
                    startActivity(intent);
                    // MEMORY LEAK FIX: Finish current Activity to prevent Context leak
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    // Sync data to cloud
    private void syncToCloud() {
        Toast.makeText(requireContext(), R.string.sync_in_progress, Toast.LENGTH_SHORT).show();

        // Start sync service
        Intent syncIntent = new Intent(requireContext(), DataSyncService.class);
        syncIntent.setAction(DataSyncService.ACTION_SYNC_TO_CLOUD);
        requireContext().startService(syncIntent);

        // Show success message after a delay (simulated)
        // MEMORY LEAK FIX: Use managed Handler instead of anonymous one
        if (syncHandler != null) {
            syncHandler.postDelayed(() -> {
                if (getContext() != null && isAdded()) {
                    Toast.makeText(requireContext(), R.string.sync_complete, Toast.LENGTH_SHORT).show();
                }
            }, 2000);
        }
    }

    private void updateUI() {
        // Note: Dynamic translation feature currently disabled
        // The required label TextViews (favoritesLabel, darkModeLabel, etc.)
        // don't exist in fragment_profile.xml layout
        // Use Android string resources for proper internationalization instead
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume Glide requests when fragment becomes visible
        if (getContext() != null) {
            try {
                Glide.with(this).resumeRequests();
            } catch (Exception e) {
                android.util.Log.w("ProfileFragment", "Failed to resume Glide requests", e);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause Glide requests when fragment is not visible
        if (getContext() != null) {
            try {
                Glide.with(this).pauseRequests();
            } catch (Exception e) {
                android.util.Log.w("ProfileFragment", "Failed to pause Glide requests", e);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // MEMORY LEAK FIX: Clean up Handler to prevent leak
        if (syncHandler != null) {
            syncHandler.removeCallbacksAndMessages(null);
            syncHandler = null;
        }

        // Clear all view references to prevent memory leaks
        editName = null;
        editEmail = null;
        editFavorites = null;
        btnEdit = null;
        btnLogout = null;
        btnDelete = null;
        btnClearCache = null;
        btnLanguage = null;
        btnSyncCloud = null;
        textName = null;
        textEmail = null;
        imageProfile = null;
        switchDarkMode = null;
        appData = null;
    }
}

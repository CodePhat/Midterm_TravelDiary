package com.example.mytraveldiary.utils.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.utils.LanguageManager;

/**
 * Dialog for language selection
 */
public class LanguageDialog {

    public interface LanguageChangeListener {
        void onLanguageChanged(String languageCode);
    }

    /**
     * Show language selection dialog
     */
    public static void show(Context context, LanguageChangeListener listener) {
        LanguageManager languageManager = new LanguageManager(context);
        String currentLanguage = languageManager.getLanguage();

        String[] languages = LanguageManager.getSupportedLanguageNames();
        String[] languageCodes = LanguageManager.getSupportedLanguages();

        // Find current selection index
        int currentIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                currentIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.select_language));
        builder.setSingleChoiceItems(languages, currentIndex, (dialog, which) -> {
            String selectedLanguageCode = languageCodes[which];

            // Save language preference
            languageManager.setLanguage(selectedLanguageCode);

            // Show success message
            Toast.makeText(context,
                    context.getString(R.string.language_changed) + "\n" +
                            context.getString(R.string.restart_required),
                    Toast.LENGTH_LONG).show();

            // Notify listener
            if (listener != null) {
                listener.onLanguageChanged(selectedLanguageCode);
            }

            dialog.dismiss();
        });

        builder.setNegativeButton(context.getString(R.string.btn_cancel), null);
        builder.show();
    }
}

package com.example.kanjilearning;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kanjilearning.data.MySqlSchemaInspectionResult;
import com.example.kanjilearning.data.MySqlUserRepository;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.CompletionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Nullable
    private MySqlUserRepository userRepository;

    private MaterialButton testingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init UI
        testingButton = findViewById(R.id.testingButton);

        // Guard: layout might not have the button in some variants
        if (testingButton != null) {
            testingButton.setOnClickListener(v -> onTestingClick());
        }

        // Demo-only: initialize a repository using BuildConfig (NOT for production).
        userRepository = new MySqlUserRepository(
                BuildConfig.MYSQL_HOST,
                BuildConfig.MYSQL_PORT,
                BuildConfig.MYSQL_DB_NAME,
                BuildConfig.MYSQL_USER,
                BuildConfig.MYSQL_PASSWORD
        );
    }

    private void onTestingClick() {
        if (userRepository == null || testingButton == null) return;

        // Read demo values from strings.xml
        final String email       = getString(R.string.testing_button_user_email);
        final String displayName = getString(R.string.testing_button_user_display_name);
        final String idToken     = getString(R.string.testing_button_user_token);

        // Disable button to avoid double-submit
        setBusy(true);

        userRepository.inspectSchemaAsync()
                .thenCompose((MySqlSchemaInspectionResult result) -> {
                    if (result == null) {
                        throw new IllegalStateException("Schema inspection returned null");
                    }
                    if (!result.isHealthy()) {
                        throw new IllegalStateException(result.describeProblems());
                    }
                    return userRepository.saveUserCredentialsAsync(email, displayName, idToken);
                })
                .thenRun(() -> runOnUiThread(() -> {
                    setBusy(false);
                    Toast.makeText(
                            this,
                            R.string.testing_button_save_success,
                            Toast.LENGTH_SHORT
                    ).show();
                }))
                .exceptionally(throwable -> {
                    final Throwable cause = unwrap(throwable);
                    Log.e(TAG, "Demo persistence failed", cause);
                    runOnUiThread(() -> {
                        setBusy(false);
                        showError(cause);
                    });
                    return null;
                });
    }

    private String safeMsg(Throwable t) {
        return (t == null || t.getMessage() == null) ? "Unknown error" : t.getMessage();
    }

    private void showError(Throwable throwable) {
        Toast.makeText(
                this,
                getString(R.string.testing_button_save_error, safeMsg(throwable)),
                Toast.LENGTH_LONG
        ).show();
    }

    private Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException && throwable.getCause() != null) {
            return throwable.getCause();
        }
        return throwable;
    }

    private void setBusy(boolean busy) {
        if (testingButton != null) {
            testingButton.setEnabled(!busy);
            testingButton.setAlpha(busy ? 0.6f : 1f);
            testingButton.setText(busy
                    ? getString(R.string.testing_button_saving)
                    : getString(R.string.testing_button_label));
            testingButton.setClickable(!busy);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (userRepository != null) {
                userRepository.shutdown();
                userRepository = null;
            }
        } finally {
            super.onDestroy();
        }
    }
}

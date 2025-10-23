package com.example.kanjilearning;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kanjilearning.data.MySqlUserRepository;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Nullable
    private MySqlUserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // VI: Khởi tạo repository với thông tin cấu hình từ BuildConfig để lưu login vào MySQL.
        userRepository = new MySqlUserRepository(
                BuildConfig.MYSQL_HOST,
                BuildConfig.MYSQL_PORT,
                BuildConfig.MYSQL_DB_NAME,
                BuildConfig.MYSQL_USER,
                BuildConfig.MYSQL_PASSWORD
        );

        MaterialButton testingButton = findViewById(R.id.testingButton);
        // VI: Gắn sự kiện cho nút "Lưu thông tin thử nghiệm" để demo luồng ghi vào MySQL.
        testingButton.setOnClickListener(view -> persistUserCredentials(
                getString(R.string.testing_button_user_email),
                getString(R.string.testing_button_user_display_name),
                getString(R.string.testing_button_user_token)
        ));
    }

    private void persistUserCredentials(String email, String displayName, String idToken) {
        if (userRepository == null) {
            return;
        }

        // VI: Lưu bất đồng bộ và hiển thị thông báo kết quả ngay trên UI khi xong.
        userRepository.saveUserCredentialsAsync(email, displayName, idToken, throwable ->
                runOnUiThread(() -> Toast.makeText(
                        this,
                        getString(R.string.testing_button_save_error, throwable.getMessage()),
                        Toast.LENGTH_LONG
                ).show())
        ).thenRun(() -> runOnUiThread(() -> Toast.makeText(
                this,
                R.string.testing_button_save_success,
                Toast.LENGTH_SHORT
        ).show()));
    }

    @Override
    protected void onDestroy() {
        if (userRepository != null) {
            userRepository.shutdown();
            userRepository = null;
        }
        super.onDestroy();
    }
}

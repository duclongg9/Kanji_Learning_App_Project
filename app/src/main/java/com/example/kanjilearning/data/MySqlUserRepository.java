package com.example.kanjilearning.data;

import android.util.Log;

import androidx.annotation.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * VI: Lớp repository chịu trách nhiệm lưu thông tin đăng nhập của người dùng lên MySQL từ app Android.
 * EN: Repository that persists user login information into a remote MySQL database.
 * <p>
 * VI: Để tránh vi phạm luật cấm network trên main thread, chúng ta gói gọn JDBC trong {@link ExecutorService} chạy nền.
 * EN: The repository wraps the blocking JDBC driver with an {@link ExecutorService}
 * so callers can persist data without violating Android's main thread
 * networking restrictions.
 */
public class MySqlUserRepository {

    private static final String TAG = "MySqlUserRepository";
    // VI: Tên driver JDBC của MySQL 8.x được include qua dependency `mysql-connector-j`.
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final List<String> REQUIRED_TABLES = Collections.unmodifiableList(Arrays.asList(
            "roles",
            "users",
            "cap_do_jlpt",
            "muc_do",
            "muc_do_cap_do",
            "kanji",
            "kanji_muc_do"
    ));
    private static final List<String> REQUIRED_VIEWS = Collections.singletonList("v_kanji_catalog");
    private static final List<String> REQUIRED_PROCEDURES = Collections.singletonList("sp_upsert_muc_do_cap_do");

    private final String jdbcUrl;
    private final String databaseName;
    private final String username;
    private final String password;
    private final ExecutorService ioExecutor;

    /**
     * VI: Khởi tạo repository với các tham số kết nối cơ bản tới server MySQL.
     * EN: Creates a repository using standard connection parameters.
     *
     * @param host     VI: hostname/IP của MySQL. EN: database host name or IP address.
     * @param port     VI: cổng MySQL. EN: database port number.
     * @param database VI: schema chứa bảng `users`. EN: schema/database name containing the user table.
     * @param username VI: user có quyền INSERT/UPDATE. EN: database user with INSERT permissions.
     * @param password VI: mật khẩu cho user trên. EN: password for the database user.
     */
    public MySqlUserRepository(
            @NonNull String host,
            int port,
            @NonNull String database,
            @NonNull String username,
            @NonNull String password
    ) {
        this.jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
        this.databaseName = database;
        this.username = username;
        this.password = password;
        this.ioExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * VI: Lưu hoặc cập nhật thông tin đăng nhập ở background thread để UI luôn mượt.
     * EN: Inserts or updates the user's login information on a background thread.
     *
     * @param email         VI: email người dùng. EN: user's email address.
     * @param displayName   VI: tên hiển thị từ nhà cung cấp đăng nhập. EN: display name provided by the identity provider.
     * @param idToken       VI: token đăng nhập (Google Sign-In hoặc nhà cung cấp khác). EN: token received from Google Sign-In (or other provider).
     * @param errorListener VI: callback nhận lỗi để hiển thị thông báo. EN: callback invoked with the {@link Throwable} if persistence fails. Optional.
     * @return VI: Future hoàn tất khi lưu xong. EN: a {@link CompletableFuture} that completes when the persistence call finishes.
     */
    public CompletableFuture<Void> saveUserCredentialsAsync(
            @NonNull String email,
            @NonNull String displayName,
            @NonNull String idToken,
            Consumer<Throwable> errorListener
    ) {
        Objects.requireNonNull(email, "email == null");
        Objects.requireNonNull(displayName, "displayName == null");
        Objects.requireNonNull(idToken, "idToken == null");

        return CompletableFuture.runAsync(() -> {
            try {
                saveUserCredentials(email, displayName, idToken);
            } catch (SQLException sqlException) {
                Log.e(TAG, "Failed to store user credentials", sqlException);
                if (errorListener != null) {
                    errorListener.accept(sqlException);
                }
                throw new RuntimeException(sqlException);
            }
        }, ioExecutor);
    }

    /**
     * Convenience overload that omits the error listener.
     */
    public CompletableFuture<Void> saveUserCredentialsAsync(
            @NonNull String email,
            @NonNull String displayName,
            @NonNull String idToken
    ) {
        return saveUserCredentialsAsync(email, displayName, idToken, null);
    }

    /**
     * VI: Hàm đồng bộ dành cho unit test hoặc tác vụ đặc biệt (không gọi trên main thread).
     * EN: Inserts or updates the user's credentials synchronously.
     *
     * <p>VI/EN: Thực hiện network I/O nên tuyệt đối không gọi trên main thread.</p>
     */
    public void saveUserCredentials(
            @NonNull String email,
            @NonNull String displayName,
            @NonNull String idToken
    ) throws SQLException {
        Objects.requireNonNull(email, "email == null");
        Objects.requireNonNull(displayName, "displayName == null");
        Objects.requireNonNull(idToken, "idToken == null");

        ensureDriverLoaded();

        final String insertSql = "INSERT INTO users (email, display_name, id_token) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), id_token = VALUES(id_token)";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setString(1, email);
            statement.setString(2, displayName);
            statement.setString(3, idToken);
            statement.executeUpdate();
        }
    }

    /**
     * Asynchronously inspects the database schema to verify that all objects defined in
     * {@code kanji_app_v1.sql} exist.
     */
    public CompletableFuture<MySqlSchemaInspectionResult> inspectSchemaAsync() {
        return inspectSchemaAsync(null);
    }

    /**
     * Asynchronously inspects the database schema to verify that all objects defined in
     * {@code kanji_app_v1.sql} exist.
     */
    public CompletableFuture<MySqlSchemaInspectionResult> inspectSchemaAsync(
            Consumer<Throwable> errorListener
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return inspectSchema();
            } catch (SQLException sqlException) {
                Log.e(TAG, "Failed to inspect MySQL schema", sqlException);
                if (errorListener != null) {
                    errorListener.accept(sqlException);
                }
                throw new RuntimeException(sqlException);
            }
        }, ioExecutor);
    }

    /**
     * Synchronously inspects the database schema.
     */
    @NonNull
    public MySqlSchemaInspectionResult inspectSchema() throws SQLException {
        ensureDriverLoaded();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            final Set<String> tables = loadTableLikeObjects(connection, "BASE TABLE");
            final Set<String> views = loadTableLikeObjects(connection, "VIEW");
            final Set<String> procedures = loadProcedures(connection);

            final List<String> missingTables = findMissing(tables, REQUIRED_TABLES);
            final List<String> missingViews = findMissing(views, REQUIRED_VIEWS);
            final List<String> missingProcedures = findMissing(procedures, REQUIRED_PROCEDURES);

            return new MySqlSchemaInspectionResult(missingTables, missingViews, missingProcedures);
        }
    }

    private void ensureDriverLoaded() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException classNotFoundException) {
            throw new SQLException("MySQL JDBC driver not found", classNotFoundException);
        }
    }

    private Set<String> loadTableLikeObjects(Connection connection, String type) throws SQLException {
        final String sql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);
            statement.setString(2, type);
            try (ResultSet resultSet = statement.executeQuery()) {
                final Set<String> names = new HashSet<>();
                while (resultSet.next()) {
                    names.add(resultSet.getString(1).toLowerCase(Locale.US));
                }
                return names;
            }
        }
    }

    private Set<String> loadProcedures(Connection connection) throws SQLException {
        final String sql = "SELECT ROUTINE_NAME FROM information_schema.ROUTINES "
                + "WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE = 'PROCEDURE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                final Set<String> names = new HashSet<>();
                while (resultSet.next()) {
                    names.add(resultSet.getString(1).toLowerCase(Locale.US));
                }
                return names;
            }
        }
    }

    private List<String> findMissing(Set<String> existing, List<String> expected) {
        final List<String> missing = new ArrayList<>();
        for (String name : expected) {
            if (!existing.contains(name.toLowerCase(Locale.US))) {
                missing.add(name);
            }
        }
        return missing;
    }

    /**
     * VI: Giải phóng thread background khi activity bị huỷ.
     * EN: Releases resources allocated by the repository.
     */
    public void shutdown() {
        ioExecutor.shutdownNow();
    }
}

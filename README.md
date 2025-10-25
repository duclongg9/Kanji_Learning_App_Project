# Kanji Learning App

This project contains an Android demo application that talks directly to a MySQL instance. The
"Save demo credentials" button in `com.example.kanjilearning.MainActivity` (for internal testing only) writes a sample user
record to the `kanji_app` schema defined in [`kanji_app_v1.sql`](kanji_app_v1.sql).

## Prerequisites

1. **Java & Android tooling**
   * JDK 17+
   * Android Studio (Hedgehog or newer) or the Android command line tools
   * An Android device/emulator with API level 24 or higher
2. **Gradle wrapper** – use the provided `./gradlew` so you get the correct plugin versions.
3. **MySQL 8.x** installed locally (or available on your network).

## Database setup (root / 123456 / kanji_app)

1. Start MySQL and create a user with the required credentials:

   ```sql
   CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '123456';
   GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
   FLUSH PRIVILEGES;
   ```

   > The Android emulator reaches the host machine via `10.0.2.2`, so make sure MySQL listens on
   > all interfaces (e.g., set `bind-address = 0.0.0.0` in `mysqld.cnf`).

2. Import the schema and seed data bundled with this repository:

   ```bash
   mysql -h 127.0.0.1 -P 3306 -u root -p123456 < kanji_app_v1.sql
   ```

   This creates the `kanji_app` schema with all tables, views, stored procedures, and sample data
   that the Android demo expects.

## Android project configuration

1. If you are building outside Android Studio, create a `local.properties` file at the repository
   root pointing to your SDK installation:

   ```properties
   sdk.dir=/absolute/path/to/Android/Sdk
   ```

2. The app exposes the MySQL connection parameters as `BuildConfig` constants. Default values are:
   * `MYSQL_HOST = 10.0.2.2`
   * `MYSQL_PORT = 3306`
   * `MYSQL_DB_NAME = kanji_app`
   * `MYSQL_USER = root`
   * `MYSQL_PASSWORD = 123456`

   Adjust them per build variant if you need to target a different server.

## Running the app

1. Launch MySQL and confirm that you can connect using the credentials above.
2. Start an emulator (or connect a device) running API 24+.
3. From the project root run:

   ```bash
   ./gradlew assembleDebug
   ```

   > On CI or headless environments you must install the Android SDK packages that match the
   > `compileSdk`/`targetSdk` versions (API 35).

4. Install the resulting APK (e.g., via Android Studio or `adb install app/build/outputs/apk/debug/app-debug.apk`).
5. Open the app and tap **Save demo credentials**. The app will:
   * Inspect the `kanji_app` schema to ensure all tables, the `v_kanji_catalog` view, and the
     `sp_upsert_muc_do_cap_do` stored procedure exist.
   * Upsert a demo row into the `users` table.

If any schema object is missing, the button displays the missing object list, allowing you to verify
that the database matches `kanji_app_v1.sql`.

## Troubleshooting

* **`SDK location not found`** – create `local.properties` pointing at your Android SDK or set the
  `ANDROID_HOME` environment variable before invoking Gradle.
* **Cannot connect from emulator** – verify the MySQL server accepts connections from `10.0.2.2` and
  that port 3306 is open.
* **Schema inspection failures** – rerun the SQL script or compare your database to
  [`kanji_app_v1.sql`](kanji_app_v1.sql) to recreate the missing tables/views/procedures.

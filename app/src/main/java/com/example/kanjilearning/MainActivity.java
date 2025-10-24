package com.example.kanjilearning;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * {{ProjectName}} bootstrap activity.
 * Platform: Android (phone & tablet), Java, Gradle Kotlin DSL.
 * Min SDK: 24, Target SDK: 35.
 * Architecture (future): Clean Architecture + MVVM + Repository, Room, Hilt, Navigation, DataStore, WorkManager.
 * Constraint: App code in Java; can rely on AndroidX Kotlin runtime libs if needed.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("{{ProjectName}} – bootstrap OK");
        tv.setTextSize(20f);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);

        setContentView(tv);
    }
}
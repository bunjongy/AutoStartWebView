package com.yosapa.auto_start_webview;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler{
    private final String TAG = "MyExceptionHandler";

    private Activity activity;
    public MyExceptionHandler(Activity a) {
        activity = a;
    }
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        //EXIT_CLICKED
        writeStringAsFile(new java.util.Date().toString() + '\n'+t.toString() +'\n'+ e.toString() + '\n');
        if (Objects.equals(e.getMessage(), "EXIT_CLICKED")){
            System.exit(0);
        } else {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra("crash", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity.getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager mgr = (AlarmManager) activity.getBaseContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, pendingIntent);
            activity.finishAffinity();
            System.exit(2);
        }
    }


    public void writeStringAsFile(final String fileContents) {
        try {
            String fileName = DateFormat.format("yyyy_MM_dd", new java.util.Date()).toString() + ".log";
            FileWriter out = new FileWriter(new File(activity.getObbDir(), fileName), true);
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }
}

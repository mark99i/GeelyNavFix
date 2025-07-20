package ru.mark99.carapp.geelynavigatorcrashfix;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NavigatorCrashFix {
    private static final String TAG = "NavigatorCrashFix";

    public static volatile String state = "non_applied";
    // non_applied
    // waiting
    // applied
    // error

    private static String exec(Context context) {
        try {
            Process process = Runtime.getRuntime().exec("su -c " + context.getFilesDir() + "/magicfile apply");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            reader.close();
            Log.d(TAG, "exec code: " + process.waitFor());
            return line != null ? line: "";
        } catch (IOException | InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return "";
        }
    }

    private static void setState(Context context, String newState) {
        state = newState;
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent("local.navigation.fix.state.changed")
        );
    }

    private static boolean placeMagicFile(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.magicfile);

            File outputFile = new File(context.getFilesDir(), "magicfile");
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[16384];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return true;
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return false;
    }

    private static int setPermissions(Context context) {
        try {
            var p = Runtime.getRuntime().exec("chmod 777 " + context.getFilesDir() + "/magicfile");
            return p.waitFor();
        } catch (IOException | InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return -1;
    }

    private static boolean runMagicfile(final Context context) {
        File magicFile = new File(context.getFilesDir(), "magicfile");

        if (!magicFile.exists() || !magicFile.canExecute()) {
            Log.e(TAG, "!magicFile.exists() || !magicFile.canExecute()");
            return false;
        }

        var ht = new HandlerThread("magicfile-thread");
        ht.start();
        var h = new Handler(ht.getLooper());

        h.post(() -> {
            Log.d(TAG, "[magicfile-thread] starting magicfile");
            String res = exec(context);
            Log.d(TAG, "[magicfile-thread] magicfile ended");

            if (res.isEmpty() || "apply=0".equals(res)) {
                Log.e(TAG, "[magicfile-thread] hack error or error exec magicfile");
                setState(context, "error");
            }

            if ("apply=1".equals(res)) {
                Log.d(TAG, "[magicfile-thread] hack success xD");
                setState(context, "applied");
            }

            ht.quitSafely();
        });

        return true;
    }

    public static void apply(Context context) {
        if (state.equals("waiting")) {
            Log.e(TAG, "cannot apply again in state waiting");
            return;
        }

        Log.d(TAG, "run apply()");

        setState(context, "waiting");
        var placeResult = placeMagicFile(context);
        Log.d(TAG, "place magicfile result: " + placeResult);
        if (!placeResult) {
            setState(context, "error");
            return;
        }

        var exitCode = setPermissions(context);
        Log.d(TAG, "set permissions result: " + exitCode);
        if (exitCode != 0) {
            setState(context, "error");
            return;
        }

        var started = runMagicfile(context);
        Log.d(TAG, "run magicfile result: " + started);
        if (!started) {
            setState(context, "error");
        }
    }

    public static boolean onBootApplyEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                "system_enable_navigator_fix_on_boot",
                true
        );
    }
}

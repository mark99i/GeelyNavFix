package ru.mark99.carapp.geelynavigatorcrashfix;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class FixOnBootService extends Service {
    public FixOnBootService() {}

    HandlerThread thread;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        FixOnBootService context = this;
        startForeground(1999, getForegroundNotification(this));

        thread = new HandlerThread("bg-thread");
        thread.start();
        handler = new Handler(thread.getLooper());

        handler.post(() -> {
            if (NavigatorCrashFix.onBootApplyEnabled(context)) {
                NavigatorCrashFix.apply(context);
            }
        });
    }

    public static Notification getForegroundNotification(Context ctx) {
        NotificationChannel channel = new NotificationChannel(
                "foreground_service",
                "Foreground",
                NotificationManager.IMPORTANCE_MIN);

        ctx.getSystemService(NotificationManager.class).createNotificationChannel(channel);

        return new Notification.Builder(ctx, "foreground_service")
                .setContentTitle("This notification allow to work app")
                .setAutoCancel(false)
                .build();
    }

    @Override
    public void onDestroy() {
        handler.post(() -> thread.quitSafely());
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
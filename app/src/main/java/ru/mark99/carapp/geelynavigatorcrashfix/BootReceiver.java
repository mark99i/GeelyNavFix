package ru.mark99.carapp.geelynavigatorcrashfix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startForegroundService(new Intent(context, FixOnBootService.class));
    }
}
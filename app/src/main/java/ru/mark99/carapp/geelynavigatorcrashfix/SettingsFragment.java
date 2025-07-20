package ru.mark99.carapp.geelynavigatorcrashfix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        //noinspection DataFlowIssue
        findPreference("system_enable_navigator_fix_on_boot").setOnPreferenceChangeListener((preference, newValue) -> {
            var vl = (boolean) newValue;

            if (vl && ("non_applied".equals(NavigatorCrashFix.state) || "error".equals(NavigatorCrashFix.state))) {
                NavigatorCrashFix.apply(requireActivity());
            }

            return true;
        });

        //noinspection DataFlowIssue
        findPreference("_rt_about_version").setSummary(getApplicationVersion(requireActivity()));
    }

    private LocalBroadcastManager getLBM() {
        return LocalBroadcastManager.getInstance(requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNavigatorFixSummaryReceiver.onReceive(requireActivity(), null);

        getLBM().registerReceiver(
                refreshNavigatorFixSummaryReceiver,
                new IntentFilter("local.navigation.fix.state.changed")
        );
    }

    @Override
    public void onPause() {
        getLBM().unregisterReceiver(refreshNavigatorFixSummaryReceiver);
        super.onPause();
    }

    BroadcastReceiver refreshNavigatorFixSummaryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            var preference = findPreference("system_enable_navigator_fix_on_boot");
            if (preference == null) return;

            var current_status = switch (NavigatorCrashFix.state) {
                case "non_applied" -> "Исправление не применено";
                case "waiting" -> "Исправление сейчас применяется, это займет до минуты";
                case "applied" -> "Исправление применено, навигаторы вылетать не должны =)";
                case "error" -> "Ошибка применения исправления";
                default -> "Неизвестный статус исправления";
            };

            preference.setSummary(current_status);
        }
    };

    public static String getApplicationVersion(Context context) {
        try {
            var pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            var versionName = pkgInfo.versionName;
            var debugFlag = false;

            try {
                var appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
                debugFlag = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            } catch (Exception ignored) {}

            return versionName + (debugFlag ? " (debug)" : "");
        } catch (Exception ignored) {return "Неизвестно";}
    }
}
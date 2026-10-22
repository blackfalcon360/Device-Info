package com.blackfalcon.deviceinfo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Starts / stops the background status-bar service according to the settings. */
public class Svc {
    public static void apply(Context c) {
        Store st = new Store(c);
        Intent i = new Intent(c, InfoService.class);
        try {
            if (st.enabled()) {
                if (Build.VERSION.SDK_INT >= 26) startFg(c, i);
                else c.startService(i);
            } else {
                c.stopService(i);
            }
        } catch (Exception e) { }
    }

    @TargetApi(26)
    static void startFg(Context c, Intent i) {
        c.startForegroundService(i);
    }
}

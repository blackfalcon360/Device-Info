package com.blackfalcon.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** After boot (or app update) restarts the status-bar service if the user turned it on. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Store st = new Store(context);
        if (st.enabled() && st.boot()) Svc.apply(context);
    }
}

package com.blackfalcon.deviceinfo;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/** Foreground service: keeps the selected phone info in a status-bar notification. */
public class InfoService extends Service {
    static final String CH = "info";
    static final int ID = 1;

    Handler h;
    Runnable tick;
    boolean screenOn = true;
    final SpeedMeter meter = new SpeedMeter();
    BroadcastReceiver screenRx;

    @Override
    public void onCreate() {
        super.onCreate();
        h = new Handler(Looper.getMainLooper());
        tick = new Runnable() {
            @Override public void run() {
                update();
                if (screenOn) h.postDelayed(this, new Store(InfoService.this).interval() * 1000L);
            }
        };
        screenRx = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                String a = i.getAction();
                h.removeCallbacks(tick);
                if (Intent.ACTION_SCREEN_ON.equals(a)) {
                    screenOn = true;
                    h.post(tick);
                } else if (Intent.ACTION_SCREEN_OFF.equals(a)) {
                    screenOn = false;
                }
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenRx, f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            goForeground(build());
        } catch (Exception e) {
            stopSelf();
            return START_NOT_STICKY;
        }
        h.removeCallbacks(tick);
        screenOn = true;
        h.postDelayed(tick, new Store(this).interval() * 1000L);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        h.removeCallbacks(tick);
        try { unregisterReceiver(screenRx); } catch (Exception e) { }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    void goForeground(Notification n) {
        if (Build.VERSION.SDK_INT >= 34) fg34(n);
        else startForeground(ID, n);
    }

    @TargetApi(34)
    void fg34(Notification n) {
        startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    }

    void update() {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.notify(ID, build());
        } catch (Exception e) { }
    }

    @TargetApi(26)
    void channelO(NotificationManager nm) {
        NotificationChannel ch = new NotificationChannel(CH, getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW);
        ch.setShowBadge(false);
        nm.createNotificationChannel(ch);
    }

    @TargetApi(26)
    Notification.Builder builderO() {
        return new Notification.Builder(this, CH);
    }

    @SuppressWarnings("deprecation")
    Notification.Builder builderLegacy() {
        return new Notification.Builder(this).setPriority(Notification.PRIORITY_LOW);
    }

    Notification build() {
        Store st = new Store(this);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26 && nm != null) channelO(nm);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? builderO() : builderLegacy();

        StringBuilder big = new StringBuilder();
        StringBuilder small = new StringBuilder();
        String iconText = null;
        int iconId = st.iconItem();
        for (int id = 0; id < Info.N; id++) {
            if (!st.has(id) && id != iconId) continue;
            if (!Info.available(this, id)) continue;
            Info.Item it = Info.get(this, st, meter, id);
            if (id == iconId) iconText = it.shortText;
            if (!st.has(id)) continue;
            if (big.length() > 0) big.append("\n");
            big.append(it.line);
            if (id <= 5 && it.shortText != null) {
                if (small.length() > 0) small.append(" \u2022 ");
                small.append(it.shortText);
            }
        }
        if (big.length() == 0) big.append(st.tr("سیٹنگز میں معلومات منتخب کریں", "Pick items in the app",
                "اختر العناصر من التطبيق"));
        if (iconText == null) {
            b.setSmallIcon(R.drawable.ic_notif);
        } else {
            b.setSmallIcon(Icon.createWithBitmap(IconMaker.make(iconText)));
        }
        b.setContentTitle(small.length() > 0 ? small.toString() : getString(R.string.app_name));
        b.setContentText(big.toString().split("\n")[0]);
        b.setStyle(new Notification.BigTextStyle().bigText(big.toString()));
        b.setOngoing(true);
        b.setOnlyAlertOnce(true);
        b.setShowWhen(false);
        Intent oi = new Intent(this, MainActivity.class);
        oi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        b.setContentIntent(PendingIntent.getActivity(this, 0, oi,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        return b.build();
    }
}

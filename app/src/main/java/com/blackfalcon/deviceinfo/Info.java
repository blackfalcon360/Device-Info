package com.blackfalcon.deviceinfo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.util.Locale;

/** Collects the phone information. Item ids: see TITLES. */
public class Info {
    public static class Item {
        public int id;
        public String title = "";
        public String line = "";
        public String shortText;   // null = cannot be used as the status-bar icon
    }

    public static final int N = 10;
    // 0 storage, 1 SD card, 2 RAM, 3 battery, 4 network, 5 uptime, 6 device, 7 CPU, 8 screen, 9 sensors

    public static String title(Store st, int id) {
        switch (id) {
            case 0: return st.tr("خالی اسٹوریج", "Free storage", "التخزين الحر");
            case 1: return st.tr("SD کارڈ", "SD card", "بطاقة SD");
            case 2: return st.tr("RAM", "RAM", "الذاكرة RAM");
            case 3: return st.tr("بیٹری", "Battery", "البطارية");
            case 4: return st.tr("نیٹ ورک اور رفتار", "Network and speed", "الشبكة والسرعة");
            case 5: return st.tr("آن ٹائم (Uptime)", "Uptime", "مدة التشغيل");
            case 6: return st.tr("فون اور اینڈرائیڈ", "Phone and Android", "الهاتف وأندرويد");
            case 7: return st.tr("CPU", "CPU", "المعالج");
            case 8: return st.tr("اسکرین", "Screen", "الشاشة");
            default: return st.tr("سینسرز", "Sensors", "الحساسات");
        }
    }

    public static boolean canIcon(int id) {
        return id == 0 || id == 1 || id == 2 || id == 3 || id == 4 || id == 5;
    }

    static File sdDir(Context c) {
        try {
            File[] d = c.getExternalFilesDirs(null);
            if (d != null && d.length > 1 && d[1] != null
                    && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(d[1]))) {
                return d[1];
            }
        } catch (Exception e) { }
        return null;
    }

    public static boolean available(Context c, int id) {
        if (id == 1) return sdDir(c) != null;
        return true;
    }

    static String storageLine(Store st, StatFs sf, String label) {
        long total = sf.getTotalBytes(), free = sf.getAvailableBytes();
        int usedPct = total > 0 ? (int) Math.round(100.0 * (total - free) / total) : 0;
        return label + st.tr("خالی ", " free ", " حر ") + Fmt.size(free, 1000)
            + st.tr(" / کل ", " of ", " من ") + Fmt.size(total, 1000)
            + " (" + usedPct + "% " + st.tr("استعمال", "used", "مستخدم") + ")";
    }

    public static Item get(Context c, Store st, SpeedMeter m, int id) {
        Item it = new Item();
        it.id = id;
        it.title = title(st, id);
        try {
            switch (id) {
                case 0: {
                    StatFs sf = new StatFs(Environment.getDataDirectory().getPath());
                    it.line = storageLine(st, sf, st.tr("اسٹوریج: ", "Storage: ", "التخزين: "));
                    it.shortText = Fmt.shortSize(sf.getAvailableBytes(), 1000);
                    break;
                }
                case 1: {
                    File d = sdDir(c);
                    if (d == null) { it.line = st.tr("SD کارڈ نہیں", "No SD card", "لا توجد بطاقة SD"); break; }
                    StatFs sf = new StatFs(d.getPath());
                    it.line = storageLine(st, sf, "SD: ");
                    it.shortText = Fmt.shortSize(sf.getAvailableBytes(), 1000);
                    break;
                }
                case 2: {
                    ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
                    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                    am.getMemoryInfo(mi);
                    it.line = "RAM: " + st.tr("خالی ", "free ", "حر ") + Fmt.size(mi.availMem, 1024)
                        + st.tr(" / کل ", " of ", " من ") + Fmt.size(mi.totalMem, 1024);
                    it.shortText = Fmt.shortSize(mi.availMem, 1024);
                    break;
                }
                case 3: batteryItem(c, st, it); break;
                case 4: {
                    m.sample();
                    String type = netType(c, st);
                    it.line = type + (m.supported
                        ? "   \u2193 " + Fmt.speed(m.down) + "   \u2191 " + Fmt.speed(m.up) : "");
                    it.shortText = m.supported ? Fmt.shortSpeed(m.down) : null;
                    break;
                }
                case 5: {
                    long up = SystemClock.elapsedRealtime();
                    it.line = st.tr("آن ٹائم: ", "Uptime: ", "مدة التشغيل: ") + Fmt.uptime(up);
                    it.shortText = Fmt.shortUptime(up);
                    break;
                }
                case 6:
                    it.line = Build.MANUFACTURER + " " + Build.MODEL + "  \u2022  Android " + Build.VERSION.RELEASE
                        + " (API " + Build.VERSION.SDK_INT + ")";
                    break;
                case 7: {
                    String abi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "?";
                    String kern = System.getProperty("os.version");
                    it.line = "CPU: " + Runtime.getRuntime().availableProcessors() + st.tr(" کور", " cores", " نوى")
                        + "  \u2022  " + abi + (kern != null ? "  \u2022  Kernel " + kern : "");
                    break;
                }
                case 8: {
                    DisplayMetrics dm = c.getResources().getDisplayMetrics();
                    String hz = "";
                    try {
                        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
                        float r = wm.getDefaultDisplay().getRefreshRate();
                        hz = "  \u2022  " + Math.round(r) + " Hz";
                    } catch (Exception e) { }
                    it.line = dm.widthPixels + "\u00D7" + dm.heightPixels + "  \u2022  " + dm.densityDpi + " dpi" + hz;
                    break;
                }
                default: {
                    SensorManager sm = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
                    int n = sm == null ? 0 : sm.getSensorList(Sensor.TYPE_ALL).size();
                    it.line = st.tr("سینسرز کی تعداد: ", "Sensors: ", "عدد الحساسات: ") + n;
                    break;
                }
            }
        } catch (Exception e) {
            it.line = st.tr("معلومات دستیاب نہیں", "Not available", "غير متاح");
            it.shortText = null;
        }
        return it;
    }

    static String netType(Context c, Store st) {
        String t = st.tr("نیٹ ورک نہیں", "No network", "لا شبكة");
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network n = cm.getActiveNetwork();
            if (n != null) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                if (nc != null) {
                    if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) t = "Wi-Fi";
                    else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                        t = st.tr("موبائل ڈیٹا", "Mobile data", "بيانات الجوال");
                    else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) t = "Ethernet";
                    else t = st.tr("دیگر", "Other", "أخرى");
                }
            }
        } catch (Exception e) { }
        return t;
    }

    static void batteryItem(Context c, Store st, Item it) {
        Intent b = c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (b == null) {
            it.line = st.tr("معلومات دستیاب نہیں", "Not available", "غير متاح");
            return;
        }
        int level = b.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = b.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int pct = (level >= 0 && scale > 0) ? Math.round(level * 100f / scale) : -1;
        int status = b.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int plugged = b.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        float temp = b.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f;
        int mv = b.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        int health = b.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);

        String stat;
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) stat = st.tr("چارج ہو رہی", "Charging", "تُشحن");
        else if (status == BatteryManager.BATTERY_STATUS_FULL) stat = st.tr("مکمل", "Full", "ممتلئة");
        else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) stat = st.tr("استعمال میں", "Discharging", "تفرغ");
        else stat = st.tr("چارج نہیں ہو رہی", "Not charging", "لا تُشحن");
        String src = "";
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC) src = " (AC)";
        else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) src = " (USB)";
        else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) src = " (" + st.tr("وائرلیس", "wireless", "لاسلكي") + ")";
        String h;
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD: h = st.tr("صحت اچھی", "Good", "جيدة"); break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT: h = st.tr("گرم", "Overheat", "ساخنة"); break;
            case BatteryManager.BATTERY_HEALTH_COLD: h = st.tr("ٹھنڈی", "Cold", "باردة"); break;
            case BatteryManager.BATTERY_HEALTH_DEAD: h = st.tr("خراب", "Dead", "تالفة"); break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: h = st.tr("زیادہ وولٹیج", "Over voltage", "جهد زائد"); break;
            default: h = "?"; break;
        }
        it.line = st.tr("بیٹری ", "Battery ", "البطارية ") + pct + "%  \u2022  " + stat + src
            + "  \u2022  " + String.format(Locale.ENGLISH, "%.1f\u00B0C", temp)
            + "  \u2022  " + String.format(Locale.ENGLISH, "%.2f V", mv / 1000f) + "  \u2022  " + h;
        it.shortText = pct >= 0 ? pct + "%" : null;
    }
}

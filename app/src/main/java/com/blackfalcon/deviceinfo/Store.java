package com.blackfalcon.deviceinfo;

import android.content.Context;
import android.content.SharedPreferences;

public class Store {
    private final SharedPreferences p;

    public Store(Context c) {
        p = c.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public String lang() { return p.getString("lang", "ur"); }
    public void setLang(String l) { p.edit().putString("lang", l).apply(); }
    public boolean dark() { return p.getBoolean("dark", true); }
    public void setDark(boolean v) { p.edit().putBoolean("dark", v).apply(); }
    public boolean rtl() { return !lang().equals("en"); }

    /** Status-bar notification master switch. */
    public boolean enabled() { return p.getBoolean("enabled", false); }
    public void setEnabled(boolean v) { p.edit().putBoolean("enabled", v).apply(); }
    public boolean boot() { return p.getBoolean("boot", true); }
    public void setBoot(boolean v) { p.edit().putBoolean("boot", v).apply(); }

    /** Bit i set = item i is shown. Default: storage, RAM, battery. */
    public int mask() { return p.getInt("mask", (1 << 0) | (1 << 2) | (1 << 3)); }
    public void setMask(int m) { p.edit().putInt("mask", m).apply(); }
    public boolean has(int id) { return (mask() & (1 << id)) != 0; }
    public void set(int id, boolean on) {
        int m = mask();
        m = on ? (m | (1 << id)) : (m & ~(1 << id));
        setMask(m);
    }

    /** Item whose short value is drawn as the status-bar icon (default: storage). */
    public int iconItem() { return p.getInt("icon", 0); }
    public void setIconItem(int id) { p.edit().putInt("icon", id).apply(); }

    public int interval() { return p.getInt("interval", 5); }
    public void setInterval(int s) { p.edit().putInt("interval", s).apply(); }

    public String tr(String ur, String en, String ar) {
        String l = lang();
        if (l.equals("en")) return en;
        if (l.equals("ar")) return ar;
        return ur;
    }
}

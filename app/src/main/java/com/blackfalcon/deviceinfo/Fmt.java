package com.blackfalcon.deviceinfo;

import java.util.Locale;

/** Pure number/text formatting (no Android classes). */
public class Fmt {
    static final String[] U = {"B", "KB", "MB", "GB", "TB"};

    public static String size(long bytes, int base) {
        double v = Math.max(0, bytes);
        int i = 0;
        while (v >= base && i < 4) { v /= base; i++; }
        String n = (i == 0 || v >= 100) ? String.format(Locale.ENGLISH, "%.0f", v)
                                        : String.format(Locale.ENGLISH, "%.1f", v);
        return n + " " + U[i];
    }

    /** Very short form for the status-bar icon: 128G, 9.5G, 512M. */
    public static String shortSize(long bytes, int base) {
        double g = bytes / Math.pow(base, 3);
        if (g >= 10) return String.format(Locale.ENGLISH, "%.0fG", g);
        if (g >= 1) return String.format(Locale.ENGLISH, "%.1fG", g);
        double m = bytes / Math.pow(base, 2);
        return String.format(Locale.ENGLISH, "%.0fM", m);
    }

    public static String speed(double bps) {
        return size((long) bps, 1024) + "/s";
    }

    public static String shortSpeed(double bps) {
        if (bps < 1024) return "0K";
        long k = Math.round(bps / 1024.0);
        if (k < 1000) return k + "K";
        double m = bps / 1048576.0;
        if (m >= 10) return Math.round(m) + "M";
        return String.format(Locale.ENGLISH, "%.1fM", m);
    }

    public static String uptime(long ms) {
        long m = ms / 60000L;
        long d = m / 1440, h = (m % 1440) / 60, mm = m % 60;
        if (d > 0) return d + "d " + h + "h " + mm + "m";
        if (h > 0) return h + "h " + mm + "m";
        return mm + "m";
    }

    public static String shortUptime(long ms) {
        long m = ms / 60000L;
        long d = m / 1440, h = (m % 1440) / 60;
        if (d > 0) return d + "d";
        if (h > 0) return h + "h";
        return (m % 60) + "m";
    }
}

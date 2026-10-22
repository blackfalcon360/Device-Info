package com.blackfalcon.deviceinfo;

import android.net.TrafficStats;
import android.os.SystemClock;

/** Network speed from byte-counter differences between two calls. */
public class SpeedMeter {
    long rx, tx, t;
    public double down = 0, up = 0;
    public boolean supported = true;

    public void sample() {
        long r = TrafficStats.getTotalRxBytes();
        long x = TrafficStats.getTotalTxBytes();
        long now = SystemClock.elapsedRealtime();
        if (r == TrafficStats.UNSUPPORTED || x == TrafficStats.UNSUPPORTED) {
            supported = false;
            return;
        }
        if (t != 0 && now > t) {
            double dt = (now - t) / 1000.0;
            down = Math.max(0, (r - rx) / dt);
            up = Math.max(0, (x - tx) / dt);
        }
        rx = r;
        tx = x;
        t = now;
    }
}

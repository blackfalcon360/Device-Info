package com.blackfalcon.deviceinfo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/** Draws a short text (e.g. "45G") as a white-on-transparent status-bar icon. */
public class IconMaker {
    public static Bitmap make(String text) {
        int sz = 96;
        Bitmap bm = Bitmap.createBitmap(sz, sz, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextAlign(Paint.Align.CENTER);
        float size = 84f;
        p.setTextSize(size);
        while (p.measureText(text) > sz - 4 && size > 18f) {
            size -= 2f;
            p.setTextSize(size);
        }
        Paint.FontMetrics fm = p.getFontMetrics();
        float y = sz / 2f - (fm.ascent + fm.descent) / 2f;
        cv.drawText(text, sz / 2f, y, p);
        return bm;
    }
}

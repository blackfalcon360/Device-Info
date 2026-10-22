package com.blackfalcon.deviceinfo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    static final int PURPLE = 0xFF5E35B1;
    static final int[] INTERVALS = {2, 5, 10, 30};

    Store st;
    Handler h;
    Runnable refresher;
    final SpeedMeter meter = new SpeedMeter();
    int cBg, cCard, cText, cSub;
    TextView[] tabViews;
    ScrollView[] scrolls;
    LinearLayout infoBox, barBox, setBox;
    final ArrayList<Integer> liveIds = new ArrayList<Integer>();
    final ArrayList<TextView> liveViews = new ArrayList<TextView>();
    RadioGroup rgLang, rgTheme;
    int[] langIds = new int[3];
    int[] themeIds = new int[2];

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        st = new Store(this);
        h = new Handler(Looper.getMainLooper());
        refresher = new Runnable() {
            @Override public void run() {
                refreshLive();
                h.postDelayed(this, 2000);
            }
        };
        buildUI();
        showTab(0);
        askNotifPerm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        h.post(refresher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(refresher);
    }

    // ---------- permission (API 33+) ----------
    void askNotifPerm() {
        if (Build.VERSION.SDK_INT >= 33) askNotifPerm33();
    }

    @TargetApi(33)
    void askNotifPerm33() {
        if (checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {"android.permission.POST_NOTIFICATIONS"}, 51);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        Svc.apply(this);
    }

    // ---------- helpers ----------
    String tr(String ur, String en, String ar) { return st.tr(ur, en, ar); }

    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    GradientDrawable round(int color, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(r));
        return g;
    }

    TextView tv(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    LinearLayout cardBox() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(round(cCard, 14));
        c.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(5), dp(10), dp(5));
        c.setLayoutParams(lp);
        return c;
    }

    Button btn(String s, int color, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(color, 10));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    CheckBox check(String s, boolean on) {
        CheckBox c = new CheckBox(this);
        c.setText(s);
        c.setTextColor(cText);
        c.setTextSize(15);
        c.setChecked(on);
        return c;
    }

    TextView label(String s) {
        TextView t = tv(s, 16, cText, true);
        t.setPadding(0, dp(12), 0, dp(4));
        return t;
    }

    // ---------- UI ----------
    void buildUI() {
        boolean dark = st.dark();
        cBg = dark ? 0xFF000000 : 0xFFF2F2F2;
        cCard = dark ? 0xFF1A1A1A : 0xFFFFFFFF;
        cText = dark ? 0xFFFFFFFF : 0xFF111111;
        cSub = dark ? 0xFFAAAAAA : 0xFF555555;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(cBg);
        root.setLayoutDirection(st.rtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        TextView title = tv("Device Info", 22, cText, true);
        title.setPadding(dp(14), dp(14), dp(14), dp(6));
        root.addView(title);

        String[] names = {tr("معلومات", "Info", "المعلومات"),
            tr("اسٹیٹس بار", "Status bar", "شريط الحالة"), tr("سیٹنگز", "Settings", "الإعدادات")};
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(6), dp(4), dp(6), dp(8));
        tabViews = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView t = tv(names[i], 14, cText, false);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(4), dp(9), dp(4), dp(9));
            t.setOnClickListener(v -> showTab(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            tabs.addView(t, lp);
            tabViews[i] = t;
        }
        root.addView(tabs);

        FrameLayout content = new FrameLayout(this);
        scrolls = new ScrollView[3];
        infoBox = new LinearLayout(this);
        barBox = new LinearLayout(this);
        setBox = new LinearLayout(this);
        LinearLayout[] boxes = {infoBox, barBox, setBox};
        for (int i = 0; i < 3; i++) {
            boxes[i].setOrientation(LinearLayout.VERTICAL);
            boxes[i].setPadding(0, dp(4), 0, dp(12));
            scrolls[i] = new ScrollView(this);
            scrolls[i].addView(boxes[i]);
            content.addView(scrolls[i], new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView credit = tv("By: Black Falcon \uD83E\uDD85", 13, cSub, false);
        credit.setGravity(Gravity.CENTER);
        credit.setPadding(0, dp(8), 0, dp(10));
        root.addView(credit);

        setContentView(root);
        buildInfo();
        buildBar();
        buildSettings();
        refreshLive();
    }

    void showTab(int i) {
        for (int k = 0; k < 3; k++) {
            boolean sel = k == i;
            tabViews[k].setTextColor(sel ? Color.WHITE : cText);
            tabViews[k].setBackground(round(sel ? PURPLE : cCard, 20));
            scrolls[k].setVisibility(sel ? View.VISIBLE : View.GONE);
        }
    }

    // ---------- live info tab ----------
    void buildInfo() {
        infoBox.removeAllViews();
        liveIds.clear();
        liveViews.clear();
        for (int id = 0; id < Info.N; id++) {
            if (!Info.available(this, id)) continue;
            LinearLayout c = cardBox();
            c.addView(tv(Info.title(st, id), 13, 0xFFB39DDB, true));
            TextView v = tv("\u2026", 14, cText, false);
            v.setPadding(0, dp(3), 0, 0);
            c.addView(v);
            infoBox.addView(c);
            liveIds.add(id);
            liveViews.add(v);
        }
    }

    void refreshLive() {
        for (int i = 0; i < liveIds.size(); i++) {
            Info.Item it = Info.get(this, st, meter, liveIds.get(i));
            liveViews.get(i).setText(it.line);
        }
    }

    // ---------- status bar tab ----------
    void buildBar() {
        barBox.removeAllViews();
        LinearLayout top = cardBox();
        CheckBox en = check(tr("اسٹیٹس بار کے نیچے معلومات دکھائیں", "Show info below the status bar",
                "عرض المعلومات أسفل شريط الحالة"), st.enabled());
        en.setOnCheckedChangeListener((cb, on) -> {
            st.setEnabled(on);
            Svc.apply(this);
        });
        top.addView(en);
        CheckBox bo = check(tr("فون چلتے ہی خود شروع ہو", "Start automatically when the phone boots",
                "ابدأ تلقائيًا عند تشغيل الهاتف"), st.boot());
        bo.setOnCheckedChangeListener((cb, on) -> st.setBoot(on));
        top.addView(bo);
        barBox.addView(top);

        LinearLayout items = cardBox();
        items.addView(label(tr("کون سی معلومات دکھانی ہیں", "Items to show", "العناصر المعروضة")));
        for (int id = 0; id < Info.N; id++) {
            if (!Info.available(this, id)) continue;
            final int fid = id;
            CheckBox c = check(Info.title(st, id), st.has(id));
            c.setOnCheckedChangeListener((cb, on) -> {
                st.set(fid, on);
                Svc.apply(this);
            });
            items.addView(c);
        }
        barBox.addView(items);

        LinearLayout icon = cardBox();
        icon.addView(label(tr("اسٹیٹس بار کے آئیکن میں کیا لکھا آئے", "Value drawn in the status-bar icon",
                "القيمة في أيقونة شريط الحالة")));
        RadioGroup rg = new RadioGroup(this);
        final ArrayList<Integer> ids = new ArrayList<Integer>();
        final ArrayList<Integer> rids = new ArrayList<Integer>();
        for (int id = 0; id < Info.N; id++) {
            if (!Info.canIcon(id) || !Info.available(this, id)) continue;
            RadioButton r = new RadioButton(this);
            r.setText(Info.title(st, id));
            r.setTextColor(cText);
            r.setTextSize(15);
            r.setId(View.generateViewId());
            rg.addView(r);
            ids.add(id);
            rids.add(r.getId());
            if (st.iconItem() == id) rg.check(r.getId());
        }
        rg.setOnCheckedChangeListener((g, checkedId) -> {
            for (int i = 0; i < rids.size(); i++) {
                if (rids.get(i) == checkedId) st.setIconItem(ids.get(i));
            }
            Svc.apply(this);
        });
        icon.addView(rg);
        barBox.addView(icon);

        LinearLayout iv = cardBox();
        iv.addView(label(tr("اپڈیٹ کا وقفہ", "Update every", "التحديث كل")));
        RadioGroup rg2 = new RadioGroup(this);
        final int[] rid2 = new int[INTERVALS.length];
        for (int i = 0; i < INTERVALS.length; i++) {
            RadioButton r = new RadioButton(this);
            r.setText(INTERVALS[i] + tr(" سیکنڈ", " seconds", " ثانية"));
            r.setTextColor(cText);
            r.setTextSize(15);
            r.setId(View.generateViewId());
            rid2[i] = r.getId();
            rg2.addView(r);
            if (st.interval() == INTERVALS[i]) rg2.check(r.getId());
        }
        rg2.setOnCheckedChangeListener((g, checkedId) -> {
            for (int i = 0; i < rid2.length; i++) {
                if (rid2[i] == checkedId) st.setInterval(INTERVALS[i]);
            }
            Svc.apply(this);
        });
        iv.addView(rg2);
        barBox.addView(iv);

        TextView note = tv(tr(
            "اسکرین بند ہو تو اپڈیٹ رک جاتی ہے تاکہ بیٹری بچے۔ اینڈرائیڈ 13+ پر نوٹیفکیشن کی اجازت دینی ہو گی۔ Xiaomi، Oppo، Vivo، Huawei وغیرہ پر ایپ کے لیے \"Autostart\" اور بیٹری کی پابندی ہٹانا ضروری ہے، ورنہ بوٹ پر یا پس منظر میں سروس بند ہو سکتی ہے۔ اینڈرائیڈ 13+ پر صارف نوٹیفکیشن ہٹا سکتا ہے مگر سروس چلتی رہتی ہے؛ ایپ کھولنے پر وہ واپس آ جاتا ہے۔",
            "Updates pause while the screen is off to save battery. Android 13+ needs the notification permission. On Xiaomi, Oppo, Vivo, Huawei etc. allow \"Autostart\" and remove battery restrictions for this app, otherwise the service may stop after boot or in the background. On Android 13+ you can swipe the notification away; the service keeps running and it returns when you open the app.",
            "تتوقف التحديثات عند إطفاء الشاشة لتوفير البطارية. يتطلب أندرويد 13+ إذن الإشعارات. في Xiaomi وOppo وVivo وHuawei وغيرها اسمح بـ\"التشغيل التلقائي\" وأزل قيود البطارية، وإلا قد تتوقف الخدمة بعد الإقلاع أو في الخلفية. في أندرويد 13+ يمكنك إزالة الإشعار مع بقاء الخدمة، ويعود عند فتح التطبيق."),
            11, cSub, false);
        note.setPadding(dp(14), dp(6), dp(14), dp(8));
        barBox.addView(note);
    }

    // ---------- settings ----------
    RadioGroup radios(String[] labels, int checked, int[] ids) {
        RadioGroup g = new RadioGroup(this);
        for (int i = 0; i < labels.length; i++) {
            RadioButton r = new RadioButton(this);
            r.setText(labels[i]);
            r.setTextColor(cText);
            r.setTextSize(15);
            r.setId(View.generateViewId());
            ids[i] = r.getId();
            g.addView(r);
        }
        g.check(ids[checked]);
        return g;
    }

    void buildSettings() {
        setBox.removeAllViews();
        LinearLayout c = cardBox();
        c.addView(label(tr("زبان", "Language", "اللغة")));
        String l = st.lang();
        rgLang = radios(new String[] {"اردو", "English", "العربية"},
                l.equals("en") ? 1 : (l.equals("ar") ? 2 : 0), langIds);
        c.addView(rgLang);
        c.addView(label(tr("تھیم", "Theme", "المظهر")));
        rgTheme = radios(new String[] {tr("ڈارک موڈ", "Dark mode", "الوضع الداكن"),
            tr("لائٹ موڈ", "Light mode", "الوضع الفاتح")}, st.dark() ? 0 : 1, themeIds);
        c.addView(rgTheme);
        c.addView(btn(tr("محفوظ کریں", "Save", "حفظ"), PURPLE, v -> saveSettings()));
        setBox.addView(c);
    }

    void saveSettings() {
        String[] langs = {"ur", "en", "ar"};
        int sel = rgLang.getCheckedRadioButtonId();
        for (int i = 0; i < 3; i++) if (langIds[i] == sel) st.setLang(langs[i]);
        st.setDark(rgTheme.getCheckedRadioButtonId() == themeIds[0]);
        buildUI();
        showTab(2);
        Svc.apply(this);
        Toast.makeText(this, tr("محفوظ ہو گیا", "Saved", "تم الحفظ"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle(tr("ایپ بند کریں؟", "Exit app?", "إغلاق التطبيق؟"))
            .setMessage(tr("کیا آپ واقعی باہر نکلنا چاہتے ہیں؟ (اسٹیٹس بار سروس چلتی رہے گی)",
                    "Do you really want to exit? (the status-bar service keeps running)",
                    "هل تريد الخروج فعلًا؟ (تستمر خدمة شريط الحالة)"))
            .setPositiveButton(tr("ہاں", "Yes", "نعم"), (dlg, w) -> finish())
            .setNegativeButton(tr("نہیں", "No", "لا"), null)
            .show();
    }
}

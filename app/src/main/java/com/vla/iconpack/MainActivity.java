package com.vla.iconpack;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final String ACTION_ADW_PICK = "org.adw.launcher.icons.ACTION_PICK_ICON";
    private static final String NOVA_PACKAGE = "com.teslacoilsw.launcher";

    private boolean pickMode;
    private String[] names;

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in = getIntent();
        pickMode = in != null && ACTION_ADW_PICK.equals(in.getAction());
        names = getResources().getStringArray(R.array.icon_names);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(16), dp(16), dp(16), 0);

        TextView title = new TextView(this);
        title.setText("VLA 4D Icon Pack");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(names.length + " icons · Black / Premium / Unique");
        sub.setTextColor(0xFF9A9A9A);
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER_HORIZONTAL);
        sub.setPadding(0, dp(2), 0, dp(12));
        root.addView(sub);

        if (!pickMode) {
            Button apply = new Button(this);
            apply.setText("Применить в Nova Launcher");
            apply.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { applyNova(); }
            });
            root.addView(apply);

            Button export = new Button(this);
            export.setText("Скопировать список моих приложений");
            export.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { copyAppList(); }
            });
            root.addView(export);

            TextView help = new TextView(this);
            help.setText("Другие лаунчеры (Lawnchair, Smart, Apex и др.): Настройки лаунчера → "
                    + "Иконки / Тема иконок → VLA 4D Icon Pack. Стандартный Samsung One UI Home "
                    + "сторонние паки не поддерживает — нужен один из этих лаунчеров.");
            help.setTextColor(0xFF9A9A9A);
            help.setTextSize(12);
            help.setPadding(0, dp(8), 0, dp(10));
            root.addView(help);
        }

        GridView grid = new GridView(this);
        grid.setNumColumns(4);
        grid.setHorizontalSpacing(dp(12));
        grid.setVerticalSpacing(dp(12));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setClipToPadding(false);
        grid.setPadding(0, 0, 0, dp(16));
        grid.setAdapter(new IconAdapter());
        grid.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                onIconClicked(position);
            }
        });
        root.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
    }

    private int resId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private void onIconClicked(int position) {
        String name = names[position];
        if (pickMode) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), resId(name));
            Intent result = new Intent();
            result.putExtra("icon", bmp);
            setResult(RESULT_OK, result);
            finish();
        } else {
            Toast.makeText(this, name.replace('_', ' '), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyNova() {
        try {
            Intent i = new Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME");
            i.setPackage(NOVA_PACKAGE);
            i.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO");
            i.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Nova Launcher не установлен или не является лаунчером по умолчанию",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void copyAppList() {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(main, 0);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(pm));
        StringBuilder sb = new StringBuilder();
        for (ResolveInfo ri : list) {
            sb.append(ri.activityInfo.packageName).append('/')
              .append(ri.activityInfo.name).append(" | ")
              .append(ri.loadLabel(pm)).append('\n');
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("apps", sb.toString()));
        Toast.makeText(this, "Скопировано приложений: " + list.size() + ". Вставь в чат с Claude.",
                Toast.LENGTH_LONG).show();
    }

    private static class SquareImageView extends ImageView {
        SquareImageView(Context c) { super(c); }
        @Override
        protected void onMeasure(int w, int h) {
            super.onMeasure(w, w);
            int s = getMeasuredWidth();
            setMeasuredDimension(s, s);
        }
    }

    private class IconAdapter extends BaseAdapter {
        @Override public int getCount() { return names.length; }
        @Override public Object getItem(int p) { return names[p]; }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;
            if (convertView == null) {
                iv = new SquareImageView(MainActivity.this);
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                iv = (ImageView) convertView;
            }
            iv.setImageResource(resId(names[position]));
            return iv;
        }
    }
}

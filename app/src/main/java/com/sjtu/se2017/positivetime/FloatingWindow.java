package com.sjtu.se2017.positivetime;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sjtu.se2017.positivetime.application.Constants;
import com.sjtu.se2017.positivetime.service.FloatWindowService;
import com.sjtu.se2017.positivetime.util.PreferenceUtil;

import java.lang.reflect.Field;

/**
 * Created by bonjour on 17-6-30.
 */

public class FloatingWindow extends AppCompatActivity implements Constants{
    private Button btnOpen;
    private Button btnClose;
    private PreferenceUtil preUtil;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceUtil.getSingleton(getApplicationContext()).getBoolean(SP_BG, false)) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.floatingwindow);
        preUtil = PreferenceUtil.getSingleton(getApplicationContext());
        intent = new Intent(FloatingWindow.this, FloatWindowService.class);
        btnOpen = (Button) findViewById(R.id.buttonOpen);
        btnOpen.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_alert), Toast.LENGTH_LONG).show();
                    Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(permissionIntent, OVERLAY_PERMISSION_REQ_CODE);
                } else {
                    startService(intent);
                }
            }
        });
        btnClose = (Button) findViewById(R.id.buttonClose);
        btnClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(FloatingWindow.this,
                        FloatWindowService.class);
                stopService(intent);
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        saveStatusBarHeight();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setChecked(preUtil.getBoolean(SP_BOOT, true));
        menu.getItem(1).setChecked(preUtil.getBoolean(SP_BG, false));
        menu.getItem(2).setChecked(preUtil.getBoolean(SP_LOC, false));
        return true;
    }



    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_result), Toast.LENGTH_SHORT).show();
            } else {
                startService(intent);
            }
        }
    }

    private void saveStatusBarHeight() {
        if (preUtil.getInt(SP_STATUSBAR_HEIGHT, 0) == 0) {
            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            Log.d("gjy", "statusBarHeight1=" + statusBarHeight);
            if (statusBarHeight == 0) {
                Class<?> c;
                Object obj;
                Field field;
                int x;
                try {
                    c = Class.forName("com.android.internal.R$dimen");
                    obj = c.newInstance();
                    field = c.getField("status_bar_height");
                    x = Integer.parseInt(field.get(obj).toString());
                    statusBarHeight = getResources().getDimensionPixelSize(x);
                    Log.d("gjy", "statusBarHeight2=" + statusBarHeight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            preUtil.saveInt(SP_STATUSBAR_HEIGHT, statusBarHeight);
        }
    }
}

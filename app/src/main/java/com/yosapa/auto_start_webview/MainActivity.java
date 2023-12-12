package com.yosapa.auto_start_webview;

import static android.app.PendingIntent.getActivity;
import static android.content.Intent.CATEGORY_LAUNCHER;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String URL_KEY = "url";
    private WebView myWebView;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName systemAdmin;
    private String PACKAGE_NAME;
    private final ActivityResultLauncher<Intent> launchDevicePolicyManagerActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    boolean isOwner = devicePolicyManager.isDeviceOwnerApp(PACKAGE_NAME);
                    addPersistentAdmin(isOwner);
                    setDefaultCosuPolicies(isOwner);
                    enableKioskMode(isOwner);
                }
            });

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(false);
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Keep the screen on and bright while this kiosk activity is running.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        systemAdmin = SystemAdmin.getComponentName(this);
        boolean isOwner = devicePolicyManager.isDeviceOwnerApp(PACKAGE_NAME);

        myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        myWebView.setWebViewClient(new MyWebViewClient());
        //myWebView.setWebChromeClient(new WebChromeClient());
        String load_url = pref.getString(URL_KEY,"https://www.google.com/");
        myWebView.loadUrl(load_url);
    }
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        // return true so that the menu pop up is opened
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String load_url = pref.getString(URL_KEY,"https://www.google.com/");
        if (id == R.id.home){
            myWebView.loadUrl(load_url);
        } else if (id == R.id.url){
            UrlDialog urlDialog = new UrlDialog(this,load_url);
            urlDialog.show();
            urlDialog.ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Editable s = urlDialog.editName.getText();
                    if (TextUtils.isEmpty(s)){
                        urlDialog.editName.setError("Null");
                        return;
                    }
                    pref.edit().putString(URL_KEY,s.toString()).apply();
                    myWebView.loadUrl(s.toString());
                    urlDialog.cancel();
                }
            });
        } else if (id == R.id.exit){
            //showSystemUI();
            showNavigationBar();
            exitKios();
            finishAffinity();
            throw new RuntimeException("EXIT_CLICKED");
            //System.exit(0);
        }
        return true;
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
        Log.i(TAG,"onWindowFocusChanged="+hasFocus);
    }

/*    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String load_url = pref.getString(URL_KEY, "https://www.google.com/");
        myWebView.loadUrl(load_url);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
/*        List<String> res = new ArrayList<String>();
        try {
            String[] cmd = new String[]{"shell", "PM2_HOME='/data/nodejs/.pm2' /system/bin/node /data/nodejs/bin/pm2 start /data/nodejs/bin/node-red -- --userDir /data/nodejs/.node-red"};
            Process su = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(su.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(su.getErrorStream()));

            String s;
            while ((s = stdInput.readLine()) != null)
                res.add(s);
            while ((s = stdError.readLine()) != null)
                res.add(s);

            Log.i(TAG,"Runtime.getRuntime="+ Arrays.toString(res.toArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.termux");
        if (LaunchIntent != null) {
            LaunchIntent.addCategory(CATEGORY_LAUNCHER);
            LaunchIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(LaunchIntent);
        }

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, systemAdmin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
        launchDevicePolicyManagerActivity.launch(intent);
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_IMMERSIVE
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }
    private void showNavigationBar() {
        // set navigation bar status, remember to disable "setNavigationBarTintEnabled"
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        // This work only for android 4.4+
        getWindow().getDecorView().setSystemUiVisibility(flags);
        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        final View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                if((v.getVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
                return insets;
            }
        });
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }

        });
    }
    private void enableKios(){
        runOnUiThread(() -> {
            boolean isOwner = devicePolicyManager.isDeviceOwnerApp(PACKAGE_NAME);
            addPersistentAdmin(isOwner);
            setDefaultCosuPolicies(isOwner);
            enableKioskMode(isOwner);
        });
    }
    private void exitKios(){ runOnUiThread(
            () -> {
                addPersistentAdmin(false);
                setDefaultCosuPolicies(false);
                enableKioskMode(false);
            });
    }
    private void enableKioskMode(boolean enabled) {
        try {
            if (enabled) {
                if (devicePolicyManager.isLockTaskPermitted(this.getPackageName())) {
                    startLockTask();
                }
            } else {
                stopLockTask();
            }
        } catch (Exception e) {
            // TODO: Log and handle appropriately
            e.printStackTrace();
        }
    }
    private void setDefaultCosuPolicies(boolean active){
        // Set user restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setUserRestriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, active);
        }

        int pluggedInto = BatteryManager.BATTERY_PLUGGED_AC |
                BatteryManager.BATTERY_PLUGGED_USB |
                BatteryManager.BATTERY_PLUGGED_WIRELESS;

        devicePolicyManager.setGlobalSetting(systemAdmin,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, String.valueOf(pluggedInto));

        // Disable keyguard and status bar
        devicePolicyManager.setKeyguardDisabled(systemAdmin, active);
        devicePolicyManager.setStatusBarDisabled(systemAdmin, active);

        // Enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

        // Set system update policy
        if (active){
            devicePolicyManager.setSystemUpdatePolicy(systemAdmin, SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
        } else {
            devicePolicyManager.setSystemUpdatePolicy(systemAdmin,null);
        }

        // set this Activity as a lock task package
        devicePolicyManager.setLockTaskPackages(systemAdmin,active ? new String[]{getPackageName()} : new String[]{});
    }
    private void addPersistentAdmin(boolean active){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            devicePolicyManager.addPersistentPreferredActivity(systemAdmin, intentFilter, new ComponentName(getPackageName(), MainActivity.class.getName()));
        } else {
            devicePolicyManager.clearPackagePersistentPreferredActivities(systemAdmin, getPackageName());
            devicePolicyManager.removeActiveAdmin(systemAdmin);
        }
    }
    private void setUserRestriction(String restriction, boolean disallow){
        if (disallow) {
            devicePolicyManager.addUserRestriction(systemAdmin,restriction);
        } else {
            devicePolicyManager.clearUserRestriction(systemAdmin,restriction);
        }
    }
    private void enableStayOnWhilePluggedIn(boolean enabled){
        if (enabled) {
            devicePolicyManager.setGlobalSetting(systemAdmin, Settings.Global.STAY_ON_WHILE_PLUGGED_IN,Integer.toString(BatteryManager.BATTERY_PLUGGED_AC| BatteryManager.BATTERY_PLUGGED_USB| BatteryManager.BATTERY_PLUGGED_WIRELESS));
        } else {
            devicePolicyManager.setGlobalSetting(systemAdmin, Settings.Global.STAY_ON_WHILE_PLUGGED_IN,"0");
        }
    }
}
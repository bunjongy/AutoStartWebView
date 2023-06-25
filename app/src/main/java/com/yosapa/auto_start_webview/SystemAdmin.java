package com.yosapa.auto_start_webview;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;

public class SystemAdmin extends DeviceAdminReceiver {
    private String TAG = "SystemAdmin";

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        Log.i(TAG,"onProfileProvisioningComplete");
        // Enable the profile
//        DevicePolicyManager manager =
//                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//        ComponentName componentName = getComponentName(context);
//        manager.setProfileName(componentName, context.getString(R.string.profile_name));
//        // Open the main screen
//        Intent launch = new Intent(context, MainActivity.class);
//        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(launch);
    }

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), SystemAdmin.class);
    }

    @NonNull
    @Override
    public DevicePolicyManager getManager(@NonNull Context context) {
        return super.getManager(context);
    }

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
    }

    @Override
    public void onLockTaskModeEntering(@NonNull Context context, @NonNull Intent intent, @NonNull String pkg) {
        super.onLockTaskModeEntering(context, intent, pkg);
    }

    @Override
    public void onLockTaskModeExiting(@NonNull Context context, @NonNull Intent intent) {
        super.onLockTaskModeExiting(context, intent);
    }

    @Override
    public CharSequence onDisableRequested(@NonNull Context context, @NonNull Intent intent) {
        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onPasswordChanged(@NonNull Context context, @NonNull Intent intent, @NonNull UserHandle user) {
        super.onPasswordChanged(context, intent, user);
    }

    @Override
    public void onPasswordFailed(@NonNull Context context, @NonNull Intent intent, @NonNull UserHandle user) {
        super.onPasswordFailed(context, intent, user);
    }

    @Override
    public void onPasswordSucceeded(@NonNull Context context, @NonNull Intent intent, @NonNull UserHandle user) {
        super.onPasswordSucceeded(context, intent, user);
    }

    @Override
    public void onPasswordExpiring(@NonNull Context context, @NonNull Intent intent, @NonNull UserHandle user) {
        super.onPasswordExpiring(context, intent, user);
    }

}
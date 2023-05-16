package com.example.roombookingsignage;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        String packageName = context.getPackageName();
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        policyManager.setLockTaskPackages(new ComponentName(context, MyDeviceAdminReceiver.class), new String[]{packageName});
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        policyManager.setLockTaskPackages(new ComponentName(context, MyDeviceAdminReceiver.class), new String[]{});
    }
}
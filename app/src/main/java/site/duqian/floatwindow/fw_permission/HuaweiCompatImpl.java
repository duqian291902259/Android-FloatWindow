package site.duqian.floatwindow.fw_permission;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 华为悬浮窗权限兼容实现
 */
public class HuaweiCompatImpl extends BelowApi23CompatImpl {

    private static final String TAG = "HuaweiCompatImpl";

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean apply(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // ComponentName comp = new ComponentName("com.huawei.systemmanager","com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            // "com.huawei.permissionmanager.ui.SingleAppActivity");//华为权限管理，跳转到指定app的权限管理位置需要华为接口权限，未解决
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");//悬浮窗管理页面
            intent.setComponent(comp);
            if (RomUtils.getEmuiVersion() == 3.1) {
                //emui 3.1 的适配
                startActivity(context, intent);
            } else {
                //emui 3.0 的适配
                comp = new ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity");//悬浮窗管理页面
                intent.setComponent(comp);
                startActivity(context, intent);
            }
        } catch (SecurityException e) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理，跳转到本app的权限管理页面,这个需要华为接口权限，未解决
            //ComponentName comp = new ComponentName("com.huawei.systemmanager","com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");//悬浮窗管理页面
            intent.setComponent(comp);
            startActivity(context, intent);
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ActivityNotFoundException e) {
            /**
             * 手机管家版本较低 HUAWEI SC-UL10
             */
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.Android.settings", "com.android.settings.permission.TabItem");//权限管理页面 android4.4
            //ComponentName comp = new ComponentName("com.android.settings","com.android.settings.permission.single_app_activity");//此处可跳转到指定app对应的权限管理页面，但是需要相关权限，未解决
            intent.setComponent(comp);
            startActivity(context, intent);
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (Exception e) {
            //抛出异常时提示信息
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return false;
    }

    private void startActivity(Context context, Intent intent) {
        context.startActivity(intent);
        //FloatWinPermissionCompat.getInstance().startActivity(intent);
    }

}

package site.duqian.floatwindow.fw_permission;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 360 悬浮窗权限兼容实现
 */
public class QihooCompatImpl extends BelowApi23CompatImpl {

    private static final String TAG = "QihooCompatImpl";

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean apply(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$OverlaySettingsActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (RomUtils.isIntentAvailable(context, intent)) {
            startActivity(context,intent);
            return true;
        } else {
            intent.setClassName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.appEnterActivity");
            if (RomUtils.isIntentAvailable(context, intent)) {
                startActivity(context,intent);
                return true;
            } else {
                Log.e(TAG, "can't open permission page with particular name, please use " +
                        "\"adb shell dumpsys activity\" command and tell me the name of the float window permission page");
            }
        }
        return false;
    }

    private void startActivity(Context context, Intent intent) {
        context.startActivity(intent);
        //FloatWinPermissionCompat.getInstance().startActivity(intent);
    }

}

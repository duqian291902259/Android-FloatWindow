package site.duqian.floatwindow.fw_permission;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 魅族悬浮窗权限兼容实现
 */
public class MeizuCompatImpl extends BelowApi23CompatImpl {

    @Override
    public boolean isSupported() {
        return true;
    }

    /**
     * 去魅族权限申请页面
     */
    @Override
    public boolean apply(Context context) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
            intent.putExtra("packageName", context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(context,intent);
        } catch (Exception e) {
            try {
                Log.d("dq","flyme 6.2.5+,apply permission failed");
                Api23CompatImpl.commonROMPermissionApplyInternal(context);
            } catch (Exception eFinal) {
                eFinal.printStackTrace();
            }
        }
        return true;
    }

    private void startActivity(Context context, Intent intent) {
        context.startActivity(intent);
        //FloatWinPermissionCompat.getInstance().startActivity(intent);
    }

}

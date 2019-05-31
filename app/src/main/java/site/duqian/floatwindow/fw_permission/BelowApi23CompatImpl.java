package site.duqian.floatwindow.fw_permission;

import android.content.Context;
import android.os.Build;

/**
 * Android 6.0 以下的通用实现基类
 */
public abstract class BelowApi23CompatImpl implements FloatWinPermissionCompat.CompatImpl {

  @Override
  public boolean check(Context context) {
    final int version = Build.VERSION.SDK_INT;
    if (version >= 19) {
      return FloatWinPermissionCompat.checkOp(context, 24); // 悬浮窗权限的 op 值是 OP_SYSTEM_ALERT_WINDOW = 24;
    }
    return true;
  }

}

package site.duqian.floatwindow.fw_permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * 悬浮窗权限兼容类
 * 参考了该项目的代码：https://github.com/zhaozepeng/FloatWindowPermission
 */
public class FloatWinPermissionCompat {

    private static final String TAG = FloatWinPermissionCompat.class.getSimpleName();

    public static FloatWinPermissionCompat getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final FloatWinPermissionCompat INSTANCE = new FloatWinPermissionCompat();
    }

    private CompatImpl compat;

    private FloatWinPermissionCompat() {
        // 6.0 以下的处理
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.isMiui()) {
                compat = new MiuiCompatImpl();
            } else if (RomUtils.isMeizu()) {
                compat = new MeizuCompatImpl();
            } else if (RomUtils.isHuawei()) {
                compat = new HuaweiCompatImpl();
            } else if (RomUtils.isQihoo()) {
                compat = new QihooCompatImpl();
            } else {
                // Android6.0以下未兼容机型默认实现
                compat = new BelowApi23CompatImpl() {
                    @Override
                    public boolean isSupported() {
                        return false;
                    }

                    @Override
                    public boolean apply(Context context) {
                        return false;
                    }
                };
            }
        } else {
            // 魅族单独适配一下
            if (RomUtils.isMeizu()) {
                compat = new MeizuCompatImpl();
            } else {
                // 6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
                compat = new Api23CompatImpl();
            }
        }
    }

    /**
     * 检查是否已开启悬浮窗权限
     *
     * @return
     */
    public boolean check(Context context) {
        return compat.check(context);
    }

    /**
     * 是否支持打开悬浮窗授权界面
     *
     * @return
     */
    public boolean isSupported() {
        return compat.isSupported();
    }


    /**
     * 检测 op 值判断悬浮窗是否已授权
     *
     * @param context
     * @param op
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else {
            Log.e(TAG, "Below API 19 cannot invoke!");
        }
        return false;
    }


    public interface CompatImpl {
        /**
         * 检测是否已经权限
         *
         * @param context
         * @return
         */
        boolean check(Context context);

        /**
         * 对于该 ROM 是否已经做了悬浮窗授权的兼容支持
         *
         * @return
         */
        boolean isSupported();

        /**
         * 申请权限
         *
         * @param context
         * @return
         */
        boolean apply(Context context);
    }

    /**
     * 申请悬浮窗权限
     *
     * @return 是否成功打开授权界面
     */
    public boolean apply(Context context) {
        if (!isSupported()) {
            return false;
        }
        forResult = false;
        this.context = context;
        return compat.apply(context);
    }

    public boolean apply(Activity activity) {
        if (activity == null || !isSupported()) {
            return false;
        }
        this.activity = activity;
        this.context = activity.getApplicationContext();
        forResult = true;
        return compat.apply(context);
    }

    public static final int REQUEST_CODE_SYSTEM_WINDOW = 1001;
    private Activity activity;
    private Context context;
    private boolean forResult = false;

    public void startActivity(Intent intent) {
        try {
            if (intent == null || context == null) {
                return;
            }
            if (!forResult) {
                context.startActivity(intent);
            } else {
                if (activity != null) {//为什么打开权限设置页就执行了onActivityResult？
                    activity.startActivityForResult(intent, REQUEST_CODE_SYSTEM_WINDOW);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
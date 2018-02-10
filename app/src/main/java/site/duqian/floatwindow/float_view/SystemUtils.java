package site.duqian.floatwindow.float_view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * Description:工具类
 *
 * @author 杜乾-Dusan,Created on 2018/2/9 - 16:11.
 *         E-mail:duqian2010@gmail.com
 */
public class SystemUtils {

    private static int screenHeight = 0;
    private static int screenWidth = 0;
    private static int statusBarHeight = 0;

    private SystemUtils() {
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        if (screenWidth > 0) {
            return screenWidth;
        }

        if (context == null) {
            return 0;
        }
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        return screenWidth;
    }

    /**
     * 获取屏幕高度,是否包含导航栏高度
     */
    public static int getScreenHeight(Context context, boolean isIncludeNav) {
        if (context == null) {
            return 0;
        }
        int screenHeight = getScreenHeight(context);
        if (isIncludeNav) {
            return screenHeight;
        } else {
            return screenHeight - getNavigationBarHeight(context);
        }
    }

    /**
     * 获取屏幕高(包括底部虚拟按键)
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        if (screenHeight > 0) {
            return screenHeight;
        }

        if (context == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        //WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = getWindowManager(context).getDefaultDisplay();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(displayMetrics);
            } else {
                display.getMetrics(displayMetrics);
            }
            screenHeight = displayMetrics.heightPixels;
        } catch (Exception e) {
            screenHeight = display.getHeight();
        }
        return screenHeight;
    }


    /**
     * 获取WindowManager。
     */
    public static WindowManager getWindowManager(Context context) {
        if (context == null) {
            return null;
        }
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * 获取NavigationBar的高度
     */
    public static int getNavigationBarHeight(Context context) {
        if (!hasNavigationBar(context)) {
            return 0;
        }
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 是否存在NavigationBar
     */
    public static boolean hasNavigationBar(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = getWindowManager(context).getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.x != size.x || realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !(menu || back);
        }
    }

    /**
     * dp转成px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static float sp2px(Context context, float spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * px转成dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getStatusBarHeightByReflect(Context context) {
        //int sbHeight;
        if (statusBarHeight > 0) {
            return statusBarHeight;
        }
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int sbHeightId = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(sbHeightId);
        } catch (Exception e1) {
            e1.printStackTrace();
            statusBarHeight = 0;
        }
        return statusBarHeight;
    }

    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = getStatusBarHeightByReflect(context);
        if (statusBarHeight == 0) {
            statusBarHeight = SystemUtils.dip2px(context, 30);
        }
        return statusBarHeight;
    }

}

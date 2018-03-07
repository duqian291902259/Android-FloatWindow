package site.duqian.floatwindow.float_view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import site.duqian.floatwindow.BaseActivity;
import site.duqian.floatwindow.uitls.SystemUtils;

/**
 * FloatWindowManager:管理悬浮窗视频播放
 * android.view.WindowManager$BadTokenException:
 * Unable to add window android.view.ViewRootImpl$W@123e0ab --
 * permission denied for this window 2003,type
 *
 * @author Nonolive-杜乾 Created on 2017/12/12-17:35.
 *         E-mail:dusan.du@nonolive.com
 */

public class FloatWindowManager {
    public static final int FW_TYPE_ROOT_VIEW = 10;
    public static final int FW_TYPE_APP_DIALOG = 11;
    public static final int FW_TYPE_ALERT_WINDOW = 12;
    public static final int FW_TYPE_APPLICATION_OVERLAY = 13;
    private int float_window_type = 0;
    private IFloatView floatView;
    private boolean isFloatWindowShowing = false;
    private FrameLayout contentView;
    private FloatViewParams floatViewParams;
    private WindowManager windowManager;
    private LastWindowInfo livePlayerWrapper;
    private BaseActivity activity;

    public FloatWindowManager() {
        livePlayerWrapper = LastWindowInfo.getInstance();
    }

    /**
     * 显示悬浮窗口
     */
    public synchronized void showFloatWindow(BaseActivity baseActivity, int floatWindowType) {
        if (baseActivity == null) {
            return;
        }
        activity = baseActivity;
        Context mContext = baseActivity.getApplicationContext();
        showFloatWindow(mContext, floatWindowType);
    }

    public synchronized void showFloatWindow(Context context, int floatWindowType) {
        if (context == null) {
            return;
        }
        float_window_type = floatWindowType;
        try {
            isFloatWindowShowing = true;
            initFloatWindow(context);
        } catch (Exception e) {
            e.printStackTrace();
            isFloatWindowShowing = false;
        }
    }

    /**
     * 初始化悬浮窗
     */
    private void initFloatWindow(final Context mContext) {
        if (mContext == null) {
            return;
        }
        floatViewParams = initFloatViewParams(mContext);
        if (float_window_type == FW_TYPE_ROOT_VIEW) {
            initCommonFloatView(mContext);
        } else {
            initSystemWindow(mContext);
        }
        isFloatWindowShowing = true;
    }

    /**
     * 直接在activity根布局添加悬浮窗
     *
     * @param mContext
     */
    private void initCommonFloatView(Context mContext) {
        try {
            if (activity == null) {
                return;
            }
            floatView = new FloatView(mContext, floatViewParams);
            View rootView = activity.getWindow().getDecorView().getRootView();
            contentView = (FrameLayout) rootView.findViewById(android.R.id.content);
            contentView.addView((View) floatView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 利用系统弹窗实现悬浮窗
     *
     * @param mContext
     */
    private void initSystemWindow(Context mContext) {
        windowManager = SystemUtils.getWindowManager(mContext);
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.packageName = mContext.getPackageName();
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SCALED
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        if (float_window_type == FW_TYPE_APP_DIALOG) {
            //wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;//TYPE_TOAST
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else if (float_window_type == FW_TYPE_ALERT_WINDOW) {//需要权限
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else if (float_window_type == FW_TYPE_APPLICATION_OVERLAY) {
            if (Build.VERSION.SDK_INT>=26) {
                wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
        }

        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.gravity = Gravity.START | Gravity.TOP;

        wmParams.width = floatViewParams.width;
        wmParams.height = floatViewParams.height;
        wmParams.x = floatViewParams.x;
        wmParams.y = floatViewParams.y;

        floatView = new FloatWindowView(mContext, floatViewParams, wmParams);
        try {
            windowManager.addView((View) floatView, wmParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((FloatWindowView) floatView).setWindowType(float_window_type);
    }

    /**
     * 初始化窗口参数
     *
     * @param mContext
     * @return
     */
    private FloatViewParams initFloatViewParams(Context mContext) {
        FloatViewParams params = new FloatViewParams();
        int screenWidth = SystemUtils.getScreenWidth(mContext);
        int screenHeight = SystemUtils.getScreenHeight(mContext, false);
        int statusBarHeight = SystemUtils.getStatusBarHeight(mContext);
        //根据实际宽高和设计稿尺寸比例适应。
        int marginBottom = SystemUtils.dip2px(mContext, 150);
        if (float_window_type == FW_TYPE_ROOT_VIEW) {
            marginBottom += statusBarHeight;
        }
        //设置窗口大小，已view、视频大小做调整
        int winWidth = LastWindowInfo.getInstance().getWidth();
        int winHeight = LastWindowInfo.getInstance().getHeight();
        int margin = SystemUtils.dip2px(mContext, 15);
        int width = 0;
        if (winWidth <= winHeight) {
            //竖屏比例
            width = (int) (screenWidth * 1.0f * 1 / 3) + margin;
        } else {//横屏比例
            width = (int) (screenWidth * 1.0f / 2) + margin;
        }
        float ratio = 1.0f * winHeight / winWidth;
        int height = (int) (width * ratio);

        //如果上次的位置不为null，则用上次的位置
        FloatViewParams lastParams = livePlayerWrapper.getLastParams();
        if (lastParams != null) {
            params.width = lastParams.width;
            params.height = lastParams.height;
            params.x = lastParams.x;
            params.y = lastParams.y;
            params.contentWidth = lastParams.contentWidth;
        } else {
            params.width = width;
            params.height = height;
            params.x = screenWidth - width;
            params.y = screenHeight - height - marginBottom;
            params.contentWidth = width;
        }

        params.screenWidth = screenWidth;
        params.screenHeight = screenHeight;
        if (float_window_type == FW_TYPE_ROOT_VIEW) {
            params.screenHeight = screenHeight - statusBarHeight;// - actionBarHeight;
        }
        params.videoViewMargin = margin;
        params.mMaxWidth = screenWidth / 2 + margin;
        params.mMinWidth = width;
        params.mRatio = ratio;
        return params;
    }

    public IFloatView getFloatView() {
        return floatView;
    }

    /**
     * 隐藏悬浮视频窗口
     */
    public synchronized void dismissFloatWindow() {
        if (!isFloatWindowShowing) {
            return;
        }
        isFloatWindowShowing = false;
        if (floatView != null) {
            FloatViewParams floatViewParams = floatView.getParams();
            livePlayerWrapper.setLastParams(floatViewParams);
        }
        try {
            if (windowManager != null && floatView != null) {
                removeWindow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (contentView != null && floatView != null) {
            contentView.removeView((View) floatView);
        }
        floatView = null;
        windowManager = null;
        contentView = null;
    }

    private void removeWindow() {
        try {
            windowManager.removeViewImmediate((View) floatView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

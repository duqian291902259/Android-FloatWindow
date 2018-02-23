package site.duqian.floatwindow.float_view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import site.duqian.floatwindow.BaseActivity;

/**
 * FloatWindowManager:管理悬浮窗视频播放
 *
 * @author Nonolive-杜乾 Created on 2017/12/12 - 17:35.
 *         E-mail:dusan.du@nonolive.com
 */
public class FloatWindowManager {
    private static final String TAG = FloatWindowManager.class.getSimpleName();
    public static final int FLOAT_WINDOW_TYPE_DIALOG = 10;
    public static final int FLOAT_WINDOW_TYPE_ROOT_VIEW = 11;
    public static final int FLOAT_WINDOW_TYPE_ALERT_WINDOW = 12;
    private int float_window_type = 0;
    private IFloatView floatView;
    private boolean isFloatWindowShowing = false;
    private FrameLayout contentView;
    private FloatViewParams floatViewParams;
    private WindowManager windowManager;
    private PositionWrapper livePlayerWrapper;
    private BaseActivity activity;
    private int actionBarHeight = 0;

    public FloatWindowManager() {
        livePlayerWrapper = PositionWrapper.getInstance();
    }

    /**
     * 显示悬浮视频小窗口
     */
    public synchronized void showFloatWindow(BaseActivity baseActivity, int floatWindowType) {
        if (baseActivity == null) {
            return;
        }
        actionBarHeight = baseActivity.getActionBarHeight();
        float_window_type = floatWindowType;
        Context mContext = baseActivity.getApplicationContext();
        activity = baseActivity;

        try {
            isFloatWindowShowing = true;
            initFloatWindow(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            isFloatWindowShowing = false;
            /*android.view.WindowManager$BadTokenException:
             Unable to add window android.view.ViewRootImpl$W@123e0ab --
             permission denied for this window 2003*/
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
        if (float_window_type == FLOAT_WINDOW_TYPE_ROOT_VIEW) {
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
        floatView = new FloatView(mContext, floatViewParams);
        View rootView = activity.getWindow().getDecorView().getRootView();
        contentView = (FrameLayout) rootView.findViewById(android.R.id.content);
        contentView.addView((View) floatView);
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

        if (float_window_type == FLOAT_WINDOW_TYPE_DIALOG) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        } else if (float_window_type == FLOAT_WINDOW_TYPE_ALERT_WINDOW) {
            //需要权限
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.gravity = Gravity.START | Gravity.TOP;

        wmParams.width = floatViewParams.width;
        wmParams.height = floatViewParams.height;
        wmParams.x = floatViewParams.x;
        wmParams.y = floatViewParams.y;

        floatView = new FloatWindowView(mContext, floatViewParams, wmParams);
        windowManager.addView((View) floatView, wmParams);
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
        //根据播放器实际宽高和设计稿尺寸比例适应。191 340 114
        int marginBottom = SystemUtils.dip2px(mContext, 150);
        if (float_window_type == FLOAT_WINDOW_TYPE_ROOT_VIEW) {
            marginBottom += statusBarHeight;
        }
        //设置窗口大小，已view、视频大小做调整
        int winWidth = PositionWrapper.getInstance().getWidth();
        int winHeight = PositionWrapper.getInstance().getHeight();
        int margin = SystemUtils.dip2px(mContext, 15);
        int width = 0;
        if (winWidth <= winHeight) {
            //竖屏比例
            width = (int) (screenWidth * 1.0f * 220 / 750) + margin;
        } else {//横屏比例
            width = (int) (screenWidth * 1.0f / 3) + margin;
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
        if (float_window_type == FLOAT_WINDOW_TYPE_ROOT_VIEW) {
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
                windowManager.removeViewImmediate((View) floatView);
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
}

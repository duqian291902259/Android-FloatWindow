package site.duqian.floatwindow;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import site.duqian.floatwindow.float_view.FloatViewListener;
import site.duqian.floatwindow.float_view.FloatWindowManager;
import site.duqian.floatwindow.float_view.IFloatView;

/**
 * Description:Activity基类
 *
 * @author 杜乾-Dusan,Created on 2018/2/9 - 15:52.
 *         E-mail:duqian2010@gmail.com
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected Context mContext;
    protected View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        int layoutId = getLayoutResId();
        if (layoutId > 0) {
            rootView = LayoutInflater.from(mContext).inflate(layoutId, null);
            setContentView(rootView);
        }
        floatWindowManager = new FloatWindowManager();

        initData();
        initView();
    }

    protected abstract int getLayoutResId();

    protected abstract void initData();

    protected abstract void initView();

    @Override
    protected void onResume() {
        super.onResume();
        showFloatWindowDelay();
    }

    protected void showFloatWindowDelay() {
        if (rootView != null && isShowFloatWindow()) {
            Log.d("dq ", " ShowFloatWindow");
            rootView.post(floatWindowRunnable);
        }
    }

    protected boolean isShowFloatWindow() {
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (floatWindowType != FloatWindowManager.FLOAT_WINDOW_TYPE_ALERT_WINDOW) {
            if (isShowFloatWindow()) {
                //不要放在closeFloatWindow()中，可能会导致其他界面熄屏
                clearScreenOn();
                closeFloatWindow();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floatWindowType != FloatWindowManager.FLOAT_WINDOW_TYPE_ALERT_WINDOW) {
            closeFloatWindow();
        }
    }

    /*---------------------------float window start---------------------------*/

    protected int floatWindowType = 0;

    private FloatWindowManager floatWindowManager;
    private final Runnable floatWindowRunnable = new Runnable() {
        @Override
        public void run() {
            showFloatWindow();
        }
    };

    /**
     * 显示悬浮窗
     */
    protected void showFloatWindow() {
        closeFloatWindow();
        floatWindowManager.showFloatWindow(this, floatWindowType);
        addFloatWindowClickListener();
    }

    /**
     * 关闭悬浮窗
     */
    protected void closeFloatWindow() {
        if (rootView != null) {
            rootView.removeCallbacks(floatWindowRunnable);
        }
        if (floatWindowManager != null) {
            floatWindowManager.dismissFloatWindow();
        }
    }

    /**
     * 监听悬浮窗关闭和点击事件
     */
    private void addFloatWindowClickListener() {
        IFloatView floatView = floatWindowManager.getFloatView();
        if (floatView == null) {
            return;
        }
        //说明悬浮窗view创建了，增加屏幕常亮
        keepScreenOn();
        floatView.setFloatViewListener(new FloatViewListener() {
            @Override
            public void onClose() {
                clearScreenOn();
                closeFloatWindow();
            }

            @Override
            public void onClick() {
                onFloatWindowClick();
            }

            @Override
            public void onMoved() {
                Log.d("dq", "onMoved");
            }

            @Override
            public void onDragged() {
                Log.d("dq", "onDragged");
            }
        });
    }

    /**
     * 开启屏幕常量
     */
    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 清除常量模式
     */
    private void clearScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 悬浮窗点击事件，子类按需重写
     */
    protected void onFloatWindowClick() {
        Toast.makeText(mContext, "FloatWindow clicked", Toast.LENGTH_LONG).show();
    }
}

package site.duqian.floatwindow;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.linchaolong.android.floatingpermissioncompat.FloatingPermissionCompat;

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
        if (!isShowTitle()) {
            //隐藏标题栏
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //隐藏状态栏
            // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
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

    protected boolean isShowTitle() {
        return false;
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

    /**
     * 获取标题栏高度-方法1
     * 标题栏高度 = View绘制区顶端位置 - 应用区顶端位置(也可以是状态栏高度，获取状态栏高度方法3中说过了)
     */
    public int getActionBarHeight() {
        int height = 0;
        //应用区域
        Rect outRect1 = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);

        //View绘制区域
        Rect outRect2 = new Rect();
        int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

        height = viewTop - outRect1.top;
        Log.e("duqian", "标题栏高度：" + height);
        return height;
    }

    protected void checkPermissionAndShow() {
        // 检查是否已经授权
        if (FloatingPermissionCompat.get().check(mContext)) {
            showFloatWindow();
        } else {
            // 授权提示
            new AlertDialog.Builder(mContext).setTitle("悬浮窗权限未开启")
                    .setMessage("你的手机没有授权" + mContext.getString(R.string.app_name) + "获得悬浮窗权限，视频悬浮窗功能将无法正常使用")
                    .setPositiveButton("开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 显示授权界面
                            FloatingPermissionCompat.get().apply(mContext);
                        }
                    })
                    .setNegativeButton("取消", null).show();
        }
    }

}

package site.duqian.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

        height = Math.abs(outRect1.top - viewTop);
        Log.d("duqian", "dq 标题栏高度：" + height);
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

    private final int mRequestCode = 1024;
    private RequestPermissionCallBack mRequestPermissionCallBack;


    /**
     * 权限请求结果回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllGranted = true;
        StringBuilder permissionName = new StringBuilder();
        for (String s : permissions) {
            permissionName = permissionName.append(s + "\r\n");
        }
        switch (requestCode) {
            case mRequestCode: {
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        hasAllGranted = false;
                        //在用户已经拒绝授权的情况下，如果shouldShowRequestPermissionRationale返回false则
                        // 可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                            new AlertDialog.Builder(BaseActivity.this).setTitle("申请权限")//设置对话框标题
                                    .setMessage(
                                            "获取相关权限失败:" + permissionName +
                                                    "将导致部分功能无法正常使用，需要到设置页面手动授权")//设置显示的内容
                                    .setPositiveButton("去授权", new DialogInterface.OnClickListener() {//添加确定按钮
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                            dialog.dismiss();
                                        }
                                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加返回按钮
                                @Override
                                public void onClick(DialogInterface dialog, int which) {//响应事件
                                    dialog.dismiss();
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mRequestPermissionCallBack.denied();
                                }
                            }).show();//在按键响应事件中显示此对话框
                        } else {
                            //用户拒绝权限请求，但未选中“不再提示”选项
                            mRequestPermissionCallBack.denied();
                        }
                        break;
                    }
                }
                if (hasAllGranted) {
                    mRequestPermissionCallBack.granted();
                }
            }
            break;
            default:
                break;
        }
    }

    /**
     * 发起权限请求
     *
     * @param context
     * @param permissions
     * @param callback
     */
    public void requestPermissions(final Context context, final String[] permissions,
                                   RequestPermissionCallBack callback) {
        this.mRequestPermissionCallBack = callback;
        StringBuilder permissionNames = new StringBuilder();
        for (String s : permissions) {
            permissionNames = permissionNames.append(s + "\r\n");
        }
        //如果所有权限都已授权，则直接返回授权成功,只要有一项未授权，则发起权限请求
        boolean isAllGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                isAllGranted = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                    new AlertDialog.Builder(BaseActivity.this).setTitle("PermissionTest")//设置对话框标题
                            //"【用户曾经拒绝过你的请求，所以这次发起请求时解释一下】" +
                            .setMessage(
                                    "您好，需要如下权限：" + permissionNames +
                                            " 请允许，否则将影响部分功能的正常使用。")//设置显示的内容
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                                @Override
                                public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                    //TODO Auto-generated method stub
                                    ActivityCompat.requestPermissions(((Activity) context), permissions, mRequestCode);
                                }
                            }).show();//在按键响应事件中显示此对话框
                } else {
                    ActivityCompat.requestPermissions(((Activity) context), permissions, mRequestCode);
                }
                break;
            }
        }
        if (isAllGranted) {
            mRequestPermissionCallBack.granted();
        }
    }

    /**
     * 权限请求结果回调接口
     */
    interface RequestPermissionCallBack {
        /**
         * 同意授权
         */
        void granted();

        /**
         * 取消授权
         */
        void denied();
    }

}

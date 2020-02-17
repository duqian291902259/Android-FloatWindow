package site.duqian.floatwindow;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import site.duqian.floatwindow.activity.SubAActivity;
import site.duqian.floatwindow.float_view.FloatWindowManager;
import site.duqian.floatwindow.float_view.LastWindowInfo;

/**
 * description:Demo展示用法。todo 目前发现显示actionBar的情况下缩放有bug
 *
 * @author 杜小菜 Created on 2019-05-09 - 10:45.
 * E-mail:duqian2010@gmail.com
 */
public class MainActivity extends BaseActivity {

    private TextView mTextMessage;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        floatWindowType = FloatWindowManager.FW_TYPE_ROOT_VIEW;
    }

    @Override
    protected void initView() {
        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        findViewById(R.id.btn_open_wm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SubAActivity.class));
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            closeFloatWindow();
            LastWindowInfo.getInstance().clear();
            switch (itemId) {
                case R.id.navigation_A:
                    mTextMessage.setText("应用内悬浮窗，只能在父view中移动和缩放,缩放不存在兼容性问题");
                    floatWindowType = FloatWindowManager.FW_TYPE_ROOT_VIEW;
                    showFloatWindowDelay();
                    break;
                case R.id.navigation_B:
                    floatWindowType = FloatWindowManager.FW_TYPE_APP_DIALOG;
                    mTextMessage.setText("WM实现，无需权限，targetSDK必须小于25，不能在桌面显示");
                    showFloatWindowDelay();
                    break;
                case R.id.navigation_C:
                    floatWindowType = FloatWindowManager.FW_TYPE_ALERT_WINDOW;
                    mTextMessage.setText("window Manager实现，需权限，退出应用，可在桌面显示，缩放存在兼容性问题");
                    checkPermissionAndShow();
                    break;
                default:
                    break;
            }

            return true;
        }
    };

}
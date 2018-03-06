package site.duqian.floatwindow;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import site.duqian.floatwindow.activity.SubAActivity;
import site.duqian.floatwindow.float_view.FloatWindowManager;
import site.duqian.floatwindow.float_view.LastWindowInfo;

public class MainActivity extends BaseActivity {

    private TextView mTextMessage;
    private Button mBtnOpen;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        floatWindowType = FloatWindowManager.FW_TYPE_ROOT_VIEW;
        //floatWindowType = FloatWindowManager.FW_TYPE_APP_DIALOG;
    }

    @Override
    protected void initView() {
        mTextMessage = (TextView) findViewById(R.id.message);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mBtnOpen.setOnClickListener(new View.OnClickListener() {
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
            Log.d("dq", "id=" + itemId);
            closeFloatWindow();
            LastWindowInfo.getInstance().clear();

            switch (itemId) {
                case R.id.navigation_A:
                    mTextMessage.setText("应用内悬浮窗，只能在父view中移动和缩放,不存在兼容性问题");
                    floatWindowType = FloatWindowManager.FW_TYPE_ROOT_VIEW;
                    showFloatWindowDelay();
                    break;
                case R.id.navigation_B:
                    floatWindowType = FloatWindowManager.FW_TYPE_APP_DIALOG;
                    mTextMessage.setText("WM实现，无需权限，但不能在桌面显示");
                    showFloatWindow();
                    break;
                case R.id.navigation_C:
                    if (Build.VERSION.SDK_INT >= 26) {
                        floatWindowType = FloatWindowManager.FW_TYPE_APPLICATION_OVERLAY;
                    } else {
                        floatWindowType = FloatWindowManager.FW_TYPE_ALERT_WINDOW;
                    }
                    mTextMessage.setText("window Manager实现，需权限，退出应用，可在桌面显示，存在兼容性问题（某些ROM机型可能无法显示）");
                    checkPermissionAndShow();
                    break;
                default:
                    break;
            }

            return true;
        }
    };

}
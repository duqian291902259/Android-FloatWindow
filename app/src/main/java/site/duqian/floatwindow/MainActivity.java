package site.duqian.floatwindow;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import site.duqian.floatwindow.activity.SubActivity;
import site.duqian.floatwindow.float_view.FloatWindowManager;

public class MainActivity extends BaseActivity {

    private TextView mTextMessage;
    private Button mBtnOpen;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_ROOT_VIEW;
        //floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_DIALOG;
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
                startActivity(new Intent(mContext, SubActivity.class));
            }
        });
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            Log.d("dq", "id=" + itemId);
            //closeFloatWindow();
            switch (itemId) {
                case R.id.navigation_A:
                    mTextMessage.setText(R.string.title_home);
                    floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_ROOT_VIEW;
                    showFloatWindowDelay();
                    break;
                case R.id.navigation_B:
                    floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_DIALOG;
                    mTextMessage.setText("window Manager实现，无需权限，待完善");
                    showFloatWindowDelay();
                    break;
                case R.id.navigation_C:
                    floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_ALERT_WINDOW;
                    mTextMessage.setText("window Manager实现，需权限，待完善");
                    checkPermissionAndShow();
                    break;
                default:
                    break;
            }

            return true;
        }
    };

    private void requestPermission() {
        requestPermissions(mContext, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, new RequestPermissionCallBack() {
            @Override
            public void granted() {
                showFloatWindowDelay();
            }

            @Override
            public void denied() {
                Toast.makeText(mContext, "悬浮窗权限获取失败，正常功能受到影响", Toast.LENGTH_LONG).show();
            }
        });
    }

}

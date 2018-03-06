package site.duqian.floatwindow.activity;

import android.Manifest;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import site.duqian.floatwindow.BaseActivity;
import site.duqian.floatwindow.R;
import site.duqian.floatwindow.float_view.FloatWindowManager;

public class SubBActivity extends BaseActivity {

    private TextView mTextMessage;

    @Override
    protected void initData() {
        floatWindowType = FloatWindowManager.FW_TYPE_ALERT_WINDOW;
        checkPermissionAndShow();
    }

    @Override
    protected void initView() {
        mTextMessage = (TextView) findViewById(R.id.message);
        mTextMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndShow();
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_sub_b;
    }


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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeFloatWindow();
    }
}

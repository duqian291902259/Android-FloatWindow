package site.duqian.floatwindow.activity;

import android.view.View;
import android.widget.TextView;

import site.duqian.floatwindow.BaseActivity;
import site.duqian.floatwindow.R;
import site.duqian.floatwindow.float_view.FloatWindowManager;

public class SubBActivity extends BaseActivity {

    private TextView mTextMessage;

    @Override
    protected void initData() {
        floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_ALERT_WINDOW;
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

}

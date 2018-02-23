package site.duqian.floatwindow.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import site.duqian.floatwindow.BaseActivity;
import site.duqian.floatwindow.R;
import site.duqian.floatwindow.float_view.FloatWindowManager;

public class SubAActivity extends BaseActivity {

    private TextView mTextMessage;

    @Override
    protected void initData() {
        floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_ROOT_VIEW;
    }

    @Override
    protected void initView() {
        mTextMessage = (TextView) findViewById(R.id.message);
        findViewById(R.id.btn_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SubBActivity.class));
            }
        });
        findViewById(R.id.btn_open_no_float_win).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SubCActivity.class));
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_sub;
    }

}

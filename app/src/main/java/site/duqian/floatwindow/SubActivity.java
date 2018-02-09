package site.duqian.floatwindow;

import android.widget.TextView;

import site.duqian.floatwindow.float_view.FloatWindowManager;

public class SubActivity extends BaseActivity {

    private TextView mTextMessage;

    @Override
    protected void initData() {
        floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_ROOT_VIEW;
    }

    @Override
    protected void initView() {
        mTextMessage = (TextView) findViewById(R.id.message);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_sub;
    }

}

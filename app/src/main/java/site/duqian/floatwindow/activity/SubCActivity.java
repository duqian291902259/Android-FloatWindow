package site.duqian.floatwindow.activity;

import android.view.View;
import android.widget.TextView;

import site.duqian.floatwindow.BaseActivity;
import site.duqian.floatwindow.R;
import site.duqian.floatwindow.float_view.FloatWindowManager;

public class SubCActivity extends BaseActivity {

    private TextView mTextMessage;

    @Override
    protected void initData() {
        floatWindowType = FloatWindowManager.FW_TYPE_ROOT_VIEW;
    }

    @Override
    protected void initView() {
        mTextMessage = (TextView) findViewById(R.id.message);
        mTextMessage.setText(R.string.tips_no_float_window);
        findViewById(R.id.btn_open_no_float_win).setVisibility(View.GONE);
        findViewById(R.id.btn_open_wm).setVisibility(View.GONE);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_sub;
    }


    @Override
    protected boolean isShowFloatWindow() {
        return false;
    }
}

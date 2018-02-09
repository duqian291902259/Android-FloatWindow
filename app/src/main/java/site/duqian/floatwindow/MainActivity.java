package site.duqian.floatwindow;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        Toast.makeText(mContext, "FLOAT_WINDOW_TYPE_ROOT_VIEW ", Toast.LENGTH_LONG).show();
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
            closeFloatWindow();
            switch (itemId) {
                case R.id.navigation_A:
                    mTextMessage.setText(R.string.title_home);
                    floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_ROOT_VIEW;
                    showFloatWindowDelay();
                    return true;
                case R.id.navigation_B:
                    //floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_DIALOG;
                    mTextMessage.setText("window Manager实现，无需权限，待完善");
                    return true;
                case R.id.navigation_C:
                    //floatWindowType = FloatWindowManager.FLOAT_WINDOW_TYPE_ALERT_WINDOW;
                    mTextMessage.setText("window Manager实现，需权限，待完善");
                    return true;
                default:
                    break;
            }

            return false;
        }
    };

}

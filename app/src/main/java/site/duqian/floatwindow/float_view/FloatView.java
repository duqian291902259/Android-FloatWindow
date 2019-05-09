
package site.duqian.floatwindow.float_view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import site.duqian.floatwindow.R;

/**
 * NonoFloatView:悬浮窗控件V2,普通的实现
 *
 * @author Nonolive-杜乾 Created on 2017/12/12 - 17:16.
 * E-mail:dusan.du@nonolive.com
 */
public class FloatView extends FrameLayout implements IFloatView {
    private float xInView;
    private float yInView;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private Context mContext;
    private RelativeLayout videoViewWrap;
    private RelativeLayout contentWrap;
    private ImageView ivZoomBtn;
    private FloatViewParams params = null;
    private FloatViewListener listener;
    private int screenWidth;
    private int screenHeight;
    private int statusBarHeight;
    private int mMinWidth;//初始宽度
    private int mMaxWidth;//视频最大宽度
    private float mRatio = 1.77f;//窗口高/宽比
    private int viewMargin;
    private View floatView;
    //悬浮窗可以移动的区域高度
    private int realHeight;

    public FloatView(Context mContext) {
        super(mContext);
        init();
    }

    public FloatView(@NonNull Context mContext, @NonNull FloatViewParams params) {
        super(mContext);
        this.params = params;
        init();
    }

    private void init() {
        initData();
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        floatView = inflater.inflate(R.layout.float_view_inner_layout, null);
        contentWrap = (RelativeLayout) floatView.findViewById(R.id.content_wrap);
        videoViewWrap = (RelativeLayout) floatView.findViewById(R.id.videoViewWrap);
        ivZoomBtn = (ImageView) floatView.findViewById(R.id.iv_zoom_btn);
        TextView tv_info = (TextView) floatView.findViewById(R.id.tv_info);
        tv_info.setText(getResources().getString(R.string.title_app_float_view));
        ivZoomBtn.setOnTouchListener(onZoomBtnTouchListener);
        contentWrap.setOnTouchListener(onMovingTouchListener);
        contentWrap.addOnLayoutChangeListener(onLayoutChangeListener);

        floatView.findViewById(R.id.iv_close_window).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != FloatView.this.listener) {
                    FloatView.this.listener.onClose();//关闭
                }
            }
        });

        int lastViewWidth = params.contentWidth;
        int lastViewHeight = (int) (lastViewWidth * mRatio);
        updateViewLayoutParams(lastViewWidth, lastViewHeight);
        addView(floatView);
    }

    private void initData() {
        mContext = getContext();
        if (params != null) {
            viewMargin = params.viewMargin;
            screenWidth = params.screenWidth;
            screenHeight = params.screenHeight;
            statusBarHeight = params.statusBarHeight;
            realHeight = screenHeight - statusBarHeight - params.titleBarHeight;
            mMaxWidth = params.mMaxWidth;
            mMinWidth = params.mMinWidth;
            mRatio = params.mRatio;

            oldX = params.x;
            oldY = params.y;
            mRight = params.x + params.width;
            mBottom = params.y + params.height;
        }
        // 如果显示了通知栏，标题栏，移动的区域边界问题要处理一下
    }

    private void updateViewLayoutParams(int width, int height) {
        if (contentWrap != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) contentWrap.getLayoutParams();
            layoutParams.height = height;
            layoutParams.width = width;
            contentWrap.setLayoutParams(layoutParams);
            params.width = width;
            params.height = height;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isRestorePosition) {
            contentWrap.layout(oldX, oldY, oldX + params.width, oldY + params.height);
            isRestorePosition = true;
        }
    }

    //是否恢复上次页面位置
    private boolean isRestorePosition = false;
    private int oldX = 0;
    private int oldY = 0;
    /**
     * 监听layout变化
     * left: View 左上顶点相对于父容器的横坐标
     * top: View 左上顶点相对于父容器的纵坐标
     * right: View 右下顶点相对于父容器的横坐标
     * bottom: View 右下顶点相对于父容器的纵坐标
     */
    private final OnLayoutChangeListener onLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (right != mRight || bottom != mBottom) {
                //Log.d("float", "dq onLayoutChange111 left=" + left + ",top=" + top + ",right=" + right + ",bottom=" + bottom);
                int width = contentWrap.getWidth();
                int height = contentWrap.getHeight();
                //防止拖出屏幕外部,顶部和右下角处理
                int l = mRight - width;
                int t = mBottom - height;
                int r = mRight;
                int b = mBottom;
                if (l < -viewMargin) {
                    l = -viewMargin;
                    r = l + width;
                }
                if (t < -viewMargin) {
                    t = -viewMargin;
                    b = t + height;
                }
                /*if (b > realHeight) {
                    b = realHeight;
                }*/
                try {
                    contentWrap.layout(l, t, r, b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                params.x = l;
                params.y = t;
            }
        }
    };

    private int mRight = 0;
    private int mBottom = 0;
    private final OnTouchListener onZoomBtnTouchListener = new OnTouchListener() {
        float lastX = 0;
        float lastY = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isDragged = true;
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    //记录右下角定点的位置，right 和 bottom
                    mRight = contentWrap.getRight();
                    mBottom = contentWrap.getBottom();
                    break;
                case MotionEvent.ACTION_MOVE:
                    showZoomView();
                    handleMoveEvent(event);
                    break;
                case MotionEvent.ACTION_UP:
                    if (listener != null) {
                        listener.onDragged();
                    }
                    displayZoomViewDelay();
                    isDragged = false;
                    break;
                default:
                    break;
            }
            return true;
        }

        private void handleMoveEvent(MotionEvent event) {
            isDragged = true;
            float moveX = event.getRawX();
            float moveY = event.getRawY();
            float dx = moveX - lastX;
            float dy = moveY - lastY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance >= 5) {
                //已经是最大或者最小不缩放
                int contentWidth = contentWrap.getWidth();
                if (moveY > lastY && moveX > lastX) {
                    if (contentWidth == mMinWidth) {
                        //最小了，不能再小了
                        return;
                    }
                    //缩小
                    distance = -distance;
                } else {
                    if (contentWidth == mMaxWidth) {
                        return;
                    }
                }
                int changedWidth = (int) (distance * Math.cos(45));
                //调节内部view大小
                updateContentViewSize(changedWidth);
            }
            lastX = moveX;
            lastY = moveY;
        }
    };

    public int getContentViewWidth() {
        return contentWrap != null ? contentWrap.getWidth() : mMinWidth;
    }

    /**
     * 更新内部view的大小
     *
     * @param width 传入变化的宽度
     */
    private void updateContentViewSize(int width) {
        int currentWidth = contentWrap.getWidth();
        int newWidth = currentWidth + width;
        newWidth = checkWidth(newWidth);
        int height = (int) (newWidth * mRatio);
        //调整视频view的大小
        updateViewLayoutParams(newWidth, height);
    }

    /**
     * 修正大小，限制最大和最小值
     *
     * @param width
     * @return
     */
    private int checkWidth(int width) {
        if (width > mMaxWidth) {
            width = mMaxWidth;
        }
        if (width < mMinWidth) {
            width = mMinWidth;
        }
        return width;
    }

    private boolean isMoving = false;
    private final OnTouchListener onMovingTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return onTouchEvent2(event);
        }
    };

    //@Override
    public boolean onTouchEvent2(MotionEvent event) {
        if (isDragged) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showZoomView();
                isMoving = false;
                xInView = event.getX();
                yInView = event.getY();
                Rect rect = new Rect();
                floatView.getGlobalVisibleRect(rect);
                if (!rect.contains((int) xInView, (int) yInView)) {
                    //不在移动的view内，不处理
                    return false;
                }
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = xDownInScreen;
                yInScreen = yDownInScreen;
                break;
            case MotionEvent.ACTION_MOVE:
                showZoomView();
                // 手指移动的时候更新小悬浮窗的位置
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                if (!isMoving) {
                    isMoving = !isClickedEvent();
                } else {
                    updateViewPosition();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isClickedEvent()) {
                    if (null != listener) {
                        listener.onClick();
                    }
                } else {
                    if (null != listener) {
                        listener.onMoved();
                    }
                }
                displayZoomViewDelay();
                isMoving = false;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 是否为点击事件
     */
    private boolean isClickedEvent() {
        int scaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        if (Math.abs(xDownInScreen - xInScreen) <= scaledTouchSlop
                && Math.abs(yDownInScreen - yInScreen) <= scaledTouchSlop) {
            return true;
        }
        return false;
    }

    /**
     * 更新悬浮窗位置,此方法用于修正移动悬浮窗的边界问题，保证不移出应用可见范围
     * todo 待修正有标题栏的情况下的边界问题
     */
    private synchronized void updateViewPosition() {
        int x = (int) (xInScreen - xInView);
        int y = (int) (yInScreen - yInView);
        int dWidth;
        //边界处理
        if (x < -viewMargin) {
            x = -viewMargin;
        }
        dWidth = screenWidth - contentWrap.getWidth();

        if (x > dWidth) {
            x = dWidth;
        }

        if (y < -viewMargin) {
            y = -viewMargin;
        }

        int dHeight = realHeight - contentWrap.getHeight();
        if (y > dHeight) {
            y = dHeight;
        }
        Log.d("duqian", "dq updateViewPosition x=" + x + ",y=" + y);
        reLayoutContentView(x, y);
    }

    /**
     * 重新布局
     *
     * @param x 左上角x坐标
     * @param y 左上角y坐标
     */
    private void reLayoutContentView(int x, int y) {
        //更新起点
        params.x = x;
        params.y = y;
        mRight = x + contentWrap.getWidth();
        mBottom = y + contentWrap.getHeight();
        contentWrap.layout(x, y, mRight, mBottom);
    }

    private boolean isDragged = false;//是否正在拖拽中
    private boolean isEdit = false;//是否进入编辑状态

    /**
     * 显示拖拽缩放按钮
     */
    private void showZoomView() {
        if (!isEdit) {
            ivZoomBtn.setVisibility(VISIBLE);
            videoViewWrap.setBackgroundColor(getResources().getColor(R.color.float_window_bg_border_edit));
            isEdit = true;
        }
    }

    /**
     * 隐藏缩放按钮
     */
    private void displayZoomView() {
        isEdit = false;
        ivZoomBtn.setVisibility(GONE);
        videoViewWrap.setBackgroundColor(getResources().getColor(R.color.float_window_bg_border_normal));
    }

    private void displayZoomViewDelay() {
        removeCallbacks(dispalyZoomBtnRunnable);
        postDelayed(dispalyZoomBtnRunnable, 2000);
    }

    private final Runnable dispalyZoomBtnRunnable = new Runnable() {
        @Override
        public void run() {
            displayZoomView();
        }
    };

    @Override
    public FloatViewParams getParams() {
        params.contentWidth = getContentViewWidth();
        return params;
    }

    @Override
    public void setFloatViewListener(FloatViewListener listener) {
        this.listener = listener;
    }

}

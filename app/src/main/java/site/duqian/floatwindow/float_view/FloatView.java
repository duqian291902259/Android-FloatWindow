
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
 *         E-mail:dusan.du@nonolive.com
 */
public class FloatView extends FrameLayout implements IFloatView {
    private static final String TAG = FloatView.class.getSimpleName();
    private float xInView;
    private float yInView;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private Context mContext;
    private RelativeLayout videoViewWrap;
    private RelativeLayout content_wrap;
    private ImageView iv_zoom_btn;

    private FloatViewParams params = null;
    private FloatViewListener listener;
    private int screenWidth;
    private int screenHeight;
    private int mMinWidth;//初始宽度
    private int mMaxWidth;//视频最大宽度
    private float mRatio = 1.77f;//窗口高/宽比
    private int videoViewMargin;
    private View floatView;

    public FloatView(Context mContext) {
        super(mContext);
        init();
    }

    public FloatView(@NonNull Context mContext, FloatViewParams params) {
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
        floatView = inflater.inflate(R.layout.view_float_window, null);
        content_wrap = (RelativeLayout) floatView.findViewById(R.id.content_wrap);
        videoViewWrap = (RelativeLayout) floatView.findViewById(R.id.videoViewWrap);
        iv_zoom_btn = (ImageView) floatView.findViewById(R.id.iv_zoom_btn);
        TextView tv_info = (TextView) floatView.findViewById(R.id.tv_info);
        tv_info.setText("view内部悬浮窗");
        iv_zoom_btn.setOnTouchListener(onZoomBtnTouchListener);
        content_wrap.setOnTouchListener(onMovingTouchListener);
        content_wrap.addOnLayoutChangeListener(onLayoutChangeListener);

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
        videoViewMargin = params.videoViewMargin;
        screenWidth = params.screenWidth;
        screenHeight = params.screenHeight;
        mMaxWidth = params.mMaxWidth;
        mMinWidth = params.mMinWidth;
        mRatio = params.mRatio;

        oldX = params.x;
        oldY = params.y;
        mRight = params.x + params.width;
        mBottom = params.y + params.height;
        //Log.d(TAG, " dq mRight=" + mRight + "/" + mBottom + ",rangeWidth=" + mMinWidth + "-" + mMaxWidth + ",mRatio=" + mRatio);
    }

    private void updateViewLayoutParams(int width, int height) {
        if (content_wrap != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) content_wrap.getLayoutParams();
            layoutParams.height = height;
            layoutParams.width = width;
            content_wrap.setLayoutParams(layoutParams);
            params.width = width;
            params.height = height;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isRestorePosition) {
            content_wrap.layout(oldX, oldY, oldX + params.width, oldY + params.height);
            isRestorePosition = true;
        }
    }

    private boolean isRestorePosition = false;//是否恢复上次页面位置
    private int oldX = 0;
    private int oldY = 0;
    // 监听layout变化
    private final OnLayoutChangeListener onLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (right != mRight || bottom != mBottom) {
                //Log.d(TAG, "dq onLayoutChange111 left=" + left + ",top=" + top + ",right=" + right + ",bottom=" + bottom);
                int width = content_wrap.getWidth();
                int height = content_wrap.getHeight();
                //防止拖出屏幕外部,顶部和右下角处理
                int l = mRight - width;
                int t = mBottom - height;
                int r = mRight;
                int b = mBottom;
                if (l < -videoViewMargin) {
                    l = -videoViewMargin;
                    r = l + width;
                }
                if (t < -videoViewMargin) {
                    t = -videoViewMargin;
                    b = t + height;
                }
                content_wrap.layout(l, t, r, b);
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
                    mRight = content_wrap.getRight();
                    mBottom = content_wrap.getBottom();
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
            if (distance >= 5) {//控制刷新频率
                //已经是最大或者最小不缩放
                int contentWidth = content_wrap.getWidth();
                if (moveY > lastY && moveX > lastX) {
                    if (contentWidth == mMinWidth) {//最小了，不能再小了
                        return;
                    }
                    distance = -distance;//缩小
                } else {
                    if (contentWidth == mMaxWidth) {
                        return;
                    }
                }
                int changedWidth = (int) (distance * Math.cos(45));//粗略计算
                //调节内部view大小
                updateContentViewSize(changedWidth);
            }
            lastX = moveX;
            lastY = moveY;
        }
    };

    public int getContentViewWidth() {
        return content_wrap != null ? content_wrap.getWidth() : mMinWidth;
    }

    /**
     * 更新内部view的大小
     *
     * @param width 传入变化的宽度
     */
    private void updateContentViewSize(int width) {
        int currentWidth = content_wrap.getWidth();
        int newWidth = currentWidth + width;
        newWidth = checkWidth(newWidth);
        int height = (int) (newWidth * mRatio);
        //params.x = params.x - width / 2;
        //params.y = params.y - width / 2;
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
                if (!rect.contains((int) xInView, (int) yInView)) {//不在移动的view内，不处理
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
                //updateEditStatus();
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
     *
     * @return
     */
    private boolean isClickedEvent() {
        int scaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();// - 10;
        if (Math.abs(xDownInScreen - xInScreen) <= scaledTouchSlop
                && Math.abs(yDownInScreen - yInScreen) <= scaledTouchSlop) {
            return true;
        }
        return false;
    }

    /**
     * 更新悬浮窗位置
     */
    private synchronized void updateViewPosition() {
        int x = (int) (xInScreen - xInView);
        int y = (int) (yInScreen - yInView);
        //边界处理
        if (x <= -videoViewMargin) {
            x = -videoViewMargin;
        }
        if (y <= -videoViewMargin) {
            y = -videoViewMargin;
        }
        int dWidth = screenWidth - content_wrap.getWidth();
        if (x >= dWidth) {
            x = dWidth;
        }
        int dHeight = screenHeight - content_wrap.getHeight();
        if (y >= dHeight) {
            y = dHeight;
        }
        if (x >= dWidth) {
            x = dWidth - 1;
        }
        Log.d(TAG, "dq updateViewPosition x=" + x + ",y=" + y);
        reLayoutContentView(x, y);
    }

    /**
     * 重新布局
     *
     * @param x
     * @param y
     */
    private void reLayoutContentView(int x, int y) {
        //更新起点
        params.x = x;
        params.y = y;
        mRight = x + content_wrap.getWidth();
        mBottom = y + content_wrap.getHeight();
        content_wrap.layout(x, y, mRight, mBottom);
    }

    private boolean isDragged = false;//是否正在拖拽中
    private boolean isEdit = false;//是否进入编辑状态

    /**
     * 显示拖拽缩放按钮
     */
    private void showZoomView() {
        if (!isEdit) {
            iv_zoom_btn.setVisibility(VISIBLE);
            videoViewWrap.setBackgroundColor(getResources().getColor(R.color.float_window_bg_border_edit));
            isEdit = true;
        }
    }

    /**
     * 隐藏缩放按钮
     */
    private void displayZoomView() {
        isEdit = false;
        iv_zoom_btn.setVisibility(GONE);
        videoViewWrap.setBackgroundColor(getResources().getColor(R.color.float_window_bg_border_normal));
    }

    /**
     * 调整视频view的边距
     */
    private void updateVideoMargin(int left, int top, int right, int bottom) {
        if (videoViewWrap != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) videoViewWrap.getLayoutParams();
            layoutParams.setMargins(left, top, right, bottom);
            videoViewWrap.setLayoutParams(layoutParams);
        }
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
    public void onPlayerError(int what, int extra) {

    }

    @Override
    public FloatViewParams getParams() {
        params.contentWidth = getContentViewWidth();
        return params;
    }

    @Override
    public void onPlayerCompletion() {

    }

    @Override
    public void release() {
        removeDelayCallBacks();
    }

    @Override
    public void setFloatViewListener(FloatViewListener listener) {
        this.listener = listener;
    }

    private void removeDelayCallBacks() {
        removeCallbacks(dispalyZoomBtnRunnable);
    }

}

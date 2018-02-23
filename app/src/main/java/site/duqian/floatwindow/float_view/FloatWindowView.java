package site.duqian.floatwindow.float_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import site.duqian.floatwindow.R;


/**
 * FloatWindowView:悬浮窗控件V1-利用windowManger控制窗口
 *
 * @author Nonolive-杜乾 Created on 2017/12/12 - 17:16.
 *         E-mail:dusan.du@nonolive.com
 */

public class FloatWindowView extends FrameLayout implements IFloatView {
    private static final String TAG = FloatWindowView.class.getSimpleName();
    private float xInView;
    private float yInView;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private Context mContext;
    private TextView tv_info;
    private RelativeLayout videoViewWrap;
    private RelativeLayout content_wrap;
    private ImageView iv_zoom_btn;

    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mWindowParams = null;
    private FloatViewParams params = null;
    private FloatViewListener listener;
    private int statusBarHeight = 0;
    private int screenWidth;
    private int screenHeight;
    private int mMinWidth;//初始宽度
    private int mMaxWidth;//视频最大宽度
    private float mRatio = 1.77f;//窗口高/宽比
    private int videoViewMargin;
    private boolean isSdkGt23 = false;//sdk版本是否>=23


    public FloatWindowView(Context mContext, FloatViewParams floatViewParams, WindowManager.LayoutParams wmParams) {
        super(mContext);
        this.params = floatViewParams;
        this.mWindowParams = wmParams;
        init();
    }

    private void init() {
        initData();
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View floatView = inflater.inflate(R.layout.view_float_window, null);
        content_wrap = (RelativeLayout) floatView.findViewById(R.id.content_wrap);
        videoViewWrap = (RelativeLayout) floatView.findViewById(R.id.videoViewWrap);
        tv_info = (TextView) floatView.findViewById(R.id.tv_info);
        tv_info.setText(getResources().getString(R.string.title_alert_window));
        iv_zoom_btn = (ImageView) floatView.findViewById(R.id.iv_zoom_btn);

        iv_zoom_btn.setOnTouchListener(onZoomBtnTouchListener);
        content_wrap.setOnTouchListener(onMovingTouchListener);

        floatView.findViewById(R.id.iv_close_window).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onClose();//关闭
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
        mWindowManager = SystemUtils.getWindowManager(mContext);
        statusBarHeight = params.statusBarHeight;
        screenWidth = params.screenWidth;
        screenHeight = params.screenHeight - statusBarHeight;//要去掉状态栏高度
        videoViewMargin = params.videoViewMargin;
        mMaxWidth = params.mMaxWidth;
        mMinWidth = params.mMinWidth;
        mRatio = params.mRatio;
        //起点
        startX = params.x;
        startY = params.y;
        //isSdkGt23 = Build.VERSION.SDK_INT >= 23;
        // >=23的部分手机缩放会卡顿，系统弹窗更新位置迟缓不够平滑
    }

    private void updateViewLayoutParams(int width, int height) {
        if (content_wrap != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) content_wrap.getLayoutParams();
            layoutParams.height = height;
            layoutParams.width = width;
            content_wrap.setLayoutParams(layoutParams);
        }
    }

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
                    changedX = 0;
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
                    if (!isSdkGt23) {
                        rejuestWindow();//缩放完成，要调整悬浮窗到视频大小。由于wm更新布局不及时，会有闪烁的问题
                    }
                    isDragged = false;
                    changedX = 0;
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
            if (distance >= 2) {//控制刷新频率
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
                //double angle = Math.atan2(Math.abs(dy), Math.abs(dx)) * 180 / Math.PI;
                int changedWidth = (int) (distance * Math.cos(45));//粗略计算
                if (!isSdkGt23) {
                    //调节内部view大小，先放大窗体到最大，方便调节大小
                    if (mWindowParams.width != mMaxWidth) {
                        updateWindowSize(mMaxWidth);
                    }
                    updateContentViewSize(changedWidth);
                } else {
                    updateFloatWindowSize(changedWidth);//大于6.0则直接改变window大小
                }
            }
            lastX = moveX;
            lastY = moveY;
        }
    };

    /**
     * 更新FloatWindow的大小
     *
     * @param width 传入变化的宽度
     */
    private void updateFloatWindowSize(int width) {
        int currentWidth = mWindowParams.width;
        int newWidth = currentWidth + width;
        newWidth = checkWidth(newWidth);
        int height = (int) (newWidth * mRatio);
        setFloatViewXYPostion(width);
        //调整window的大小
        updateWindowWidthAndHeight(newWidth, height);
        //调整视频view的大小
        updateViewLayoutParams(newWidth, height);
    }

    /**
     * 设置悬浮窗坐标位置
     *
     * @param changedWidth view宽度的变化
     */
    private void setFloatViewXYPostion(int changedWidth) {
        changedX += changedWidth / 2;
        int x = startX - changedX;
        int y = (int) (startY - changedX * mRatio);
        int width = mWindowParams.width;
        if (width >= mMinWidth && width <= mMaxWidth) {
            mWindowParams.x = x;
            mWindowParams.y = y;
        }
    }

    private int changedX = 0;//x轴方向的变化量
    private int dx = 0;//缩放宽度变化量
    private int dy = 0;//缩放高度变化量
    private int startX = 0;//缩放前的x坐标
    private int startY = 0;//缩放前的y坐标

    /**
     * 改变窗体大小前，获取一下x，y变化大小
     */
    private void rejuestWindow() {
        dx = content_wrap.getLeft();
        dy = content_wrap.getTop();
        //修正窗体xy坐标
        fixWindowXYPostion();
        updateWindowSize(content_wrap.getWidth());
        if (dx > 0 && dy > 0) {
            removeCallbacks(updateWindowPostionRunnable);
            //回到缩放后的位置，用post，并且0 delay效果好一些
            long duration = 0;
            postDelayed(updateWindowPostionRunnable, duration);
        }
    }

    private final Runnable updateWindowPostionRunnable = new Runnable() {
        @Override
        public void run() {
            updateWindowXYPosition(mWindowParams.x + dx, mWindowParams.y + dy);
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
        //调整视频view的大小
        updateViewLayoutParams(newWidth, height);
    }

    /**
     * 更新WM的宽高大小
     */
    private void updateWindowSize(int width) {
        width = checkWidth(width);
        int height = (int) (width * mRatio);
        updateWindowWidthAndHeight(width, height);
    }

    /**
     * 更新WM的宽高大小
     */
    private synchronized void updateWindowWidthAndHeight(int width, int height) {
        if (mWindowManager != null) {
            mWindowParams.width = width;
            mWindowParams.height = height;
            mWindowManager.updateViewLayout(this, mWindowParams);
        }
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

    /**
     * 调整悬浮窗坐标位置
     */
    private void fixWindowXYPostion() {
        int width = content_wrap.getWidth();
        if (mWindowParams.x + width >= screenWidth) {
            mWindowParams.x = screenWidth - width - 1;// 不让贴近右边和底部
        }
        if (mWindowParams.x <= 0) {
            mWindowParams.x = 0;
        }
        int height = content_wrap.getHeight();
        if (mWindowParams.y + height >= screenHeight) {
            mWindowParams.y = screenHeight - height - 1;
        }
        if (mWindowParams.y <= statusBarHeight) {
            mWindowParams.y = statusBarHeight;
        }
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
                isMoving = false;
                xInView = event.getX();
                yInView = event.getY();
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
                updateEditStatus();
                isMoving = false;
                break;
            default:
                break;
        }
        return true;
    }

    private boolean isClickedEvent() {
        int scaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        if (Math.abs(xDownInScreen - xInScreen) <= scaledTouchSlop
                && Math.abs(yDownInScreen - yInScreen) <= scaledTouchSlop) {
            // 是点击事件
            return true;
        }
        return false;
    }

    /**
     * 更新悬浮窗位置
     */
    private void updateViewPosition() {
        int x = (int) (xInScreen - xInView);
        int y = (int) (yInScreen - yInView);
        //防止超出通知栏
        if (y < statusBarHeight) {
            y = statusBarHeight;
        }
        //更新起点
        startX = x;
        startY = y;
        updateWindowXYPosition(x, y);
    }

    /**
     * 更新窗体坐标位置
     *
     * @param x
     * @param y
     */
    private synchronized void updateWindowXYPosition(int x, int y) {
        if (mWindowManager != null) {
            mWindowParams.x = x;
            mWindowParams.y = y;
            mWindowManager.updateViewLayout(this, mWindowParams);
        }
    }

    private void updateEditStatus() {
        handleZoomStatus();
        displayZoomViewDelay();
    }

    private boolean isDragged = false;//是否正在拖拽中
    private boolean isEdit = false;//是否进入编辑状态

    /**
     * 处理缩放按钮的状态
     */
    private void handleZoomStatus() {
        //左，上贴边时，隐藏dragView
        boolean isLeft = mWindowParams.x <= 0;
        boolean isTop = mWindowParams.y <= statusBarHeight;
        if (isLeft || isTop) {
            displayZoomView();
            // 贴边时设置视频margin
            if (isLeft && isTop) {
                updateVideoMargin(0, 0, videoViewMargin, videoViewMargin);
            } else if (isLeft) {
                updateVideoMargin(0, videoViewMargin, videoViewMargin, 0);
            } else if (isTop) {
                updateVideoMargin(videoViewMargin, 0, 0, videoViewMargin);
            }
        } else {
            showZoomView();
        }
    }

    private void showZoomView() {
        if (!isEdit ) {//&& isSdkGt23 只有6.0及以上才显示缩放按钮
            updateVideoMargin(videoViewMargin, videoViewMargin, 0, 0);
            iv_zoom_btn.setVisibility(VISIBLE);
            videoViewWrap.setBackgroundColor(getResources().getColor(R.color.float_window_bg_border_edit));
            isEdit = true;
        }
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

    private void displayZoomView() {
        isEdit = false;
        iv_zoom_btn.setVisibility(GONE);
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
    public void onPlayerError(int what, int extra) {

    }

    @Override
    public FloatViewParams getParams() {
        params.contentWidth = getContentViewWidth();
        params.x = mWindowParams.x;
        params.y = mWindowParams.y;
        params.width = mWindowParams.width;
        params.height = mWindowParams.height;
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

    public void setWindowType(int float_window_type) {
        if (float_window_type == FloatWindowManager.FLOAT_WINDOW_TYPE_DIALOG) {
            tv_info.setText(getResources().getString(R.string.title_float_window_dialog));
        } else if (float_window_type == FloatWindowManager.FLOAT_WINDOW_TYPE_ALERT_WINDOW) {
            tv_info.setText(getResources().getString(R.string.title_alert_window));
        }
    }


    private void removeDelayCallBacks() {
        removeCallbacks(dispalyZoomBtnRunnable);
    }

}

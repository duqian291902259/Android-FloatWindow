package site.duqian.floatwindow.float_view;

public class PositionWrapper {

    //记录上次悬浮窗的位置
    private FloatViewParams mLastParams = null;//上次的窗口参数

    public static PositionWrapper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PositionWrapper() {
    }

    private static class SingletonHolder {
        private static final PositionWrapper INSTANCE = new PositionWrapper();
    }

    public synchronized void clear() {
        resetPosition();
    }

    private void resetPosition() {
        setLastParams(null);
    }

    public FloatViewParams getLastParams() {
        return mLastParams;
    }

    public void setLastParams(FloatViewParams floatViewParams) {
        this.mLastParams = floatViewParams;
    }


    public int getWidth() {
        return 640;
    }

    public int getHeight() {
        return 960;
    }

}

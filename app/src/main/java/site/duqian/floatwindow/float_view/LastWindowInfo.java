package site.duqian.floatwindow.float_view;

/**
 * description:保存上一次悬浮窗的位置
 *
 * @author 杜小菜 Created on 2019-05-09 - 10:41.
 * E-mail:duqian2010@gmail.com
 */
public class LastWindowInfo {

    //记录上次悬浮窗的位置
    private FloatViewParams mLastParams = null;

    public static LastWindowInfo getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private LastWindowInfo() {
    }

    private static class SingletonHolder {
        private static final LastWindowInfo INSTANCE = new LastWindowInfo();
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

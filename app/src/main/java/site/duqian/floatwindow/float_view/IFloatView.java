package site.duqian.floatwindow.float_view;

/**
 * Description:悬浮窗抽象方法
 *
 * @author 杜乾-Dusan,Created on 2018/1/24 - 10:24.
 *         E-mail:duqian2010@gmail.com
 */
public interface IFloatView {
    public FloatViewParams getParams();
    public void onPlayerCompletion();
    public void release();
    public void onPlayerError(int what, int extra);
    public void setFloatViewListener(FloatViewListener listener);

    //void setWindowType(int float_window_type);
}

package site.duqian.floatwindow.float_view;

/**
 * Description:监听floatView点击事件
 *
 * @author 杜乾-Dusan,Created on 2017/12/12 - 17:27.
 *         E-mail:duqian2010@gmail.com
 */
public interface FloatViewListener {
    public void onClose();

    public void onClick();

    public void onMoved();

    public void onDragged();
}

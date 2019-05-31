package site.duqian.floatwindow.float_view;

/**
 * Description:监听floatView点击事件
 *
 * @author 杜乾-Dusan,Created on 2017/12/12 - 17:27.
 * E-mail:duqian2010@gmail.com
 */
public abstract class FloatViewListener {

    public abstract void onClose();

    public abstract void onClick();

    //新增双击事件，非必须实现
    public void onDoubleClick() {
    }

    public void onMoved() {
    }

    public void onDragged() {
    }

}
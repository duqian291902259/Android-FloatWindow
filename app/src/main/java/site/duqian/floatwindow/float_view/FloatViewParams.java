package site.duqian.floatwindow.float_view;

/**
 * Description:记录悬浮窗的宽高，坐标等信息
 *
 * @author 杜乾-Dusan,Created on 2018/1/24 - 10:28.
 *         E-mail:duqian2010@gmail.com
 */
public class FloatViewParams {
    public int width;//窗口的宽
    public int height;//窗口的高
    public int x;//窗口的x坐标
    public int y;//窗口的y坐标
    public int contentWidth;//当前窗口content view的宽度

    public int screenWidth;//屏幕宽度
    public int screenHeight;//屏幕高度
    public int statusBarHeight;//状态栏高度
    public int mMinWidth;//初始宽度
    public int mMaxWidth;//视频最大宽度
    public float mRatio = 1.77f;//窗口高/宽比
    public int viewMargin;//悬浮窗距离父view的边距

    public int titleBarHeight;//标题栏高度

}

## About FloatWindow

Android APP中实现悬浮窗的场景很多，比如悬浮窗播放视频，离开直播间，或者退出应用，继续播放直播流等。
本人研究并写了一套相对优雅的悬浮窗实现方案，缩放，移动，关闭，点击，自定义某个页面显示等。推荐采用内部view添加布局展示悬浮窗，此方案在线上稳定运行，无兼容性问题，可根据需要自行修改，欢迎交流。

![Android-FloatWindow](https://github.com/duqian291902259/Android-FloatWindow/blob/master/screenshot/float-window-no-permission-1.png)

下载apk体验一下吧：

[float-window-app-release.apk](https://github.com/duqian291902259/Android-FloatWindow/blob/master/release-app/float-window-app-release.apk)


## 悬浮窗的实现方式
### 一，Window Manager实现
通常的做法是使用WindowManager，使用其addView方法，添加一个布局view来实现系统弹窗，大部分window type需要申请权限，如果是 ```TYPE_SYSTEM_ALERT ``` 形式，需要申请以下权限：

``` java
android.permission.SYSTEM_ALERT_WINDOW

```

该方式，既可以在app内部显示，也可以在应用外也显示，缺点是有兼容性问题，需要做ROM适配，而且因为更新布局迟钝，不适合对悬浮窗做缩放操作，本项目有该方案的具体实现，具体参照demo。
悬浮窗主要是通过WindowManager这个类实现的，这个类有3个方法：

``` java
void addView (View view, WindowManager.LayoutParams params)//添加一个悬浮窗
void removeView (View view)//移除悬浮窗
void updateViewLayout (View view, WindowManager.LayoutParams params)//更新悬浮窗参数
```

以下是部分代码：

``` java
 /**
     * 利用系统弹窗实现悬浮窗
     *
     * @param mContext
     */
    private void initSystemWindow(Context mContext) {
        windowManager = SystemUtils.getWindowManager(mContext);
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.packageName = mContext.getPackageName();
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SCALED
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        if (float_window_type == FW_TYPE_DIALOG) {
            //wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else if (float_window_type == FW_TYPE_ALERT_WINDOW) {
            //需要权限
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.gravity = Gravity.START | Gravity.TOP;

        wmParams.width = floatViewParams.width;
        wmParams.height = floatViewParams.height;
        wmParams.x = floatViewParams.x;
        wmParams.y = floatViewParams.y;

        floatView = new FloatWindowView(mContext, floatViewParams, wmParams);
        windowManager.addView((View) floatView, wmParams);

    }

```

### 二，顶级view添加悬浮窗
demo中还提供了另外一种优雅的实现方式，无需申请权限，任意界面都可以显示悬浮窗，此弹窗,无需申请权限，可以拖动，缩放，关闭，点击，没有兼容性问题，限于app内部，可以在BaseActivity中注入。

``` java
/**
     * 直接在activity根布局添加悬浮窗
     *
     * @param mContext
     */
    private void initCommonFloatView(Context mContext) {
        floatView = new FloatView(mContext, floatViewParams);
        View rootView = activity.getWindow().getDecorView().getRootView();
        contentView = (FrameLayout) rootView.findViewById(android.R.id.content);
        contentView.addView((View) floatView);
    }
```

基本上可以满足大部分的需要，仅供参考。

![Android-FloatWindow](https://github.com/duqian291902259/Android-FloatWindow/blob/master/screenshot/float-window-system-permission.png)


### duqian2010@gmail.com
Github：
[Android-FloatWindow](https://github.com/duqian291902259/Android-FloatWindow)
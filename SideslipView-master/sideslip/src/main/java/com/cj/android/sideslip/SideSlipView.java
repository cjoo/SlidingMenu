package com.cj.android.sideslip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 侧滑View
 * Created by jian.cao on 2016/2/18.
 */
public class SideSlipView extends FrameLayout {
    private static final String TAG = "SideSlipView";
    //主内容视图
    private View mainContent;
    //覆盖主内容视图的View（注：为了实现当打开侧滑时，点击mainContent关闭侧滑）
    private View coverMainContent;
    //菜单视图
    private View menuWidget;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    @IntDef({LEFT, RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
    }

    @Direction
    int enableDirection = LEFT;//默认为left

    private float openMainContentDeviationX;//打开时主内容X坐标偏移值

    private float scale = 0.6f;//压缩值

    private boolean isOpen = false;//是否打开了侧滑菜单

    public SideSlipView(Context context) {
        super(context);
        init();
    }

    public SideSlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SideSlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SideSlipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Activity activity = (Activity) getContext();
        //获得根视图
        ViewGroup viewDecor = (ViewGroup) activity.getWindow().getDecorView();
        //从根视图获取主内容区
        mainContent = viewDecor.getChildAt(0);
        //从根视图移除主内容区
        viewDecor.removeViewAt(0);
        //把主内容区添加进侧滑View
        this.addView(mainContent);
        //侧滑View添加进根视图
        viewDecor.addView(this, 0);
        //把覆盖View添加进侧滑View
        coverMainContent = new View(getContext());
        coverMainContent.setVisibility(View.GONE);
        coverMainContent.setOnClickListener(mainContentOnClickListener);
        this.addView(coverMainContent, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    //当为打开状态时，点击mainContent关闭侧滑
    private OnClickListener mainContentOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeMenu();
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (openMainContentDeviationX <= 0) {
            //取个默认值
            openMainContentDeviationX = this.getWidth() * 2f / 3;
        }
    }

    /**
     * 获取菜单状态（打开或者关闭这两种状态）
     *
     * @return
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 设置方向
     *
     * @param enableDirection
     */
    public void setEnableDirection(@Direction int enableDirection) {
        this.enableDirection = enableDirection;
    }

    /**
     * 设置打开菜单后，主内容压缩值
     *
     * @param scale
     */
    public void setScale(float scale) {
        if (scale <= 0.0f || scale > 1.0f) {
            throw new IllegalStateException("scale取值范围(0.0f-1.0f]");
        }
        this.scale = scale;
    }

    /**
     * 设置打开菜单后，主内容X坐标偏移值
     *
     * @param openMainContentDeviationX
     */
    public void setMainContentDeviationX(float openMainContentDeviationX) {
        this.openMainContentDeviationX = Math.abs(openMainContentDeviationX);
    }

    /**
     * 设置菜单控件
     *
     * @param menuWidget
     * @param lp
     */
    public void setMenuWidget(View menuWidget, LayoutParams lp) {
        if (this.getChildCount() == 3) {
            this.removeViewAt(0);
        }
        this.addView(this.menuWidget = menuWidget, 0, lp);
    }

    /**
     * 设置菜单控件
     *
     * @param menuWidget
     */
    public void setMenuWidget(View menuWidget) {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setMenuWidget(menuWidget, lp);
    }

    /**
     * 设置菜单控件
     *
     * @param resourceId
     */
    public void setMenuWidget(int resourceId) {
        setMenuWidget(LayoutInflater.from(getContext()).inflate(resourceId, null));
    }

    /**
     * 打开
     */
    public void openMenu() {
        if (doType != NONE) {
            return;
        }
        doType = OPENING;
        coverMainContent.setVisibility(View.VISIBLE);
        float translationX = 0;
        if (enableDirection == LEFT) {
            translationX = openMainContentDeviationX - mainContent.getMeasuredWidth() * (1 - scale) * 0.5f;
        } else if (enableDirection == RIGHT) {
            translationX = getMeasuredWidth() - openMainContentDeviationX + mainContent.getMeasuredWidth() * (1 - scale) * 0.5f - mainContent.getMeasuredWidth();
        }
        startAnimation(translationX, scale, 1f, 250);
    }

    /**
     * 关闭
     */
    public void closeMenu() {
        if (doType != NONE) {
            return;
        }
        doType = CLOSEING;
        coverMainContent.setVisibility(View.GONE);
        startAnimation(0, 1f, 0f, 250);
    }

    //手指移动产生的效果
    private void action(float translationX, float scale, float alpha) {
        startAnimation(translationX, scale, alpha, 0);
    }

    private ObjectAnimator translationAnimator, scaleXAnimator, scaleYAnimator, alphaAnimator;
    private AnimatorSet bouncer;

    private void startAnimation(float targetX, float targetScale, float targetAlpha, long duration) {
        if (translationAnimator == null) {
            translationAnimator = ObjectAnimator.ofFloat(mainContent, "translationX", targetX);
        } else {
            translationAnimator.setFloatValues(targetX);
        }
        if (scaleXAnimator == null) {
            scaleXAnimator = ObjectAnimator.ofFloat(mainContent, "scaleX", targetScale);
        } else {
            scaleXAnimator.setFloatValues(targetScale);
        }
        if (scaleYAnimator == null) {
            scaleYAnimator = ObjectAnimator.ofFloat(mainContent, "scaleY", targetScale);
        } else {
            scaleYAnimator.setFloatValues(targetScale);
        }
        if (alphaAnimator == null) {
            alphaAnimator = ObjectAnimator.ofFloat(menuWidget, "alpha", targetAlpha);
        } else {
            alphaAnimator.setFloatValues(targetAlpha);
        }
        if (bouncer == null) {
            bouncer = new AnimatorSet();
            bouncer.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mainContent.clearAnimation();
                    menuWidget.clearAnimation();
                    switch (doType) {
                        case OPENING:
                            pullX = enableDirection == LEFT ? openMainContentDeviationX : -openMainContentDeviationX;
                            doType = NONE;
                            isOpen = true;
                            if (menuListener != null) {
                                menuListener.openMenu();
                            }
                            break;
                        case CLOSEING:
                            pullX = 0;
                            doType = NONE;
                            isOpen = false;
                            if (menuListener != null) {
                                menuListener.closeMenu();
                            }
                            break;
                    }
                }
            });
            bouncer.setInterpolator(new LinearInterpolator());
            bouncer.playTogether(translationAnimator, scaleXAnimator, scaleYAnimator, alphaAnimator);
        }
        bouncer.setDuration(duration);
        bouncer.start();
    }

    private OnMenuListener menuListener;//菜单打开关闭监听

    /**
     * 设置菜单打开关闭监听
     *
     * @param menuListener
     */
    public void setMenuListener(OnMenuListener menuListener) {
        this.menuListener = menuListener;
    }

    public interface OnMenuListener {
        void openMenu();

        void closeMenu();
    }

    private static final int NONE = 0;
    private static final int OPENING = 1;
    private static final int CLOSEING = 2;
    private int doType = NONE;//正在打开或关闭菜单中

    private boolean isFindFingerSlideDirection = false;
    private float lastX;//上次触摸坐标
    private static final float PRECISION = 4;//触摸距离达到该值时得到手指滑动方向

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            isFindFingerSlideDirection = false;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (doType != NONE) {
            return false;
        }
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isFindFingerSlideDirection) {
                    return true;
                }
                findFingerSlideDirection(ev);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return isFindFingerSlideDirection;
    }

    //找到手指滑动方向
    private void findFingerSlideDirection(MotionEvent event) {
        if (event.getX() - lastX >= PRECISION) {
            if ((!isOpen && enableDirection == LEFT) || (isOpen && enableDirection == RIGHT)) {
                isFindFingerSlideDirection = true;
            }
        } else if (event.getX() - lastX <= -PRECISION) {
            if ((!isOpen && enableDirection == RIGHT) || (isOpen && enableDirection == LEFT)) {
                isFindFingerSlideDirection = true;
            }
        }
        lastX = event.getX();
    }

    private float pullX;//拉拽距离
    private int pointerId = NONE_POINTER_ID;//触摸ID
    private static final int NONE_POINTER_ID = -1;//无效pointerId值

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (doType != NONE) {
            return true;
        }
        if (!isFindFingerSlideDirection) {
            findFingerSlideDirection(event);
            return true;
        }
        if (pointerId == NONE_POINTER_ID) {
            findPointerId(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                clearPointerId();
                break;
            case MotionEvent.ACTION_MOVE:
                pullX += (int) (event.getX(getPointerIndex(event)) - lastX);
                lastX = event.getX(getPointerIndex(event));
                if (enableDirection == LEFT) {
                    if (pullX < 0) {
                        pullX = 0;
                    }
                } else if (enableDirection == RIGHT) {
                    if (pullX > 0) {
                        pullX = 0;
                    }
                }
                update();
                break;
            case MotionEvent.ACTION_CANCEL://与ACTION_UP处理相同
            case MotionEvent.ACTION_UP:
                if (Math.abs(pullX) >= openMainContentDeviationX / 2) {
                    openMenu();
                } else {
                    closeMenu();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                clearPointerId();
                break;
        }
        return true;
    }

    //找到触摸ID
    private int findPointerId(MotionEvent event) {
        pointerId = event.getPointerId(0);
        lastX = event.getX(getPointerIndex(event));
        return 0;
    }

    //根据触摸ID获取位置
    private int getPointerIndex(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (pointerId == event.getPointerId(i)) {
                return i;
            }
        }
        return findPointerId(event);
    }

    //清除找到的触摸ID
    private void clearPointerId() {
        pointerId = NONE_POINTER_ID;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (enableDirection == LEFT) {
            coverMainContent.layout((int) openMainContentDeviationX,
                    (int) (getMeasuredHeight() / 2 - coverMainContent.getMeasuredHeight() * scale / 2),
                    (int) (openMainContentDeviationX + coverMainContent.getMeasuredWidth() * scale),
                    (int) (getMeasuredHeight() / 2 + coverMainContent.getMeasuredHeight() * scale / 2));
        } else if (enableDirection == RIGHT) {
            coverMainContent.layout(0,
                    (int) (getMeasuredHeight() / 2 - coverMainContent.getMeasuredHeight() * scale / 2),
                    (int) (coverMainContent.getMeasuredWidth() - openMainContentDeviationX),
                    (int) (getMeasuredHeight() / 2 + coverMainContent.getMeasuredHeight() * scale / 2));
        }

    }

    //更新UI
    private void update() {
        //计算缩放值
        float scale = (1 - Math.abs(pullX) * (1 - this.scale) / openMainContentDeviationX);
        //缩放后位置的变化值
        float dx = mainContent.getWidth() * (1 - scale) * 0.5f;
        //根据方向取正负数
        dx = enableDirection == LEFT ? dx : -dx;
        action(pullX - dx, scale, 1 - scale + this.scale * (Math.abs(pullX) / openMainContentDeviationX));
    }

}

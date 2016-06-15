package com.baymax.circlemenu.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.baymax.circlemenu.R;

public class ArcMenu extends ViewGroup implements OnClickListener {
    private static final int POS_LEFT_TOP = 0;
    private static final int POS_LEFT_BOTTOM = 1;
    private static final int POS_RIGHT_TOP = 2;
    private static final int POS_RIGHT_BOTTOM = 3;
    private static final int POS_CENTER = 4;

    private Position mPosition = Position.RIGHT_BOTTOM;
    private int mRadius;
    private int circleRadius;//小圆半径

    private Point centerPoint;
    private Point otherPoint[] = new Point[5];

    private Paint mPaint;
    // 颜色
    private static final int[] ALL_COLORS = {0xFF288CFF, 0xFF30A400, 0xFFFE4E37, 0xFF8A39FF, 0xFFFF6A00};
    //点击的状态
    private boolean[] status = new boolean[5];
    private int angel = 1;
     /**
     * 菜单的状态
     */
    private Status mCurrentStatus = Status.CLOSE;
    /**
     * 菜单的主按钮
     */
    private View mCButton;

    private OnMenuItemClickListener mMenuItemClickListener;

    public enum Status {
        OPEN, CLOSE
    }

    /**
     * 菜单的位置枚举类
     */
    public enum Position {
        LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM, CENTER
    }

    /**
     * 点击子菜单项的回调接口
     */
    public interface OnMenuItemClickListener {
        void onClick(View view, int pos);
    }

    public void setOnMenuItemClickListener(
            OnMenuItemClickListener mMenuItemClickListener) {
        this.mMenuItemClickListener = mMenuItemClickListener;
    }

    public ArcMenu(Context context) {
        this(context, null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        initPoint();
        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                100, getResources().getDisplayMetrics());
        circleRadius = 70;
        // 获取自定义属性的值
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ArcMenu, defStyle, 0);

        int pos = a.getInt(R.styleable.ArcMenu_position, POS_RIGHT_BOTTOM);
        switch (pos) {
            case POS_LEFT_TOP:
                mPosition = Position.LEFT_TOP;
                break;
            case POS_LEFT_BOTTOM:
                mPosition = Position.LEFT_BOTTOM;
                break;
            case POS_RIGHT_TOP:
                mPosition = Position.RIGHT_TOP;
                break;
            case POS_RIGHT_BOTTOM:
                mPosition = Position.RIGHT_BOTTOM;
                break;
            case POS_CENTER:
                mPosition = Position.CENTER;
                break;
        }
        mRadius = (int) a.getDimension(R.styleable.ArcMenu_radius, TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
                        getResources().getDisplayMetrics()));

        Log.e("TAG", "position = " + mPosition + " , radius =  " + mRadius);
        a.recycle();//回收资源
    }

    /**
     * 初始化点坐标
     */
    private void initPoint() {
        centerPoint = new Point(0,0);
        for(int i = 0;i < 5; i++){
            otherPoint[i] = new Point(0,0);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            // 测量child
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            layoutCButton();
            int count = getChildCount();
            for (int i = 0; i < count - 1; i++) {//otherPoint
                View child = getChildAt(i + 1);
                child.setVisibility(View.GONE);
                otherPoint[i].setX((int) (centerPoint.getX() - mRadius * Math.sin(i * 2 * Math.PI / (count -1))));
                otherPoint[i].setY((int) (centerPoint.getY() - mRadius * Math.cos(i * 2 * Math.PI / (count -1))));
                child.layout(otherPoint[i].getX() - circleRadius, otherPoint[i].getY() - circleRadius, otherPoint[i].getX() + circleRadius, otherPoint[i].getY() + circleRadius);
            }
        }
    }

    /**
     * 定位主菜单按钮
     */
    private void layoutCButton() {
        mCButton = getChildAt(0);        //主按钮
        mCButton.setOnClickListener(this);

        switch (mPosition) {
            case LEFT_TOP:
                centerPoint.setX(mRadius + circleRadius);
                centerPoint.setY(mRadius + circleRadius);
                break;
            case LEFT_BOTTOM:
                centerPoint.setX(mRadius + circleRadius);
                centerPoint.setY(getMeasuredHeight() - mRadius - circleRadius);
                break;
            case RIGHT_TOP:
                centerPoint.setX(getMeasuredWidth() - mRadius - circleRadius);
                centerPoint.setY(mRadius + circleRadius);
                break;
            case RIGHT_BOTTOM:
                centerPoint.setX(getMeasuredWidth() - mRadius - circleRadius);
                centerPoint.setY(getMeasuredHeight() - mRadius - circleRadius);
                break;
            case CENTER:
                centerPoint.setX(getMeasuredWidth()/2);
                centerPoint.setY(getMeasuredHeight()/2);
                break;
        }
        mCButton.layout(centerPoint.getX()- circleRadius, centerPoint.getY()- circleRadius, centerPoint.getX() + circleRadius, centerPoint.getY() + circleRadius);
    }

    @Override
    public void onClick(View v) {
        rotateCButton(v, 0f, 90f, 200);
        toggleMenu(300);
    }

    /**
     * 切换菜单
     */
    public void toggleMenu(int duration) {
        // 为menuItem添加平移动画和旋转动画
        int count = getChildCount();

        for (int i = 0; i < count - 1; i++) {
            final View childView = getChildAt(i + 1);
            childView.setVisibility(View.VISIBLE);

            AnimationSet animset = new AnimationSet(true);
            Animation tranAnim = null;
            // to open
            if (mCurrentStatus == Status.CLOSE) {
                tranAnim = new TranslateAnimation(centerPoint.getX() - otherPoint[i].getX(), 0, centerPoint.getY() - otherPoint[i].getY(), 0);
                childView.setClickable(true);
                childView.setFocusable(true);
            } else{// to close
                tranAnim = new TranslateAnimation(0, centerPoint.getX() - otherPoint[i].getX(), 0, centerPoint.getY() - otherPoint[i].getY());
                childView.setClickable(false);
                childView.setFocusable(false);
            }
            tranAnim.setFillAfter(true);
            tranAnim.setDuration(duration);
            tranAnim.setStartOffset((i * 100) / count);

            tranAnim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mCurrentStatus == Status.CLOSE) {
                        childView.setVisibility(View.GONE);
                    }
                }
            });
            // 旋转动画
            RotateAnimation rotateAnim = new RotateAnimation(0, 720,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setDuration(duration);
            rotateAnim.setFillAfter(true);

            animset.addAnimation(rotateAnim);
            animset.addAnimation(tranAnim);
            childView.startAnimation(animset);

            final int pos = i + 1;
            childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMenuItemClickListener != null)
                        mMenuItemClickListener.onClick(childView, pos);

                    menuItemAnim(pos - 1);
                    changeStatus();
                }
            });
        }
        // 切换菜单状态
        changeStatus();
    }

    /**
     * 子按钮的位移动画
     */
    private Animation translateButon(int offset, int duration,float fromXDelta, float toXDelta, float fromYDelta, float toYDelta){
        Animation tranAnim = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        tranAnim.setFillAfter(true);
        tranAnim.setDuration(duration);
        tranAnim.setStartOffset(duration * offset);
        return tranAnim;
    }
    /**
     * 添加menuItem的点击动画
     *
     * @param pos
     */
    private void menuItemAnim(int pos) {
        for (int i = 0; i < getChildCount() - 1; i++) {
            View childView = getChildAt(i + 1);
            if (i == pos) {
                childView.startAnimation(scaleBigAnim(300));
                //AnimationSet animationSet = new AnimationSet(true);
//                int cur = pos;
//                for(int j = 0;j < 1; j++){
//                    int next = (cur + 1) % 5;
//                    //childView.startAnimation(translateButon(1, 100, otherPoint[cur].getX() - otherPoint[pos].getX(), otherPoint[next].getX() - otherPoint[pos].getX(), otherPoint[cur].getY() - otherPoint[pos].getY(), otherPoint[next].getY() - otherPoint[pos].getY()));
////                    AnimationSet animset = new AnimationSet(true);
////
////                    animset.addAnimation(translateButon(1, 100, 0, otherPoint[1].getX() - otherPoint[0].getX(), 0, otherPoint[1].getY() - otherPoint[0].getY()));
////                    animset.addAnimation(translateButon(1, 100, otherPoint[1].getX() - otherPoint[0].getX(), otherPoint[2].getX() - otherPoint[0].getX(), otherPoint[1].getY() - otherPoint[0].getY(), otherPoint[2].getY() - otherPoint[0].getY()));
//
//                    //childView.startAnimation(translateButon(1, 100, 0, otherPoint[1].getX() - otherPoint[0].getX(), 0, otherPoint[1].getY() - otherPoint[0].getY()));
//                    //childView.startAnimation(translateButon(2, 100, otherPoint[1].getX() - otherPoint[0].getX(), otherPoint[2].getX() - otherPoint[0].getX(), otherPoint[1].getY() - otherPoint[0].getY(), otherPoint[2].getY() - otherPoint[0].getY()));
//                    //childView.startAnimation(animset);
//                    cur = next;
//                }
                //childView.startAnimation(animationSet);
            } else {
                childView.startAnimation(scaleSmallAnim(300));
            }
            childView.setClickable(false);
            childView.setFocusable(false);
        }
    }

    private Animation scaleSmallAnim(int duration) {
        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        AlphaAnimation alphaAnim = new AlphaAnimation(1f, 0.0f);
        animationSet.addAnimation(scaleAnim);
        animationSet.addAnimation(alphaAnim);
        animationSet.setDuration(duration);
        animationSet.setFillAfter(true);
        return animationSet;
    }
    /**
     * 为当前点击的Item设置变大和透明度降低的动画
     *
     * @param duration
     * @return
     */
    private Animation scaleBigAnim(int duration) {
        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 4.0f, 1.0f, 4.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        AlphaAnimation alphaAnim = new AlphaAnimation(1f, 0.0f);

        animationSet.addAnimation(scaleAnim);
        animationSet.addAnimation(alphaAnim);

        animationSet.setDuration(duration);
        animationSet.setFillAfter(true);
        return animationSet;
    }
    /**
     * 切换菜单状态
     */
    private void changeStatus() {
        mCurrentStatus = (mCurrentStatus == Status.CLOSE ? Status.OPEN
                : Status.CLOSE);
    }

    public boolean isOpen() {
        return mCurrentStatus == Status.OPEN;
    }

    private void rotateCButton(View v, float start, float end, int duration) {
        RotateAnimation anim = new RotateAnimation(start, end,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }

}

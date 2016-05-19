package com.opalinskiy.ostap.dyinglightprogress;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.RelativeLayout;


public class DyingLightProgress extends RelativeLayout implements ValueAnimator.AnimatorUpdateListener {
    private View viewA;
    private View viewB;
    private View viewC;
    private View viewD;
    private View viewE;
    private Context context;
    private int displayWidth;
    private int displayHeight;
    private Paint pShape;
    private Paint pBackground;
    private int viewColor;
    private int lineStroke;
    private int backgroundColor;
    private int smallViewSize;
    private int bigViewSize;
    private int animStepX;
    private int animStepY;
    private int viewWidth;
    private int viewHeight;

    private long speedAnim = 500;

    public DyingLightProgress(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public DyingLightProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DyingLightProgress,
                0, 0);
        try {
            viewColor = a.getInteger(R.styleable.DyingLightProgress_viewColor, Color.RED);
            backgroundColor = a.getInteger(R.styleable.DyingLightProgress_backgroundColor, Color.WHITE);
            lineStroke = a.getDimensionPixelSize(R.styleable.DyingLightProgress_lineStroke, 1);
        } finally {
            a.recycle();
        }
        init();
    }

    public DyingLightProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPaint(pBackground);
        Log.d("TAG", "onDraw()");
        drawLine(canvas, viewA, viewB);
        drawLine(canvas, viewB, viewC);
        drawLine(canvas, viewC, viewD);
        drawLine(canvas, viewD, viewA);
        drawLine(canvas, viewA, viewC);
        drawLine(canvas, viewB, viewD);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        viewWidth = w;
        viewHeight = h;

        smallViewSize = Math.min(w, h) / 10;
        bigViewSize = Math.min(w, h) / 4;
        animStepX = w / 2;
        animStepY = h / 2;

        setViews(smallViewSize, RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP, viewA);
        setViews(smallViewSize, RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP, viewB);
        setViews(smallViewSize, RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM, viewC);
        setViews(smallViewSize, RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_BOTTOM, viewD);
        setViews(bigViewSize, RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.CENTER_VERTICAL, viewE);

        Log.d("TAG", "onSizeChanged()");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("TAG", "onMeasure()");
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void init() {

        Log.d("TAG", "init");
        setWillNotDraw(false);
        pShape = new Paint();
        pShape.setColor(viewColor);
        pShape.setAntiAlias(true);
        pShape.setStyle(Paint.Style.STROKE);
        pShape.setStrokeWidth(lineStroke);

        pBackground = new Paint();
        pBackground.setColor(backgroundColor);
        pBackground.setStyle(Paint.Style.FILL);


        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        displayWidth = display.getWidth();
        displayHeight = display.getHeight();

        viewA = new View(context);
        viewB = new View(context);
        viewC = new View(context);
        viewD = new View(context);
        viewE = new View(context);

        final ViewTreeObserver observer = this.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playAnimation();
                    }
                }, 100);

            }
        });
    }

    private void playAnimation() {

        float bigViewCenterX = viewE.getX() + viewE.getWidth()/2;
        float bigViewCenterY = viewE.getY() + viewE.getHeight()/2;

        float smallViewCenterX = bigViewCenterX - smallViewSize/2;
        float smallViewCenterY = bigViewCenterY - smallViewSize/2;

        animateForward(smallViewCenterX, smallViewCenterY);
    }

    private void animateForward(float smallViewCenterX, float smallViewCenterY) {
        ObjectAnimator alfaAnim = ObjectAnimator.ofFloat(viewE, View.ALPHA, 0.0f, 1.0f, 0.0f);
        alfaAnim.setDuration((long) (speedAnim * converter(1000)));
        alfaAnim.setRepeatCount(2);

        ObjectAnimator action1 = ObjectAnimator.ofFloat(viewC, "y", viewC.getY()  + smallViewSize/2 - animStepY);
        action1.addUpdateListener(this);

        ObjectAnimator action2 = ObjectAnimator.ofFloat(viewD, "x", animStepX - smallViewSize/2);
        action2.setStartDelay((long) (speedAnim * converter(200)));
        action2.addUpdateListener(this);

        ObjectAnimator action3 = ObjectAnimator.ofFloat(viewA, "y", animStepY - smallViewSize/2);
        action3.setStartDelay((long) (speedAnim * converter(1000)));
        action3.addUpdateListener(this);

        ObjectAnimator action4 = ObjectAnimator.ofFloat(viewB, "x", viewB.getX() -  animStepX + smallViewSize/2);
        action4.setStartDelay((long) (speedAnim * converter(1600)));
        action4.addUpdateListener(this);

        ObjectAnimator action5 = ObjectAnimator.ofFloat(viewC, "x", smallViewCenterX);
        action5.addUpdateListener(this);

        ObjectAnimator action6 = ObjectAnimator.ofFloat(viewD, "y", smallViewCenterY);
        action6.setStartDelay((long) (speedAnim * converter(1600)));
        action6.addUpdateListener(this);

        ObjectAnimator action7 = ObjectAnimator.ofFloat(viewA, "x", smallViewCenterX);
        action7.setStartDelay((long) (speedAnim * converter(100)));
        action7.addUpdateListener(this);

        ObjectAnimator action8 = ObjectAnimator.ofFloat(viewB, "y", smallViewCenterY);
        action8.addUpdateListener(this);

        ObjectAnimator animA = ObjectAnimator.ofFloat(viewA, View.ALPHA, 1.0f, 0.0f, 1.0f);
        alfaAnim.setDuration((long) (speedAnim * converter(2000)));
        alfaAnim.setRepeatCount(2);

        ObjectAnimator animB = ObjectAnimator.ofFloat(viewB, View.ALPHA, 1.0f, 0.0f, 1.0f);
        alfaAnim.setDuration((long) (speedAnim * converter(2000)));
        alfaAnim.setRepeatCount(2);

        ObjectAnimator animC = ObjectAnimator.ofFloat(viewC, View.ALPHA, 1.0f, 0.0f, 1.0f);
        alfaAnim.setDuration((long) (speedAnim * converter(2000)));
        alfaAnim.setRepeatCount(2);

        ObjectAnimator animD = ObjectAnimator.ofFloat(viewD, View.ALPHA, 1.0f, 0.0f, 1.0f);
        alfaAnim.setDuration((long) (speedAnim * converter(2000)));
        alfaAnim.setRepeatCount(2);

        ObjectAnimator actionBack1 = ObjectAnimator.ofFloat(viewC, "x", viewWidth - smallViewSize);
        actionBack1.addUpdateListener(this);

        ObjectAnimator actionBack2 =  getObjectAnimator(viewD, "y", viewHeight - smallViewSize, 1000);

        ObjectAnimator actionBack3 = ObjectAnimator.ofFloat(viewA, "x", smallViewCenterX - viewWidth/2 + smallViewSize/2);
        actionBack3.setStartDelay((long) (speedAnim * converter(2000)));
        actionBack3.addUpdateListener(this);

        ObjectAnimator actionBack4 = ObjectAnimator.ofFloat(viewB, "y", smallViewCenterY - viewHeight/2 + smallViewSize/2);
        actionBack4.setStartDelay((long) (speedAnim * converter(3000)));
        actionBack4.addUpdateListener(this);

        ObjectAnimator actionBack5 = ObjectAnimator.ofFloat(viewC, "y", viewHeight - smallViewSize);
        actionBack5.addUpdateListener(this);

        ObjectAnimator actionBack6 = ObjectAnimator.ofFloat(viewD, "x", smallViewCenterX - viewWidth/2 + smallViewSize/2);
        actionBack6.setStartDelay((long) (speedAnim * converter(1000)));
        actionBack6.addUpdateListener(this);

        ObjectAnimator actionBack7 = ObjectAnimator.ofFloat(viewA, "y", smallViewCenterY - viewHeight/2 + smallViewSize/2);
        actionBack7.setStartDelay((long) (speedAnim * converter(2000)));
        actionBack7.addUpdateListener(this);

        ObjectAnimator actionBack8 = ObjectAnimator.ofFloat(viewB, "x", viewWidth - smallViewSize);
        actionBack8.setStartDelay((long) (speedAnim * converter(3000)));
        actionBack8.addUpdateListener(this);

        ObjectAnimator alphaAnimEnd = ObjectAnimator.ofFloat(viewE, View.ALPHA, 0.0f, 1.0f, 1.0f);
        alfaAnim.setDuration((long) (speedAnim * converter(2000)));
        alfaAnim.setRepeatCount(4);

        AnimatorSet animSet = new AnimatorSet();
        animSet.play(action1).with(action2).with(action3).with(action4);
        animSet.play(alfaAnim).before(action1);
        animSet.play(action5).with(action6).with(action7).after(action4);
        animSet.play(action8).after(action6);
        animSet.play(animA).with(animB).with(animC).with(animD).after(action8);
        animSet.play(actionBack1).with(actionBack2).with(actionBack3).with(actionBack4).after(animD);
        animSet.play(actionBack5).with(actionBack6).with(actionBack7).with(actionBack8).after(actionBack3);
        animSet.play(alphaAnimEnd).after(actionBack8);

        animSet.setDuration(speedAnim);
        animSet.start();
    }

    @NonNull
    private ObjectAnimator getObjectAnimator(View viewD, String y, int i, int startDelay) {
        ObjectAnimator actionBack2 = ObjectAnimator.ofFloat(viewD, y, i);
        actionBack2.setStartDelay(startDelay);
        actionBack2.addUpdateListener(this);
        return actionBack2;
    }

    private void drawLine(Canvas canvas, View viewStart, View viewEnd) {
        float startViewX = viewStart.getX();
        float startViewY = viewStart.getY();

        float startX = startViewX + viewStart.getWidth() / 2;
        float startY = startViewY + viewStart.getHeight() / 2;

        float endViewX = viewEnd.getX();
        float endViewY = viewEnd.getY();

        float endX = endViewX + viewEnd.getWidth() / 2;
        float endY = endViewY + viewEnd.getHeight() / 2;

        canvas.drawLine(startX, startY, endX, endY, pShape);
    }

    private float converter(long delay){
        return delay/1500;
    }

    private void setViews(int viewSize, int alignParentLeft, int alignParentTop, View viewA) {
        LayoutParams lpA = new LayoutParams(viewSize, viewSize);
        lpA.addRule(alignParentLeft);
        lpA.addRule(alignParentTop);
        viewA.setBackgroundColor(viewColor);
        this.addView(viewA, lpA);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }
}

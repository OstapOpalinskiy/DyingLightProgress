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


public class DyingLightProgress extends RelativeLayout implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
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
    private int viewWidth;
    private int viewHeight;
    private Handler handler;
    private boolean runProgress;
    private float pause;

    private long speedAnim = 300;

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

        smallViewSize = Math.min(w, h) / 9;
        bigViewSize = Math.min(w, h) / 3;

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
        runProgress = true;
        pause = (float) 0.3;
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

        // left, top
        viewA = new View(context);
        // right top
        viewB = new View(context);
        // left bottom
        viewC = new View(context);
        // right bottom
        viewD = new View(context);
        // big center view
        viewE = new View(context);

//        ratio = baseRatio / speedAnim;

        handler = new Handler();

        final ViewTreeObserver observer = this.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    observer.removeOnGlobalLayoutListener(this);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        playAnimation();
                    }
                });
            }
        });
    }

    private void playAnimation() {

        float bigViewCenterX = viewE.getX() + viewE.getWidth() / 2;
        float bigViewCenterY = viewE.getY() + viewE.getHeight() / 2;
        long breakTime = (long) (speedAnim * pause);
        float centerX = bigViewCenterX - smallViewSize / 2;
        float centerY = bigViewCenterY - smallViewSize / 2;
        float leftX = centerX - viewWidth / 2 + smallViewSize / 2;
        float rightX = viewWidth - smallViewSize;
        float topY = centerY - viewHeight / 2 + smallViewSize / 2;
        float bottomY = viewHeight - smallViewSize;

        // big view fading
        ObjectAnimator eFade = getObjectAnimatorAlpha(viewE, 0.0f, 1.0f, 0.0f);

        // reduction animation
        ObjectAnimator cCenterY = getObjectAnimator(viewC, "y", centerY, 0);

        ObjectAnimator dCenterX = getObjectAnimator(viewD, "x", centerX, breakTime);

        ObjectAnimator aCenterY = getObjectAnimator(viewA, "y", centerY, breakTime * 2);

        ObjectAnimator bCenterX = getObjectAnimator(viewB, "x", centerX, breakTime * 3);

        ObjectAnimator cCenterX = getObjectAnimator(viewC, "x", centerX, breakTime * 4);

        ObjectAnimator dCenterY = getObjectAnimator(viewD, "y", centerY, breakTime * 5);

        ObjectAnimator aCenterX = getObjectAnimator(viewA, "x", centerX, breakTime * 6);

        ObjectAnimator bCenterY = getObjectAnimator(viewB, "y", centerY, breakTime * 7);

        //small view fading
        ObjectAnimator aFade = getObjectAnimatorAlpha(viewA, 1.0f, 0.0f, 1.0f);

        ObjectAnimator bFade = getObjectAnimatorAlpha(viewB, 1.0f, 0.0f, 1.0f);

        ObjectAnimator cFade = getObjectAnimatorAlpha(viewC, 1.0f, 0.0f, 1.0f);

        ObjectAnimator dFade = getObjectAnimatorAlpha(viewD, 1.0f, 0.0f, 1.0f);

        //expansion animation
        ObjectAnimator cRight = getObjectAnimator(viewC, "x", rightX, 0);

        ObjectAnimator dBottom = getObjectAnimator(viewD, "y", bottomY, breakTime);

        ObjectAnimator aLeft = getObjectAnimator(viewA, "x", leftX, breakTime * 3);

        ObjectAnimator bTop = getObjectAnimator(viewB, "y", topY, breakTime * 4);

        ObjectAnimator cBottom = getObjectAnimator(viewC, "y", bottomY, breakTime * 5);

        ObjectAnimator dLeft = getObjectAnimator(viewD, "x", leftX, breakTime * 7);

        ObjectAnimator aTop = getObjectAnimator(viewA, "y", topY, breakTime * 8);

        ObjectAnimator bRight = getObjectAnimator(viewB, "x", rightX, (long) (speedAnim * pause * 8.5));

        // plays the animation
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(cCenterY).with(dCenterX).with(aCenterY).with(bCenterX)
                .with(cCenterX).with(dCenterY).with(aCenterX).with(bCenterY);
        animSet.play(eFade).before(cCenterY);
        animSet.play(aFade).with(bFade).with(cFade).with(dFade).after(bCenterY);
        animSet.play(cRight).with(dBottom).with(aLeft).with(bTop)
                .with(cBottom).with(dLeft).with(aTop).with(bRight).after(dFade);
        animSet.addListener(this);
        animSet.start();
    }

    @NonNull
    private ObjectAnimator getObjectAnimatorAlpha(View viewA, float v, float v2, float v3) {
        ObjectAnimator animA = ObjectAnimator.ofFloat(viewA, View.ALPHA, v, v2, v3);
        animA.setDuration((long) (speedAnim * pause * 6));
        animA.setRepeatCount(3);
        return animA;
    }

    @NonNull
    private ObjectAnimator getObjectAnimator(View viewD, String x, float i, long startDelay) {
        ObjectAnimator action2 = ObjectAnimator.ofFloat(viewD, x, i);
        action2.setDuration(speedAnim);
        action2.setStartDelay(startDelay);
        action2.addUpdateListener(this);
        return action2;
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

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (runProgress) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    playAnimation();
                }
            });
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

}

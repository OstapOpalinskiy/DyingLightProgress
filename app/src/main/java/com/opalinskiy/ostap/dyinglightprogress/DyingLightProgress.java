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
    private int animStepX;
    private int animStepY;
    private int viewWidth;
    private int viewHeight;
    private int baseRatio = 1500;
    private float ratio;
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

        viewA = new View(context);
        viewB = new View(context);
        viewC = new View(context);
        viewD = new View(context);
        viewE = new View(context);

        ratio = baseRatio / speedAnim;

        handler = new Handler();

        final ViewTreeObserver observer = this.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    observer.removeOnGlobalLayoutListener(this);
                }

//                for (int i = 0; i < 4; i++) {
//                    playAnimation();
//                }

                  playAnimation();

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

        float smallViewCenterX = bigViewCenterX - smallViewSize / 2;
        float smallViewCenterY = bigViewCenterY - smallViewSize / 2;

        animateForward(smallViewCenterX, smallViewCenterY);
    }

    private void animateForward(float smallViewCenterX, float smallViewCenterY) {

        // fades big square
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(viewE, View.ALPHA, 0.0f, 1.0f, 0.0f);
        alphaAnim.setDuration((long) (speedAnim * pause * 6));
        alphaAnim.setRepeatCount(3);

        ObjectAnimator action1 = ObjectAnimator.ofFloat(viewC, "y", viewC.getY() + smallViewSize / 2 - animStepY);
        action1.setDuration(speedAnim);
        action1.addUpdateListener(this);

        ObjectAnimator action2 = getObjectAnimator(viewD, "x", animStepX - smallViewSize / 2, speedAnim, (long) (speedAnim * pause));

        ObjectAnimator action3 = getObjectAnimator(viewA, "y", animStepY - smallViewSize / 2, speedAnim, (long) (speedAnim * pause * 2));

//                ObjectAnimator.ofFloat(viewA, "y", animStepY - smallViewSize / 2);
//        action3.setDuration(speedAnim);
//        action3.setStartDelay((long) (speedAnim * pause * 2));
//        action3.addUpdateListener(this);

        ObjectAnimator action4 =  getObjectAnimator(viewB, "x", (int) (viewB.getX() - animStepX + smallViewSize / 2), speedAnim, (long) (speedAnim * pause * 3));

//                ObjectAnimator.ofFloat(viewB, "x", viewB.getX() - animStepX + smallViewSize / 2);
//        action4.setDuration(speedAnim);
//        action4.setStartDelay((long) (speedAnim * pause * 3));
//        action4.addUpdateListener(this);

        ObjectAnimator action5 =  getObjectAnimator(viewC, "x", (int) smallViewCenterX, speedAnim, (long) (speedAnim * pause * 4));
//        ObjectAnimator.ofFloat(viewC, "x", smallViewCenterX);
//        action5.setDuration(speedAnim);
//        action5.setStartDelay((long) (speedAnim * pause * 4));
//        action5.addUpdateListener(this);

        ObjectAnimator action6 = getObjectAnimator(viewD, "y", (int) smallViewCenterY, speedAnim, (long) (speedAnim * pause * 5));

//        ObjectAnimator.ofFloat(viewD, "y", smallViewCenterY);
//        action6.setDuration(speedAnim);
//        action6.setStartDelay((long) (speedAnim * pause * 5));
//        action6.addUpdateListener(this);

        ObjectAnimator action7 = getObjectAnimator(viewA, "x", (int) smallViewCenterX, speedAnim, (long) (speedAnim * pause * 6));
//                ObjectAnimator.ofFloat(viewA, "x", smallViewCenterX);
//        action7.setDuration(speedAnim);
//        action7.setStartDelay((long) (speedAnim * pause * 6));
//        action7.addUpdateListener(this);

        ObjectAnimator action8 = getObjectAnimator(viewB, "y", (int) smallViewCenterY, (long) (speedAnim * pause * 7), (long) (speedAnim * pause * 7));
//                ObjectAnimator.ofFloat(viewB, "y", smallViewCenterY);
//        action8.setDuration(speedAnim);
//        action8.setStartDelay((long) (speedAnim * pause * 7));
//        action8.addUpdateListener(this);

        ObjectAnimator animA = getObjectAnimatorAlpha(viewA, 1.0f, 0.0f);

        ObjectAnimator animB = getObjectAnimatorAlpha(viewB, 1.0f, 0.0f);
//                ObjectAnimator.ofFloat(viewB, View.ALPHA, 1.0f, 0.0f, 1.0f);
//        animB.setDuration((long) (speedAnim * pause * 6));
//        animB.setRepeatCount(3);

        ObjectAnimator animC = getObjectAnimatorAlpha(viewC, 1.0f, 0.0f);
//                ObjectAnimator.ofFloat(viewC, View.ALPHA, 1.0f, 0.0f, 1.0f);
//        animC.setDuration((long) (speedAnim * pause * 6));
//        animC.setRepeatCount(3);

        ObjectAnimator animD = getObjectAnimatorAlpha(viewD, 1.0f, 0.0f);

//                ObjectAnimator.ofFloat(viewD, View.ALPHA, 1.0f, 0.0f, 1.0f);
//        animD.setDuration((long) (speedAnim * pause * 6));
//        animD.setRepeatCount(3);

        ObjectAnimator actionBack1 = getObjectAnimator(viewC, "x", viewWidth - smallViewSize, speedAnim, 0);
//                ObjectAnimator.ofFloat(viewC, "x", viewWidth - smallViewSize);
//        actionBack1.setDuration(speedAnim);
//        actionBack1.addUpdateListener(this);

        ObjectAnimator actionBack2 = getObjectAnimator(viewD, "y", viewHeight - smallViewSize, speedAnim, (long) (speedAnim * pause));

//                ObjectAnimator.ofFloat(viewD, "y", viewHeight - smallViewSize);
//        actionBack2.setDuration(speedAnim);
//        actionBack2.setStartDelay((long) (speedAnim * pause));
//        actionBack2.addUpdateListener(this);

        ObjectAnimator actionBack3 = getObjectAnimator(viewA, "x", (int) (smallViewCenterX - viewWidth / 2 + smallViewSize / 2), speedAnim, (long) (speedAnim * pause * 3));

//                ObjectAnimator.ofFloat(viewA, "x", smallViewCenterX - viewWidth / 2 + smallViewSize / 2);
//        actionBack3.setDuration(speedAnim);
//        actionBack3.setStartDelay((long) (speedAnim * pause * 3));
//        actionBack3.addUpdateListener(this);

        ObjectAnimator actionBack4 = getObjectAnimator(viewB, "y", (int) (smallViewCenterY - viewHeight / 2 + smallViewSize / 2), speedAnim, (long) (speedAnim * pause * 4));

//        ObjectAnimator.ofFloat(viewB, "y", smallViewCenterY - viewHeight / 2 + smallViewSize / 2);
//        actionBack4.setDuration(speedAnim);
//        actionBack4.setStartDelay((long) (speedAnim * pause * 4));
//        actionBack4.addUpdateListener(this);

        ObjectAnimator actionBack5 = getObjectAnimator(viewC, "y", (int) viewHeight - smallViewSize, speedAnim, (long) (speedAnim * pause * 5));

//        ObjectAnimator.ofFloat(viewC, "y", viewHeight - smallViewSize);
//        actionBack5.setDuration(speedAnim);
//        actionBack5.setStartDelay((long) (speedAnim * pause * 5));
//        actionBack5.addUpdateListener(this);

        ObjectAnimator actionBack6 = getObjectAnimator(viewD, "x", (int) (smallViewCenterX - viewWidth / 2 + smallViewSize / 2), speedAnim, (long) ( speedAnim * pause * 6.5));
//                ObjectAnimator.ofFloat(viewD, "x", smallViewCenterX - viewWidth / 2 + smallViewSize / 2);
//        actionBack6.setDuration(speedAnim);
//        actionBack6.setStartDelay((long) (speedAnim * pause * 6.5));
//        actionBack6.addUpdateListener(this);

        ObjectAnimator actionBack7 = getObjectAnimator(viewA, "y", (int) smallViewCenterY - viewHeight / 2 + smallViewSize / 2, speedAnim, (long) (speedAnim * pause * 7.5));
//                ObjectAnimator.ofFloat(viewA, "y", smallViewCenterY - viewHeight / 2 + smallViewSize / 2);
//        actionBack7.setDuration(speedAnim);
//        actionBack7.setStartDelay((long) (speedAnim * pause * 7.5));
//        actionBack7.addUpdateListener(this);

        ObjectAnimator actionBack8 = getObjectAnimator(viewB, "x", (int) viewWidth - smallViewSize, speedAnim, (long) (speedAnim * pause * 8.5));

//        ObjectAnimator.ofFloat(viewB, "x", viewWidth - smallViewSize);
//        actionBack8.setDuration(speedAnim);
//        actionBack8.setStartDelay((long) (speedAnim * pause * 8.5));
//        actionBack8.addUpdateListener(this);

//        ObjectAnimator alphaAnimEnd = ObjectAnimator.ofFloat(viewE, View.ALPHA, 0.0f, 1.0f, 1.0f);
//        alphaAnim.setDuration((long) (speedAnim * 2));
//        alphaAnim.setRepeatCount(4);

        AnimatorSet animSet = new AnimatorSet();
        animSet.play(action1).with(action2).with(action3).with(action4)
                .with(action5).with(action6).with(action7).with(action8);
        animSet.play(alphaAnim).before(action1);
        animSet.play(animA).with(animB).with(animC).with(animD).after(action8);
        animSet.play(actionBack1).with(actionBack2).with(actionBack3).with(actionBack4)
                .with(actionBack5).with(actionBack6).with(actionBack7).with(actionBack8).after(animD);
      //  animSet.play(alphaAnimEnd).after(actionBack8);

      //  animSet.setDuration(speedAnim);
        animSet.addListener(this);
        animSet.start();
    }

    @NonNull
    private ObjectAnimator getObjectAnimatorAlpha(View viewA, float v, float v2) {
        ObjectAnimator animA = ObjectAnimator.ofFloat(viewA, View.ALPHA, v, v2, v);
        animA.setDuration((long) (speedAnim * pause * 6));
        animA.setRepeatCount(3);
        return animA;
    }

    @NonNull
    private ObjectAnimator getObjectAnimator(View viewD, String x, int i, long speedAnim, long startDelay) {
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

    private float converter(long delay) {
        return delay / 1500;
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
        if(runProgress){
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

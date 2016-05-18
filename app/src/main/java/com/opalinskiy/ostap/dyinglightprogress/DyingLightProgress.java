package com.opalinskiy.ostap.dyinglightprogress;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
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
    private int startOffset;

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
        smallViewSize = Math.min(w, h) / 12;
        bigViewSize = Math.min(w, h) / 4;

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
        int viewSize = 30;

        viewA = new View(context);
        viewB = new View(context);
        viewC = new View(context);
        viewD = new View(context);
        viewE = new View(context);

        ObjectAnimator alfaAnim = ObjectAnimator.ofFloat(viewE, View.ALPHA, 0.0f, 1f, 0.0f);
        alfaAnim.setDuration(2500);
        alfaAnim.setRepeatCount(4);
//        alfaAnim.start();
        int distanceBetween = (int) (viewC.getX() - viewD.getX());
        float newX = (((viewC.getX() + viewC.getWidth() / 2) - (viewD.getX() + viewD.getWidth() / 2))) / 6;
        Log.d("TAG", "offset: " + newX);
        Log.d("TAG", "distance cd: " + distanceBetween);
        ObjectAnimator animX = ObjectAnimator.ofFloat(viewD, "x", newX);
        animX.addUpdateListener(this);

        // ObjectAnimator animY = ObjectAnimator.ofFloat(viewA, "y", 400f);

//        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                invalidate();
//            }
//        });
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(alfaAnim);
        animSet.play(animX).after(alfaAnim);
        animSet.start();
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

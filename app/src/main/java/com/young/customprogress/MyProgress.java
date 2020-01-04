package com.young.customprogress;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;

public class MyProgress extends View {

    private Paint bgPaint;
    private Paint progressPaint;
    private Paint tipPaint;
    private Paint textPaint;

    private int mWidth;
    private int mHeight;
    private int mViewHeight;

    private float mProgress;
    private float currentProgress;
    private int duration = 10000;
    private int startDelay = 10000;

    private ValueAnimator progressAnimator;
    private ProgressListener progressListener;

    private int progressPaintWidth;
    private int tipPaintWidth;
    private int tipHeight;
    private int tipWidth;
    private int triangleHeight;
    private int progressMarginTop;
    private int roundRect;
    private int textPaintSize;
    private float moveDis;

    private Path path = new Path();
    private RectF rectF = new RectF();
    private String textString = "0";
    private Rect textRect = new Rect();


    public MyProgress(Context context) {
        super(context);
        init();
        initPaint();
    }

    public MyProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        initPaint();
    }

    public MyProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initPaint();
    }

    private void init(){
        progressPaintWidth = dp2px(4);
        tipHeight = dp2px(15);
        tipWidth = dp2px(30);
        tipPaintWidth = dp2px(1);
        triangleHeight = dp2px(3);
        roundRect = dp2px(2);
        progressMarginTop = dp2px(8);
        textPaintSize = sp2px(10);

        Log.d("young","tipwidth: "+tipWidth);

    }

    private void initPaint() {
        mViewHeight = progressPaintWidth+progressMarginTop+tipHeight+tipPaintWidth+triangleHeight;
        bgPaint = getPaint(progressPaintWidth, Color.DKGRAY,Paint.Style.STROKE);
        progressPaint = getPaint(progressPaintWidth,Color.RED, Paint.Style.STROKE);
        tipPaint = getPaint(tipPaintWidth,Color.RED, Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textPaintSize);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private Paint getPaint(int width, int color, Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(style);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(measureWidth(widthMode, width),measureHeight(heightMode,height));
    }

    private int measureWidth(int widthMode, int width) {
        switch (widthMode){
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                mWidth = width;
                break;
        }
        return mWidth;
    }

    private int measureHeight(int heightMode, int height) {
        switch (heightMode){
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                mHeight = mViewHeight;
                break;
            case MeasureSpec.EXACTLY:
                mHeight = height;
                break;
        }
        return mHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(getPaddingLeft(),tipHeight+progressMarginTop,getWidth(),tipHeight+progressMarginTop,bgPaint);
        canvas.drawLine(getPaddingLeft(),tipHeight+progressMarginTop,currentProgress,tipHeight+progressMarginTop,progressPaint);

        drawTipView(canvas);
        drawTriangle(canvas);
        drawText(canvas,textString);
    }

    private void drawText(Canvas canvas, String textString) {
        textRect.left = (int) moveDis;
        textRect.top = 0;
        textRect.right = (int) (tipWidth+moveDis);
        textRect.bottom = tipHeight;
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        int baseLine = (textRect.bottom+textRect.top-fontMetrics.bottom-fontMetrics.top)/2;
        canvas.drawText(textString+"%",textRect.centerX(),baseLine,textPaint);
    }

    private void drawTriangle(Canvas canvas) {
        path.moveTo(tipWidth/2-triangleHeight+moveDis,tipHeight);
        path.lineTo(tipWidth/2+moveDis,tipHeight+triangleHeight);
        path.lineTo(tipWidth/2+triangleHeight+moveDis,tipHeight);
        canvas.drawPath(path,tipPaint);
        path.reset();
    }

    private void drawTipView(Canvas canvas) {
        rectF.set(moveDis,0,tipWidth+moveDis,tipHeight);
        canvas.drawRoundRect(rectF,roundRect,roundRect,tipPaint);
    }

    private int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,getResources().getDisplayMetrics());
    }
    private int sp2px(int sp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,getResources().getDisplayMetrics());
    }

    public MyProgress setProgressWithAnimation(float progress){
        mProgress = progress;
        initAnimation();
        return this;
    }

    private void initAnimation(){
        progressAnimator = ValueAnimator.ofFloat(0,mProgress);
        progressAnimator.setDuration(duration);
        progressAnimator.setStartDelay(startDelay);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                textString = formatNum(value);
                currentProgress = value*mWidth/100;
                if(progressListener !=null){
                    progressListener.currentProgressListener(currentProgress);
                }
                if(currentProgress >=(tipWidth/2) && currentProgress<= mWidth-(tipWidth/2)){
                    moveDis = currentProgress-(tipWidth/2);
                }
                invalidate();
                //setCurrentProgress(value);
            }
        });
        progressAnimator.start();
    }

    public interface ProgressListener{
        void currentProgressListener(float currentProgress);
    }

    public MyProgress setProgressListener(ProgressListener listener){
        progressListener = listener;
        return this;
    }

    public MyProgress setCurrentProgress(float progress){
        mProgress = progress;
        currentProgress = mProgress*mWidth/100;
        textString = formatNum(mProgress);
        if(currentProgress >=(tipWidth/2) && currentProgress<= mWidth-(tipWidth/2)){
            moveDis = currentProgress-(tipWidth/2);
        }
        invalidate();
        return this;
    }

    public static String formatNum(float progress){
        DecimalFormat decimalFormat = new DecimalFormat("0");
        return  decimalFormat.format(progress);
    }
}

package com.lxy.ball.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.lxy.ball.R;

/**
 * Created by lxy on 2017/6/5.
 */

public class BitmapView extends View {

    private Paint mPaint;
    private Bitmap mBitmap;
    private int mDistance;

    public BitmapView(Context context) {
        this(context, null);
    }

    public BitmapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BitmapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPaint(context);
        initAnimator();
    }

    private void initAnimator() {
        ValueAnimator animator = ValueAnimator.ofInt(1, 200);
        animator.setDuration(500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDistance = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });

        animator.start();
    }

    private void initPaint(Context context) {

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(20);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.long2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        int x = getWidth() / 2;
        int y = getHeight() / 2 + mDistance;

        //Rect rect = new Rect(width - 100, height - 100 + mDistance, imgWidth, imgHeight);

        //canvas.drawCircle(width, height + mDistance, 20, mPaint);
        //canvas.drawBitmap(mBitmap, null, rect, mPaint);

        canvas.translate(x, y);
        Rect rect = new Rect(0, 0, 100, 100);
        canvas.drawBitmap(mBitmap, null, rect, mPaint);
       // canvas.save();
       // canvas.restore();

    }
}

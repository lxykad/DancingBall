package com.lxy.ball.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;

import com.lxy.ball.DancingInterpolator;
import com.lxy.ball.R;

/**
 * Created by lxy on 2017/6/3.
 */

public class DancingBall extends SurfaceView implements SurfaceHolder.Callback {

    public static final int STATE_DOWN = 1;//向下状态
    public static final int STATE_UP = 2;//向上状态

    public static final int DEFAULT_POINT_RADIUS = 10;
    public static final int DEFAULT_BALL_RADIUS = 13;
    public static final int DEFAULT_LINE_WIDTH = 500;
    public static final int DEFAULT_LINE_HEIGHT = 20;
    public static final int DEFAULT_LINE_COLOR = Color.parseColor("#FF9800");
    public static final int DEFAULT_POINT_COLOR = Color.parseColor("#9C27B0");
    public static final int DEFAULT_BALL_COLOR = Color.parseColor("#FF4081");

    public static final int DEFAULT_DOWN_DURATION = 600;//ms
    public static final int DEFAULT_UP_DURATION = 600;//ms
    public static final int DEFAULT_FREEDOWN_DURATION = 1000;//ms

    public static final int MAX_OFFSET_Y = 80;//水平下降最大偏移距离

    public int PONIT_RADIUS = DEFAULT_POINT_RADIUS;//圆固定点半径
    public int BALL_RADIUS = DEFAULT_BALL_RADIUS;//小球半径

    private Paint mPaint;
    private Path mPath;
    private int mLineColor;
    private int mPonitColor;
    private int mBallColor;
    private int mLineWidth;
    private int mLineHeight;

    private float mDownDistance;
    private float mUpDistance;
    private float freeBallDistance;

    private ValueAnimator mDownAnimator;//下落控制器
    private ValueAnimator mUpAnimator;//上弹控制器
    private ValueAnimator mFreeDownAnimator;//自由落体控制器

    private AnimatorSet animatorSet;
    private int mAnimatorState;

    private boolean ismUpControllerDied = false;
    private boolean isAnimationShowing = false;
    private boolean isBounced = false;//是否分离
    private boolean isBallFreeUp = false;
    private Bitmap mBitmap;
    private Rect mRect;

    private int IMG_WIDTH = 150;
    private int IMG_HEIGHT = 150;

    public DancingBall(Context context) {
        this(context, null);
    }

    public DancingBall(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DancingBall(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initBitmap(context);
        initAttrs(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mLineHeight);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPath = new Path();
        getHolder().addCallback(this);

        initAnimator();
    }

    private void initBitmap(Context context) {

        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.long2);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DancingBall);

        if (typedArray != null) {
            mBallColor = typedArray.getColor(R.styleable.DancingBall_ball_color, DEFAULT_BALL_COLOR);

            mLineColor = typedArray.getColor(R.styleable.DancingBall_lineColor, DEFAULT_LINE_COLOR);
            mLineWidth = typedArray.getDimensionPixelOffset(R.styleable.DancingBall_lineWidth, DEFAULT_LINE_WIDTH);
            mLineHeight = typedArray.getDimensionPixelOffset(R.styleable.DancingBall_lineHeight, DEFAULT_LINE_HEIGHT);
            mPonitColor = typedArray.getColor(R.styleable.DancingBall_pointColor, DEFAULT_POINT_COLOR);

            typedArray.recycle();
        }
    }

    private void initAnimator() {
        //下落动画
        mDownAnimator = ValueAnimator.ofFloat(0, 1);
        mDownAnimator.setDuration(DEFAULT_DOWN_DURATION);
        mDownAnimator.setInterpolator(new DecelerateInterpolator());
        mDownAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDownDistance = MAX_OFFSET_Y * (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mDownAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatorState = STATE_DOWN;
                System.out.println("DancingBall==========mDownAnimatorStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                System.out.println("DancingBall==========mDownAnimatorEnd");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        //上抛动画
        mUpAnimator = ValueAnimator.ofFloat(0, 1);
        mUpAnimator.setDuration(DEFAULT_UP_DURATION);
        mUpAnimator.setInterpolator(new DancingInterpolator());
        mUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mUpDistance = MAX_OFFSET_Y * (float) animation.getAnimatedValue();

                if (mUpDistance >= MAX_OFFSET_Y) {
                    //和绳子脱离 进入自由落体状态
                    isBounced = true;
                    if (mFreeDownAnimator != null && !mFreeDownAnimator.isRunning() && !mFreeDownAnimator.isStarted() && !isBallFreeUp) {
                        mFreeDownAnimator.start();
                    }
                }

                postInvalidate();
            }
        });
        mUpAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatorState = STATE_UP;
                System.out.println("DancingBall=========================mUpAnimatorStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ismUpControllerDied = true;
                System.out.println("DancingBall=========================mUpAnimatorEnd");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        //自由落体
        mFreeDownAnimator = ValueAnimator.ofFloat(0, 8f);
        mFreeDownAnimator.setDuration(DEFAULT_FREEDOWN_DURATION);
        mFreeDownAnimator.setInterpolator(new DecelerateInterpolator());

        mFreeDownAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //该公式解决上升减速 和 下降加速
                float t = (float) animation.getAnimatedValue();
                freeBallDistance = 40 * t - 5 * t * t;

                if (ismUpControllerDied) {//往上抛,到临界点
                    postInvalidate();
                }
            }
        });

        mFreeDownAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isBallFreeUp = true;
                System.out.println("DancingBall=========================mFreeDownAnimatorStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                System.out.println("DancingBall=========================mFreeDownAnimatorStartEnd");
                isAnimationShowing = false;
                //循环第二次
                startAnimations();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        //
        animatorSet = new AnimatorSet();
        animatorSet.play(mDownAnimator).before(mUpAnimator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimationShowing = true;
                // System.out.println("DancingBall==========onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //System.out.println("DancingBall==========onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //System.out.println("DancingBall==========onAnimationRepeat");
            }
        });

        animatorSet.start();

    }

    /**
     * 开启动画
     */
    private void startAnimations() {

        System.out.println("DancingBall=========================startAnimations11");
        if (isAnimationShowing) {
            return;
        }
        System.out.println("DancingBall=========================startAnimations22");
        if (animatorSet.isRunning()) {
            animatorSet.end();
            animatorSet.cancel();
        }
        isBounced = false;
        isBallFreeUp = false;
        ismUpControllerDied = false;

        animatorSet.start();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*mPaint.setColor(mPonitColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth() / 2 - mLineWidth / 2, getHeight() / 2, PONIT_RADIUS, mPaint);
        canvas.drawCircle(getWidth() / 2 + mLineWidth / 2, getHeight() / 2, PONIT_RADIUS, mPaint);*/

        canvas.save();
        //左右两条贝塞尔曲线构成该条绳子
        mPaint.setColor(mLineColor);
        mPath.reset();

        //move到起始点
        mPath.moveTo(getWidth() / 2 - mLineWidth / 2, getHeight() / 2);

        if (mAnimatorState == STATE_DOWN) {//下落
            // System.out.println("DancingBall==========down");
            //绘制左边的绳子
            mPath.quadTo((float) (getWidth() / 2 - mLineWidth / 2 + mLineWidth * 0.375), getHeight() / 2 + mDownDistance,
                    getWidth() / 2, getHeight() / 2 + mDownDistance);

            //绘制右边的绳子
            mPath.quadTo((float) (getWidth() / 2 + mLineWidth / 2 - mLineWidth * 0.375), getHeight() / 2 + mDownDistance,
                    getWidth() / 2 + mLineWidth / 2, getHeight() / 2);

            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);

            //绘制小球
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mBallColor);
            //canvas.drawCircle(getWidth() / 2, getHeight() / 2 + mDownDistance - BALL_RADIUS, BALL_RADIUS, mPaint);


            //绘制图片
            int x = getWidth() / 2 - IMG_WIDTH / 2;
            int y = (int) (getHeight() / 2 + mDownDistance) - IMG_HEIGHT - DEFAULT_LINE_HEIGHT;
            canvas.translate(x, y);
            mRect = new Rect(0, 0, IMG_WIDTH, IMG_HEIGHT);
            canvas.drawBitmap(mBitmap, null, mRect, mPaint);


        } else if (mAnimatorState == STATE_UP) {//上弹
            /**************绘制绳子开始*************/
            //左边的绳子
            mPath.quadTo((float) (getWidth() / 2 - mLineWidth / 2 + mLineWidth * 0.375), getHeight() / 2 + MAX_OFFSET_Y - mUpDistance,
                    getWidth() / 2,
                    getHeight() / 2 + (MAX_OFFSET_Y - mUpDistance));

            //右边的绳子
            mPath.quadTo((float) (getWidth() / 2 + mLineWidth / 2 - mLineWidth * 0.375), getHeight() / 2 + MAX_OFFSET_Y - mUpDistance,
                    getWidth() / 2 + mLineWidth / 2,
                    getHeight() / 2);

            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);
            /**************绘制绳子结束*************/

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mBallColor);

            //弹性小球,自由落体
            if (!isBounced) {
                //上升--小球
                //canvas.drawCircle(getWidth() / 2, getHeight() / 2 + (MAX_OFFSET_Y - mUpDistance) - BALL_RADIUS, BALL_RADIUS, mPaint);

                //绘制图片
                int x = getWidth() / 2 - IMG_WIDTH / 2;
                int y = (int) (getHeight() / 2 + MAX_OFFSET_Y - mUpDistance) - IMG_HEIGHT - DEFAULT_LINE_HEIGHT;
                canvas.translate(x, y);
                mRect = new Rect(0, 0, IMG_WIDTH, IMG_HEIGHT);

                canvas.drawBitmap(mBitmap, null, mRect, mPaint);


            } else {
                //自由落体--小球
                //canvas.drawCircle(getWidth() / 2, getHeight() / 2 - freeBallDistance - BALL_RADIUS, BALL_RADIUS, mPaint);

                //绘制图片
                int x = getWidth() / 2 - IMG_WIDTH / 2;
                int y = (int) (getHeight() / 2 - freeBallDistance) - IMG_HEIGHT - DEFAULT_LINE_HEIGHT;
                canvas.translate(x, y);
                mRect = new Rect(0, 0, IMG_WIDTH, IMG_HEIGHT);

                canvas.drawBitmap(mBitmap, null, mRect, mPaint);

            }

        }

        canvas.restore();
        mPaint.setColor(mPonitColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth() / 2 - mLineWidth / 2, getHeight() / 2, PONIT_RADIUS, mPaint);
        canvas.drawCircle(getWidth() / 2 + mLineWidth / 2, getHeight() / 2, PONIT_RADIUS, mPaint);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //锁定整个SurfaceView对象,获取该Surface上的Canvas
        Canvas canvas = holder.lockCanvas();
        draw(canvas);
        //释放画布，提交修改
        holder.unlockCanvasAndPost(canvas);

        //System.out.println("DancingBall==========surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void drawLong() {

    }
}

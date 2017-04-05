package com.netease.cardstack.stack.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.netease.cardstack.stack.SimpleTorque;
import com.netease.cardstack.stack.animation.Animation;
import com.netease.cardstack.stack.animation.Animation.AnimationListener;
import com.netease.cardstack.stack.animation.AnimationManager;
import com.netease.cardstack.stack.animation.FloatAnimation;
import com.netease.cardstack.stack.animation.GravityAnimation;

public class LayerGestureView extends GestureView implements AnimationListener {

    private static final String TAG = "LayerGestureView";

    private static final boolean BLOCK_MULTIPLE_TOUCH = true;

    private static final float DEFAULT_GRAVITY = 10 * 0.001f;
    private static final long DEFAULT_DURATION = 400;

    private static final int ID_ANIMATION_X = 0x01;
    private static final int ID_ANIMATION_Y = 0x02;
    private static final int ID_ANIMATION_ROTATE = 0x04;
    private static final int ID_ANIMATION_CANCEL = 0x08;

    private static final int ID_ANIMATION_NONE = 0x0;

    private static final int ID_GRAVITY_X = 0x10;
    private static final int ID_GRAVITY_Y = 0x20;

    Matrix matrix = new Matrix();
    Bitmap bitmap = null;
    private float[] values; // x,y, scale, rotate
    private float[] anchor; // x,y, scale, rotate
    private View slideView;
    private LayerGestureListener slideListener;

    SimpleTorque simpleTorque;

    private FloatAnimation xFloatAnimation;
    private FloatAnimation yFloatAnimation;
    private FloatAnimation rotateFloatAnimation;
    private FloatAnimation cancelFloatAnimation;

    private GravityAnimation yGravityAnimation;
    private GravityAnimation xGravityAnimation;

    private AnimationManager animationManager = new AnimationManager();

    private int slideWidth; //
    private int slideHeight;
    private float slideX;
    private float slideY;
    private boolean slideTouch = false;
    private RectF slideRect = new RectF();
    private Bitmap slideBitmap = null;
    private boolean isFling = false;
    private boolean lockTouch = false;
    private boolean touchDown;
    private boolean touchMove;
    private float radius; // slide view's

    private long duration = DEFAULT_DURATION;
    private float gravity = DEFAULT_GRAVITY;

    private int flagAnimation = ID_ANIMATION_NONE;

    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

    private RectF tmpRect = new RectF();

    public LayerGestureView(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LayerGestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayerGestureView(Context context) {
        super(context);
    }

    protected void init() {
        super.init();
        values = new float[5];
        values[INDEX_SCALE] = 1;

        anchor = new float[4];

        simpleTorque = new SimpleTorque(getContext());

        xFloatAnimation = new FloatAnimation(ID_ANIMATION_X);
        yFloatAnimation = new FloatAnimation(ID_ANIMATION_Y);
        rotateFloatAnimation = new FloatAnimation(ID_ANIMATION_ROTATE);
        cancelFloatAnimation = new FloatAnimation(ID_ANIMATION_CANCEL);

        xFloatAnimation.listener = this;
        yFloatAnimation.listener = this;
        rotateFloatAnimation.listener = this;
        cancelFloatAnimation.listener = this;

        xGravityAnimation = new GravityAnimation(ID_GRAVITY_X);
        yGravityAnimation = new GravityAnimation(ID_GRAVITY_Y);

        xGravityAnimation.listener = this;
        yGravityAnimation.listener = this;
    }

    /**
     * @param gravity  默认是10m/s
     * @param duration 动画持续时间
     */
    public void updateSlideSetting(int gravity, long duration) {
        this.gravity = gravity * 0.001f;
        this.duration = duration;
    }

    public void bindSlideViews(View slideView,
                               LayerGestureListener slideListener) {
        this.slideView = slideView;
        this.slideListener = slideListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (lockTouch) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (lockTouch) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        boolean down = super.onDown(e);
        resetTouch();
        return down;
    }

    private void resetTouch() {
        touchDown = true;
        touchMove = false;
        cancelAnimation(false);
    }

    public Rect getSlideViewRect() {
        int[] location = new int[2];
        slideView.getLocationOnScreen(location);
        return new Rect(location[0], location[1], location[0] + slideView.getWidth(), location[1] + slideView.getHeight());
    }

    private boolean slideViewContainTouch(float[] touchXY) {
        Log.d(TAG, "slideViewContainTouch:");
        int[] location = new int[2];
        slideView.getLocationOnScreen(location);
        slideX = location[0];
        slideY = location[1];

        slideWidth = slideView.getWidth();
        slideHeight = slideView.getHeight();

        radius = (float) Math.sqrt(slideWidth * slideWidth + slideHeight
                * slideHeight) * 0.5f;

        getLocationOnScreen(location);
        slideX -= location[0];
        slideY -= location[1];

        float x = touchX - slideX;
        float y = touchY - slideY;

        slideRect.right = slideWidth;
        slideRect.bottom = slideHeight;
        boolean containTouch = true;
        if (!slideRect.contains(x, y)) {
            containTouch = false;
        }
        if (touchXY != null) {
            touchXY[0] = x;
            touchXY[1] = y;
        }
        return containTouch;
    }

    private void slideViewDown() {

        float[] touchXY = new float[2];
        if (!slideViewContainTouch(touchXY)) {
            touchDown = false;
            touchMove = false;
            return;
        }
        touchDown = true;
        touchMove = true;
        slideListener.onLayerGesture(GestureStatus.TouchStart);

        createSlideBitmap();

        simpleTorque.updateSize(slideWidth, slideHeight)
                .updateTouch(touchXY[0], touchXY[1]).start();

        animationManager.reset(false);
        isFling = false;
        slideTouch = true;
        values[INDEX_X] = 0;
        values[INDEX_Y] = 0;
        values[INDEX_ROTATE] = 0;
        anchor[INDEX_X] = slideWidth * 0.5f;
        anchor[INDEX_Y] = slideHeight * 0.5f;
    }

    private void createSlideBitmap() {
        if (slideBitmap == null || slideBitmap.getWidth() != slideWidth
                || slideBitmap.getHeight() != slideHeight) {
            if (slideBitmap != null) {
                slideBitmap.recycle();
            }
            try {
                slideBitmap = Bitmap.createBitmap(slideWidth, slideHeight, Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                try {
                    slideBitmap = Bitmap.createBitmap(slideWidth / 4, slideHeight / 4, Config.ARGB_4444);
                } catch (OutOfMemoryError e2) {
                }
            }
        } else {
            Canvas canvas = new Canvas(slideBitmap);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        Canvas canvas = new Canvas(slideBitmap);
        slideView.draw(canvas);
    }

    private void slideViewBack() {
        PointF point = simpleTorque.getUpdateTouch();
        anchor[INDEX_X] = point.x;
        anchor[INDEX_Y] = point.y;

        simpleTorque.stop();

        int degrees = (int) values[INDEX_ROTATE];
        degrees = adjustBackDegrees(degrees);
        values[INDEX_ROTATE] = degrees;

        xFloatAnimation.startAnimation(values, INDEX_X, values[INDEX_X], 0,
                duration, accelerateInterpolator);
        yFloatAnimation.startAnimation(values, INDEX_Y, values[INDEX_Y], 0,
                duration, accelerateInterpolator);
        rotateFloatAnimation.startAnimation(values, INDEX_ROTATE,
                values[INDEX_ROTATE], 0, duration, accelerateInterpolator);

        cancelFloatAnimation.startAnimation(values, INDEX_CANCEl,
                values[INDEX_CANCEl], 0, duration, accelerateInterpolator);

        animationManager.addAnimation(xFloatAnimation);
        animationManager.addAnimation(yFloatAnimation);
        animationManager.addAnimation(rotateFloatAnimation);
        animationManager.addAnimation(cancelFloatAnimation);

        flagAnimation = ID_ANIMATION_CANCEL;
    }

    private int adjustBackDegrees(int pDegrees) {
        while (pDegrees < 0) {
            pDegrees += 360;
        }
        while (pDegrees > 360) {
            pDegrees -= 360;
        }
        if (pDegrees > 180) {
            pDegrees = pDegrees - 360;
        }
        return pDegrees;
    }

    @Override
    protected void onUp(MotionEvent event) {
        super.onUp(event);

        slideTouch = false;
        Log.d(TAG, "onUp:" + isFling);
//		if (isFling){
//			Log.i("xie module","eventx"+event.getX()+" eventxy"+ event.getY());
//		}
        if (touchMove) {
            if (!isFling) {
                slideViewBack();
            } else {
                slideListener.onFinishUp(event);
            }

        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        Log.d(TAG, "onScroll:");
        if (!touchMove) {
            slideViewDown();

        }

        if (!touchDown) {
            return false;
        }

        slideListener.onLayerGesture(GestureStatus.TouchMoveBefore);

        values[INDEX_X] -= distanceX;
        values[INDEX_Y] -= distanceY;

        slideListener.onLayerGesture(GestureStatus.TouchMove);

        if (e1 == null || e2 == null) {
            return true;
        }
        simpleTorque.applyDragVelocity(-distanceX, -distanceY);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        Log.d(TAG, "onFling:");
        if (!touchDown) {
            return false;
        }
        slideListener.onLayerGesture(GestureStatus.FlingStart);
        isFling = true;
        float secondsToMs = 1000;

        simpleTorque.flingEnable();
        PointF center = simpleTorque.getCenter();
        anchor[INDEX_X] = center.x;
        anchor[INDEX_Y] = center.y;

        PointF point = simpleTorque.getCenter();
        PointF pc = new PointF(point.x, point.y);
        point = simpleTorque.getRotateCenter();
        PointF pn = new PointF(point.x, point.y);
        values[INDEX_X] += (pn.x - pc.x);
        values[INDEX_Y] += (pn.y - pc.y);

        xGravityAnimation.startAnimation(values, INDEX_X, 0, velocityX
                / secondsToMs);
        yGravityAnimation.startAnimation(values, INDEX_Y, gravity, velocityY
                / secondsToMs);

        animationManager.addAnimation(xGravityAnimation);
        animationManager.addAnimation(yGravityAnimation);

        if (BLOCK_MULTIPLE_TOUCH) {
            lockTouch = true;
            postDelayed(new Runnable() {

                @Override
                public void run() {
                    lockTouch = false;
                }
            }, (int) secondsToMs / 3);
        }

        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (touchDown && slideViewContainTouch(null)) {
            slideListener.onLayerViewClick();
            return true;
        }
        return false;
    }

    public void scaleTime(float scaleTime) {
        xGravityAnimation.scaleTime(scaleTime);
        yGravityAnimation.scaleTime(scaleTime);
        simpleTorque.scaleTime(scaleTime);
    }

    String demoString;
    int count = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        if (!slideTouch && !animationManager.hasMore()) {
            return;
        }
        if (slideBitmap == null) {
            return;
        }

        animationManager.step();

        float anchorX = 0;
        float anchorY = 0;

        if (simpleTorque.animation()) {
            simpleTorque.update();
            if (isFling) {
                anchorX = anchor[INDEX_X];
                anchorY = anchor[INDEX_Y];
                // values[INDEX_ROTATE] = (float)
                // simpleTorque.getCenterRotateAngularDegrees();
            } else {
                PointF point = simpleTorque.getUpdateTouch();
                anchorX = point.x;
                anchorY = point.y;
            }
            values[INDEX_ROTATE] = (float) simpleTorque
                    .getRotateAngularDegrees();
        } else {
            anchorX = anchor[INDEX_X];
            anchorY = anchor[INDEX_Y];
        }
        matrix.reset();

        matrix.preTranslate(-anchorX, -anchorY);
        float scale = values[INDEX_SCALE];
        matrix.postScale(scale, scale);
        matrix.postRotate(values[INDEX_ROTATE]);

        float dx = anchorX * scale;
        float dy = anchorY * scale;

        matrix.postTranslate(slideX + dx + values[INDEX_X], slideY + dy
                + values[INDEX_Y]);

        canvas.drawBitmap(slideBitmap, matrix, bmpPaint);

        updateFling();
        invalidate();

    }

    // int

    private void updateFling() {
        if (!isFling) {
            return;
        }

        // 判断slideview是否在屏幕中
        float cy = slideY + values[INDEX_Y] + anchor[INDEX_Y] - radius;
        if (cy > getHeight()) {
//			Log.i("xie module","cy"+cy+" getHeight()"+ getHeight());
//			Log.i("xie module","slidex"+slideX+" getWidth()"+ getWidth());
            autoFinishFling();
        }
    }

    private void autoFinishFling() {
        animationManager.reset(false);
        post(flingFinish);
    }

    public static final int INDEX_X = 0;
    public static final int INDEX_Y = 1;
    public static final int INDEX_SCALE = 2;
    public static final int INDEX_ROTATE = 3;
    public static final int INDEX_CANCEl = 4;
    public static final int INDEX_ALPHA = 0;

    @Override
    public void onAnimationFinish(Animation animation) {
        switch (animation.id) {
            case ID_ANIMATION_CANCEL:
                post(cancelAnimationRunnable);
                break;
            case ID_ANIMATION_X:
            case ID_ANIMATION_Y:
            case ID_ANIMATION_ROTATE:
            case ID_GRAVITY_X:
            case ID_GRAVITY_Y:
                break;
        }
    }

    Runnable cancelAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            cancelAnimation(true);
        }
    };

    private void cancelAnimation(boolean fromRun) {
        if (flagAnimation != ID_ANIMATION_NONE) {
            flagAnimation = ID_ANIMATION_NONE;
            slideListener.onLayerGesture(GestureStatus.TouchCancel);
            if (!fromRun) {
                removeCallbacks(cancelAnimationRunnable);
                animationManager.reset(false);
            }
        }
    }

    Runnable flingFinish = new Runnable() {
        @Override
        public void run() {
            isFling = false;
            slideListener.onLayerGesture(GestureStatus.FlingFinish);
        }
    };

    public static enum GestureStatus {
        TouchStart,
        TouchMoveBefore,
        TouchMove,
        TouchCancel,
        FlingStart,
        FlingFinish
    }

    public static interface LayerGestureListener {
        public void onLayerGesture(GestureStatus slideStatus);

        public void onFinishUp(MotionEvent event);

        public void onLayerViewClick();
    }

}

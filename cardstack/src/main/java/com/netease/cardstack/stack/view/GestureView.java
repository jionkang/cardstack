package com.netease.cardstack.stack.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class GestureView extends View implements OnGestureListener {

    private static final String TAG = "GestureView";

    protected GestureDetector gestureDetector;

    protected float touchX;
    protected float touchY;
    protected Paint bmpPaint = new Paint();

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GestureView(Context context) {
        super(context);
        init();
    }

    protected void init() {
        gestureDetector = new GestureDetector(getContext(), this);

        bmpPaint.setAntiAlias(true);
        bmpPaint.setDither(true);
        bmpPaint.setFilterBitmap(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean touchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onUp(event);
                break;
        }
        invalidate();
        return true;
    }

    protected void onUp(MotionEvent event) {
        Log.d(TAG, "onUp:" + touchX + "," + touchY + "; "
                + (event.getAction() == MotionEvent.ACTION_UP));
    }

    @Override
    public boolean onDown(MotionEvent e) {
        touchX = e.getX();
        touchY = e.getY();
        Log.d(TAG, "onDown:" + touchX + "," + touchY);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "onSingleTapUp:" + touchX + "," + touchY);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return false;
    }

}

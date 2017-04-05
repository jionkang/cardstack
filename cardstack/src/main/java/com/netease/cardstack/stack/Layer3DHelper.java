package com.netease.cardstack.stack;

import android.graphics.Rect;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.netease.cardstack.R;
import com.netease.cardstack.stack.view.Layer3DLayout;
import com.netease.cardstack.stack.view.Layer3DLayout.Layer3DListener;
import com.netease.cardstack.stack.view.LayerGestureView;
import com.netease.cardstack.stack.view.LayerGestureView.GestureStatus;
import com.netease.cardstack.stack.view.LayerGestureView.LayerGestureListener;
import com.netease.cardstack.stack.view.MaskView;
import com.netease.cardstack.stack.view.MaskView.MaskTouchListener;


public class Layer3DHelper implements Layer3DListener, LayerGestureListener {

    private static final String TAG = "Layer3DHelper";

	private static final float SCALE_VIEW_LARGE_BACK = 0.86f;
	private static final float SCALE_VIEW_LARGE_CENTER = 0.93f;
	private static final float SCALE_VIEW_BACK = 0.81f;
	private static final float SCALE_VIEW_CENTER = 0.92f;
	private static final float SCALE_VIEW_FRONT = 1f;

	private static final int ID_LAYER_BACK = 0x01;
	private static final int ID_LAYER_CENTER = 0x02;

	private static final int ID_ANIMATION_NONE = 0x0;
	private static final int ID_ANIMATION_TO_FRONT = 0x01;

	public static final int FRONT = 0;
	public static final int CENTER = 1;
	public static final int BACK = 2;

	private Layer3DLayout d3FlingViewBack;
	private Layer3DLayout d3FlingViewCenter;
	private Layer3DLayout d3FlingViewFront;

	private LayerGestureView gestureView;
	private MaskView maskView;

	private View container;
	private FlingListener flingListener;

	private int flagAnimation = ID_ANIMATION_NONE;
	private int index;

	private float viewBackScale = SCALE_VIEW_BACK;
	private float viewCenterScale = SCALE_VIEW_CENTER;
	
	private boolean isFling = true;

	public Layer3DHelper(View container, FlingListener flingListener,boolean isFling) {
        this.container = container;
		this.flingListener = flingListener;
		this.isFling = isFling;
		init();
	}

	private View findViewById(int id) {
        return container.findViewById(id);
	}

	@SuppressWarnings("deprecation")
	private void init() {
		d3FlingViewBack = (Layer3DLayout) findViewById(R.id.fling_view_back);
		d3FlingViewCenter = (Layer3DLayout) findViewById(R.id.fling_view_center);
		d3FlingViewFront = (Layer3DLayout) findViewById(R.id.fling_view_front);

		updateLayerCenterScale();
		d3FlingViewBack.setDefaultScale(viewBackScale);
		d3FlingViewCenter.setDefaultScale(viewCenterScale);

		d3FlingViewBack.registerLayer3DListener(ID_LAYER_BACK, this);
		d3FlingViewCenter.registerLayer3DListener(ID_LAYER_CENTER, this);

		gestureView = (LayerGestureView) findViewById(R.id.slide_view);
		
		gestureView.bindSlideViews(d3FlingViewFront.findViewById(R.id.card),this);  

		maskView = (MaskView) findViewById(R.id.mask_view);
		maskView.setOnMaskTouchListener(maskTouchListener);

		index = 0;
		updateData(false);
		container.setVisibility(View.VISIBLE);
	}

	boolean frontViewVisible() {
		return index < flingListener.flingSize();
	}

	MaskTouchListener maskTouchListener = new MaskTouchListener() {

		@Override
		public void onMaskTouchEvent(MotionEvent event) {
		    if (isFling) {
		        gestureView.touchEvent(event);
                    }
		}

		@Override
		public boolean onMaskTouchDown() {
			finishFlagAnimation();
			return frontViewVisible();
		}
	};

	private void viewVisibility(View view, int visibility) {
		if (view.getVisibility() != visibility) {
			view.setVisibility(visibility);
		}
	}

    public void showNext(final boolean left) {
        final Rect rect = gestureView.getSlideViewRect();
        final long downTime = SystemClock.uptimeMillis();
        final int startX = rect.centerX();
        final int startY = rect.centerY();
        final float step = left ? (rect.width() / -40f) : (rect.width() / 40f);
        for (int i = 0; i < 12; i++) {
            final int index = i;
            maskView.postDelayed(new Runnable() {

                @Override
                public void run() {
                    int action = MotionEvent.ACTION_CANCEL;
                    int x = (int) (startX + index * step);
                    int y = (int) (startY - index * Math.abs(step / 3));
                    switch (index) {
                        case 0:
                            action = MotionEvent.ACTION_DOWN;
                            break;
                        case 9:
                            action = MotionEvent.ACTION_UP;
                            break;
                        default:
                            action = MotionEvent.ACTION_MOVE;
                            break;
                    }
                    MotionEvent event = MotionEvent.obtain(downTime, downTime + index * 20, action, x, y, 0);
                    maskView.dispatchTouchEvent(event);
                    event.recycle();
                }
        }, (i + 3) * 20);
        }
    }

	public void updateData(boolean next) {
		if (next) {
			flingListener.removeCurContact(index);
		}
		int size = flingListener.flingSize();
		int _index = index;
        viewDataUpdate(d3FlingViewFront, _index < size, _index, FRONT);
        ++_index;
        viewDataUpdate(d3FlingViewCenter, _index < size, _index, CENTER);
        if (!next) {
            d3FlingViewCenter.postDelayed(new Runnable() {

                @Override
                public void run() {
                    viewDataUpdate(d3FlingViewCenter, index + 1 < flingListener.flingSize(), index + 1, CENTER);
                }
            }, 200);
        }
        ++_index;
        if (!next) {
            d3FlingViewBack.postDelayed(new Runnable() {

                @Override
                public void run() {
                    viewDataUpdate(d3FlingViewBack, index + 2 < flingListener.flingSize(), index + 2, CENTER);
                }
            }, 200);
        }
        viewDataUpdate(d3FlingViewBack, _index < size, _index, BACK);
        ++_index;
		LOG("updateData");
	}

	private void viewDataUpdate(View view, boolean update, int index,
			int position) {
		if (update) {
			viewVisibility(view, View.VISIBLE);
			flingListener.updateView(view, index, position);
		} else {
			viewVisibility(view, View.INVISIBLE);
		}
	}

	public void updateUInfos() {
		maskView.setVisibility(View.VISIBLE);
		updateData(false);
	}

	@Override
	public void onLayer3DFinish(final int id, final boolean restore) {
		switch (id) {
		case ID_LAYER_BACK:
		case ID_LAYER_CENTER:
			finishFlagAnimation();
			break;
		}
	}

	private void finishFlagAnimation() {
		if ((flagAnimation & ID_ANIMATION_TO_FRONT) == ID_ANIMATION_TO_FRONT) {
			flagAnimation &= ~ID_ANIMATION_TO_FRONT;
			d3FlingViewBack.restoreToDefaultScale();
			d3FlingViewCenter.restoreToDefaultScale();
			updateData(true);
			if (frontViewVisible()) {
				viewVisibility(d3FlingViewFront, View.VISIBLE);
			}

			LOG("finishFlagAnimation:" + flagAnimation);
		}
	}



	@Override
	public void onLayerGesture(final GestureStatus slideStatus) {
		switch (slideStatus) {
		case TouchStart:
                d3FlingViewFront.setVisibility(View.INVISIBLE);
			break;
		case TouchCancel:
                d3FlingViewFront.setVisibility(View.VISIBLE);
			break;
		case FlingStart:
			flagAnimation = ID_ANIMATION_NONE;
			if (d3FlingViewBack.getVisibility() == View.VISIBLE) {
				d3FlingViewBack.applayScaleAnimation(viewCenterScale);
			}
			if (d3FlingViewCenter.getVisibility() == View.VISIBLE) {
				d3FlingViewCenter.applayScaleAnimation(SCALE_VIEW_FRONT);
				flagAnimation = ID_ANIMATION_TO_FRONT;
			}
			if (flagAnimation == ID_ANIMATION_NONE) { // 已经到最后一页完了
				++index;
			}
                viewVisibility(d3FlingViewFront, View.INVISIBLE);
			break;
		case FlingFinish:
                if (flingListener != null) {
                    flingListener.onFinishFling(index);
                }
			break;
		default:
			break;
		}
		LOG("onLayerGesture:" + slideStatus);
	}

	@Override
	public void onFinishUp(MotionEvent event) {
		flingListener.onFinshUp(event);
	}

	public static interface FlingListener {
		public int flingSize();
		public void updateView(View view, int index, int position);
		public void removeCurContact(int index);
		public void setOnClickListener(int index);
        public void onFinishFling(int index);
        public void onFinshUp(MotionEvent event);

	}

	@Override
	public void onLayerViewClick() {
		flingListener.setOnClickListener(index);

		LOG("onLayerViewClick");
	}

	private void LOG(String message) {
	}

	public void updateLayerCenterScale() {
        DisplayMetrics dm = container.getResources().getDisplayMetrics();
		float widthDpi = dm.widthPixels / dm.density;
		if (widthDpi < 320 + 1) {
			return;
		}
		viewBackScale = SCALE_VIEW_LARGE_BACK;
		viewCenterScale = SCALE_VIEW_LARGE_CENTER;
	}
}

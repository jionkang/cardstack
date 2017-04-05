package com.netease.cardstack.stack.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class MaskView extends View {

	private MaskTouchListener maskTouchListener;

	public MaskView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public MaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MaskView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setEnabled(true);
		setClickable(true);
	}

	public void setOnMaskTouchListener(MaskTouchListener maskTouchListener) {
		this.maskTouchListener = maskTouchListener;
	}

	private boolean bindTouch;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			bindTouch = maskTouchListener.onMaskTouchDown();
			break;
		}
		if (bindTouch) {
			maskTouchListener.onMaskTouchEvent(event);
		}
		return true;
	}

	public static interface MaskTouchListener {
		public boolean onMaskTouchDown();
		public void onMaskTouchEvent(MotionEvent event);
	}

}

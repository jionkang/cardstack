package com.netease.cardstack.stack.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.netease.cardstack.stack.animation.Animation;
import com.netease.cardstack.stack.animation.Animation.AnimationListener;
import com.netease.cardstack.stack.animation.AnimationManager;
import com.netease.cardstack.stack.animation.FloatAnimation;

public class Layer3DLayout extends LinearLayout implements AnimationListener {

	// private static final String TAG = "Layer3DLayout";

	private static final int INDEX_SCALE = 0;

	private static final int ID_ANIMATION_NONE = 0;
	private static final int ID_ANIMATION_SCALE = 0x01;
	private static final int ID_ANIMATION_RESTORE = 0x02;

	private static final long DURATION = 400;

	private Matrix matrix = new Matrix();

	private float[] values = new float[1];
	private float[] targetValues = new float[1];

	private FloatAnimation scaleAnimation;
	private AnimationManager animationManager = new AnimationManager();

	private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
	private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

	private int flagAnimation;
	private int id;
	private Layer3DListener layer3DListener;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Layer3DLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public Layer3DLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Layer3DLayout(Context context) {
		super(context);
		init();
	}

	public void registerLayer3DListener(int id, Layer3DListener layer3DListener) {
		this.id = id;
		this.layer3DListener = layer3DListener;
	}

	private void init() {
		setWillNotDraw(false);
		setGravity(Gravity.CENTER);

		scaleAnimation = new FloatAnimation(ID_ANIMATION_SCALE);

		scaleAnimation.listener = this;

		values[INDEX_SCALE] = 1;
		targetValues[INDEX_SCALE] = 1;
	}

	/**
	 * @param scale
	 */
	public void applayScaleAnimation(float scale) {
		animationManager.reset(false);
		flagAnimation = ID_ANIMATION_SCALE;
		scaleAnimation.startAnimation(values, INDEX_SCALE, values[INDEX_SCALE],
				scale, DURATION, accelerateInterpolator);
		animationManager.addAnimation(scaleAnimation);

		invalidate();
	}

	public void setDefaultScale(float scale) {
		values[INDEX_SCALE] = scale;
		targetValues[INDEX_SCALE] = scale;

		animationManager.reset(false);

		invalidate();
	}

	public void restoreToDefaultScale() {
		animationManager.reset(false);
		flagAnimation = ID_ANIMATION_NONE;
		values[INDEX_SCALE] = targetValues[INDEX_SCALE];
		invalidate();
	}

	public void restoreToDefaultScaleAnimation() {
		animationManager.reset(false);
		flagAnimation = ID_ANIMATION_RESTORE;
		scaleAnimation.startAnimation(values, INDEX_SCALE, values[INDEX_SCALE],
				targetValues[INDEX_SCALE], DURATION, decelerateInterpolator);
		animationManager.addAnimation(scaleAnimation);
		invalidate();
	}

	@Override
	public void draw(Canvas canvas) {
		animationManager.step();

		float width = getWidth();
		matrix.reset();
		matrix.postScale(values[INDEX_SCALE], values[INDEX_SCALE]);

		float dscale = (1 - values[INDEX_SCALE]) * 0.5f;
		float dx = dscale * width;
		matrix.postTranslate(dx, 0);
		canvas.save();
		canvas.concat(matrix);
		super.draw(canvas);
		canvas.restore();

		if (animationManager.hasMore()) {
			invalidate();
		}
	}

	@Override
	public void onAnimationFinish(Animation animation) {
		switch (animation.id) {
		case ID_ANIMATION_SCALE:
			if (layer3DListener == null) {
				break;
			}
			post(runnable);
			break;
		}
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (flagAnimation == ID_ANIMATION_SCALE) {
				layer3DListener.onLayer3DFinish(id, false);
			} else if (flagAnimation == ID_ANIMATION_RESTORE) {
				layer3DListener.onLayer3DFinish(id, true);
			}
			flagAnimation = ID_ANIMATION_NONE;
		}
	};

	public static interface Layer3DListener {
		/**
		 * @param id
		 * @param revert
		 *            true(�ӵ�ǰ״̬�ָ���Ĭ��״̬)��false(��Ĭ��״̬������״̬)
		 */
		public void onLayer3DFinish(int id, boolean revert);
	}
}

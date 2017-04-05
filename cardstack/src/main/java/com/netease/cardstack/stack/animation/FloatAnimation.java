package com.netease.cardstack.stack.animation;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class FloatAnimation extends Animation {

	public FloatAnimation() {
		this(-1);
	}

	public FloatAnimation(int id) {
		super(id);
	}

	private int index;
	private float[] value;
	private float source;
	private float target;
	private float delta_value;

	Interpolator interpolator = null;

	public void startAnimation(float[] value, int index, float source,
			float target, long duration, Interpolator interpolator) {
		this.value = value;
		this.index = index;
		this.source = source;
		this.target = target;
		this.duration = duration;
		this.interpolator = interpolator;
		if (interpolator == null) {
			this.interpolator = new LinearInterpolator();
		}
		this.value[index] = source;
		delta_value = target - source;
		time = -1;
		animation = true;
		delay = 0;
	}

	public void startAnimation(float[] value, int index, float start, float end) {
		startAnimation(value, index, start, end, 1000, null);
	}

	public void stepSelf() {
		if (delta_time >= duration) {
			animation = false;
			value[index] = target;
			return;
		}
		float input = 1.0f * delta_time / duration;
		input = interpolator.getInterpolation(input);
		value[index] = this.source + delta_value * input;
	}

	@Override
	public boolean removeAnimation(Object obj, int index, boolean toTarget) {
		if (value != obj || this.index != index) {
			return false;
		}
		if (toTarget) {
			value[index] = target;
		}
		animation = false;
		return true;
	}
}

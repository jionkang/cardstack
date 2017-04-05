package com.netease.cardstack.stack.animation;

public abstract class Animation {

	public final int id;
	public boolean animation = false;
	protected float duration;
	protected long time;
	protected long delta_time;
	protected long delay = 0;

	public Object data;
	public AnimationListener listener;

	public Animation(int id) {
		this.id = id;
	}

	public void step(long uptimeMillis) {
		if (!animation) {
			return;
		}
		if (time <= 0) {
			time = uptimeMillis;
		}
		delta_time = uptimeMillis - time;
		if (delta_time < delay) {
			return;
		}
		delta_time -= delay;
		stepSelf();
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public abstract void stepSelf();

	public boolean removeAnimation(Object obj, boolean toTarget) {
		return removeAnimation(obj, 0, toTarget);
	}

	public abstract boolean removeAnimation(Object obj, int index,
			boolean toTarget);

	public static interface AnimationListener {
		public void onAnimationFinish(Animation animation);
	}

	public void reset() {
		animation = false;
		time = -1;
		delay = 0;
	}
}

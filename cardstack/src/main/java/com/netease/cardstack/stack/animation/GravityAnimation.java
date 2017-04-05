package com.netease.cardstack.stack.animation;

public class GravityAnimation extends Animation {

	public GravityAnimation(int id) {
		super(id);
	}

	private int index;
	private float[] value;
	private float gravity;
	private float velocity;
	
	private float scaleTime = 1; // ʱ������
	
	/**
	 * �ö���Ҫ�ֶ�reset
	 * @param value
	 * @param index
	 * @param gravity
	 * @param velocity
	 */
	public void startAnimation(float[] value, int index, float gravity, float velocity) {
		this.index = index;
		this.value = value;
		
		this.gravity = gravity;
		this.velocity = velocity;
		
		reset();
		this.animation = true;
	}
	
	public void scaleTime(float scaleTime) {
		this.scaleTime = scaleTime;
	}
	
	public void stopAnimation() {
		reset();
	}
	
	@Override
	public void step(long uptimeMillis) {
		if(time < 0) {
			this.time = uptimeMillis;
		}
		long delta = uptimeMillis - time;
		this.time = uptimeMillis;
		float _delta_velocity = delta * scaleTime * gravity;
		float dx = (velocity + _delta_velocity * 0.5f) * delta * scaleTime;
		velocity += _delta_velocity;
		value[index] += dx;
	}

//	/**
//	 * @see im.yixin.common.animation.Animation#stepSelf()
//	 */
	@Override
	public void stepSelf() {
		
	}

//	/**
//	 * @see im.yixin.common.animation.Animation#removeAnimation(Object, int, boolean)
//	 */
	@Override
	public boolean removeAnimation(Object obj, int index, boolean toTarget) {
		return false;
	}

}

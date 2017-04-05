package com.netease.cardstack.stack;

import android.content.Context;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.ViewConfiguration;


public class SimpleTorque {

	private static final String TAG = "SimpleTorque";

	protected static final double PI = Math.PI;
	protected static final double PI2 = Math.PI * 2;
	private static final double PI_2 = Math.PI * 0.5f;

	private static final double PI_TO_DEGREES = 180 / Math.PI;

	private static final float TIME_ITERATION = 0.002f; // 2ms
	private static final float FORCE_COEFFICIENT = 1;
	private static final float FORCE = 20 * FORCE_COEFFICIENT;
	private static final float DAMPING_COEFFICIENT = 0.5f * FORCE; // [0, 1]

	private static final float MAX_DRAG_VELOCITY = (float) Math.PI * 4;

	private static final float DRAG_COEFFICIENT = 0.06f;

	private float maxTorque;

	private PointF center; // 中心点
	private PointF touch; // 固定的接触点

	private PointF temp; // 旋转后的中心点

	private double initAngular; // 初始角度
	private double angular; // 角度
	private float torqueCoeffient; // 力矩系数
	private float angularSpeed; // 角速度

	private PointF centerToTouch;
	private boolean animation = false;
	private long startTime;
	private float scaleTime = 1; // 时间缩放
	private float touchSlot;

	public SimpleTorque(Context contex) {
		center = new PointF();
		touch = new PointF();
		centerToTouch = new PointF();
		temp = new PointF();
		touchSlot = ViewConfiguration.get(contex).getScaledTouchSlop() * 0.5f;
	}

	public SimpleTorque updateSize(float width, float height) {
		maxTorque = width * 0.5f;
		maxTorque = Math.max(1, maxTorque);

		float centerX = width * 0.5f;
		float centerY = height * 0.5f;
		center.set(centerX, centerY);

		centerToTouch.set(center);
		return this;
	}

	public SimpleTorque updateTouch(float x, float y) {
		touch.set(x, y);
		return this;
	}

	private float radius; // 中点到touch点的距离

	public void start() {
		centerToTouch.offset(-touch.x, -touch.y);

		angular = requireAngular(centerToTouch);
		initAngular = angular;

		angularSpeed = 0;
		radius = centerToTouch.length();

		torqueCoeffient = radius / maxTorque;

		animation = true;
		startTime = SystemClock.uptimeMillis();

		float x = radius * (float) Math.sin(angular);
		float y = radius * (float) Math.cos(angular);

	}

	public void flingEnable() {
		torqueCoeffient = 1;
	}

	public PointF getCenter() {
		temp.set(center.x, center.y);
		return temp;
	}

	public PointF getRotateCenter() {
		// 计算当前的center position
		float x = radius * (float) Math.sin(angular);
		float y = radius * (float) Math.cos(angular);
		float x_centerToTouch = touch.x + x;
		float y_centerToTouch = touch.y + y;
		temp.set(x_centerToTouch, y_centerToTouch);
		return temp;
	}

	public void stop() {
		animation = false;
	}

	public boolean animation() {
		return animation;
	}

	private void calcAngular(float delta) {

		// 计算delta时间后的angular
		int iteration = (int) (delta / TIME_ITERATION); // 迭代数
		iteration = Math.max(1, iteration);
		// iteration = Math.min(100, iteration); // 约束
		float _delta = delta / iteration;

		for (int i = 0; i < iteration; ++i) {

			float angularSine = (float) Math.sin(angular);
			float delta_coefficient = 0;

			float overDamping = angularSine * angularSpeed;
			if (overDamping > 0) {
				delta_coefficient = -DAMPING_COEFFICIENT;
			} else if (overDamping < 0) {
				delta_coefficient = DAMPING_COEFFICIENT;
			}
			//
			float coefficient = (FORCE + delta_coefficient) * torqueCoeffient;

			float k = coefficient * _delta;
			angularSpeed += k * angularSine; // 角加速度=coefficient *
												// (float)Math.sin(angular);

			angular -= (angularSpeed * _delta); // 当前角度
			// Log.d(TAG, "calcAngular:" + i + "]" + getDegrees() + ";" +
			// angularSpeed + ";" + angular + ";");
		}
		// Log.d(TAG, "calcAngular=========================================");

	}

	private int getDegrees() {
		return (int) (angular * 180 / PI);
	}

	public void applyImpulseVelocity(float velocityX, float velocityY) {
		applyForceVelocity(velocityX, velocityY, false);
	}

	private void applyForceVelocity(float fx, float fy, boolean slot) {
		float deltaVelocity = (float) Math.sqrt(fx * fx + fy * fy)
				* torqueCoeffient;

		if (slot && deltaVelocity < touchSlot) {
			return;
		}

		deltaVelocity *= DRAG_COEFFICIENT;

		temp.set(touch);
		temp.offset(-center.x, -center.y);

		double angularTouchToCenter = requireAngular(temp);
		temp.set(fx, fy);
		double angularDrag = requireAngular(temp);
		double deltaAngular = angularDrag - angularTouchToCenter;

		float delta = (float) Math.sin(deltaAngular);

		angularSpeed -= (deltaVelocity * delta);

		adjustAngularSpeed();
	}

	public void applyDragVelocity(float distanceX, float distanceY) {
		applyForceVelocity(distanceX, distanceY, true);
	}

	private void adjustAngularSpeed() {
		if (angularSpeed > MAX_DRAG_VELOCITY) {
			angularSpeed = MAX_DRAG_VELOCITY;
		} else if (angularSpeed < -MAX_DRAG_VELOCITY) {
			angularSpeed = -MAX_DRAG_VELOCITY;
		}
	}

	/**
	 * @return
	 */
	public PointF getUpdateTouch() {
		temp.set(touch.x, touch.y);
		return temp;
	}

	public double getCenterRotateAngularDegrees() {
		double degrees = (angular - initAngular) * PI_TO_DEGREES;
		return degrees;
	}

	public double getRotateAngularDegrees() {
		double degrees = (initAngular - angular) * PI_TO_DEGREES;
		return degrees;
	}

	/**
	 * 垂直向下为0角度（左负右正）
	 * 
	 * @param point
	 * @return
	 */
	private double requireAngular(PointF point) {
		float x = point.x;
		if (x == 0) {
			return 0;
		}
		double radians = Math.atan(point.y / x);

		radians = -radians;

		if (x > 0) {
			radians += PI_2;
		} else {
			radians -= PI_2;
		}

		return radians;
	}

	private float updateTime() {
		long time = SystemClock.uptimeMillis();
		long dtime = time - startTime;
		if (dtime < 0) {
			dtime = 0;
		}
		startTime = time;
		return 1.0f * dtime * scaleTime / 1000;
	}

	public void scaleTime(float scaleTime) {
		this.scaleTime = scaleTime;
	}

	public void update() {
		if (!animation) {
			return;
		}
		float delta = updateTime();
		calcAngular(delta);
	}
}

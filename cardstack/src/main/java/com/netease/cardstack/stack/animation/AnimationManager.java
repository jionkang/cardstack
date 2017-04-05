package com.netease.cardstack.stack.animation;


import android.os.SystemClock;

import com.netease.cardstack.stack.animation.Animation.AnimationListener;

import java.util.ArrayList;

public class AnimationManager {

	ArrayList<Animation> animationList = new ArrayList<Animation>();
	ArrayList<Animation> clearList = new ArrayList<Animation>();

	public void addAnimation(Animation animation) {
		animationList.add(animation);
	}

	public void addAnimationOne(Animation animation) {
		for (Animation _animation : animationList) {
			if (animation == _animation) {
				return;
			}
		}
		animationList.add(animation);
	}

	public void removeAnimation(Animation animation) {
		animationList.remove(animation);
	}

	public void removeValue(float values[], int index, boolean toTarget) {
		for (Animation animation : animationList) {
			animation.removeAnimation(values, index, toTarget);
		}
	}

	public void step() {
		long delta = SystemClock.uptimeMillis();
		boolean clear = false;
		for (Animation animation : animationList) {
			animation.step(delta);
			if (!animation.animation) {
				clearList.add(animation);
				clear = true;
			}
		}
		if (clear) {
			animationList.removeAll(clearList);

			for (Animation animation : clearList) {
				AnimationListener listener = animation.listener;
				if (listener != null) {
					listener.onAnimationFinish(animation);
				}
			}
			clearList.clear();
		}
	}

	public boolean hasMore() {
		return animationList.size() > 0;
	}

	public void reset(boolean notify) {
		clearList.addAll(animationList);
		animationList.clear();
		if (notify) {
			for (Animation animation : clearList) {
				if (animation.listener != null) {
					animation.listener.onAnimationFinish(animation);
				}
			}
		}
		clearList.clear();
	}
}

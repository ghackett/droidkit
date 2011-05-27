package org.droidkit.util.tricks;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

public class AnimationTricks {

	public static Animation createHeightChangeAnimation(int oldHeight, int newHeight, int duration, AnimationListener listener) {
		float targetScale = (float)newHeight / (float)oldHeight;
		ScaleAnimation anim = new ScaleAnimation(1, 1, 1.0f, targetScale);
		anim.setDuration(duration);
		anim.setAnimationListener(listener);
		return anim;
	}
	
	public static Animation createFadeAnimation(boolean out, int duration, AnimationListener listener) {
		AlphaAnimation anim = new AlphaAnimation((out ? 1.0f : 0.0f), (out ? 0.0f : 1.0f));
		anim.setDuration(duration);
		anim.setAnimationListener(listener);
		return anim;
	}
	
    public static Animation createSlideAnimation(boolean relToParent, boolean horizontal, boolean fromLeftOrTop, boolean exiting, int duration) {
        int rel = (relToParent ? Animation.RELATIVE_TO_PARENT : Animation.RELATIVE_TO_SELF);
        float movingFrom = (exiting ? 0f : (fromLeftOrTop ? -1f : 1f));
        float movingTo = (exiting ? (fromLeftOrTop ? 1f : -1f) : 0f);
        TranslateAnimation anim;
        if (horizontal) {
            anim = new TranslateAnimation(rel, movingFrom, rel, movingTo, rel, 0, rel, 0);
        } else {
            anim = new TranslateAnimation(rel, 0, rel, 0, rel, movingFrom, rel, movingTo);
        }
        anim.setDuration(duration);
        return anim;
    }
	
}

package com.lxy.ball;

import android.view.animation.Interpolator;

/**
 * Created by lxy on 2017/6/3.
 */

public class DancingInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        return (float) (1 - Math.exp(-3 * input) * Math.cos(10 * input));
    }
}

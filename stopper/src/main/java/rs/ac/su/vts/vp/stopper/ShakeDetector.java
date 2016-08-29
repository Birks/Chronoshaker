package rs.ac.su.vts.vp.stopper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;
import android.util.Log;


public class ShakeDetector implements SensorEventListener {


    private static final int SHAKE_IMMIDIATELY_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;
    private static final float SHAKE_POWER = 1.3F;
    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;


    @Override
    public void onSensorChanged(SensorEvent event) {
 
        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
 
            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;
            float gForce = FloatMath.sqrt(gX * gX + gY * gY + gZ * gZ);
            if (gForce > SHAKE_POWER) {
                final long now = System.currentTimeMillis();
                if (mShakeTimestamp + SHAKE_IMMIDIATELY_TIME_MS > now) {
                    return;
                }

                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }
 
                mShakeTimestamp = now;
                mShakeCount++;
 
                mListener.onShake(mShakeCount);
            }
        }
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    public interface OnShakeListener {
        public void onShake(int count);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nemkell ide semmi
    }

}
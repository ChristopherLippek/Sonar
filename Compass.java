package de.fh_erfurt.omnichrom.schweizertaschenmesser;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Compass application
 * written by Christopher Lippek
 */
public class Compass extends AppCompatActivity implements SensorEventListener
{
    /**
     * imageView -> picture from the compass
     * gravity -> gravity information to identify the rotation
     * geomagnetic -> geomagnetic information to located the position based on the earth field
     * azimuth -> degree from sensor
     * currectAzimuth-> degree one step before to calculate the difference
     * sensorManager -> holds the hardware information from the sensor
     */
    private ImageView imageView;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float azimuth = 0.0f;
    private float currectAzimuth = 0.0f;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        imageView = findViewById(R.id.compass);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }


    /**
     * sensorManager gets all information about the gravity and geomagnetic from the Sensor
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Updates the values when the sensor values are changing
     * rotate the compass based on the values given by the sensor
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        final float alpha = 0.97f;
        synchronized (this)
        {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
            }

            //Rotationmatrix
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success)
            {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                //
                Animation anim = new RotateAnimation(-currectAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                currectAzimuth = azimuth;

                anim.setDuration(500);
                anim.setRepeatCount(0);
                anim.setFillAfter(true);

                imageView.startAnimation(anim);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}

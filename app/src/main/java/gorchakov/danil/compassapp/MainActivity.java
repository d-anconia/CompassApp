package gorchakov.danil.compassapp;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ImageView imageCompass;
    TextView txtAzimuth;
    int mAzimuth;
    private SensorManager sensorManager;
    private Sensor mRotation, mAccelerometer, mMagnetometer;
    float [] rMat = new float[9];
    float [] orientation = new float[9];
    private float [] mLastAccelerometer = new float[3];
    private float [] mLastMagnetometer = new float[3];
    private boolean isHaveSensor = false, isHaveSensorTwo = false;
    private boolean isLastAccelerometerSet = false;
    private boolean isLastMagnetometerSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        imageCompass = (ImageView) findViewById(R.id.img_compass);
        txtAzimuth = (TextView) findViewById(R.id.txt_azimuth);

        start();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rMat, sensorEvent.values);
            mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360);
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(sensorEvent.values, 0, mLastAccelerometer, 0 , sensorEvent.values.length);
            isLastAccelerometerSet = true;
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(sensorEvent.values, 0, mLastMagnetometer, 0 , sensorEvent.values.length);
            isLastMagnetometerSet = true;
        }
        if(isLastMagnetometerSet && isLastAccelerometerSet){
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360);
        }

        mAzimuth = Math.round(mAzimuth);
        imageCompass.setRotation(-mAzimuth);

        String where = "NO";
        if(mAzimuth >= 350 || mAzimuth <= 10)
            where = "N";
        if(mAzimuth < 350 || mAzimuth > 280)
            where = "NW";
        if(mAzimuth <= 280 || mAzimuth > 260)
            where = "W";
        if(mAzimuth <= 260 || mAzimuth > 190)
            where = "SW";
        if(mAzimuth <= 190 || mAzimuth > 170)
            where = "S";
        if(mAzimuth <= 170 || mAzimuth > 100)
            where = "SE";
        if(mAzimuth <= 100 || mAzimuth > 90)
            where = "E";
        if(mAzimuth <= 80 || mAzimuth > 10)
            where = "NE";

        txtAzimuth.setText(mAzimuth + "° " + where);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void start(){
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null){
            if(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null || sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null)
                noSensorAlert();
            else {
                mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                isHaveSensor = sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                isHaveSensorTwo = sensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }else{
            mRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            isHaveSensor = sensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_UI);
        }
    }
    public void noSensorAlert(){
        AlertDialog.Builder alertDialog  = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the compass")
                .setCancelable(false)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
    }

    public void stop(){
        if(isHaveSensor && isHaveSensorTwo){
            sensorManager.unregisterListener(this, mAccelerometer);
            sensorManager.unregisterListener(this, mMagnetometer);
        }else{
            if(isHaveSensor)
                sensorManager.unregisterListener(this, mRotation);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        stop();
    }
    @Override
    protected void onResume(){
        super.onResume();
        start();
    }
}

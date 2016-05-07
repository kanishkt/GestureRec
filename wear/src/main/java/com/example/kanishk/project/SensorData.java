package com.example.kanishk.project;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
//
//import static android.util.FloatMath.cos;
//import static android.util.FloatMath.sin;
//import static android.util.FloatMath.sqrt;

/**
 * Created by kanishk on 5/7/16.
 */
public class SensorData implements SensorEventListener{

    private static final float SHAKE_THRESHOLD = 1.1f;
    private static final int SHAKE_WAIT_TIME_MS = 250;
    private static final float ROTATION_THRESHOLD = 2.0f;
    private static final int ROTATION_WAIT_TIME_MS = 100;
    private int mSensorType;
    private long mShakeTime = 0;
    private long mRotationTime = 0;
    private float acc1,acc2,acc3;
    private float gyro1,gyro2,gyro3;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    float[] orientation = new float[3];
    private float rotationCurrent[] = new float[]{1f,0f,0f,0f,1f,0f,0f,0f,1f};
    int mAzimuth=0;

    private static final String COUNT_KEY = "com.example.key.count";
    private GoogleApiClient mGoogleApiClient;

    public SensorData(GoogleApiClient mGoogleApiClient){
        this.mGoogleApiClient = mGoogleApiClient;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long now = System.currentTimeMillis();

        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if ((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
                mRotationTime = now;
                if (Math.abs(event.values[0]) > ROTATION_THRESHOLD ||
                        Math.abs(event.values[1]) > ROTATION_THRESHOLD ||
                        Math.abs(event.values[2]) > ROTATION_THRESHOLD) {
                    gyro1 = event.values[0];
                    gyro2 = event.values[1];
                    gyro3 = event.values[2];
                    Log.d("Gyro", Float.toString(gyro1) + "," + Float.toString(gyro2) + "," + Float.toString(gyro3));
                    getAzimuth(gyro1,gyro2,gyro3,event);
                }
            }
        }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if ((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
                    mShakeTime = now;

                    acc1 = event.values[0] / SensorManager.GRAVITY_EARTH;
                    acc2 = event.values[1] / SensorManager.GRAVITY_EARTH;
                    acc3 = event.values[2] / SensorManager.GRAVITY_EARTH;
                    Log.d("Acc",Float.toString(acc1)+","+Float.toString(acc2)+","+Float.toString(acc3));
                }
            }
        increaseCounter();

    }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    public void getAzimuth(float axisX, float axisY, float axisZ, SensorEvent event){

        // Calculate the angular speed of the sample
        double omegaMagnitude = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
        final float dT = (event.timestamp - timestamp) * NS2S;
        // Normalize the rotation vector if it's big enough to get the axis
        // (that is, EPSILON should represent your maximum allowable margin of error)
        if (omegaMagnitude > 0.52) {
            axisX /= omegaMagnitude;
            axisY /= omegaMagnitude;
            axisZ /= omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = (float) (omegaMagnitude * dT / 2.0f);
        double sinThetaOverTwo = Math.sin(thetaOverTwo);
        double cosThetaOverTwo = Math.cos(thetaOverTwo);
        deltaRotationVector[0] = (float) (sinThetaOverTwo * axisX);
        deltaRotationVector[1] = (float) (sinThetaOverTwo * axisY);
        deltaRotationVector[2] = (float) (sinThetaOverTwo * axisZ);
        deltaRotationVector[3] = (float) cosThetaOverTwo;
    float[] deltaRotationMatrix = new float[9];

    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
    // tv.setText(String.valueOf(Math.toDegrees(deltaRotationMatrix[2])) + " degrees");
    // User code should concatenate the delta rotation we computed with the current rotation
    // in order to get the updated rotation.
    // rotationCurrent = rotationCurrent * deltaRotationMatrix;
    float a[][] = new float[3][3];
    float b[][] = new float[3][3];
    float c[][] = new float[3][3];

    for(int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            a[i][j] = rotationCurrent[i * 3 + j];
            b[i][j] = deltaRotationMatrix[i * 3 + j];
        }
    }

    for (int i = 0; i < 3; i++) { // aRow
        for (int j = 0; j < 3; j++) { // bColumn
            for (int k = 0; k < 3; k++) { // aColumn
                c[i][j] += a[i][k] * b[k][j];
            }
        }
    }

    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            rotationCurrent[i * 3 + j] = c[i][j];
        }
    }
        mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rotationCurrent, orientation)[0]) + 360) % 360;

}

    private void increaseCounter() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
        putDataMapReq.getDataMap().putString(COUNT_KEY, acc1+","+acc2+","+acc3+","+gyro1+","+gyro2+","+gyro3+","+mAzimuth);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("Here","Here");
    }
}

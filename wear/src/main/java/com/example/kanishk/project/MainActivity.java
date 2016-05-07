package com.example.kanishk.project;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import watch.nudge.gesturelibrary.AbstractGestureClientActivity;
import watch.nudge.gesturelibrary.GestureConstants;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

public class MainActivity extends AbstractGestureClientActivity implements  DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    SensorEventListener accelListener;
    private int mSensorType;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        setSubscribeWindowEvents(true);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
        activate();
    }

    public void activate(){
        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                accelListener = new SensorData(mGoogleApiClient);
                mSensorManager.registerListener(accelListener, mAccelerometer, SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(accelListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPause();
            }
        });
    }

    @Override
    public ArrayList<GestureConstants.SubscriptionGesture> getGestureSubscpitionList() {
        ArrayList<GestureConstants.SubscriptionGesture> gestures = new ArrayList<GestureConstants.SubscriptionGesture>();
        gestures.add(GestureConstants.SubscriptionGesture.FLICK);
        gestures.add(GestureConstants.SubscriptionGesture.SNAP);
        gestures.add(GestureConstants.SubscriptionGesture.TWIST);
        gestures.add(GestureConstants.SubscriptionGesture.TILT_X);
        return gestures;
    }

    @Override
    public boolean sendsGestureToPhone() {
        return true;
    }

    @Override
    public void onSnap() {
        Toast.makeText(this,"Snap it up",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFlick() {
        Toast.makeText(this,"Got a flick!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTwist() {
        Toast.makeText(this,"Just twist it",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTiltX(float v) {
        //Toast.makeText(this,"Change in the volume",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTilt(float v, float v1, float v2) {
//        //Toast.makeText(this,"X is"+v,Toast.LENGTH_LONG).show();
//        //Toast.makeText(this,"Y is"+v1,Toast.LENGTH_LONG).show();
//        //Toast.makeText(this,"Z is"+v2,Toast.LENGTH_LONG).show();
//        Log.d("Y value ", Float.toString(v1));
    }

    @Override
    public void onGestureWindowClosed() {
        Toast.makeText(this,"Gesture window closed.",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activate();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(accelListener);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

package com.example.alien.evernoteandroidjob;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TimeUtils;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.example.alien.evernoteandroidjob.DemoSyncJob.UPDATE_INTERVAL;

public class MainActivity extends AppCompatActivity {
    final String TAG = "job_demo_tag";

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "onLocationResult: ");
            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());
                //mTrackHelper.onRouteUpdate(newPosition);
                Log.d(TAG, "onLocationResult: " + newPosition.toString());

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JobManager.instance().cancelAll();
        //schedulePeriodicJob();
        scheduleOneTimeJob();
        //getLocation();
    }

    private void schedulePeriodicJob() {
        int jobId = new JobRequest.Builder(DemoSyncJob.TAG)
                .setPeriodic(TimeUnit.SECONDS.toMillis(900), TimeUnit.SECONDS.toMillis(300))
                .build()
                .schedule();
    }

    private void scheduleOneTimeJob() {
        int jobId = new JobRequest.Builder(DemoSyncJob.TAG)
                .setExact(TimeUnit.SECONDS.toMillis(1))
                //.setExecutionWindow(TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(60))
                .setBackoffCriteria(TimeUnit.SECONDS.toMillis(1), JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }

    private void getLocation() {
        new HandlerThread("proc") {
            @Override
            public void run() {
                Looper.prepare();
                final CountDownLatch mDoneSignal = new CountDownLatch(1);

                final int UPDATE_INTERVAL = 5000;
                final int UPDATE_FASTEST_INTERVAL = 2000;
                final int UPDATE_MIN_DISTANCE = 10;
                final String TAG = "job_demo_tag";

                FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(UPDATE_FASTEST_INTERVAL);
                mLocationRequest.setSmallestDisplacement(UPDATE_MIN_DISTANCE);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                Log.d(TAG, "onRunJob: ");
                try {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRunJob: PERMISSION_GRANTED ---------------------------------------------------------------------------------");
                        Task<Location> resTask = mFusedLocationProviderClient.getLastLocation();
                        int sleepCount = 0;
                        while (!resTask.isComplete() && sleepCount < 300) {
                            Thread.sleep(100);
                            Log.d(TAG, "getLocation: sleep");
                            sleepCount++;
                        }
                        Log.d(TAG, "getLocation: " + resTask.getResult().toString());
                    }
                } catch (Throwable e) {
                    Log.d(TAG, "onRunJob: " + e.getMessage());
                    e.printStackTrace();
                }
                try {
                    //  mDoneSignal.await(30, TimeUnit.SECONDS);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }.start();
    }
}


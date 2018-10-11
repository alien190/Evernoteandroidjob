package com.example.alien.evernoteandroidjob;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.evernote.android.job.Job;
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

public class DemoSyncJob extends Job {

    public static final String TAG = "job_demo_tag";
    public static final int UPDATE_INTERVAL = 5000;
    public static final int UPDATE_FASTEST_INTERVAL = 2000;
    public static final int UPDATE_MIN_DISTANCE = 10;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest = new LocationRequest();

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "onLocationResult: ");
            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());
                //mTrackHelper.onRouteUpdate(newPosition);
                Log.d(TAG, "onLocationResult: " + newPosition.toString());
                mDoneSignal.countDown();
            }
        }
    };
    private CountDownLatch mDoneSignal;

    @Override
    @NonNull

    protected Result onRunJob(Params params) {
        Log.d(TAG, "onRunJob: ");


        mDoneSignal = new CountDownLatch(1);


        new HandlerThread("proc") {
            @Override
            public void run() {
                Looper.prepare();
                final int UPDATE_INTERVAL = 5000;
                final int UPDATE_FASTEST_INTERVAL = 2000;
                final int UPDATE_MIN_DISTANCE = 10;
                final String TAG = "job_demo_tag";

                FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(UPDATE_FASTEST_INTERVAL);
                mLocationRequest.setSmallestDisplacement(UPDATE_MIN_DISTANCE);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                Log.d(TAG, "onRunJob: ");
                try {
                    if (ContextCompat.checkSelfPermission(getContext(),
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
                        mDoneSignal.countDown();
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

        try {
            Log.d(TAG, "onRunJob: await---------------------------------------------------------------------------------");
            mDoneSignal.await(30, TimeUnit.SECONDS);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return Result.RESCHEDULE;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(DemoSyncJob.TAG)
                .setExecutionWindow(30_000L, 40_000L)
                .build()
                .schedule();
    }
}

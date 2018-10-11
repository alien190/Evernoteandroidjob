package com.example.alien.evernoteandroidjob;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.CountDownLatch;

public class LocationJob extends Job {

    public static final String TAG = "LocationJobTag";
    public static final String RESCHEDULE_KEY = "LocationJobRescheduleKey";
    private static final CountDownLatch doneSignal = new CountDownLatch(1);
    private static final JobLocationCallback mCallback = new JobLocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "onLocationResult: " + locationResult.toString());
            doneSignal.countDown();
        }
    };

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        LocationThread thread = new LocationThread("locationThread", getContext(), mCallback);
        thread.start();
        try {
            doneSignal.await();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if (params.getExtras().getBoolean(RESCHEDULE_KEY, false)) {
            return Result.RESCHEDULE;
        } else {
            return Result.SUCCESS;
        }

    }

    private class LocationThread extends HandlerThread {

        private static final int UPDATE_INTERVAL = 5000;
        private static final int UPDATE_FASTEST_INTERVAL = 2000;
        private static final int UPDATE_MIN_DISTANCE = 10;

        private FusedLocationProviderClient mFusedLocationProviderClient;
        private LocationRequest mLocationRequest;
        private JobLocationCallback mCallback;


        public LocationThread(String name, Context context, JobLocationCallback callback) {
            super(name);
            mCallback = callback;
            init(context);
        }

        private void init(Context context) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(UPDATE_FASTEST_INTERVAL);
            mLocationRequest.setSmallestDisplacement(UPDATE_MIN_DISTANCE);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            Looper.prepare();
            try {
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (mCallback != null) {
                            mCallback.onLocationResult(locationResult);
                        }
                        quit();
                    }
                }, Looper.myLooper());
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Looper.loop();
        }
    }

    interface JobLocationCallback {
        void onLocationResult(LocationResult locationResult);
    }
}

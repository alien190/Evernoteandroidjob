package com.example.alien.evernoteandroidjob;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JobManager.instance().cancelAll();
        schedulePeriodicJob();
       // scheduleOneTimeJob();
    }

    private void schedulePeriodicJob() {
        int jobId = new JobRequest.Builder(DemoSyncJob.TAG)
                .setPeriodic(TimeUnit.SECONDS.toMillis(900), TimeUnit.SECONDS.toMillis(300))
                .build()
                .schedule();
    }

    private void scheduleOneTimeJob() {
        int jobId = new JobRequest.Builder(DemoSyncJob.TAG)
                //.setExact(TimeUnit.SECONDS.toMillis(1))
                .setExecutionWindow(TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(60))
                .setBackoffCriteria(TimeUnit.SECONDS.toMillis(1), JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }
}

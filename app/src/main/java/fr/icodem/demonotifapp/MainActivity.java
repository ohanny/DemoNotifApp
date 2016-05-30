package fr.icodem.demonotifapp;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.TimerTask;

/**
 * The main activity launches the job
 * that periodically fetches new events
 * from the server.
 */
public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private Handler myHandler;// marche mieux que TimerTask

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "Starting Job (Lollipop)");
            runJob();
        } else {
            Log.d(TAG, "Starting timer (before Lollipop)");
            myHandler = new Handler();
            myHandler.postDelayed(myRunnable, 5000);
        }
    }

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Timer triggered");
            Intent i= new Intent(MainActivity.this, GetNotificationService2.class);
            MainActivity.this.startService(i);
            myHandler.postDelayed(this, 30000);
        }
    };

    public void onPause() {
        super.onPause();
        if(myHandler != null)
            myHandler.removeCallbacks(myRunnable); // On arrête le callback
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void runJob() {
        JobScheduler jobScheduler =
                (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName =
                new ComponentName(getApplicationContext(), GetNotificationService.class);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("SOURCE", "RABBITMQ");
        //bundle.putString("SOURCE", "REST");

        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                .setRequiresCharging(false)
                .setPeriodic(10000)
                .setExtras(bundle)
                .build();
        jobScheduler.schedule(jobInfo);
    }


}

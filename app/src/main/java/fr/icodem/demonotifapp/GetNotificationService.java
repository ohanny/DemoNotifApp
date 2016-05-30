package fr.icodem.demonotifapp;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class GetNotificationService extends JobService {

    private static final String TAG = "GetNotificationService";

    private JobParameters params;

    private String source;

    private GetDataTask getDataTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job starting...");

        this.params = params;
        this.source = params.getExtras().getString("SOURCE");

        getDataTask = new GetDataTask();
        getDataTask.execute();

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job stopping...");

        if (getDataTask != null)
            getDataTask.cancel(true);

        return false;
    }

    private class GetDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            Log.d("GetDataTask", "Get data from source...");

            String data = null;

            if ("REST".equals(source)) {
                Log.d("GetDataTask", "Get data from REST...");
                try {
                    data = getEventFromRest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if ("RABBITMQ".equals(source)) {
                Log.d("GetDataTask", "Get data from RabbitMQ...");
                try {
                    data = getEventFromRabbitMQ();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            Log.d("GetDataTask", "Get data from source completed...");

            if (data != null) showNotification(data);

            jobFinished(params, false);
            super.onPostExecute(data);
        }
    }

    private String getEventFromRest() throws Exception {
        return UUID.randomUUID().toString(); // fake
    }

    private String getEventFromRabbitMQ() throws Exception {
        String queueName = "olivier";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("myserver");
        factory.setUsername("myuser");
        factory.setPassword("secret");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);

        String result = null;

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);

        QueueingConsumer.Delivery delivery = consumer.nextDelivery(10000);
        if (delivery != null) {
            result = new String(delivery.getBody());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }

        if (channel != null) channel.close();
        if (connection != null) connection.close();

        return result;
    }

    private void showNotification(String data) {
        int notificationId = 001;

        // intent triggered if the notification is selected
        Intent viewIntent = new Intent(this, ViewEventActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        // build notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notif)
                        .setContentTitle("The title")
                        .setContentText("The content : " + data)
                        .setContentIntent(viewPendingIntent);

        // get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // build the notification and issues it with notification manager.
        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationId, notification);
    }

}

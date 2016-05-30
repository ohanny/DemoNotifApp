package fr.icodem.demonotifapp;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.util.UUID;

// Notification service before Lollipop
public class GetNotificationService2 extends Service {

    private static final String TAG = "GetNotificationService2";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting service...");
        new GetDataTask().execute();
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class GetDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            Log.d("GetDataTask", "Get data from source...");

            String data = null;

            Log.d("GetDataTask", "Get data from RabbitMQ...");
            try {
                data = getEventFromRabbitMQ();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            Log.d("GetDataTask", "Get data from source completed..." + data);

            if (data != null) showNotification(data);

            super.onPostExecute(data);
        }
    }

    private String getEventFromRabbitMQ() throws Exception {
        String queueName = "olivier";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("SEM11.sesame.infotel.com");
        factory.setUsername("infotel");
        factory.setPassword("infotel");
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
        Log.d(TAG, "Showing notification : " + data);
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

        Log.d(TAG, "Notification done : " + data);
    }

}

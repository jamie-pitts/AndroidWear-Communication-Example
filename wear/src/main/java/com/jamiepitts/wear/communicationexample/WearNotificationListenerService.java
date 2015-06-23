package com.jamiepitts.wear.communicationexample;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import static com.jamiepitts.wear.communicationexample.shared.Constants.*;

/**
 * Service on the wear, listens out for communication from the handheld to respond to
 */
public class WearNotificationListenerService extends WearableListenerService {
    private static final String TAG = "NotificationLS";
    private static final int NOTIFICATION_ID = 1;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "On Message Received: " + messageEvent.getPath() + " " + new String(messageEvent.getData()));

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v(TAG, "On Data Changed: " + dataEvents.getCount() + ", " + dataEvents.getStatus());
        for (DataEvent event : dataEvents) {
            //If a notification request is sent over, create and display a notification on the wear
            if(event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().equals(PATH_WEARABLE_NOTIFICATION)) {
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                sendLocalNotification(dataMap);
            }
        }
    }

    /**
     * Creates and displays a notification locally on the wear device
     */
    private void sendLocalNotification(DataMap dataMap) {
        Intent startWearAppIntent = new Intent(this, WearActivity.class).setAction(Intent.ACTION_MAIN);
        startWearAppIntent.putExtra(NOTIFICATION_MESSAGE, dataMap.getString(NOTIFICATION_MESSAGE));
        PendingIntent startWearAppPendingIntent =
                PendingIntent.getActivity(this, 0, startWearAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(dataMap.getString(NOTIFICATION_TITLE))
                .setContentText(dataMap.getString(NOTIFICATION_BODY))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .addAction(R.mipmap.ic_launcher, "Open Wear App", startWearAppPendingIntent);

        Notification notification = notificationBuilder
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}

package com.jamiepitts.wear.communicationexample;

import android.app.Activity;
import android.app.Notification;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import static com.jamiepitts.wear.communicationexample.shared.CommunicationUtils.*;
import static com.jamiepitts.wear.communicationexample.shared.Constants.*;

public class HandHeldActivity extends Activity {
    private Button mButton, mMessageButton, mFullScreenAppButton;
    private EditText mMessageEditText;
    private NotificationManagerCompat mNotificationManager;
    private int mNotificationId = 1;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_held);

        mNotificationManager = NotificationManagerCompat.from(getApplication());
        mMessageEditText = (EditText) findViewById(R.id.textMessage);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addApi(Wearable.API)
                                .build();

        mButton = (Button) findViewById(R.id.create_notification_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotificationManager.notify(mNotificationId, createNotification());
                mNotificationId++;
            }
        });

        mMessageButton = (Button) findViewById(R.id.message_button);
        mMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(mGoogleApiClient, PATH_MESSAGE, mMessageEditText.getText().toString());
            }
        });

        mFullScreenAppButton = (Button) findViewById(R.id.wear_notification_button);
        mFullScreenAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataMap notifyWearable = new DataMap();
                notifyWearable.putString(NOTIFICATION_TITLE, "Open Wear App");
                notifyWearable.putString(NOTIFICATION_BODY, "< Swipe <");
                notifyWearable.putString(NOTIFICATION_MESSAGE, "message from notification");
                notifyWearable.putFloat(NOTIFICATION_TIME, System.currentTimeMillis());
                sendDataMap(mGoogleApiClient, PATH_WEARABLE_NOTIFICATION, notifyWearable);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Creates a notification, will appear both on the wear and the handheld device
     */
    private Notification createNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplication())
                                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                                .setContentTitle("Example notification")
                                                                .setContentText("Hello wear and mobile!");

        //Adds a second page to the notification on the watch
        //In this case it is just an image of the android logo
        NotificationCompat.Extender wearNotificationExtender = new NotificationCompat.WearableExtender()
                                                                    .addPage(new NotificationCompat.Builder(getApplication())
                                                                            .extend(new NotificationCompat.WearableExtender()
                                                                                    .setBackground(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                                                                    .setHintShowBackgroundOnly(true))
                                                                                    .build()
                                                                            );
        return notificationBuilder
                .extend(wearNotificationExtender)
                .build();
    }
}


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


public class HandHeldActivity extends Activity {
    private static final String TAG = "HandHeldActivity";

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
                sendMessage("/message", mMessageEditText.getText().toString());
            }
        });

        mFullScreenAppButton = (Button) findViewById(R.id.wear_notification_button);
        mFullScreenAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataMap notifyWearable = new DataMap();
                notifyWearable.putString("title", "Open Wear App");
                notifyWearable.putString("body", "< Swipe <");
                notifyWearable.putString("message", "message from notification");
                notifyWearable.putFloat("time", System.currentTimeMillis());
                sendDataMap(mGoogleApiClient, "/wearable_start", notifyWearable);
            }
        });
    }

    /**
     * Sends a string message to all connected nodes
     */
    private void sendMessage(final String path, final String message){
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, message.getBytes()).await();

                    if (result.getStatus().isSuccess()) {
                        Log.v(TAG, "Successfully sent message to node: " + node.getId());
                    } else {
                        Log.v(TAG, "Problem sending message to: " + node.getId());
                    }
                }
            }
        }).start();
    }

    public static void sendDataMap(final GoogleApiClient mGoogleApiClient, final String path,
                                   final DataMap dataMap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PutDataMapRequest dataMapRequest = PutDataMapRequest.create(path);
                dataMapRequest.getDataMap().putAll(dataMap);
                PutDataRequest request = dataMapRequest.asPutDataRequest();

                DataApi.DataItemResult result = Wearable.DataApi
                        .putDataItem(mGoogleApiClient, request).await();

                if (result.getStatus().isSuccess()) {
                    Log.v(TAG, "Successfully put data onto data api");
                } else {
                    Log.v(TAG, "Problem putting data onto data api");
                }
            }
        }).start();
    }

    /**
     * Creates a notification
     */
    private Notification createNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplication())
                                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                                .setContentTitle("Hello World!")
                                                                .setContentText("Notification from Jamie");

        //Adds a second page to the notification on the watch
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
}


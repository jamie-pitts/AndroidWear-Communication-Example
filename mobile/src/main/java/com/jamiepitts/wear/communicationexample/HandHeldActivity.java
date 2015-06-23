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
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class HandHeldActivity extends Activity{ //implements GoogleApiClient.ConnectionCallbacks,
                                               //           GoogleApiClient.OnConnectionFailedListener,
                                               //           MessageApi.MessageListener{
    private static final String TAG = "HandHeldActivity";

    private Button mButton, mMessageButton;
    private EditText editText;
    private NotificationManagerCompat notificationManager;
    private int notificationId = 1;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_held);

        notificationManager = NotificationManagerCompat.from(getApplication());
        editText = (EditText) findViewById(R.id.textMessage);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addApi(Wearable.API)
                                .build();

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationManager.notify(notificationId, createNotification());
                notificationId++;
            }
        });

        mMessageButton = (Button) findViewById(R.id.message_button);
        mMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("/message", editText.getText().toString());
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


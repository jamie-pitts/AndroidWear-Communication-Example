package com.jamiepitts.wear.communicationexample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WearActivity extends Activity implements MessageApi.MessageListener,
                                                      DataApi.DataListener,
                                                      GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "WearActivity";

    private TextView mTextView;
    private WatchViewStub mWatchViewStub;
    private Button mImageButton;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText("Waiting for message");
                mWatchViewStub = (WatchViewStub) stub.findViewById(R.id.watch_view_stub);
                mImageButton = (Button) stub.findViewById(R.id.image_button);
                mImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage("/request", "image");
                    }
                });
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "CONNECTION SUSPENDED!!");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Connected to Google Api Service");
        }
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    /**
     * Displays a message sent from the phone on the wear
     */
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.v(TAG, "Wear message received: " + messageEvent.getPath() + " " + new String(messageEvent.getData()));
        if (messageEvent.getPath().equals("/message")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(new String(messageEvent.getData()));
                }
            });
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for (DataEvent event : events) {
            //If a image has been sent over
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().equals("/image")) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset imageAsset = dataMapItem.getDataMap().getAsset("profileImage");
                Long timeSentAt = dataMapItem.getDataMap().getLong("time");

                Log.v(TAG, "Received Image! Took " + (System.currentTimeMillis() - timeSentAt) + "ms to send");

                final Bitmap bitmap = loadBitmapFromAsset(imageAsset);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWatchViewStub.setBackground(new BitmapDrawable(getResources(), bitmap));
                    }
                });
            }
        }
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
     * Extracts a bitmap from an asset
     */
    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result = mGoogleApiClient.blockingConnect(2000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}

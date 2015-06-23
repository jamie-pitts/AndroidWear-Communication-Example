package com.jamiepitts.wear.communicationexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;

import static com.jamiepitts.wear.communicationexample.BitmapUtils.createAssetFromBitmap;
import static com.jamiepitts.wear.communicationexample.BitmapUtils.downloadBitmap;
import static com.jamiepitts.wear.communicationexample.shared.CommunicationUtils.sendDataMap;
import static com.jamiepitts.wear.communicationexample.shared.Constants.*;

/**
 * Service that listens for communication from the wearable, in this case listens for requests for images
 */
public class ImageListenerService extends WearableListenerService {
    private static final String TAG = "ImageListenerService";

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
        if (messageEvent.getPath().equals(PATH_REQUEST)) {
            if(new String(messageEvent.getData()).equals(IMAGE)) {
                getAndSendImage();
            } else {
                Log.e(TAG, "Unknown request found for data: " + new String(messageEvent.getData()));
            }
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v(TAG, "On Data Changed: " + dataEvents.getCount() + ", " + dataEvents.getStatus());
        for (DataEvent event : dataEvents) {

        }
    }

    private void getAndSendImage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap;
                try {
                    bitmap = downloadBitmap("http://lorempixel.com/1024/768/animals/", getApplicationContext());
                } catch (IOException e) {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                }
                Asset asset = createAssetFromBitmap(bitmap);

                DataMap dataMap = new DataMap();
                dataMap.putAsset(IMAGE, asset);
                dataMap.putLong(IMAGE_TIME_SENT, System.currentTimeMillis());

                sendDataMap(mGoogleApiClient, IMAGE_REPLY, dataMap);
            }
        }).start();
    }

}

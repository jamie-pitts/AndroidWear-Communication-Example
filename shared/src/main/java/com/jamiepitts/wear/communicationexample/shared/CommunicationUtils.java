package com.jamiepitts.wear.communicationexample.shared;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class CommunicationUtils {
    private static final String TAG = "CommunicationUtils";

    /**
     * Sends a message containing a datamap using the data api
     */
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
     * Sends a string message to all connected nodes
     */
    public static void sendMessage(final GoogleApiClient mGoogleApiClient, final String path, final String message){
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
}

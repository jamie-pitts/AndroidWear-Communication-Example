package com.jamiepitts.wear.communicationexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.wearable.Asset;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    public static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        Log.v(TAG, "Compressed Image Size: " + (byteStream.size()/1024 + "kb"));
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    public static Bitmap downloadBitmap(String src, Context context) throws IOException {
       return Picasso.with(context).load(src).get();
    }
}

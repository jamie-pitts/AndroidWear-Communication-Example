package com.jamiepitts.wearapp.jamieswearableapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.wearable.Asset;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class BitmapUtils {

    public static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        Log.v("BitmapUtils", "Compressed Image Size: " + (byteStream.size()/1024 + "kb"));
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    public static Bitmap downloadBitmap(String src, Context context) throws IOException {
       return Picasso.with(context).load(src).get();
    }
}

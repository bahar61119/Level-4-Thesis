package com.thesis.application.handler;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.thesis.application.activities.ThesisActivity;

/**
 * Created by bahar61119 on 8/29/2015.
 */
public class MethodHandler {



    public static String getPath(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        Log.e(ThesisActivity.TAG, "get path method->> " + uri.getPath());
        return uri.getPath();
    }
}

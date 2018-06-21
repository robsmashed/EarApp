package com.example.robsmashed.facedetection.Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.robsmashed.facedetection.R;

import java.util.List;

public class CustomAdapterPictures extends ArrayAdapter<Picture> {
    private int resource;
    private LayoutInflater inflater;

    public CustomAdapterPictures(Context context, int resourceId, List<Picture> objects) {
        super(context, resourceId, objects);
        resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null)
            v = inflater.inflate(R.layout.list_photo, null);

        Picture c = getItem(position);
        ImageView image = v.findViewById(R.id.image);

        Bitmap bm = BitmapFactory.decodeByteArray(c.getImmagine(), 0, c.getImmagine().length);
        int width = bm.getWidth();
        int height = bm.getHeight();
        int min = Math.min(width, height);
        Bitmap cropImg = Bitmap.createBitmap(bm, 0,0, min, min);

        image.setImageBitmap(cropImg);

        image.setTag(position);

        return v;
    }
}

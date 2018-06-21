package com.example.robsmashed.facedetection.Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robsmashed.facedetection.R;

import java.util.List;

public class CustomAdapterGroupPictures extends ArrayAdapter<Picture> {
    private int resource;
    private LayoutInflater inflater;
    public int numberOfPhotos = 0;

    public CustomAdapterGroupPictures(Context context, int resourceId, List<Picture> objects) {
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

        if(position==5 && numberOfPhotos>1){
            v.findViewById(R.id.morePhotosOverlay).setVisibility(View.VISIBLE);
            TextView n = v.findViewById(R.id.morePhotosText);
            n.setText(String.valueOf(numberOfPhotos) + " >");
        }

        image.setImageBitmap(cropImg);
        image.setTag(position);

        return v;
    }
}

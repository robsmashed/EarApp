package com.example.robsmashed.facedetection.Classes;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.robsmashed.facedetection.R;
import com.example.robsmashed.facedetection.SubjectsActivity;

import java.util.List;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static android.support.v4.content.ContextCompat.getDrawable;

public class CustomAdapterSubjects extends ArrayAdapter<Subject> {
    private int resource;
    private LayoutInflater inflater;
    private int size;
    private Context context;

    public CustomAdapterSubjects(Context context, int resourceId, List<Subject> objects) {
        super(context, resourceId, objects);
        size = objects.size();
        resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null)
            v = inflater.inflate(R.layout.list_element, null);

        TextView nameTextView = v.findViewById(R.id.elem_lista_nome);
        LinearLayout l = v.findViewById(R.id.mainLayout);
        ImageView icona = v.findViewById(R.id.elem_lista_icona);


        if(position==size-1){
            nameTextView.setText("Aggiungi soggetto");
            icona.setImageResource(icona.getContext().getResources().getIdentifier("ic_add_green_24dp", "drawable", icona.getContext().getPackageName()));
            l.setTag("add");
        } else {
            Subject c = getItem(position);
            nameTextView.setText(c.getNome() + " " + c.getCognome());
            l.setTag(position);
        }

        return v;
    }
}

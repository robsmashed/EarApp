package com.example.robsmashed.facedetection.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.robsmashed.facedetection.R;

import java.util.List;

public class CustomAdapterNav extends ArrayAdapter<String> {
    private int resource;
    private LayoutInflater inflater;
    private int size;
    private Context context;

    public CustomAdapterNav(Context context, int resourceId, List<String> objects) {
        super(context, resourceId, objects);
        size = objects.size();
        resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null)
            v = inflater.inflate(R.layout.nav_element, null);

        TextView nameTextView = v.findViewById(R.id.elem_lista_nome);
        LinearLayout l = v.findViewById(R.id.mainLayout);
        ImageView icona = v.findViewById(R.id.elem_lista_icona);

        if(position==0){
            nameTextView.setText("Galleria");
            icona.setImageResource(icona.getContext().getResources().getIdentifier("ic_photo_library_grey600_48dp", "drawable", icona.getContext().getPackageName()));
            l.setTag(position);
        } else if(position==1){
            nameTextView.setText("Sessioni");
            icona.setImageResource(icona.getContext().getResources().getIdentifier("ic_assignment_grey600_48dp", "drawable", icona.getContext().getPackageName()));
            l.setTag(position);
        } else if(position==2){
            nameTextView.setText("Soggetti");
            icona.setImageResource(icona.getContext().getResources().getIdentifier("ic_supervisor_account_gray_24dp", "drawable", icona.getContext().getPackageName()));
            l.setTag(position);
        } else if(position==3){
            nameTextView.setText("Impostazioni");
            icona.setImageResource(icona.getContext().getResources().getIdentifier("ic_settings_grey600_48dp", "drawable", icona.getContext().getPackageName()));
            l.setTag(position);
        }

        return v;
    }
}

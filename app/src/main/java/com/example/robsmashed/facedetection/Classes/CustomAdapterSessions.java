package com.example.robsmashed.facedetection.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.robsmashed.facedetection.R;

import java.util.List;

public class CustomAdapterSessions extends ArrayAdapter<Session> {
    private int resource;
    private LayoutInflater inflater;
    private int size;

    public CustomAdapterSessions(Context context, int resourceId, List<Session> objects) {
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
            nameTextView.setText("Aggiungi sessione");
            icona.setImageResource(icona.getContext().getResources().getIdentifier("ic_add_green_24dp", "drawable", icona.getContext().getPackageName()));
            l.setTag("add");
        } else {
            icona.setImageResource(icona.getContext().getResources().getIdentifier("ic_assignment_grey600_48dp", "drawable", icona.getContext().getPackageName()));
            Session s = getItem(position);
            nameTextView.setText("Sessione " + s.getNumber());
            l.setTag(position);
        }

        return v;
    }
}

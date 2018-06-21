package com.example.robsmashed.facedetection;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robsmashed.facedetection.Classes.Association;
import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PhotoGroupFragment extends Fragment {
    private ArrayList<Session> sessions;
    private ArrayList<Subject> subjects;
    private String type = "";
    boolean more = false;
    private Session session;
    private Subject subject;
    private ArrayList<Picture> pictures;
    private List<Picture> picturesGroup = new ArrayList<>();
    private ArrayList<Picture> picturesToShow = new ArrayList<>();
    private TextView title;
    private ArrayList<Association> associations;

    public PhotoGroupFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("TYPE");
            pictures = getArguments().getParcelableArrayList("LIST");
            associations = getArguments().getParcelableArrayList("ASSOCIATIONS");
            if(type.equals("SESSION")) {
                session = getArguments().getParcelable("SESSION");
                for(Picture p: pictures){
                    if(p.findAssociation(associations).getSessione_id()==session.getId())
                        picturesToShow.add(p);
                }
            }
            else if(type.equals("SUBJECT")) {
                subject = getArguments().getParcelable("SUBJECT");
                for(Picture p: pictures){
                    if(p.findAssociation(associations).getSoggetto_id()==subject.getID())
                        picturesToShow.add(p);
                }
            }
            sessions = getArguments().getParcelableArrayList("SESSIONS");
            subjects = getArguments().getParcelableArrayList("SUBJECTS");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_browse_photos, container, false);
        title = v.findViewById(R.id.title);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPhotoClick(title);
            }
        });

        if(type.equals("SESSION"))
            title.setText("Sessione " + session.getNumber());
        else
            title.setText(subject.getNome() + " " + subject.getCognome());

        if(pictures.size()==0)
            v.findViewById(R.id.fragmentRootView).setVisibility(View.GONE);

        int rowCount = 3;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            rowCount = 4;

        final int[] photos = new int[6];
        photos[0] = R.id.photo0;
        photos[1] = R.id.photo1;
        photos[2] = R.id.photo2;
        photos[3] = R.id.photo3;
        photos[4] = R.id.photo4;
        photos[5] = R.id.photo5;

        for(int i=0; i<picturesToShow.size() && i<=6; i++) {
            if(i==rowCount)
                v.findViewById(R.id.photosRow2).setVisibility(View.VISIBLE); // aggiungi una riga
            if(i<=5) { // aggiungi le prime sei foto
                picturesGroup.add(picturesToShow.get(i));
                final int id = i;
                v.findViewById(photos[i]).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPhotoClick(v.findViewById(photos[id]));
                    }
                });
            } else { // se vi sono più di 6 foto
                more = true;
            }
        }

        List<ImageView> images = new ArrayList<>();
        images.add((ImageView)v.findViewById(R.id.photo0));
        images.add((ImageView)v.findViewById(R.id.photo1));
        images.add((ImageView)v.findViewById(R.id.photo2));
        images.add((ImageView)v.findViewById(R.id.photo3));
        images.add((ImageView)v.findViewById(R.id.photo4));
        images.add((ImageView)v.findViewById(R.id.photo5));

        for(int i=0; i<picturesGroup.size(); i++){
            Bitmap bm = BitmapFactory.decodeByteArray(picturesGroup.get(i).getImmagine(), 0, picturesGroup.get(i).getImmagine().length);

            // scala bitmap a dimensione miniatura
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getResources().getDisplayMetrics());
            if(bm.getWidth()>bm.getHeight()){
                float factor = px / (float) bm.getHeight();
                bm = Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() * factor), px, true);
            } else {
                float factor = px / (float) bm.getWidth();
                bm = Bitmap.createScaledBitmap(bm, px, (int) (bm.getHeight() * factor), true);
            }

            // crea miniatura quadrata
            int width = bm.getWidth();
            int height = bm.getHeight();
            int min = Math.min(width, height);
            Bitmap cropImg = Bitmap.createBitmap(bm, 0,0, min, min);

            if(i==5 && more){ // se le foto sono più di 6 aggiungi overlay
                v.findViewById(R.id.morePhotosOverlay).setVisibility(View.VISIBLE);
                TextView n = v.findViewById(R.id.morePhotosText);
                n.setText(String.valueOf(picturesToShow.size()-5) + " >");
            }

            images.get(i).setImageBitmap(cropImg);
            images.get(i).setTag(String.valueOf(i));
        }
        return v;
    }

    public void onPhotoClick(View view){
            String position = view.getTag().toString();
            if ((position.equals("5") && more) || position.equals("title")) { // se viene clickato l'overlay o il titolo mostra gruppo di foto
                Intent intent = new Intent(getActivity(), BrowseGroupPhotosActivity.class);
                intent.putExtra("TYPE", type);
                if(type.equals("SESSION"))
                    intent.putExtra("SESSION", session);
                else if(type.equals("SUBJECT"))
                    intent.putExtra("SUBJECT", subject);
                intent.putParcelableArrayListExtra("SESSIONS", sessions);
                intent.putParcelableArrayListExtra("SUBJECTS", subjects);
                intent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
                intent.putParcelableArrayListExtra("PICTURES", pictures);
                getActivity().startActivityForResult(intent, 1);
            } else { // altrimenti mostra singola foto
                Intent intent = new Intent(getActivity(), ViewPhotoActivity.class);
                intent.putExtra("PICTURE", picturesToShow.get(Integer.parseInt(position)));
                intent.putParcelableArrayListExtra("SESSIONS", sessions);
                intent.putParcelableArrayListExtra("SUBJECTS", subjects);
                intent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
                getActivity().startActivityForResult(intent, 2);
            }
    }
}

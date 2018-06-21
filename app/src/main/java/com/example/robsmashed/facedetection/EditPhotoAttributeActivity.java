package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.robsmashed.facedetection.Classes.Association;
import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.util.ArrayList;
import java.util.List;

public class EditPhotoAttributeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo_attribute);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        dbHelper = new DatabaseOpenHelper(this);

        List<String> array = new ArrayList<>();
        type = getIntent().getStringExtra("TYPE");
        picture = getIntent().getExtras().getParcelable("PICTURE");
        associations = getIntent().getExtras().getParcelableArrayList("ASSOCIATIONS");
        if(type.equals("SUBJECT")) {
            setTitle("Soggetto");
            subjects = getIntent().getExtras().getParcelableArrayList("SUBJECTS");
            for(int i=0; i<subjects.size(); i++)
                array.add(subjects.get(i).getNome() + " " + subjects.get(i).getCognome());
        }
        else if(type.equals("SESSION")) {
            setTitle("Sessione");
            sessions = getIntent().getExtras().getParcelableArrayList("SESSIONS");
            for(int i=0; i<sessions.size(); i++)
                array.add("Sessione " + sessions.get(i).getNumber());
        }
        else if(type.equals("PICTURE")) {
            array.add("Sinistro");
            array.add("Destro");
            setTitle("Orecchio");
        }

        listView = findViewById(R.id.listview);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_attribute_element, R.id.textViewList, array);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent();
                if(type.equals("SUBJECT")) {
                    dbHelper.updatePictureSubject(picture.getId(), subjects.get(position).getID());
                    for(Association a: associations){
                        if(a.getImmagine_id()== picture.getId())
                            a.setSoggetto_id(subjects.get(position).getID());
                    }
                }
                else if(type.equals("SESSION")) {
                    dbHelper.updatePictureSession(picture.getId(), sessions.get(position).getId());
                    for(Association a: associations){
                        if(a.getImmagine_id()== picture.getId())
                            a.setSessione_id(sessions.get(position).getId());
                    }
                }
                else if(type.equals("PICTURE")) {
                    if(listView.getItemAtPosition(position).toString().equals("Sinistro")) {
                        dbHelper.updatePictureEar(picture.getId(), "SX");
                        picture.setOrecchio("SX");
                    }
                    else {
                        dbHelper.updatePictureEar(picture.getId(), "DX");
                        picture.setOrecchio("DX");
                    }
                }
                returnIntent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
                returnIntent.putExtra("PICTURE", picture);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    private ArrayList<Association> associations;
    private Picture picture;
    private ArrayList<Subject> subjects;
    private ArrayList<Session> sessions;
    private DatabaseOpenHelper dbHelper;
    private String type;
    public ListView listView;
}

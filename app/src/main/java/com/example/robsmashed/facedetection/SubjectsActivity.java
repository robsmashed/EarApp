package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.CustomAdapterSubjects;
import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubjectsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Soggetti");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        setContentView(R.layout.activity_subjects);
        listView = findViewById(R.id.listview);

        deletedIDs = new ArrayList<>();
        if(savedInstanceState==null) {
            subjects = getIntent().getExtras().getParcelableArrayList("SUBJECTS");
            edited = false;
        } else{
            long[] array = savedInstanceState.getLongArray("DELETEDIDS");
            for(Long id: array)
                deletedIDs.add(id);
            subjects = savedInstanceState.getParcelableArrayList("SUBJECTS");
            edited = savedInstanceState.getBoolean("EDITED");
        }
        update();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long[] array = new long[deletedIDs.size()];
        for(int i=0; i<deletedIDs.size(); i++)
            array[i] = deletedIDs.get(i);
        outState.putLongArray("DELETEDIDS", array);
        outState.putParcelableArrayList("SUBJECTS", subjects);
        outState.putBoolean("EDITED", edited);
    }

    private void update(){
        Subject fakeSubject = new Subject();
        ArrayList<Subject> listToShow = ( ArrayList<Subject>) subjects.clone();
        listToShow.add(fakeSubject);
        customAdapter = new CustomAdapterSubjects(this, R.layout.list_element, listToShow);
        listView.setAdapter(customAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onItemClick(View view){
        if(view.getTag().toString().equals("add")){
            addSubject();
        } else{
            final int position = Integer.parseInt(view.getTag().toString());
            final Subject s = customAdapter.getItem(position);
            Intent intent = new Intent(this, SubjectDetailActivity.class);
            intent.putExtra("SUBJECT", s);
            intent.putExtra("EDITED", edited);
            startActivityForResult(intent, 1);
        }
    }

    public void addSubject() {
        final DatabaseOpenHelper dbHelper;
        dbHelper = new DatabaseOpenHelper(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(SubjectsActivity.this);
        builder.setTitle("Inserisci soggetto");

        final LayoutInflater inflater = this.getLayoutInflater();
        final View v = inflater.inflate(R.layout.subject_dialog, null);

        final EditText nome = v.findViewById(R.id.nameField);
        final EditText cognome = v.findViewById(R.id.surnameField);
        final EditText data = v.findViewById(R.id.dateField);
        data.addTextChangedListener(new TextWatcher() {
            boolean writed = false;
            boolean writed2 = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("WRITED", String.valueOf(writed));
                if(s.toString().length()==3 && !writed2) {
                    String thirdChar = data.getText().toString().substring(2, 3);
                    String firstTwoChars = data.getText().toString().substring(0, 2);
                    data.setText(firstTwoChars + "/" + thirdChar);
                    data.setSelection(data.getText().length());
                    writed2 = true;
                }
                if(s.toString().length()==6 && !writed) {
                    String sixthChar = data.getText().toString().substring(5, 6);
                    String firstFiveChars = data.getText().toString().substring(0, 5);
                    data.setText(firstFiveChars + "/" + sixthChar);
                    data.setSelection(data.getText().length());
                    writed = true;
                }
                if(s.toString().length()==5)
                    writed = false;
                if(s.toString().length()==2)
                    writed2 = false;
                if(s.toString().length()==11){
                    data.setText(data.getText().toString().substring(0, data.getText().toString().length()-1));
                    data.setSelection(data.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        builder.setView(v).setPositiveButton("Aggiungi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("LENGTH", String.valueOf( data.getText().toString().length()));
                final EditText nome = v.findViewById(R.id.nameField);
                final EditText cognome = v.findViewById(R.id.surnameField);
                final EditText data = v.findViewById(R.id.dateField);
                if(nome.getText().toString().matches("") || cognome.getText().toString().matches("") || data.getText().toString().length()<10)
                    Toast.makeText(getApplicationContext(), "Compila tutti i campi correttamente", Toast.LENGTH_LONG).show();
                else{
                    Subject s = new Subject();
                    s.setNome(nome.getText().toString());
                    s.setCognome(cognome.getText().toString());
                    s.setDataDiNascita(data.getText().toString());
                    long id = dbHelper.addSubject(s);
                    if(id>0) {
                        Toast.makeText(getApplicationContext(), "Soggetto aggiunto", Toast.LENGTH_LONG).show();
                        edited = true;
                        s.setID(id);
                        subjects.add(s);
                        Collections.sort(subjects);
                        update();
                    }
                }
            }
        }).setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if(edited) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SUBJECTS", subjects);
            resultIntent.putExtra("DELETEDIDS", deletedIDs);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==Activity.RESULT_OK){
            Subject s = data.getParcelableExtra("SUBJECT");
            if(data.getBooleanExtra("DELETE", false)){ // se il soggetto è stato cancellato
                deletedIDs.add(s.getID());
                boolean found = false;
                for(int i=0; i<subjects.size() && !found; i++){
                    if(subjects.get(i).getID()==s.getID()) {
                        found = true;
                        subjects.remove(i);
                    }
                }
                edited = true;
            } else if(data.getBooleanExtra("EDITED", false)){ // se il soggetto è stato eventualmente modificato
                boolean found = false;
                for(int i=0; i<subjects.size() && !found; i++){
                    if(subjects.get(i).getID()==s.getID()) {
                        found = true;
                        subjects.get(i).update(s);
                    }
                }
                edited = true;
            }
            update();
        }
    }

    private boolean edited;
    private ArrayList<Long> deletedIDs;
    private ArrayList<Subject> subjects;
    private CustomAdapterSubjects customAdapter;
    private ListView listView;
}

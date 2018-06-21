package com.example.robsmashed.facedetection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        setTitle("Impostazioni");

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        positiveFramesText = findViewById(R.id.positiveText);
        negativeFramesText = findViewById(R.id.negativeText);
        bordersText = findViewById(R.id.bordersText);
        if(prefs.getInt("FIXEDBORDER", 20)==0)
            bordersText.setText("Nessun bordo");
        else
            bordersText.setText(prefs.getInt("FIXEDBORDER", 20) + "px");
        positiveFramesText.setText(prefs.getInt("POSITIVEFRAMES", 15) + " frame");
        negativeFramesText.setText(prefs.getInt("NEGATIVEFRAMES", 2) + " frame");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void importDB(View view) {
        Toast.makeText(this, "Funzionalità ancora non supportata", Toast.LENGTH_LONG).show();
    }

    public void exportDB(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Esporta Database")
                .setMessage("Ogni database precedentemente esportato verrà sostituito, continuare comunque? Il database verrà esportato nella directory principale del dispositivo.")
                .setPositiveButton("Esporta", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File externalStorageDirectory = Environment.getExternalStorageDirectory();
                        File dataDirectory = Environment.getDataDirectory();

                        FileChannel source = null;
                        FileChannel destination = null;

                        String currentDBPath = "/data/" + getApplicationContext().getApplicationInfo().packageName + "/databases/" + getIntent().getStringExtra("DATABASENAME");
                        String backupDBPath = "earDetectionDB.sqlite";
                        File currentDB = new File(dataDirectory, currentDBPath);
                        File backupDB = new File(externalStorageDirectory, backupDBPath);

                        try {
                            source = new FileInputStream(currentDB).getChannel();
                            destination = new FileOutputStream(backupDB).getChannel();
                            destination.transferFrom(source, 0, source.size());
                            Toast.makeText(getApplicationContext(), "Database esportato", Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Errore durante l'esportazione", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        } finally {
                            try {
                                if (source != null) source.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (destination != null) destination.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }})
                .setNegativeButton("Annulla", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // non fare niente
                    }
                }).show();
    }

    public void onNegativeFramesSettingClick(View view) {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.positive_frames_dialog, null);

        List<String> stringList = new ArrayList<>();
        int j=0;
        int numberOfSettings;
        if(positiveFramesText.getText().toString().equals("5 frame"))
            numberOfSettings = 2;
        else if(positiveFramesText.getText().toString().equals("10 frame"))
            numberOfSettings = 3;
        else if(positiveFramesText.getText().toString().equals("15 frame"))
            numberOfSettings = 4;
        else
            numberOfSettings = 5;
        for(int i=0; i<numberOfSettings; i++) {
            stringList.add(j + " frame");
            j+=2;
        }
        RadioGroup rg = v.findViewById(R.id.radiogroup);
        for(int i=0; i<stringList.size(); i++){
            RadioButton rb = new RadioButton(SettingsActivity.this);
            rb.setText(stringList.get(i));
            rb.setTextSize(18);
            if(stringList.get(i).equals(negativeFramesText.getText().toString()))
                rb.setChecked(true);
            if(i<stringList.size()-1)
                rb.setPadding(20, 20, 0, 20);
            else
                rb.setPadding(20, 20, 0, 20);
            rg.addView(rb);
        }

        db.setView(v);
        db.setTitle("Frame negativi tollerati");
        db.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // non fare nulla
            }
        });

        final AlertDialog dialog = db.show();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for(int i=0; i<childCount; i++){
                    RadioButton btn = (RadioButton) group.getChildAt(i);
                    if(btn.getId() == checkedId){
                        negativeFramesText.setText(btn.getText().toString());
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveStateToPreferences();
    }

    public void onBordersSettingClick(View view) {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.positive_frames_dialog, null);

        List<String> stringList = new ArrayList<>();
        int j=0;
        for(int i=0; i<6; i++) {
            if(j==0)
                stringList.add("Nessun bordo");
            else
                stringList.add(j + "px");
            j+=20;
        }
        RadioGroup rg = v.findViewById(R.id.radiogroup);
        for(int i=0; i<stringList.size(); i++){
            RadioButton rb = new RadioButton(SettingsActivity.this);
            rb.setText(stringList.get(i));
            rb.setTextSize(18);
            if(stringList.get(i).equals(bordersText.getText().toString()))
                rb.setChecked(true);
            if(i<stringList.size()-1)
                rb.setPadding(20, 20, 0, 20);
            else
                rb.setPadding(20, 20, 0, 20);
            rg.addView(rb);
        }

        db.setView(v);
        db.setTitle("Bordo sulla regione rilevata");
        db.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        final AlertDialog dialog = db.show();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for(int i=0; i<childCount; i++){
                    RadioButton btn = (RadioButton) group.getChildAt(i);
                    if(btn.getId() == checkedId){
                        bordersText.setText(btn.getText().toString());
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    public void onPositiveFramesSettingClick(View view) {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.positive_frames_dialog, null);

        List<String> stringList = new ArrayList<>();
        int j=5;
        for(int i=0; i<4; i++) {
            stringList.add(j + " frame");
            j+=5;
        }
        RadioGroup rg = v.findViewById(R.id.radiogroup);
        for(int i=0; i<stringList.size(); i++){
            RadioButton rb = new RadioButton(SettingsActivity.this);
            rb.setText(stringList.get(i));
            rb.setTextSize(18);
            if(stringList.get(i).equals(positiveFramesText.getText().toString()))
                rb.setChecked(true);
            if(i<stringList.size()-1)
                rb.setPadding(20, 20, 0, 20);
            else
                rb.setPadding(20, 20, 0, 20);
            rg.addView(rb);
        }

        db.setView(v);
        db.setTitle("Frame positivi consecutivi");
        db.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog dialog = db.show();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for(int i=0; i<childCount; i++){
                    RadioButton btn = (RadioButton) group.getChildAt(i);
                    if(btn.getId() == checkedId){
                        positiveFramesText.setText(btn.getText().toString());
                        if(positiveFramesText.getText().toString().equals("5 frame") && Integer.parseInt(negativeFramesText.getText().toString().replace(" frame", ""))>2)
                            negativeFramesText.setText("2 frame");
                        else if(positiveFramesText.getText().toString().equals("10 frame") && Integer.parseInt(negativeFramesText.getText().toString().replace(" frame", ""))>4)
                            negativeFramesText.setText("4 frame");
                        else if(positiveFramesText.getText().toString().equals("15 frame") && Integer.parseInt(negativeFramesText.getText().toString().replace(" frame", ""))>6)
                            negativeFramesText.setText("6 frame");
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    private void saveStateToPreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        String s1 = positiveFramesText.getText().toString();
        String s2 = negativeFramesText.getText().toString();
        String s3 = bordersText.getText().toString();
        if(s3.equals("Nessun bordo"))
            editor.putInt("FIXEDBORDER", 0);
        else
            editor.putInt("FIXEDBORDER", Integer.parseInt(s3.replace("px", "")));
        editor.putInt("POSITIVEFRAMES", Integer.parseInt(s1.replace(" frame", "")));
        editor.putInt("NEGATIVEFRAMES", Integer.parseInt(s2.replace(" frame", "")));
        editor.commit();
    }

    private TextView bordersText;
    private TextView positiveFramesText;
    private TextView negativeFramesText;
    private SharedPreferences prefs;



}

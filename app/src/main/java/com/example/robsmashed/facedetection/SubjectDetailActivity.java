package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Subject;

public class SubjectDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_detail);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        dbHelper = new DatabaseOpenHelper(this);
        nome = findViewById(R.id.nomeText);
        cognome = findViewById(R.id.cognomeText);
        datadinascita = findViewById(R.id.dataText);

        if(savedInstanceState==null) {
            s = getIntent().getExtras().getParcelable("SUBJECT");
            edited = getIntent().getBooleanExtra("EDITED", false);
        } else {
            s = savedInstanceState.getParcelable("SUBJECT");
            edited = savedInstanceState.getBoolean("EDITED");
        }
        update();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("SUBJECT", s);
        outState.putBoolean("EDITED", edited);
    }

    private void update(){
        setTitle(s.getNome() + " " + s.getCognome());
        nome.setText(s.getNome());
        cognome.setText(s.getCognome());
        datadinascita.setText(s.getDataDiNascita());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mItemDelete = menu.add("Elimina soggetto");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemDelete) {
            new AlertDialog.Builder(this)
                    .setTitle("Attenzione!")
                    .setMessage("Quest'azione comporterà l'eliminazione di tutte le foto del soggetto, procedere comunque?")
                    .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (dbHelper.deleteSubject(s.getID()) > 0) {
                                Toast.makeText(getApplicationContext(), "Soggetto eliminato", Toast.LENGTH_LONG).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("DELETE", true);
                                resultIntent.putExtra("SUBJECT", s);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            } else
                                Toast.makeText(getApplicationContext(), "Errore durante l'eliminazione", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // non fare niente
                        }
                    }).show();
        } else if(item.getItemId() == android.R.id.home)
            onBackPressed();

        return true;
    }

    public void onItemClick(View v){
        final String pos = v.getTag().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.edit_detail_dialog, null);
        final TextView label = view.findViewById(R.id.label);
        final EditText field = view.findViewById(R.id.field);

        if(pos.equals("0")){
            // nome
            label.setText("Nome");
            field.setText(s.getNome(), TextView.BufferType.EDITABLE);
            builder.setTitle("Modifica nome");
        } else if(pos.equals("1")){
            // cognome
            label.setText("Cognome");
            field.setText(s.getCognome(), TextView.BufferType.EDITABLE);
            builder.setTitle("Modifica cognome");
        } else if(pos.equals("2")){
            // data di nascita
            label.setText("Data di nascita");
            field.setText(s.getDataDiNascita(), TextView.BufferType.EDITABLE);
            field.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
            builder.setTitle("Modifica data");
        }

        builder.setView(view).setPositiveButton("Modifica", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(field.getText().toString().matches(""))
                    Toast.makeText(getApplicationContext(), "Riprova compilando il campo di testo correttamente", Toast.LENGTH_LONG).show();
                else{
                    Subject newSubject = new Subject();
                    newSubject.setID(s.getID());
                    newSubject.setNome(s.getNome());
                    newSubject.setCognome(s.getCognome());
                    newSubject.setDataDiNascita(s.getDataDiNascita());

                    if(pos.equals("0")){
                        newSubject.setNome(field.getText().toString());
                    } else if(pos.equals("1"))
                        newSubject.setCognome(field.getText().toString());
                    else if(pos.equals("2"))
                        newSubject.setDataDiNascita(field.getText().toString());

                    if(dbHelper.updateSubject(newSubject)) {
                        s.update(newSubject);
                        Toast.makeText(getApplicationContext(), "Soggetto modificato", Toast.LENGTH_LONG).show();
                        edited = true;
                        update();
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Errore nella modifica", Toast.LENGTH_LONG).show();
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
        Intent resultIntent = new Intent();
        resultIntent.putExtra("EDITED", edited);
        resultIntent.putExtra("DELETE", false);
        resultIntent.putExtra("SUBJECT", s);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        super.onBackPressed();
    }

    private boolean edited;
    private TextView nome;
    private TextView cognome;
    private TextView datadinascita;
    private Subject s;
    private DatabaseOpenHelper dbHelper;
    private MenuItem mItemDelete;
}

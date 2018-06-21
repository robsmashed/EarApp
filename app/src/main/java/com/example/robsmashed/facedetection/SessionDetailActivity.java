package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Session;

public class SessionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        value = findViewById(R.id.value);
        dbHelper = new DatabaseOpenHelper(this);
        if(savedInstanceState==null) {
            s = getIntent().getExtras().getParcelable("SESSION");
            open = s.isOpen();
        } else {
            s = savedInstanceState.getParcelable("SESSION");
            open = savedInstanceState.getBoolean("OPEN");
        }
        sessionSwitch = findViewById(R.id.sessionSwitch);
        setTitle("Sessione " + s.getNumber());
        sessionSwitch.setChecked(s.isOpen());
        if(sessionSwitch.isChecked())
            value.setText("La sessione è aperta");
        else
            value.setText("La sessione è chiusa");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("SESSION", s);
        outState.putBoolean("OPEN", open);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mItemDelete = menu.add("Elimina sessione");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemDelete) {
            new AlertDialog.Builder(this)
                    .setTitle("Attenzione!")
                    .setMessage("Quest'azione comporterà l'eliminazione di tutte le foto della sessione, procedere comunque?")
                    .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(dbHelper.deleteSession(s.getId())>0) {
                                Toast.makeText(getApplicationContext(), "Sessione eliminata", Toast.LENGTH_LONG).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("DELETE", true);
                                resultIntent.putExtra("SESSION", s.getId());
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                            else
                                Toast.makeText(getApplicationContext(), "Errore durante l'eliminazione", Toast.LENGTH_LONG).show();
                        }})
                    .setNegativeButton("Annulla", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // non fare niente
                        }
                    }).show();
        } else if(item.getItemId() == android.R.id.home)
            onBackPressed();

        return true;
    }

    public void onSwitchClick(View v){
        dbHelper.setSessionOpen(s.getId(), sessionSwitch.isChecked());
        if(sessionSwitch.isChecked()) {
            s.setOpen(true);
            value.setText("La sessione è aperta");
        } else {
            s.setOpen(false);
            value.setText("La sessione è chiusa");
        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        if(s.isOpen()!=open)
            resultIntent.putExtra("EDITED", true);
        resultIntent.putExtra("SESSION", s.getId());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        super.onBackPressed();
    }

    private boolean open;
    private Switch sessionSwitch;
    private TextView value;
    private Session s;
    private DatabaseOpenHelper dbHelper;
    private MenuItem mItemDelete;
}

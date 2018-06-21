package com.example.robsmashed.facedetection;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PhotoInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_info);
        setTitle("Informazioni");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Picture p = getIntent().getExtras().getParcelable("PICTURE");
        TextView t1 = findViewById(R.id.dataText);
        String timestamp = p.getData();
        if(timestamp.length()>0)
            t1.setText(timestamp.substring(0,10));
        TextView t2 = findViewById(R.id.dayText);
        if(timestamp.length()>0)
            t2.setText(getDayOfTheWeek(timestamp.substring(0,10)) + " " + timestamp.substring(11, 16));

        TextView t3 = findViewById(R.id.orecchioText);
        t3.setText("Orecchio " + p.getOrecchio());
        TextView t4 = findViewById(R.id.mpText);
        Subject s1 = getIntent().getExtras().getParcelable("SUBJECT");
        Session s2 = getIntent().getExtras().getParcelable("SESSION");
        t4.setText(s1.getNome() + " " + s1.getCognome() + ", Sessione " + s2.getNumber());

        TextView t5 = findViewById(R.id.infoText1);
        t5.setText("Config " + p.getConfig());
        TextView t6 = findViewById(R.id.infoText2);
        t6.setText(p.getWidth() + "x" + p.getHeight() + " " + humanReadableByteCount(p.getByteCount(), false));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private String getDayOfTheWeek(String date){
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        String day;
        if(dayOfWeek==2)
            day = "Lunedì";
        else if(dayOfWeek==3)
            day = "Martedì";
        else if(dayOfWeek==4)
            day = "Mercoledì";
        else if(dayOfWeek==5)
            day = "Giovedì";
        else if(dayOfWeek==6)
            day = "Venerdì";
        else if(dayOfWeek==7)
            day = "Sabato";
        else
            day = "Domenica";
        return day;
    }
}

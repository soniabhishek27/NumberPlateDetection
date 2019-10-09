package com.example.finaltextdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Data extends AppCompatActivity {
    TextView setdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        setdata=findViewById(R.id.setdata);

            Intent intent= getIntent();
            String key =intent.getStringExtra("value");

            setdata.setText(key);


    }
}

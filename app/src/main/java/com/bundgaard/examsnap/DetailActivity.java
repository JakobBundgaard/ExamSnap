package com.bundgaard.examsnap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;


import com.bundgaard.examsnap.repo.MyAdapter;
import com.bundgaard.examsnap.repo.Repo;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements TaskListener {


    ImageView imageView;
    private Bitmap currentBitmap;
    private String currentNote;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        currentNote = getIntent().getStringExtra("noteid");
        imageView = findViewById(R.id.imageView2);


        Repo.r().downloadBitmap(currentNote, DetailActivity.this);
    }

    @Override
    public void receive(byte[] bytes) {
        // figure out, how to get the byte array to an image, and from there to the imageView

        currentBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(currentBitmap);
    }
    @Override
    public void onBackPressed(){
        Repo.r().deleteNote(currentNote);
        finish();
    }
}
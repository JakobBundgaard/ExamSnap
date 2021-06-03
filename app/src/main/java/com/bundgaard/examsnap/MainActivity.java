package com.bundgaard.examsnap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bundgaard.examsnap.repo.MyAdapter;
import com.bundgaard.examsnap.repo.Repo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Updatable, TaskListener{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;

    List<String> items = new ArrayList<>();

    ListView listView;
    MyAdapter myAdapter;

    Button cameraBtn;
    Bitmap imageBitmap;

    ImageView imageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = (Button) findViewById(R.id.cameraBtn);
        setupListView();

        imageView = (ImageView) findViewById(R.id.imageView2);


        Repo.r().setup(this, items);

    }

    public void cameraBtnPressed(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // ask for some app, to handle the camera
        try {
            startActivityForResult(intent, 1); // provide a different number to identify the

        }catch (ActivityNotFoundException e) {
            System.out.println("Error: " + e);
        }
        System.out.println("camera kaldet");
    }

    private void setupListView() {
        listView = findViewById(R.id.listView1);
        myAdapter = new MyAdapter(items, this);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            System.out.println("click on row: " + position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("noteid", items.get(position));
            startActivity(intent);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            assert data != null;
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = buildEditText(builder);

        yesBtnClick(builder, input);

        noBtnClick(builder);

        builder.show();
    }
/*
buildEditText er hvor dialog boksen til at påføre tekst til billedet sker. Metodenbliver kaldt med
med en alertBuilder som parameter. Der instantieres et editable textfield og layoutet defineres. Der bliver påført en title,
 viewet bliver sat og returneret.
 */
    @NotNull
    private EditText buildEditText(AlertDialog.Builder builder) {
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setTitle("Add text to image");
        builder.setView(input);
        return input;
    }
/*
I yesBtnClick bliver text og billede gemt når der clickes på yes. Her tages builderen og inputtet fra før som Parameter
. Vi tilføjer en onClicklistener og bruger metoden drawTextToBitmap, som vi fik af dig i lektionen, ogtoString
til at fange og gemme teksten fra input feltet sammen med billedet. Tilsidst kaldes updateTextAndImage, også fra dig,
og så bruges Repo til at gemme i firestoe og firestorage
 */

    private void yesBtnClick(AlertDialog.Builder builder, EditText input) {
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                drawTextToBitmap(input.getText().toString());
                System.out.println("From textField: " + input.getText().toString());
                Repo.r().updateNoteAndImage(input.getText().toString(), imageBitmap);
            }
        });
    }

    private void noBtnClick(AlertDialog.Builder builder) {
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"You cancelled text",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void drawTextToBitmap(String gText) {
        Bitmap.Config bitmapConfig = imageBitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        imageBitmap = imageBitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(imageBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);// new antialised Paint
        paint.setColor(Color.rgb(161, 161, 161));
        paint.setTextSize((int) (20)); // text size in pixels
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow
        canvas.drawText(String.valueOf(gText), 10, 100, paint);


    }

    @Override
    public void update(Object o) {
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void receive(byte[] bytes) {

        imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        imageView.setImageBitmap(imageBitmap);
    }
}
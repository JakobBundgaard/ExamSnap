package com.bundgaard.examsnap.repo;

import android.graphics.Bitmap;

import com.bundgaard.examsnap.TaskListener;
import com.bundgaard.examsnap.Updatable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repo {
    private static Repo repo = new Repo();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    public List<String> items = new ArrayList<>(); // you could use Note, instead of String
    private final String NOTES = "notes";
    private Updatable activity;
    public static Repo r(){
        return repo;
    }

    public void setup(Updatable a, List<String> list){
        activity = a;
        this.items = list;
        startListener();
    }


    public void startListener(){
        db.collection(NOTES).addSnapshotListener((values, error) ->{
            items.clear();
            //assert values != null;
            for(DocumentSnapshot snap: values.getDocuments()){
                Object title = snap.get("title");
                if(title != null){
                    items.add(snap.getId());
                }
                System.out.println("Snap: " + snap.toString());
            }
            // have a reference to MainActivity, and call a update()
            activity.update(null);
        });
    }




    public void deleteNote(String id){
        db.collection(NOTES).document(id).delete();
        storage.getReference(NOTES + "/" + id).delete();
    }

    public void updateNoteAndImage(String note, Bitmap bitmap) {
        DocumentReference docs = db.collection(NOTES).document();
        Map<String,String> map = new HashMap<>();
        map.put("title", note);
        docs.set(map); // will replace any previous value.
        String id = docs.getId();
        System.out.println("Done updating document " + docs.getId());
        System.out.println("uploadBitmap called " + bitmap.getByteCount());
        StorageReference ref = storage.getReference(NOTES + "/" + id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ref.putBytes(baos.toByteArray()).addOnCompleteListener(snap -> {
            System.out.println("OK to upload " + snap);
        }).addOnFailureListener(exception -> {
            System.out.println("failure to upload " + exception);
        });
    }

    public void downloadBitmap(String id, TaskListener taskListener){ // when to call this method?
        StorageReference ref = storage.getReference(NOTES + "/" + id);
        int max = 1024 * 1024; // you are free to set the limit here
        ref.getBytes(max).addOnSuccessListener(bytes -> {
            taskListener.receive(bytes); // god linie!
            System.out.println("Download OK");
        }).addOnFailureListener(ex -> {
            System.out.println("error in download " + ex);
        });
    }


}
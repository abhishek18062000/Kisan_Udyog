package com.example.kisan_udyog.sell;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kisan_udyog.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;



import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class  AddPostActivity extends AppCompatActivity {
    private static final String TAG = "AddPostActivity";
    EditText title_blog , description_blog ;
    Button upload ;
    ImageView blog_image ;
    private FirebaseAuth mAuth;

    Uri image_uri = null ;
    private static final  int GALLERY_IMAGE_CODE = 100 ;
    private static final  int CAMERA_IMAGE_CODE = 200 ;
    ProgressDialog pd ;
    FirebaseAuth auth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        title_blog = findViewById(R.id.title_blog);
        description_blog = findViewById(R.id.description_blog);
        upload = findViewById(R.id.upload);
        blog_image = findViewById(R.id.post_image_blog);

        pd = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();


        blog_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = title_blog.getText().toString();
                String description = description_blog.getText().toString();

                if (TextUtils.isEmpty(title)){
                    title_blog.setError("Title is required");
                }
                else if (TextUtils.isEmpty(description)){
                    description_blog.setError("Description is required");
                }
                else {
                    uploadData(title , description);
                }
            }
        });

    }

    private void uploadData(final String title, final String description) {

        pd.setMessage("Publising post");
        pd.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        final String filepath = "Posts/"+"post_"+timeStamp;

        if (blog_image.getDrawable() != null){
            Bitmap bitmap = ((BitmapDrawable)blog_image.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG , 100 , baos);
            byte[] data = baos.toByteArray();

            final StorageReference reference = FirebaseStorage.getInstance().getReference().child(filepath);
            UploadTask uploadTask = reference.putBytes(data);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                throw task.getException();
                }
                    Log.d(TAG,"URL IS  "+ reference.getDownloadUrl());

                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Intent intent = getIntent();
                        String total = intent.getStringExtra("total");
                        String quantity = intent.getStringExtra("quantity");
                        //mtypePrice.setText(typePrice);
                        Uri downloadUril = task.getResult();
                        Log.d(TAG,"URLS IS  "+ downloadUril);
                        String downloadUri=downloadUril.toString();
                        FirebaseUser user = auth.getCurrentUser();
                        HashMap<String , Object> hashMap = new HashMap<>();
                        hashMap.put("uid" , user.getUid());
                        hashMap.put("pQuantity" , quantity);
                        hashMap.put("pPrice" , total);
                        hashMap.put("uEmail" , user.getEmail());
                        hashMap.put("pId" , timeStamp);
                        hashMap.put("pTitle" , title);
                        hashMap.put("pImage" , downloadUri);
                        hashMap.put("pDescription" , description);
                        hashMap.put("pTime" ,  timeStamp);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timeStamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();;
                                        Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                        title_blog.setText("");
                                        description_blog.setText("");
                                        blog_image.setImageURI(null);
                                        image_uri = null ;
                                    }
                                });
                    } else {
                    }
                }
            });

           /* reference.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            //here uri.Tast is not success to end the while loop so put not equal to sing !
                           // while (!uriTask.isSuccessful());



                            if (uriTask.isSuccessful()){
                                //uri is recieved post is publised to database
                                String downloadUri = uriTask.getResult().toString();
                                //now we will upload the daata to firebase database for
                                FirebaseUser user = auth.getCurrentUser();

                                HashMap<String , Object> hashMap = new HashMap<>();

                                hashMap.put("uid" , user.getUid());
                                hashMap.put("uEmail" , user.getEmail());
                                hashMap.put("pId" , timeStamp);
                                hashMap.put("pTitle" , title);
                                hashMap.put("pImage" , downloadUri);
                                hashMap.put("pDescription" , description);
                                hashMap.put("pTime" ,  timeStamp);

                                //now we will pust the data to firebase database
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();;
                                                Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                                title_blog.setText("");
                                                description_blog.setText("");
                                                blog_image.setImageURI(null);
                                                image_uri = null ;

                                                //when post is publised user must go to home activity means main dashboad
                                                startActivity(new Intent(AddPostActivity.this , SellActivity.class));


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }); */
        }
    }

    private void imagePickDialog() {
        //here 0 is for camera and 1 is for gallery so please do it like me
        String[] options = {"Camera" , "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    cameraPick();
                }
                if (which == 1){
                    galleryPick();

                }
            }
        });

        builder.create().show();
    }

    private void cameraPick() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE , "Temp Pick");
        contentValues.put(MediaStore.Images.Media.TITLE , "Temp desc");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT , image_uri);
        startActivityForResult(intent , CAMERA_IMAGE_CODE);
    }

    private void galleryPick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent , GALLERY_IMAGE_CODE);
    }

 /*   private void permission(){

        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken toke) {
                        toke.continuePermissionRequest();
                    }
                }).check();
        //hold alt key and press enter to import the library
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... }
        }).check();
    } */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == GALLERY_IMAGE_CODE){
                image_uri = data.getData();
                blog_image.setImageURI(image_uri);
            }
            if (requestCode == CAMERA_IMAGE_CODE){
                blog_image.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
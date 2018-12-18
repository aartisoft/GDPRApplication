package com.example.abbieturner.gdprapplication.UI.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.abbieturner.gdprapplication.Models.User;
import com.example.abbieturner.gdprapplication.R;
import com.example.abbieturner.gdprapplication.VM.MainViewModel;
import com.example.abbieturner.gdprapplication.utils.Utils;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class UpdateProfileActivity extends AppCompatActivity {

    @BindView(R.id.img_profile)
    ImageView img_profile;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_address)
    EditText etAddress;
    @BindView(R.id.et_phone)
    EditText etPhone;
    @BindView(R.id.et_fax)
    EditText etFax;
    @BindView(R.id.et_lang)
    Spinner etLang;
    @BindView(R.id.et_ethnicity)
    Spinner etEthn;
    @BindView(R.id.et_medical)
    EditText etMedical;
    @BindView(R.id.et_work_hour)
    EditText etWorkHour;
    @BindView(R.id.et_work_place)
    EditText etWorkPlace;
    @BindView(R.id.btn_update)
    Button btn_update;

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private StorageReference mStorageReference;
    MainViewModel mainViewModel;
    private HashMap hashMap;
    private static Uri photoUri;
    private boolean hasImage = false;
    public static final int PIC_IMAGE_PICKER = 19;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        hashMap = new HashMap();

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getUserLiveData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                startWait("Fetching data");
                if (user != null) {
                    BindDataToUI(user);
                    //check if user had image or not
                    hasImage = !user.getProfile().equals("default");
                }
            }
        });

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(UpdateProfileActivity.this)) {
                    if (hasImage) {
                        ImageChoice();
                    } else {
                        startImagePicker();
                    }
                } else {
                    Toasty.warning(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT, true).show();
                }

            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpdating();
            }
        });

    }

    private void startUpdating() {
        if (Utils.isNetworkAvailable(this)) {
            if (Utils.checkError(etName) && Utils.checkError(etAddress) && Utils.checkError(etPhone)
                    && Utils.checkError(etFax) && Utils.checkError(etMedical)
                    && Utils.checkError(etWorkHour)
                    && Utils.checkError(etWorkPlace) && Utils.checkPhoneSize(etPhone)) {

                startWait("Updating data");

                hashMap.put("name", etName.getText().toString());
                hashMap.put("address", etAddress.getText().toString());
                hashMap.put("phone", etPhone.getText().toString());
                hashMap.put("fax", etFax.getText().toString());
                hashMap.put("admin", false);
                hashMap.put("lang", etLang.toString());
                hashMap.put("medical", etMedical.getText().toString());
                hashMap.put("ethnicity", etEthn.toString());
                hashMap.put("workHour", etWorkHour.getText().toString());
                hashMap.put("workPlace", etWorkPlace.getText().toString());

                mRootRef.child("users").child(mAuth.getCurrentUser().getUid())
                        .updateChildren(hashMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                stopWait();
                                Toasty.success(getApplicationContext(),
                                        "Profile data updated",
                                        Toast.LENGTH_SHORT, true).show();
                            }
                        });

            }
        } else {
            Toasty.warning(this, "Check internet connection", Toast.LENGTH_SHORT, true).show();
        }
    }

    private void BindDataToUI(User user) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Ethnicitys, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etEthn.setAdapter(adapter);
        if (user.getEthnicity() != null) {
            int spinnerPosition = adapter.getPosition(user.getEthnicity() );
            etEthn.setSelection(spinnerPosition);
        }

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.Languages, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etLang.setAdapter(adapter1);
        if (user.getLang() != null) {
            int spinnerPosition = adapter.getPosition(user.getLang() );
            etLang.setSelection(spinnerPosition);
        }

        Picasso.get().load(user.getProfile()).placeholder(R.drawable.person_placeholder).into(img_profile);
        etName.setText(user.getName());
        etAddress.setText(user.getAddress());
        etPhone.setText(user.getPhone());
        etFax.setText(user.getFax());
        etMedical.setText(user.getMedical());
        etWorkPlace.setText(user.getWorkPlace());
        etWorkHour.setText(user.getWorkHour());

        stopWait();
    }


    private void ImageChoice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("Pick Image Profile or delete it")
                .setPositiveButton("Pick Image", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startImagePicker();
                    }
                })
                .setNegativeButton("Delete Image", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteImage();
                    }
                });
        builder.show();
    }

    private void DeleteImage() {
        startWait("Deleting Image");

        final StorageReference spaceRef = mStorageReference.child("users");

        spaceRef.child(mAuth.getCurrentUser().getUid() + ".jpg")
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    mRootRef.child("users").child(mAuth.getCurrentUser().getUid())
                            .child("profile").setValue("default")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        hasImage = false;
                                        img_profile.setImageResource(R.drawable.person_placeholder);
                                        stopWait();
                                        Toasty.success(getApplicationContext(),
                                                "Profile image deleted",
                                                Toast.LENGTH_SHORT, true).show();
                                    }
                                }
                            });


                }
            }
        });
    }

    private void startImagePicker() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Pix.start(UpdateProfileActivity.this, PIC_IMAGE_PICKER);
                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            Utils.goToImageSettings(UpdateProfileActivity.this);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    void startWait(String msg) {
        progressDialog.setTitle(msg);
        progressDialog.setMessage("Please wait ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    void stopWait() {
        progressDialog.dismiss();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PIC_IMAGE_PICKER) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            photoUri = Uri.fromFile(new File(returnValue.get(0)));
            img_profile.setImageURI(photoUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        startWait("Uploading profile image");
        final StorageReference spaceRef = mStorageReference.child("users");
        spaceRef.child(mAuth.getCurrentUser().getUid() + ".jpg")
                .putFile(photoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        spaceRef.child(mAuth.getCurrentUser().getUid() + ".jpg")
                                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mRootRef.child("users").child(mAuth.getCurrentUser().getUid())
                                        .child("profile").setValue(uri.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    stopWait();
                                                    Toasty.success(getApplicationContext(),
                                                            "Profile image uploaded",
                                                            Toast.LENGTH_SHORT, true).show();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
    }
}

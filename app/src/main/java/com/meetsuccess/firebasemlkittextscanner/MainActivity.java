package com.meetsuccess.firebasemlkittextscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Text;
//import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.text.FirebaseVisionText;
//import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String value;
    private File currentPictureFile;
    private static final int IMAGE_REQUEST_CODE = 2;
    TextView detetectedtext;

    private static final String TIME24HOURS_PATTERN =
            "([01]?[0-9]|2[0-3]):[0-5][0-9]";
    String housenumber = "\\d{1,2}-\\d{1,2}$";
    String format1 = "\\d{1,2}/\\d{1,2}/\\d{2,4}";
    String format2 = "\\d{1,2}-\\d{1,2}-\\d{2,4}";
    String format3 = "\\d{4}/\\d{1,2}/\\d{1,2}";
    String format4 = "\\d{4}-\\d{1,2}-\\d{4}";
    String format5 = "\\d{1,2} \\d{1,2} \\d{4}";
    String format6 = "\\d{4} \\d{1,2} \\d{1,2}";
    String format7 = "\\d{4}\\.\\d{1,2}\\.\\d{1,2}";
    String format8 = "\\d{1,2}\\.\\d{1,2}\\.\\d{4}";
    String format9 = "\\d{4}\\.[a-zA-Z]{1,12}\\.\\d{1,2}";
    String format10 = "\\d{1,2}\\.[a-zA-Z]{1,12}\\.\\d{4}";
    String format11 = "[a-zA-Z]{1,12} \\d{1,2}\\,\\d{4}";
    String format12 = "[a-zA-Z]{1,12} \\d{1,2}\\, \\d{4}";
    String format13="\\d{1,2} [a-zA-Z]{1,12} \\d{2,4}\n";
    String format14="\\d{1,2}\\.[a-zA-Z]{1,12}\\.\\d{2,4}\n";

    List<String> dateformats;
    TextView dategetting, titletext, amount, Receiptno,gst_amount;


    ImageView mImageView;
    // Button cameraBtn;
    // ImageButton detectBtn;
    Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    //  TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detetectedtext=findViewById(R.id.detetectedtext);
        gst_amount=findViewById(R.id.gst_amount);
        dateformats = new ArrayList<>();
        dateformats.add(format1);
        dateformats.add(format2);
        dateformats.add(format3);
        dateformats.add(format4);
        dateformats.add(format5);
        dateformats.add(format6);
        dateformats.add(format7);
        dateformats.add(format8);
        dateformats.add(format9);
        dateformats.add(format10);
        dateformats.add(format11);
        dateformats.add(format12);
        dateformats.add(format13);
        dateformats.add(format14);
        dategetting = findViewById(R.id.recognizeText);
        titletext = findViewById(R.id.titletext);
        amount = findViewById(R.id.amounttext);
        Receiptno = findViewById(R.id.Receiptno);

        mImageView = findViewById(R.id.quick_start_cropped_image);


        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPictureDialog();
            }
        });

    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {"Select photo from gallery", "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Dexter.withActivity(MainActivity.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
                                    @Override
                                    public void onPermissionGranted(PermissionGrantedResponse response) {
                                        choosePhotoFromGallary();
                                    }

                                    @Override
                                    public void onPermissionDenied(PermissionDeniedResponse response) {

                                    }

                                    @Override
                                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                                    }
                                }).check();

                                break;
                            case 1:
                                openCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, 2);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap1 = null;

            // Uri uri = data.getData();
            Bitmap image = null;
            Uri contentUri = null;
            if (Build.VERSION.SDK_INT >= 24) {
                contentUri = FileProvider.getUriForFile(this,
                        this.getPackageName() + ".provider",//"com.infotech.mobileattendancenew.provider",
                        currentPictureFile);
            } else {
                contentUri = Uri.fromFile(currentPictureFile);
            }

            try {
                bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {

                File imgFile = new File(currentPictureFile.getAbsolutePath());
                ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                imageBitmap = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true); // rotating bitmap
                mImageView.setImageBitmap(imageBitmap);
                InputImage image1 = InputImage.fromBitmap(imageBitmap, 0);

                detectImgchk(image1);
               // detectImg();
            } catch (Exception e) {

            }



            mImageView.setImageBitmap(imageBitmap);
        } else if (requestCode == 2) {
            Uri uri = data.getData();

Log.d("uriii",uri+"");
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.OFF)
                    .setInitialCropWindowPaddingRatio(0)
                    .start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.d("imaagesetup","skdjd");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    imageBitmap = bitmap;
                    mImageView.setImageBitmap(imageBitmap);
                    InputImage image1 = InputImage.fromBitmap(imageBitmap, 0);

                    detectImgchk(image1);
                   // Toast.makeText(this,detectImg(),Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }




    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri contentUri = null;
        currentPictureFile = getMediaFile(this);
        if (Build.VERSION.SDK_INT >= 24) {
            this.getPackageName();
            contentUri = FileProvider.getUriForFile(this,
                    this.getPackageName() + ".provider",
                    currentPictureFile);
        } else {
            contentUri = Uri.fromFile(currentPictureFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        startActivityForResult(intent, 1);
    }

    public static File getMediaFile(Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File mediaStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.e("MyCameraApp", mediaStorageDir.getAbsolutePath());
        try {
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
            return new File(mediaStorageDir.getPath() + File.separator + "INFOTECH_" + timeStamp + ".jpg");//File.separator + "Images" +
        } catch (Exception e) {
            return null;
        }
    }
    public void detectImgchk( InputImage image) {


        TextRecognizer recognizer = TextRecognition.getClient();
        Task<com.google.mlkit.vision.text.Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<com.google.mlkit.vision.text.Text>() {
                            @Override
                            public void onSuccess(com.google.mlkit.vision.text.Text firebaseVisionText) {
                                String str = firebaseVisionText.getText().toString();
                                Log.d("gettingtext",str+"--");


                                int i = 0;
                                String gettingDate=gettingdatepartfromregex(str, i);
                                dategetting.setText(gettingDate);
                                String titleReceipt=getttingTitleComapny(str);
                                titletext.setText(titleReceipt);

                                String TotalAmount=GettingAmountFromDetections(str.toString());
                                amount.setText(TotalAmount);
                                String receiptNumber= GettingReceiptNumber(str.toString());
                                Receiptno.setText(receiptNumber);
                                String GstAmount=GettingGstAmount(str);
                                gst_amount.setText(GstAmount);

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("getting_Text", e.getMessage() + "----");

                                        // Task failed with an exception
                                        // ...
                                    }
                                });


    }
//    private String detectImg() {
//        final String[] pp = new String[1];
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
//        FirebaseVisionTextRecognizer textRecognizer =
//                FirebaseVision.getInstance().getOnDeviceTextRecognizer();
//        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//            @Override
//            public void onSuccess(FirebaseVisionText firebaseVisionText) {
//                if ((firebaseVisionText.getText() != null)&&(firebaseVisionText.getText().length()>0)) {
//                    detetectedtext.setText( firebaseVisionText.getText());
//                    Log.d("gettingvalue", firebaseVisionText.getText().toString());
//                    String str = firebaseVisionText.getText().toString();
//                    int i = 0;
//                    String gettingDate=gettingdatepartfromregex(str, i);
//                    dategetting.setText(gettingDate);
//                    String titleReceipt=getttingTitleComapny(str);
//                    titletext.setText(titleReceipt);
//
//                    String TotalAmount=GettingAmountFromDetections(str.toString());
//                    amount.setText(TotalAmount);
//                    String receiptNumber= GettingReceiptNumber(str.toString());
//                    Receiptno.setText(receiptNumber);
//                    String GstAmount=GettingGstAmount(str);
//                    gst_amount.setText(GstAmount);
//
//
//                    pp[0] =GstAmount;
//
//
//                    // processTxt(firebaseVisionText);
//                }
//                else
//                {
//                    Toast.makeText(MainActivity.this, "Data is coming null", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });
//return  pp[0];
//
//    }

    public String GettingGstAmount(String detections) {
        String value=null;
        if(detections!=null){
            String amountctching = null;
            final Pattern p = Pattern.compile("\\d{1,10}\\.{0,1},{0,1}\\d{1,10}\n");
            if (detections.toUpperCase().contains("GST AMOUNT")) {
                amountctching = detections.substring(detections.toUpperCase().indexOf("GST AMOUNT"), detections.length() - 1);
                Log.d("matchingword1",amountctching);
                Matcher m = p.matcher(amountctching);

                value = m.find() ? m.group() : null;
                Log.d("GST_amount_is", amountctching + "--" + value);
                if(value!=null)
                    Log.d("gst_amount",value+"-");

                else {
                    Matcher m1 = p.matcher(detections);

                    value = m1.find() ? m1.group() : null;
                    Log.d("Bill_amount_is", amountctching + "--" + value);
                    //   gst_amount.setText(value1);
                }
            }
            else if (detections.toUpperCase().contains("GST")) {


                amountctching = detections.substring(detections.toUpperCase().indexOf("GST"), detections.length() - 1);
                Log.d("matchingword1",amountctching);
                Matcher m = p.matcher(amountctching);

                value = m.find() ? m.group() : null;
                String BillAmount = amountctching.replaceAll("[^0-9]", "");
                Log.d("Bill_amount_is", BillAmount + "--" + value);
                if(value!=null)
                    Log.d("gst_amount",value+"-");
                else {
                    Matcher m1 = p.matcher(detections);

                    value = m1.find() ? m1.group() : null;
                    Log.d("gst_amount",value+"-");
                }

            }

        }
        return value;
    }
    public String gettingdatepartfromregex(String detections, int i) {

        if(detections!=null)
        {
            if ((i == 14) && (value == null)) {
                { Log.d("gst_amount",value+"-");
                    Log.d("title_date","no date");
                    return "No date"; }
            }
            final Pattern p = Pattern.compile(dateformats.get(i));
            Matcher m = p.matcher(detections);
            value = m.find() ? m.group() : null;
            if (value == null) {
                gettingdatepartfromregex(detections, ++i);

            } else {
                Log.d("dategetting", "--" + value + "-" + dateformats.get(i));

            }

        }
        return value;



    }
    String checkAlphaNumericWord(String checkword) {
        String addingwords = null;
        if(checkword!=null)
        {      if (checkword.toUpperCase().contains("INVOICE NO")) {
            String substring = checkword.substring(checkword.toUpperCase().indexOf("INVOICE NO"), checkword.length() - 1);
            Log.d("gettingcheck",substring);
            Pattern p = Pattern.compile("[a-zA-Z]{0,12}[0-9]{1,10}[a-zA-Z]{0,12}[0-9]{0,10}[a-zA-Z]{0,12}[0-9]{0,10}");
            Matcher m = p.matcher(substring);
            String value = m.find() ? m.group() : null;
            if (value != null)
                addingwords = value;
            else {
                Pattern p1 = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}[a-zA-Z]{1,10}[0-9]{0,10}[a-zA-Z]{1,10}[0-9]{0,10}");
                Matcher m1 = p1.matcher(checkword);
                String value1 = m1.find() ? m1.group() : null;
                if (value1 != null)
                    addingwords = value1;
                else {
                    addingwords = "receipt not found";
                }
            }

        }
        else  if (checkword.toUpperCase().contains("BILL NO"))
        {
            String substring = checkword.substring(checkword.toUpperCase().indexOf("BILL NO"), checkword.length() - 1);
            Log.d("gettingcheck",substring);
            Pattern p = Pattern.compile("[0-9]{1,10}[a-zA-Z]{0,12}");
            Matcher m = p.matcher(substring);
            String value = m.find() ? m.group() : null;
            if (value != null)
                addingwords = value;
            else {
                Pattern p1 = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}[a-zA-Z]{1,10}[0-9]{0,10}[a-zA-Z]{1,10}[0-9]{0,10}");
                Matcher m1 = p1.matcher(checkword);
                String value1 = m1.find() ? m1.group() : null;
                if (value1 != null)
                    addingwords = value1;
                else {
                    addingwords = "receipt not found";
                }
            }

        }

        else   if (checkword.toUpperCase().contains("RECEIPT NO")) {
            String substring = checkword.substring(checkword.toUpperCase().indexOf("RECEIPT NO"), checkword.length() - 1);
            Log.d("gettingcheck",substring);
            Pattern p = Pattern.compile("[a-zA-Z]{0,12}[0-9]{2,10}[a-zA-Z]{0,12}[0-9]{0,10}[a-zA-Z]{0,12}[0-9]{0,10}");
            Matcher m = p.matcher(substring);
            String value = m.find() ? m.group() : null;
            if (value != null)
                addingwords = value;
            else {
                Pattern p1 = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}[a-zA-Z]{1,10}[0-9]{0,10}[a-zA-Z]{1,10}[0-9]{0,10}");
                Matcher m1 = p1.matcher(checkword);
                String value1 = m1.find() ? m1.group() : null;
                if (value1 != null)
                    addingwords = value1;
                else {
                    addingwords = "receipt not found";
                }
            }
        }

        else   if (checkword.toUpperCase().contains("RECEIPT")) {
            String substring = checkword.substring(checkword.toUpperCase().indexOf("RECEIPT"), checkword.length() - 1);
            Log.d("gettingcheck",substring);
            Pattern p = Pattern.compile("[a-zA-Z]{0,12}[0-9]{5,10}[a-zA-Z]{0,12}[0-9]{0,10}[a-zA-Z]{0,12}[0-9]{0,10}");
            Matcher m = p.matcher(substring);
            String value = m.find() ? m.group() : null;
            if (value != null)
                addingwords = value;
            else {
                Pattern p1 = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}[a-zA-Z]{1,10}[0-9]{0,10}[a-zA-Z]{1,10}[0-9]{0,10}");
                Matcher m1 = p1.matcher(checkword);
                String value1 = m1.find() ? m1.group() : null;
                if (value1 != null)
                    addingwords = value1;
                else {
                    addingwords = "receipt not found";
                }
            }
        }
        else  if (checkword.toUpperCase().contains("BILL"))
        {
            String substring = checkword.substring(checkword.toUpperCase().indexOf("BILL"), checkword.length() - 1);
            Log.d("gettingcheck",substring);
            Pattern p = Pattern.compile("[0-9]{4,10}[a-zA-Z]{0,12}");
            Matcher m = p.matcher(substring);
            String value = m.find() ? m.group() : null;
            if (value != null)
                addingwords = value;
            else {
                Pattern p1 = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}[a-zA-Z]{1,10}[0-9]{0,10}[a-zA-Z]{1,10}[0-9]{0,10}");
                Matcher m1 = p1.matcher(checkword);
                String value1 = m1.find() ? m1.group() : null;
                if (value1 != null)
                    addingwords = value1;
                else {
                    addingwords = "receipt not found";
                }
            }

        }
        else   if (checkword.toUpperCase().contains("REG")) {
            String substring = checkword.substring(checkword.toUpperCase().indexOf("REG"), checkword.length() - 1);
            Log.d("gettingcheck",substring);
            Pattern p = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}");
            Matcher m = p.matcher(substring);
            String value = m.find() ? m.group() : null;
            if (value != null)
                addingwords = value;
            else {
                Pattern p1 = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}[a-zA-Z]{1,10}[0-9]{0,10}[a-zA-Z]{1,10}[0-9]{0,10}");
                Matcher m1 = p1.matcher(checkword);
                String value1 = m1.find() ? m1.group() : null;
                if (value1 != null)
                    addingwords = value1;
                else {
                    addingwords = "receipt not found";
                }
            }
        }
        else   if (checkword.toUpperCase().contains(" ID")) {
            String substring = checkword.substring(checkword.toUpperCase().indexOf(" ID"), checkword.length() - 1);
            Log.d("gettingcheck",substring);
            Pattern p = Pattern.compile("[a-zA-Z]{0,12}[0-9]{1,17}[a-zA-Z]{0,15}");
            Matcher m = p.matcher(substring);
            String value = m.find() ? m.group() : null;
            if (value != null)
                addingwords = value;
            else {
                Pattern p1 = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}[a-zA-Z]{1,10}[0-9]{0,10}[a-zA-Z]{1,10}[0-9]{0,10}");
                Matcher m1 = p1.matcher(checkword);
                String value1 = m1.find() ? m1.group() : null;
                if (value1 != null)
                    addingwords = value1;
                else {
                    addingwords = "receipt not found";
                }
            }
        }

        else
        {
            Pattern p = Pattern.compile("[a-zA-Z]{1,12}[0-9]{1,10}[a-zA-Z]{0,12}[a-zA-Z]{1,10}[0-9]{0,10}[a-zA-Z]{1,10}[0-9]{0,10}");
            Matcher m = p.matcher(checkword);
            String value = m.find() ? m.group() : null;
            if (value != null)
                addingwords = value;
            else {
                addingwords = "receipt not found";
            }


        }

        }




        return addingwords;

    }
    public String GettingReceiptNumber(String detections) {
        String receipt=null;
        if(detections!=null) {
            receipt = checkAlphaNumericWord(detections);
            Log.d("receipt_number",receipt+"--");
            //   Receiptno.setText(receipt);
        }
        return receipt;
    }
    public String GettingAmountFromDetections(String detections) {
        String amountctching = null;
        String value=null;
        if(detections!=null){

            final Pattern p = Pattern.compile("\\d{1,10}\\.{0,1},{0,1}\\d{1,10}\n");
            if (detections.toUpperCase().contains("TOTAL AMOUNT")) {
                int index_number=detections.toUpperCase().indexOf("TOTAL AMOUNT");
                amountctching = detections.substring(index_number, detections.length()-1);
                Log.d("matchingword",amountctching);
                Matcher m = p.matcher(amountctching);

                value = m.find() ? m.group() : null;
                Log.d("Bill_amount_is", amountctching + "--" + value);
                if(value!=null)
                    Log.d("amount",value+"--");
                else {
                    Matcher m1 = p.matcher(detections);

                    value = m1.find() ? m1.group() : null;
                    //  amount.setText(value1);
                }
            }
            else   if (detections.toUpperCase().contains("CURRENT CHARGES")) {
                int index_number=detections.toUpperCase().indexOf("CURRENT CHARGES");
                amountctching = detections.substring(index_number, detections.length()-1);
                Log.d("matchingword",amountctching);
                Matcher m = p.matcher(amountctching);

                value = m.find() ? m.group() : null;
                Log.d("Bill_amount_is", amountctching + "--" + value);
                if(value!=null)
                    Log.d("amount",value+"--");
                else {
                    Matcher m1 = p.matcher(detections);

                    value = m1.find() ? m1.group() : null;
                }
            }
            else   if (detections.toUpperCase().contains("$")) {
                final Pattern p1 = Pattern.compile("\\d{1,10}\\.{0,1},{0,1}-{0,1}\\d{1,10}\n");
                int index_number=detections.toUpperCase().indexOf("$");
                amountctching = detections.substring(index_number, detections.length()-1);
                Log.d("matchingword",amountctching);
                Matcher m = p1.matcher(amountctching);

                value = m.find() ? m.group() : null;
                Log.d("Bill_amount_is", amountctching + "--" + value);
                if(value!=null)
                    Log.d("amount",value+"--");
                else {
                    Matcher m1 = p.matcher(detections);

                    value= m1.find() ? m1.group() : null;
                }
            }

            else if (detections.toUpperCase().contains("TOTAL")) {
                int index_number=detections.toUpperCase().indexOf("TOTAL");
                amountctching = detections.substring(index_number,detections.length()-1);
                Log.d("matchingword",amountctching);
                Matcher m = p.matcher(amountctching);

                value = m.find() ? m.group() : null;
                Log.d("Bill_amount_is", amountctching + "--" + value);
                if(value!=null)
                    Log.d("amount",value+"--");
                else {
                    Matcher m1 = p.matcher(detections);

                    value = m1.find() ? m1.group() : null;
                }
            }
            else if (detections.toUpperCase().contains("BALANCE")) {

                int index=detections.toUpperCase().indexOf("BALANCE");
                amountctching = detections.substring(index, detections.length()-1);
                Log.d("matchingword",amountctching);
                Matcher m = p.matcher(amountctching);

                value = m.find() ? m.group() : null;
                String BillAmount = amountctching.replaceAll("[^0-9]", "");
                Log.d("Bill_amount_is", BillAmount + "--" + value);
                if(value!=null)
                    Log.d("amount",value+"--");
                else {
                    Matcher m1 = p.matcher(detections);

                    value = m1.find() ? m1.group() : null;
                }

            }
            else
            {
                Matcher m = p.matcher(detections);

                value = m.find() ? m.group() : null;
                Log.d("Bill_amount_is", amountctching + "--" + value);
                Log.d("amount",value+"--");
            }
        }
        return value;

    }
    public String getttingTitleComapny(String str) {
        String title="empty";
        if(str.length()>2)
        {
            Log.d("strget",str.length()+"--");
            title = str.substring(0, str.indexOf("\n"));
            //titletext.setText(title);
        }
        return title;


    }
}
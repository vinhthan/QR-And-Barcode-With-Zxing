package com.example.qrandbarcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrandbarcodescanner.model.QRGeoModel;
import com.example.qrandbarcodescanner.model.QRURLModel;
import com.example.qrandbarcodescanner.model.QRVcardModel;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.security.Permission;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission_group.CAMERA;

//here show 4 case: VCARD / EVENT, URL, GEO, TEXT... cant do more...

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    /*public static final int REQUEST_CAMERA = 1;*/
    private ZXingScannerView scannerView;
    private TextView txvResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        scannerView = findViewById(R.id.zxscan);
        txvResult = findViewById(R.id.txvResult);

        //permission
        Dexter.withActivity(this)
                //"withPermission" khong co "s" nhe
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(MainActivity.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You must accept this permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
    }

    //ket thuc
    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }

    //here we can reseive rawResult
    @Override
    public void handleResult(Result rawResult) {
        //
        //txvResult.setText(rawResult.getText());
        processRawResult(rawResult.getText());//
        //scannerView.startCamera();
    }

    //handle result here
    @SuppressLint("SetTextI18n")
    private void processRawResult(String text) {
        if (text.startsWith("BEGIN:")){
            String[] tokens = text.split("\n");

            QRVcardModel qrVcardModel = new QRVcardModel();
            for (int i = 0; i <tokens.length; i++){
                if (tokens[i].startsWith("BEGIN:")){//
                    qrVcardModel.setType(tokens[i].substring("BEGIN:".length()));//remove BEGIN: to get Type
                }else if (tokens[i].startsWith("N:")){//
                    qrVcardModel.setName(tokens[i].substring("N:".length()));//remove BEGIN: to get Type
                }else if (tokens[i].startsWith("ORG:")){//
                    qrVcardModel.setOrg(tokens[i].substring("ORG:".length()));//remove BEGIN: to get Type
                }else if (tokens[i].startsWith("TEL;WORK;VOICE:")){//
                    qrVcardModel.setTel(tokens[i].substring("TEL;WORK;VOICE:".length()));
                }else if (tokens[i].startsWith("URL:")){
                    qrVcardModel.setUrl(tokens[i].substring("URL:".length()));
                }else if (tokens[i].startsWith("EMAIL;PREF;INTERNET:")){//
                    qrVcardModel.setEmail(tokens[i].substring("EMAIL;PREF;INTERNET:".length()));
                }else if (tokens[i].startsWith("ADR")){//
                    qrVcardModel.setAddress(tokens[i].substring("ADR:".length()));
                }else if (tokens[i].startsWith("NOTE:")){
                    qrVcardModel.setNote(tokens[i].substring("NOTE:".length()));
                }else if (tokens[i].startsWith("SUMMARY:")){
                    qrVcardModel.setSummary(tokens[i].substring("SUMMARY:".length()));
                    //DTSTART
                }else if (tokens[i].startsWith("START:")){
                    qrVcardModel.setDtstart(tokens[i].substring("START:".length()));
                    //DTEND
                }else if (tokens[i].startsWith("END:")){//
                    qrVcardModel.setDtend(tokens[i].substring("END:".length()));
                }
                //try to show
                /*txvResult.setText(qrVcardModel.getType());*/
                txvResult.setText("Type: "+qrVcardModel.getType()+"\nName: "+qrVcardModel.getName()+"\nCountry: "+qrVcardModel.getOrg()+"\nPhone: "
                +qrVcardModel.getTel()+"\n"+qrVcardModel.getUrl()+"\nEmail: "+qrVcardModel.getEmail()+"\nAddress: "
                +qrVcardModel.getAddress()+"\n"+qrVcardModel.getNote()+"\n"+qrVcardModel.getSummary()+"\n"
                +qrVcardModel.getDtstart()+"\nEnd: "+qrVcardModel.getDtend());

            }
        }
        //URL
        else if (text.startsWith("http://") || text.startsWith("https://") ||
                text.startsWith("www.")){

            QRURLModel qrurlModel = new QRURLModel(text);
            txvResult.setText(qrurlModel.getUrl());
        }
        //GEO
        else if (text.startsWith("geo:")){
            QRGeoModel qrGeoModel = new QRGeoModel();
            String delines = "[ , ?q= ]+";

            String token[] = text.split(delines);

            for (int i = 0; i < token.length; i++){
                if (token[i].startsWith("geo:")){
                    qrGeoModel.setLat(token[i].substring("geo:".length()));
                }
            }
            qrGeoModel.setLat(token[0].substring("geo:".length()));
            qrGeoModel.setLng(token[1]);
            qrGeoModel.setGeoPlace(token[2]);

            txvResult.setText(qrGeoModel.getLat() + "/" + qrGeoModel.getLng());
        }

        else {
            txvResult.setText(text);
        }

        scannerView.resumeCameraPreview(MainActivity.this);
    }


}

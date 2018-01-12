package com.example.dell.rtificialtrainer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isStoragePermissionGranted())createAppDirectory();
    }

    /**
     * Tworzy katalogi na pliki aplikacji
     */
    private void createAppDirectory() {
        String modelsDirPath = "/"+getString(R.string.appDirName)+"/"+getString(R.string.modelDirName)+"/";
        File modelsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+modelsDirPath);
        String pdfDirPath = "/"+getString(R.string.appDirName)+"/"+getString(R.string.pdfDirName)+"/";
        File pdfDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+pdfDirPath);

        boolean result;
        if (!modelsDir.exists()) {
            result = modelsDir.mkdirs();
            Log.wtf("TEST DIR", "Creating " + modelsDir.getAbsolutePath() + " result: " + result + " is dir: " + modelsDir.isDirectory());
            createInfoFile(modelsDir,getString(R.string.model_dir_info_cont));
        }
        if (!pdfDir.exists()) {
            result = pdfDir.mkdirs();
            Log.wtf("TEST DIR", "Creating " + pdfDir.getAbsolutePath() + " result: " + result + " is dir: " + pdfDir.isDirectory());
            createInfoFile(pdfDir, getString(R.string.pdf_dir_info_cont));
        }
    }

    /**
     * Tworzy pliki tekstowe z informacją o przeznaczeniu katalogu
     * @param directory katalog w którym ma się znajdować
     * @param infoText informacja o przeznaczeniu katalogu
     */
    public void createInfoFile(File directory, String infoText){
        File infoFile = new File(directory,"info.txt");

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(infoFile));
            writer.write(infoText);
            Log.wtf("INFO FILE", infoFile.getAbsolutePath() + " created");
            MediaScannerConnection.scanFile(this, new String[]{infoFile.getAbsolutePath()}, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Uruchomienie modułu "Zawodnicy"
     * @param view
     */
    public void onAthletesBtnClick(View view) {
        Intent athlIntent = new Intent(this,AthletesActivity.class);
        startActivity(athlIntent);
    }

    /**
     * Uruchomienie modułu "Predykcja rezultatu"
     * @param view
     */
    public void onResultBtnClick(View view) {
        Intent resIntent = new Intent(this,EvaluateActivity.class);
        resIntent.putExtra("TRYB","REZULTAT");
        startActivity(resIntent);
    }

    /**
     * Uruchomienie modułu "Generowanie treningu"
     * @param view
     */
    public void onTrainingBtnClick(View view) {
        Intent trainIntent = new Intent(this,EvaluateActivity.class);
        trainIntent.putExtra("TRYB","TRENING");
        startActivity(trainIntent);
    }

    /**
     * Uruchomienie modułu "O Aplikacji"
     * @param view
     */
    public void onAboutBtnClick(View view) {
        Intent trainIntent = new Intent(this,AboutActivity.class);
        startActivity(trainIntent);
    }

    /**
     * Zwraca informację o tym czy dostęp do pamięci dla tej aplikacji został przyznany
     * @return true jeśli tak false jeśli nie
     */
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Permission","Permission is granted");
                return true;
            } else {
                Log.v("Permission","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.v("Permission","Permission is granted");
            return true;
        }
    }

    /**
     * Obługuje zachowanie aplikacji po uruchomieniu menagera przyznawania uprawnień
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("Permission","Permission: "+permissions[0]+ "was "+grantResults[0]);
            createAppDirectory();
        }else if(grantResults[0]== PackageManager.PERMISSION_DENIED){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.permission_alert_text))
                    .setPositiveButton(getString(R.string.alert_pos_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isStoragePermissionGranted();
                        }
                    })
                    .setNegativeButton(getString(R.string.alert_neg_btn), null)
                    .show();
        }
    }
}

package jp.techacademy.hirohide.itagaki.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    //getContentsInfoで取得したデータを保有するための変数
    Cursor cursor;

    Timer mTimer;
    Handler mHandler = new Handler();

    //スライドショーが停止中か判定するためのフラグ
    boolean mStopflug = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(this);

        Button play_button = (Button) findViewById(R.id.play_button);
        play_button.setOnClickListener(this);

        Button next_button = (Button) findViewById(R.id.next_button);
        next_button.setOnClickListener(this);

        ImageView imageView =(ImageView) findViewById(R.id.imageView);
        checkPermissions();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }else{
                    View view = (View) findViewById(android.R.id.content);
                    Snackbar mySnackBar = Snackbar.make(view, "画像を表示するには、\n外部ストレージの読み込み許可が必要です", Snackbar.LENGTH_INDEFINITE);
                    mySnackBar.setAction("OK!", new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            checkPermissions();
                            appConfig();
                        }
                    });
                    mySnackBar.show();
                }
                break;
            default:
                break;        }
    }
    public void appConfig(){
        //「今後は確認しない」がチェックされた場合、アプリ設定画面を表示する
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)==false){
            String uriString = "package:" + getPackageName();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
            startActivity(intent);
        }
    }
    public void checkPermissions(){
        // パーミッションの許可状態を確認する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    private void getContentsInfo() {
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
        }
    }

    public void onClick(View v){
            if (v.getId() == R.id.next_button) {
                getNextInfo();
            } else if (v.getId() == R.id.back_button) {
                getBackInfo();
            } else if (v.getId() == R.id.play_button) {
                getPlayInfo();
            }
    }

    private void getNextInfo() {
        if (cursor.moveToNext()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
        }else{
            cursor.moveToFirst();
        }
    }
    private void getBackInfo() {
        if (cursor.moveToPrevious()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
        }else{
            cursor.moveToLast();
        }
    }
    private void getPlayInfo() {
        if(mStopflug) {
            //スライドショー停止中
            Button next_button = (Button) findViewById(R.id.next_button);
            next_button.setEnabled(false);
            Button back_button = (Button) findViewById(R.id.back_button);
            back_button.setEnabled(false);

            mTimer = new Timer();
            mStopflug = false;
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getNextInfo();
                        }
                    });
                }
            }, 2000, 2000);
        }else{
            //スライドショー再生中
            Button next_button = (Button) findViewById(R.id.next_button);
            next_button.setEnabled(true);
            Button back_button = (Button) findViewById(R.id.back_button);
            back_button.setEnabled(true);

            mTimer.cancel();
            mStopflug = true;
        }
    }
}

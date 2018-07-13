package com.example.tongxiwen.photogathertest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

public class MainActivity extends FoxActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE = 0X01;

    private ImageView view;
    private PhotoTakerSheetDialog dialog;

    @Override
    protected int onInflateLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void create(@Nullable Bundle savedInstanceState) {
        view = findViewById(R.id.image_field);
        dialog = PhotoTakerSheetDialog.get(this, true);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkNrequestPermission(Manifest.permission.CAMERA
                    ,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        view.setImageResource(R.mipmap.ic_launcher);

        dialog.setCropSquare(false);    // 不要固定比例的正方形剪裁
    }

    @Override
    protected void onPermissionResult(List<String> failedList) {
        StringBuilder sb = new StringBuilder();
        if (failedList.isEmpty())
            return;
        for (String permission : failedList){
            if (!TextUtils.isEmpty(sb))
                sb.append("\n与\n");
            else
                sb.append("权限： \n");
            sb.append(permission);
        }
        sb.append("\n获取失败，即将退出应用");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("提示：")
                .setMessage(sb.toString())
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Uri uri = null;
        if (resultCode == RESULT_OK)
            uri = dialog.onResult(requestCode, data);
//        if (uri != null && requestCode == PhotoTakerSheetDialog.REQUEST_CROP) {
//            view.setImageURI(uri);
//        }

        if (uri != null && requestCode == PhotoTakerSheetDialog.REQUEST_CROP) {
            String path = uri.toString();
            Bitmap bitmap = BitmapUtil.getBitmapFromFile(new File(path));
            view.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                dialog.show();
                break;
            case R.id.button2:
                FileUtil.clearImgCacheDir(MainActivity.this);
                FileUtil.clearCropImgDir();
                break;
            default:
                break;
        }
    }
}

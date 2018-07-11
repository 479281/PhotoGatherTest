package com.example.tongxiwen.photogathertest;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * 获取照片底部抽屉
 * TODO 相册获取、相机获取、照片剪裁、照片压缩、照片上传
 * TODO 注意Glide缓存机制的键冲突
 */
public class PhotoTakerSheetDialog extends BottomSheetDialog implements View.OnClickListener {

    private static final int REQUEST_CAMERA = 0x00;
    private static final int REQUEST_ALBUM = 0x01;

    private Activity mContext;
    private String tempPath;
    private boolean isCrop; // 是否剪裁
    private boolean isNougat;   // 是否为7.0

    private Button btnCamera;
    private Button btnAlbum;

    public static PhotoTakerSheetDialog get(@NonNull Activity context) {
        return get(context, false);
    }

    public static PhotoTakerSheetDialog get(@NonNull Activity context, boolean isCrop) {
        return new PhotoTakerSheetDialog(context, isCrop);
    }

    private PhotoTakerSheetDialog(@NonNull Activity context, boolean isCrop) {
        super(context);
        this.isCrop = isCrop;
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_taker_sheet_dialog_layout);
        isNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

        btnCamera = findViewById(R.id.dialog_camera);
        btnAlbum = findViewById(R.id.dialog_album);
        btnCamera.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
    }

    /**
     * 获取结果
     * <p>
     * 自行判断resultCode
     *
     * @return 图片Uri
     */
    public Uri onResult(int requestCode, Intent resultIntent) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                return Uri.parse(tempPath);
            case REQUEST_ALBUM:
                //不考虑4.4以下版本
                if (resultIntent != null) {
                    Uri rawUri = resultIntent.getData();
                    return Uri.parse(handleImage(rawUri));
                }
                break;
            default:
                return null;
        }
        return null;
    }

    /**
     * 开启相机6.0
     */
    private void openCamera() {
        tempPath = null;
        tempPath = FileUtil.getImgCachePath(mContext);
        if (TextUtils.isEmpty(tempPath))
            return;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(tempPath)));
        mContext.startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * 开启相机7.0
     */
    private void openCameraN() {
        tempPath = null;
        tempPath = FileUtil.getImgCachePath(mContext);
        if (TextUtils.isEmpty(tempPath))
            return;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT
                /*下面这个参数用到了FileProvider的方法获取Uri，是因为7.0对文件权限做了修改*/
                ,FileProvider.getUriForFile(mContext, "com.example.tongxiwen.photogathertest"
                        ,new File(tempPath)));
        mContext.startActivityForResult(intent, REQUEST_CAMERA);
        Log.d("tempPath:", tempPath);
    }

    /**
     * 开启相册
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {// 相机被卸载时不会崩溃
            mContext.startActivityForResult(intent, REQUEST_ALBUM);
        }
    }

    /**
     * 处理从相册得到的uri
     *
     * @param rawUri 从相册得到的uri
     */
    private String handleImage(Uri rawUri) {
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(mContext, rawUri)) {
            //如果是document类型的Uri,则通过document id处理
            String docId = DocumentsContract.getDocumentId(rawUri);
            if ("com.android.providers.media.documents".equals(rawUri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(rawUri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(rawUri.getScheme())) {
            //如果是content类型的URI，则使用普通方式处理
            imagePath = getImagePath(rawUri, null);
        } else if ("file".equalsIgnoreCase(rawUri.getScheme())) {
            imagePath = rawUri.getPath();
        }
        return imagePath;
    }

    /**
     * 通过Uri和Selection获取真实Uri
     *
     * @param uri       uri
     * @param selection selection
     * @return 真实的uri字符串
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = mContext.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 对7.0的文件操作权限问题做了适配，因此通过该方法获取相机的临时存储uri
     *
     * @deprecated 实际上并没有用的上。。。
     * @return 存储Uri
     */
    private Uri getSaveUri() {
        File file = new File(tempPath);
        Uri imageUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //若为7.0系统，则需适配内容提供者和权限

            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            imageUri = FileProvider.getUriForFile(mContext
                    , "com.example.tongxiwen.photogathertest", file);

            //创建并添加到内容提供者里
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            imageUri = mContext.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            imageUri = Uri.fromFile(file);
        }
        return imageUri;
    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_camera:
                if (isNougat)
                    openCameraN();
                else
                    openCamera();
                break;
            case R.id.dialog_album:
                openAlbum();
                break;
        }
        dismiss();
    }
}

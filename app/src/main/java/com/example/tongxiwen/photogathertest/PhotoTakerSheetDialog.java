package com.example.tongxiwen.photogathertest;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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

/**
 * 获取照片底部抽屉
 * 相册获取、相机获取、照片剪裁、照片压缩、照片上传
 */
public class PhotoTakerSheetDialog extends BottomSheetDialog implements View.OnClickListener {

    public static final int REQUEST_CAMERA = 0x00;
    public static final int REQUEST_ALBUM = 0x01;
    public static final int REQUEST_CROP = 0x02;

    private Activity mContext;
    private String tempPath;
    private String rawCropPath;
    private boolean isCrop; // 是否剪裁
    private boolean isNougat;   // 是否为7.0

    private boolean isSquare = false; // 剪裁区是否固定正方形

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

        findViewById(R.id.dialog_camera).setOnClickListener(this);
        findViewById(R.id.dialog_album).setOnClickListener(this);
    }

    /**
     * 获取结果
     * <p>
     * 自行判断resultCode
     *
     * @return 图片Uri
     */
    public Uri onResult(int requestCode, Intent resultIntent) {
        String path = null;
        switch (requestCode) {
            case REQUEST_CAMERA:
                path = tempPath;
                rawCropPath = path;
                break;
            case REQUEST_ALBUM:
                //不考虑4.4以下版本
                rawCropPath = null;
                if (resultIntent != null) {
                    Uri rawUri = resultIntent.getData();
                    path = handleImage(rawUri);
                }
                break;
            case REQUEST_CROP:
                if (!TextUtils.isEmpty(rawCropPath))
                    FileUtil.deleteFileFromPath(rawCropPath);
                rawCropPath = null;
                return Uri.parse(tempPath);
            default:
                path = null;
                break;
        }
        if (isCrop) {
            if (isNougat) {
                openCropN(path);
            } else {
                openCrop(path);
            }
            return null;
        }
        return Uri.parse(path);
    }

    /**
     * 设置剪裁区是否为正方形，默认为false
     */
    public void setCropSquare(boolean isSquare) {
        this.isSquare = isSquare;
    }

    /**
     * 检查是否剪裁区为正方形
     */
    public boolean isCropSquare() {
        return isSquare;
    }

//-----------------------------私有方法--------------------------------

    /**
     * 6.0开启剪裁
     *
     * @param path 图片路径
     */
    private void openCrop(String path) {
        int aspect = isSquare ? 1 : 0;
        tempPath = FileUtil.getCropImgPath();
        Uri outputUri = Uri.parse(tempPath);
        Uri imageUri = Uri.parse(path);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspect);
        intent.putExtra("aspectY", aspect);

//        intent.putExtra("outputX", 100);  //返回数据的时候的 X 像素大小。
//        intent.putExtra("outputY", 100);  //返回的时候 Y 的像素大小。
        //以上两个值，设置之后会按照两个值生成一个Bitmap, 两个值就是这个bitmap的横向和纵向的像素值，如果裁剪的图像和这个像素值不符合，那么空白部分以黑色填充。

        intent.putExtra("scale", true);
//        intent.putExtra("circleCrop", "true"); // 该句会报错，可能和原图 比例有关
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        mContext.startActivityForResult(intent, REQUEST_CROP);
    }

    /**
     * 7.0开启剪裁
     *
     * @param path 图片路径
     */
    private void openCropN(String path) {
        String authorities = mContext.getString(R.string.authorities);

        tempPath = FileUtil.getCropImgPath();
        Uri outputUri = FileUtil.getImageContentUri(mContext, tempPath);  // 输出路径

        Uri imgUri = FileProvider.getUriForFile(mContext
                , authorities, new File(path));  // 输入路径

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(imgUri, "image/*");   // 输入url和类别
        intent.putExtra("crop", "false");

        int aspect = isSquare ? 1 : 0;
        intent.putExtra("aspectX", aspect);  // 剪裁比例，默认为0即自由比例
        intent.putExtra("aspectY", aspect);  // 剪裁比例，默认为0即自由比例

//        intent.putExtra("circleCrop", "true");

        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);    // 输出uri
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // 不要面部识别
        intent.putExtra("return-data", false);  // 是否返回Bitmap，占用内存
        mContext.startActivityForResult(intent, REQUEST_CROP);
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
                , FileProvider.getUriForFile(mContext, "com.example.tongxiwen.photogathertest"
                        , new File(tempPath)));
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

//    @Override
//    public void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        FileUtil.clearImgCacheDir(mContext);
//    }

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

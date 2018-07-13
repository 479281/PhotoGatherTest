package com.example.tongxiwen.photogathertest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateFormat;

import java.io.File;
import java.io.IOException;

public class FileUtil {

    private static final String OUTER_MAIN_PATH = "/图片获取测试app";

    /**
     * 获取图片缓存文件路径
     *
     * @param context 上下文
     * @return 路径
     */
    public static String getImgCachePath(Context context) {
        String path = getImgCacheDir(context);
        if (TextUtils.isEmpty(path))
            return null;
        try {
            path += "/img_cache" + System.currentTimeMillis() + ".jpg";
            File file = new File(path);
            if (file.exists())
                file.delete();
            file.createNewFile();
            return file.getAbsolutePath();
        } catch (IOException e) {
            path = null;
        }
        return null;
    }

    public static String getCropImgPath() {
        String path = null;
        try {
            long curTime = System.currentTimeMillis();
            String curTimeStr = (String) DateFormat.format("yyyy-MM-dd-hh:mm:ss", curTime);
            path = getCropImgDir() + "/" + curTimeStr + ".jpg";
            File file = new File(path);
            if (file.exists())
                file.delete();
            file.createNewFile();
        } catch (IOException e) {
            path = null;
        }
        return path;
    }

    /**
     * 获取剪切图片存储文件夹路径
     * <p>
     * 需要权限 Manifest.permission.WRITE_EXTERNAL_STORAGE
     *
     * @return 路径
     */
    public static String getCropImgDir() {
        String path = getOuterStoragePath() + "/剪切图";
        File file = new File(path);
        if (file.exists()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        } else
            file.mkdir();
        return file.getAbsolutePath();
    }

    /**
     * 将文件路径转化成FileProvider Uri
     * @param context
     * @return
     */
    public static Uri getImageContentUri(Context context, String path) {
        String filePath = path; // 获取剪切图路径
        File imageFile = new File(filePath);
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 清空图片缓存文件
     *
     * @param ctx 上下文
     * @return 文件是否不再存在
     */
    public static boolean clearImgCacheDir(Context ctx) {
        String path = getImgCacheDir(ctx);
        if (TextUtils.isEmpty(path))
            return true;
        File file = new File(path);
        if (file.exists())
            return deleteDir(file.getAbsolutePath());
        return true;
    }

    /**
     * 清空剪切图缓存文件
     *
     * @return 文件是否不再存在
     */
    public static boolean clearCropImgDir() {
        String path = getCropImgDir();
        if (TextUtils.isEmpty(path))
            return true;
        File file = new File(path);
        if (file.exists())
            return deleteDir(file.getAbsolutePath());
        return true;
    }

//    /**
//     * 复制文件
//     * @param temp  缓存文件
//     * @param path  目标路径
//     * @return  是否成功
//     */
//    public static boolean saveFile(File temp, String path){
//        if (!temp.exists())
//            return false;
//        try {
//            int bytesum = 0;
//            int byteread = 0;
//
//            InputStream in = new FileInputStream(temp.getAbsolutePath());
//            OutputStream out = new FileOutputStream(path);
//            byte buffer = new byte[]
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 删除路径的文件
     * @param path  文件路径
     * @return  返回该路径下是否依然存在文件
     */
    public static boolean deleteFileFromPath(String path) {
        File file = new File(path);
        return !file.exists() || file.delete();
    }

    /**
     * 删除文件夹
     *
     * @param pPath 文件夹路径
     */
    public static boolean deleteDir(final String pPath) {
        File dir = new File(pPath);
        return deleteDirWithFile(dir);
    }

// ---------------------------私有方法-------------------------------

    /**
     * 获取拍照缓存目录路径
     *
     * @param context 上下文
     * @return 路径
     */
    private static String getImgCacheDir(@NonNull Context context) {
        String path = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            path = context.getExternalCacheDir().getAbsolutePath();
        } else {
            path = context.getCacheDir().getAbsolutePath();
        }

        if (path.isEmpty())
            return null;

        path += "/img";

        File imgDir = new File(path);
        if (!imgDir.exists()) {
            imgDir.mkdir();
        }
        return path;
    }

    /**
     * 获取外部存储根目录
     *
     * @return 路径
     */
    private static String getOuterStoragePath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path += OUTER_MAIN_PATH;
        File file = new File(path);
        if (file.exists()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        } else
            file.mkdir();
        return file.getAbsolutePath();
    }

    /**
     * 清空文件夹
     *
     * @param dir 文件夹路径
     */
    private static boolean deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return false;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        return dir.delete();// 删除目录本身
    }
}

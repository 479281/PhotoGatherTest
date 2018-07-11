package com.example.tongxiwen.photogathertest;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class FileUtil {

    /**
     * 获取图片缓存文件路径
     * @param context   上下文
     * @return  路径
     */
    public static String getImgCachePath(Context context){
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
        }catch (IOException e){
            path = null;
        }
        return null;
    }

    /**
     * 清空图片缓存文件
     * @param ctx   上下文
     * @return  文件是否不再存在
     */
    public static boolean clearImgCacheDir(Context ctx){
        String path = getImgCacheDir(ctx);
        if (TextUtils.isEmpty(path))
            return false;
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
     * 删除文件夹
     * @param pPath 文件夹路径
     */
    public static boolean deleteDir(final String pPath) {
        File dir = new File(pPath);
        return deleteDirWithFile(dir);
    }

// ---------------------------私有方法-------------------------------

    /**
     * 获取应用缓存目录路径
     * @param context 上下文
     * @return  路径
     */
    private static String getImgCacheDir(@NonNull Context context) {
        String path = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            path = context.getExternalCacheDir().getAbsolutePath();
        }else {
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
     * 清空文件夹
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

package com.example.tongxiwen.photogathertest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class BitmapUtil {

    /**
     * 从路径获取图片
     *
     * @param file
     * @return
     */
    public static Bitmap getBitmapFromFile(File file) {
        if (!file.exists() || file.isDirectory())
            return null;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), getBitmapOption(2));
    }

    private static BitmapFactory.Options getBitmapOption(int inSampleSize) {
        System.gc();    //清理垃圾
        BitmapFactory.Options options = new BitmapFactory.Options();
        //TODO 需要搞清楚BitmapFactory.Option的用处

        options.inSampleSize = inSampleSize;
        return options;
    }
}

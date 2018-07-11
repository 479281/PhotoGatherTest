package com.example.tongxiwen.photogathertest;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class FoxActivity extends AppCompatActivity {

    private static final int CODE_PERMISSION = 0x00;

    private LifeCycleAdapter lifeCycleListener;
    private static final String TAG = "FoxActivity";
    private Toast mToast;

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (onInflateLayout() != 0) {
            setContentView(onInflateLayout());
        }
        //TODO 在此下自定义预设的加载和设置

        create(savedInstanceState);
        if (lifeCycleListener != null)
            lifeCycleListener.onCreate(savedInstanceState);
    }

    /**
     * 加载布局时的方法，返回布局文件ID
     *
     * @return 布局文件ID
     */
    protected abstract int onInflateLayout();

    /**
     * 封装onCreate，暴露抽象方法
     */
    protected abstract void create(@Nullable Bundle savedInstanceState);

    @Override
    protected void onResume() {
        super.onResume();
        if (lifeCycleListener != null)
            lifeCycleListener.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (lifeCycleListener != null)
            lifeCycleListener.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lifeCycleListener != null)
            lifeCycleListener.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lifeCycleListener != null) {
            lifeCycleListener.onDestroy();
            removeCallbacks();
        }
    }

    /**
     * 检查并请求权限
     * @param permissions 权限列表
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void checkNrequestPermission(String... permissions){
        requestPermissions(checkPermissions(permissions));
    }

    /**
     * 检查权限
     *
     * @param permissions 权限
     * @return 失败权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected List<String> checkPermissions(String... permissions) {
        List<String> permissionList = Arrays.asList(permissions);
        return checkPermissions(permissionList);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected List<String> checkPermissions(List<String> permissions) {
        List<String> failedList = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                failedList.add(permission);
        }
        return failedList;
    }

    /**
     * 请求权限
     *
     * @param permissions 权限列表
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void requestPermissions(List<String> permissions) {
        if (permissions.isEmpty())
            return;
        String[] pers = new String[permissions.size()];
        for (int i = 0; i < permissions.size(); i++)
            pers[i] = permissions.get(i);
        requestPermissions(pers);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void requestPermissions(String... permissions) {
        requestPermissions(permissions, CODE_PERMISSION);
    }

    /**
     * 父类的原生权限获取结果，final重写保护
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> failedList = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++)
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                failedList.add(permissions[i]);
        onPermissionResult(failedList);
    }

    /**
     * 权限获取结果接口
     * @param failedList 失败权限
     */
    protected void onPermissionResult(List<String> failedList){}

    /**
     * 移除生命周期监听器
     */
    public void removeLifeCycleListener() {
        this.lifeCycleListener = null;
    }

    /**
     * 添加生命周期监听器
     *
     * @param listener 监听器实例
     */
    public void addLifeCycleListener(LifeCycleAdapter listener) {
        this.lifeCycleListener = listener;
    }

    /**
     * 移除所有监听器
     */
    protected void removeCallbacks() {
        removeLifeCycleListener();
    }

    /**
     * 弹出Toast
     *
     * @param message 信息
     * @return Toast实例
     */
    public Toast makeToast(CharSequence message) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
        return mToast;
    }

    public Toast makeToast(int resource) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, resource, Toast.LENGTH_LONG);
        mToast.show();
        return mToast;
    }

    /**
     * 生命周期监听器，依选择重写方法
     * 执行先于子类Activity实例
     */
    public static abstract class LifeCycleAdapter {
        public void onCreate(Bundle savedInstanceState) {
        }

        public void onResume() {
        }

        public void onStop() {
        }

        public void onPause() {
        }

        public void onDestroy() {
        }
    }

    /**
     * 统一强制转换，直接返回自定义Activity
     *
     * @return FoxActivity实例
     */
    public FoxActivity getActivity() {
        return this;
    }
}

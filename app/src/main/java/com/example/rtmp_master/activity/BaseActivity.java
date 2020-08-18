package com.example.rtmp_master.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.rtmp_master.R;

/**
 *
 * @package com.example.rtmp_master
 * @date 2020/7/14
 * @desc
 */
public  abstract  class BaseActivity extends AppCompatActivity {

    private static String[] permissions;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermission();
        hideLayoutBar();
        initData();
        setContentView(getLayoutId());
        initView();
        doEvent();
    }

    /**
     * 隐藏顶部状态栏
     *
     */
    private void hideLayoutBar() {
            Window window = getWindow();
            View decorView = window.getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
    }

    /**
     * 动态申请权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initPermission() {
        permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO};

        boolean flag=true;
        for(int i=0;i<permissions.length;i++){
            if(!(ActivityCompat.checkSelfPermission(this,permissions[i])== PackageManager.PERMISSION_GRANTED)){
                flag=false;
            }
        }
        if(!flag){
            requestPermissions(permissions,100);
        }
    }

    /**
     * 响应权限事件
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            boolean flag = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    flag = false;
                }
            }
            if (flag) {
                Toast.makeText(this, R.string.permission_pass, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.permission_no_pass, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 布局ID
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化布局控件
     */
    protected abstract void initView();

    /**
     * 数据初始化
     */
    protected abstract void initData();

    /**
     * 事件处理
     */
    protected abstract void doEvent();
}

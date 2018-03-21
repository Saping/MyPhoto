package com.example.administrator.myphoto03;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    //访问相册
    private int PHOTO_ZOOM = 2;
    //访问相机
    private Cursor cursor;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = findViewById(R.id.image);
    }

    //从相册中添加
    public void play(View view) {
        //这段话 的意思   如果读的权限没有授权
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //就跳到让用户选择是否授权  给个码走返回方法
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        } else {
            //有权限时候 直接跳相册
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PHOTO_ZOOM);
        }

    }

    //拍照
    public void paizhao(View view) {
        //如果相机没有授权权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //就跳到让用户选择是否授权  给个码走返回方法
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1002);
        } else {
            //有权限时候 直接跳相机
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            return;
        }
        if (resultCode == 1) {
            Log.d("TAG", "照相的onActivityResult!!!!");
        }
        //读取相册图片PHOTO_ZOOM为启动相册选取的请求码
        if (requestCode == PHOTO_ZOOM && (data != null && data.getData() != null)) {
            Uri uri = data.getData();
            uri = geturi(data);//解决方案
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = managedQuery(uri, proj, null, null, null);
        }
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);// 图片的路径
//            Bitmap bitmap = BitmapFactory.decodeFile(path);通过转成bitap赋值
//            image.setImageBitmap(bitmap);
            //获取不到图片    因为缺少动态权限
            //我已经得到图片的路径了！！！！！
            Log.d("tag", path + "+++++++++++");
            image.setImageURI(Uri.parse(path));//通过url赋值
//             Glide.with(MainActivity.this).load(path).into(image);//通过glide赋值
//path 路径有了,下面操作....
        }
    }

    //当我申请权限的时候,调用
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //如果用户选择了同意授权     走我的方法
        if (requestCode == 1001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PHOTO_ZOOM);
            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied  权限拒绝",
                        Toast.LENGTH_SHORT).show();
            }
        }
        //如果用户选择了同意授权     走我的方法
        if (requestCode == 1002) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied  权限拒绝",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * 解决小米手机上获取图片路径为null的情况
     *
     * @param intent
     * @return
     */
    public Uri geturi(android.content.Intent intent) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/*"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.ImageColumns._ID},
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
// set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
// do nothing
                } else {
                    Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
    }
}

package com.chdc.chdctextwidget;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Wen on 2017/2/16.
 */

public class Utils {

    /**
     * 从 URI 中获取文件路径
     * @param context
     * @param uri
     * @return
     */
    public static String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * 将对象序列化到文件系统中
     * @param filePath
     * @param object
     * @return
     */
    public static boolean saveObjectToFileSystem(String filePath, Object object){
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(object);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从文件系统中读取 Object
     * @param file
     * @return
     */
    public static Object loadObjectFromFileSystem(String file){
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            return ois.readObject();
        }
        catch (Exception e)
        {
            return null;
        }
        finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置 EditText 只读
     * @param view
     * @param uneditable
     */
    public static void setEditTextUneditable(EditText view, boolean uneditable)
    {
        if(uneditable) {
            view.setCursorVisible(false);      //设置输入框中的光标不可见
            view.setFocusable(false);           //无焦点
            view.setFocusableInTouchMode(false);     //触摸时也得不到焦点
        }
        else
        {
            view.setCursorVisible(true);      //设置输入框中的光标可见
            view.setFocusable(true);           //有焦点
            view.setFocusableInTouchMode(true);     //触摸时可得到焦点
        }
    }

    /**
     * 显示消息
     */
    public static void showMessage(Context context, String msg ){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * 显示确认对话框
     * @param context
     * @param onOkButtonClickListener
     * @param onCancelButtonClickListener
     * @return
     */
    public static void showConfirmDialog(Context context, String title, String message,
                                         DialogInterface.OnClickListener onOkButtonClickListener,
                                         DialogInterface.OnClickListener onCancelButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);

        builder.setPositiveButton("Yes", onOkButtonClickListener);
        builder.setNegativeButton("No", onCancelButtonClickListener != null ? onCancelButtonClickListener :
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

        builder.create().show();
    }

    /**
     * 显示确认对话框
     * @param context
     * @param title
     * @param message
     * @param onOkButtonClickListener
     */
    public static void showConfirmDialog(Context context, String title, String message,
                                         DialogInterface.OnClickListener onOkButtonClickListener){
        showConfirmDialog(context, title, message, onOkButtonClickListener, null);
    }

    public static int Dp2Px(Context context, float dp) {
         final float scale = context.getResources().getDisplayMetrics().density;
         return (int) (dp * scale + 0.5f);
    }

    /**
     * 用多个字符串拼贴成一个字符串
     * @param origin
     * @param times
     * @return
     */
    public static String stringTimes(String origin, int times){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < times; i++){
            sb.append(origin);
        }
        return sb.toString();
    }
}

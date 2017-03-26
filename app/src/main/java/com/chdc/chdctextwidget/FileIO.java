package com.chdc.chdctextwidget;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 6/8/15.
 */
public class FileIO{

    /**
     * 获取文本文件的内容
      */
    public static String readAllText(String file)
    {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = null;
        try {
            FileInputStream fis = new FileInputStream(file);

            isr = new InputStreamReader(fis, "UTF-8");
            char[] buf = new char[1024];
            //用于保存实际读取的字节数
            int hasRead;
            while ((hasRead = isr.read(buf)) > 0 )
            {
                sb.append(buf,0, hasRead);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return sb.toString();
        } finally {
            if (isr != null)
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return sb.toString();
    }

    /**
     * 将所有文本写道文件中
     * @param file
     * @param data
     */
    public static boolean writeAllText(String file, String data){
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(data);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * 删除文件
     * @param context
     * @param documentUri
     * @param file
     * @return
     */
    public static boolean deleteFile(Context context, Uri documentUri, String file){
        boolean result = new File(file).delete();
        if(!result){
            DocumentFile targetDocument = getDocumentFile(context, documentUri, new File(file), false);
            if(targetDocument != null)
                return targetDocument.delete();
            else
                return true;
        }
        return true;
    }

    /**
     * 将所有文本写道文件中
     * @param file
     * @param data
     */
    public static boolean writeAllText(Context context, Uri documentUri, String file, String data){
        boolean result = writeAllText(file, data);
        if(!result){
            OutputStreamWriter osw = null;
            try{
                DocumentFile targetDocument = getDocumentFile(context, documentUri, new File(file), false);
                OutputStream outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
                osw = new OutputStreamWriter(outStream);
                osw.write(data);
                return true;
            }
            catch (Exception e1){
                return false;
            }
            finally {
                if(osw != null){
                    try {
                        osw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    public static DocumentFile getDocumentFile(Context context, Uri treeUri, File file, final boolean isDirectory) {
        String baseFolder = getExtSdCardFolder(context, file);

        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            relativePath = fullPath.substring(baseFolder.length() + 1);
        }
        catch (IOException e) {
            return null;
        }

        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);

        String[] parts = relativePath.split("/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                }
                else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    public static String getExtSdCardFolder(Context context,  File file) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<>();
        File[] externalFilesDirs = context.getExternalFilesDirs("external");
        File externalFilesDir = context.getExternalFilesDir("external");
        for (File file : externalFilesDirs) {
            if (file != null && !file.equals(externalFilesDir)) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("Error", "Unexpected external file dir: " + file.getAbsolutePath());
                }
                else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    }
                    catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }
}

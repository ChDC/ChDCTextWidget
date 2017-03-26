package com.chdc.chdctextwidget;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.util.Objects;

public class SelectEditorActivity extends AppCompatActivity {

    /**
     * 从小部件打开的 Intent Action
     */
    static final String ACTION_OPEN_BY_WIDGET ="com.chdc.chdctextwidget.SelectEditorActivity.action.OPEN_BY_WIDGET";

    String currentFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_select_editor);
        handleIntent();
        openFile();
    }

    void openFile(){
        // 创建的用于点击小部件触发的事件
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(currentFile));
        intent.setDataAndType(uri, "text/plain");

        startActivityForResult(intent, 0);
    }


    /**
     * 处理 Intent
     */
    private void handleIntent()
    {
        Intent i = getIntent();
        if(i != null) {
            String action = i.getAction();
            if(ACTION_OPEN_BY_WIDGET.equals(action))
            {
                currentFile = i.getStringExtra("file");
            }
        }
    }
}

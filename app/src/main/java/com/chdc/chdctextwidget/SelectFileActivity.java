package com.chdc.chdctextwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

public class SelectFileActivity extends AppCompatActivity {

    private int mAppWidgetId;
    private final int SELECT_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_view_text);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        Intent intentOpenFile = new Intent(Intent.ACTION_GET_CONTENT);
        intentOpenFile.setType("text/plain");
        startActivityForResult(intentOpenFile, SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_FILE:
            {
                if(resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    String filePath = Utils.getPath(this, uri);

                    // 将文件信息添加到小部件的映射信息中
                    boolean result = TextWidgetManager.addWidgetInfoToMapFile(this, mAppWidgetId, filePath);
                    if(result){
                        // 构建布局
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                        TextWidgetManager.buildWidget(this, appWidgetManager, mAppWidgetId, filePath);

                        // 返回结果
                        Intent resultValue = new Intent();
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                        setResult(RESULT_OK, resultValue);
                        finish();
                    }
                }
                else{
                    finish();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

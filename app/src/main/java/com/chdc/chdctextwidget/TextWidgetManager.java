package com.chdc.chdctextwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;


/**
 * Created by wen on 6/12/15.
 */
class TextWidgetManager implements java.io.Serializable {

    /**
     * ID map 文件名
     */
    private static final String WIDGET_ID_MAP = "widget_id_map";

    /**
     * 刷新请求的标识
     */
    static final String ACTION_APPWIDGET_REFRESH="com.chdc.chdctextwidget.ViewTextWidget.action.APPWIDGET_REFRESH";

    /**
     * 加载 id map 数据
     * @param context
     * @return
     */
    static JSONObject loadWidgetMap(Context context){
        String data = FileIO.readAllText(getWidgetMapFile(context));
        try {
            JSONObject json = new JSONObject(data);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    /**
     * 保存 map 数据
     * @param context
     * @param map
     */
    static void saveWidgetMap(Context context, JSONObject map){
        FileIO.writeAllText(getWidgetMapFile(context), map.toString());
    }

    /**
     * 添加小部件信息到 map 中
     * @param context
     * @param widgetId
     * @param file
     */
    static boolean addWidgetInfoToMapFile(Context context, int widgetId, String file)
    {
        if(file == null)
            return false;

        // 写入 小部件 map
        JSONObject map = loadWidgetMap(context);
        try {
            map.put(String.valueOf(widgetId), file);
            saveWidgetMap(context, map);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取 map 文件的路径
     * @param context
     * @return
     */
    private static String getWidgetMapFile(Context context){
        return context.getFilesDir() + File.separator + WIDGET_ID_MAP;
    }

    /**
     * 当系统要更新小部件时调用这个方法
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    public static void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // 写入 小部件 map
        JSONObject map = loadWidgetMap(context);

        for(int i : appWidgetIds)
        {
            try {
                buildWidget(context, appWidgetManager, i, map.getString(String.valueOf(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 当文件内容发生改变并通知小部件更新时调用
     * @param context
     * @param file
     */
    public static void update(Context context, String file)
    {
        if(file == null || file.isEmpty())
            return;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        JSONObject map = loadWidgetMap(context);
        Iterator<String> keys = map.keys();

        while(keys.hasNext()){
            String key = keys.next();
            try {
                String value = map.getString(key);
                if(file.equals(value)) {
                    buildWidget(context, appWidgetManager, Integer.parseInt(key), file);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 构建小部件
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param file
     */
    static void buildWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String file)
    {
        if(file == null)
            return;

        Intent intent = new Intent(context, ViewTextActivity.class);
        intent.setAction(ViewTextActivity.ACTION_OPEN_BY_WIDGET);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        intent.putExtra("file", file);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),  R.layout.widget_view_text);
        remoteViews.setOnClickPendingIntent(R.id.lblTextTitle, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.lblTextContent, pendingIntent);

        remoteViews.setTextViewText(R.id.lblTextTitle, FileManager.getTitle(file));
        remoteViews.setTextViewText(R.id.lblTextContent, FileManager.getProcessedContent(file));
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }


    /**
     * 当某个文件内容被更新了就调用该方法通知小部件更新
     * @param context
     * @param file
     */
    public static void refresh(Context context, String file)
    {
        Intent i = new Intent(TextWidgetManager.ACTION_APPWIDGET_REFRESH);
        i.setPackage(context.getPackageName());
        i.putExtra("file", file);
        context.sendBroadcast(i);
    }


    /**
     * 当小部件被删除的时候触发
     * @param context
     * @param widgetId
     */
    static void deleteByWidgetId(Context context, int widgetId)
    {
        JSONObject map = loadWidgetMap(context);
        map.remove(String.valueOf(widgetId));
        saveWidgetMap(context, map);
    }
}

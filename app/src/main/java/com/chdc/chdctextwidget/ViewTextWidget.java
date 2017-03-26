package com.chdc.chdctextwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;


/**
 * Created by wen on 6/10/15.
 */
public class ViewTextWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        TextWidgetManager.update(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onDeleted (Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
        for(int aid : appWidgetIds)
        {
            TextWidgetManager.deleteByWidgetId(context, aid);
        }
    }

    @Override
    public void onReceive (@NonNull Context context, @NonNull Intent intent)
    {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (TextWidgetManager.ACTION_APPWIDGET_REFRESH.equals(action))
        {
            Bundle b = intent.getExtras();
            if(b != null) {
                String file = b.getString("file");
                TextWidgetManager.update(context, file);
            }
        }
    }


}

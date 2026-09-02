package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class OsrsPetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            petName: String = "Baby Black Dragon",
            petIcon: String = "🐉",
            hunger: Int = 85,
            happiness: Int = 90,
            petQuote: String = "\"Welcome to your Player Owned House!\"",
            pohRoomsCount: Int = 1,
            pohStatus: String = "🏡 POH Estate Ready"
        ) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.osrs_pet_widget)
            views.setTextViewText(R.id.widget_poh_title, "🏡 POH ($pohRoomsCount Rooms)")
            views.setTextViewText(R.id.widget_poh_status, pohStatus)
            views.setTextViewText(R.id.widget_pet_icon, petIcon)
            views.setTextViewText(R.id.widget_pet_name, petName)
            views.setTextViewText(R.id.widget_pet_status, "🍗 Hunger: $hunger%  •  💖 Happy: $happiness%")
            views.setTextViewText(R.id.widget_pet_quote, petQuote)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

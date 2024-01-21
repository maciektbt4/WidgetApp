package com.example.widgetapp

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.widget.RemoteViews
import java.io.IOException

/**
 * Implementation of App Widget functionality.
 */
class MultimediaWidget : AppWidgetProvider() {
    private val ACTION_CHANGE_IMAGE_1 = "ACTION_CHANGE_IMAGE_1"
    private val ACTION_CHANGE_IMAGE_2 = "ACTION_CHANGE_IMAGE_2"
    private val ACTION_SONG_PLAY = "ACTION_SONG_PLAY"
    private val ACTION_SONG_PAUSE = "ACTION_SONG_PAUSE"
    private val ACTION_SONG_STOP = "ACTION_SONG_STOP"
    private val ACTION_SONG_CHANGE = "ACTION_SONG_CHANGE"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        val remoteViews = RemoteViews(context.packageName, R.layout.multimedia_widget)

        // register web browser listener
        val intentWeb = Intent(Intent.ACTION_VIEW)
        intentWeb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intentWeb.data = Uri.parse("https://pja.edu.pl/")
        val pendingIntentWeb = PendingIntent.getActivity(context,0,intentWeb, PendingIntent.FLAG_IMMUTABLE)
        remoteViews.setOnClickPendingIntent(R.id.btnWeb, pendingIntentWeb)

        // register image change listener
        remoteViews.setOnClickPendingIntent(R.id.btnImage1,
            createButtonPendingIntent(context, ACTION_CHANGE_IMAGE_1))
        remoteViews.setOnClickPendingIntent(R.id.btnImage2,
            createButtonPendingIntent(context, ACTION_CHANGE_IMAGE_2))

        // register media player
        remoteViews.setOnClickPendingIntent(R.id.btnPlay,
            createButtonPendingIntent(context, ACTION_SONG_PLAY))
        remoteViews.setOnClickPendingIntent(R.id.btnPause,
            createButtonPendingIntent(context, ACTION_SONG_PAUSE))
        remoteViews.setOnClickPendingIntent(R.id.btnStop,
            createButtonPendingIntent(context, ACTION_SONG_STOP))
        remoteViews.setOnClickPendingIntent(R.id.btnNext,
            createButtonPendingIntent(context, ACTION_SONG_CHANGE))

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        disableMediaPlayer()
        // Release MediaPlayer resources when the last widget is removed

    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (ACTION_CHANGE_IMAGE_1 == intent.action || ACTION_CHANGE_IMAGE_2 == intent.action) {
            // Handle the button click here and update the image in the widget
            updateWidgetImage(context, intent)
        }else if (ACTION_SONG_PLAY == intent.action || ACTION_SONG_PAUSE == intent.action ||
            ACTION_SONG_STOP == intent.action || ACTION_SONG_CHANGE == intent.action){
            updateWidgetSong(context, intent)
        }
    }

    private fun updateWidgetImage(context: Context, intent: Intent) {
        // Update the image in the widget's RemoteViews
        val remoteViews = RemoteViews(context.packageName, R.layout.multimedia_widget)

        if (ACTION_CHANGE_IMAGE_1 == intent.action) {
            remoteViews.setImageViewResource(R.id.imageView, R.drawable.img1)
        }else{
            remoteViews.setImageViewResource(R.id.imageView, R.drawable.img2)
        }

        // Update the widget
        val componentName = ComponentName(context, MultimediaWidget::class.java)
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews)
    }

    private fun updateWidgetSong(context: Context, intent: Intent) {
        // Check if mediaPlayer is null or not initialized
        if (mediaPlayer == null) {
            // Initialize mediaPlayer if it hasn't been initialized
            val songId1 = R.raw.em1
            val songId2= R.raw.em2

            if(songIdMemory == null || songIdMemory == songId1){
                songIdMemory = songId1
                mediaPlayer = MediaPlayer.create(context.applicationContext, songId1)
            }else{
                mediaPlayer = MediaPlayer.create(context.applicationContext, songId2)
            }
        }

        // Perform the desired action based on the intent
        when (intent.action) {
            ACTION_SONG_PLAY -> mediaPlayer?.start()
            ACTION_SONG_PAUSE -> mediaPlayer?.pause()
            ACTION_SONG_STOP -> disableMediaPlayer()
        }

        if (ACTION_SONG_CHANGE == intent.action){
            disableMediaPlayer()
            val songId1 = R.raw.em1
            val songId2 = R.raw.em2
            mediaPlayer = if (songId1 == songIdMemory){
                songIdMemory = songId2
                MediaPlayer.create(context.applicationContext, songId2)
            }else{
                songIdMemory = songId1
                MediaPlayer.create(context.applicationContext, songId1)
            }
            mediaPlayer?.start()
        }
    }

    private fun createButtonPendingIntent(context:Context, action:String):PendingIntent{
        val intent = Intent(context, MultimediaWidget::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
    }

    private fun disableMediaPlayer(){
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private var mediaPlayer: MediaPlayer? = null
        private var songIdMemory: Int? = null
    }

}


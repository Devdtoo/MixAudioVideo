package com.example.mixaudiovideo

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.CursorLoader
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.callback.ILoadCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File


class MainActivity : AppCompatActivity() {

    val RC_AUDIO_PICK: Int = 1
    val RC_VIDEO_PICK: Int = 2
    var audioURI: Uri? = null
    var videoURI: Uri? = null
    var audioPath: String? = null
    var videoPath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())

        // Setup Code for Audio Conversion Library
        AndroidAudioConverter.load(this, object : ILoadCallback {
            override fun onSuccess() {
                // Great!
                Timber.e("Your Device Supports FFmpeg")
            }

            override fun onFailure(error: Exception) {
                // FFmpeg is not supported by device
                Timber.e("FFmpeg is not supported by device")
            }
        })

        // Audio Pickup Button
        audioPickBtn.setOnClickListener{
            val audioIntent= Intent(
                Intent.ACTION_PICK,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult( audioIntent, RC_AUDIO_PICK)
        }

        // Video Pickup button
        videoPickBtn.setOnClickListener{
            val videoIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult( videoIntent, RC_VIDEO_PICK)

        }

        // Submit Button
        muxBtn.setOnClickListener {
            if (audioPath != null && videoPath != null) startMuxing(audioPath, videoPath)
        }

       /* var root = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        var audio : String = "$root/audio.aac"
        var video : String = "$root/video.mp4";
        var output : String = "$root/output.mp4"

        var file = File(audio)
        Timber.e("is exist : ${file.exists()}")
        Timber.e("audioPath : $audio, video : $video, output : $output")
        Merge_Video_Audio(this).apply {
            this.doInBackground(audio, video, output)
        }*/



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //After Audio Pickup
        if (requestCode == RC_AUDIO_PICK && resultCode == RESULT_OK){
            audioURI = data?.data
            val audPath = getAudioPath(audioURI!!)

            // To Check:- If Picked up audio is not of "ACC" format, than only Convert audio to ACC
            if (audPath?.substring(audPath.lastIndexOf(".") + 1) != "aac"){
                startAudioConversion(audPath!!)
            } else {
                audioPath = audPath
            }
        }

        //After Video Pickup
        if (requestCode == RC_VIDEO_PICK && resultCode == RESULT_OK){
            videoURI = data?.data
            videoPath = getVideoPath(videoURI!!)
            Timber.e("VIDEO PATH : $videoPath")
        }
    }

    //To get Audio path with extension
    private fun getAudioPath(uri: Uri): String? {
        val data = arrayOf(MediaStore.Audio.Media.DATA)
        val loader =
            CursorLoader(applicationContext, uri, data, null, null, null)
        val cursor: Cursor? = loader.loadInBackground()
        val column_index: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor?.moveToFirst()
        return cursor?.getString(column_index!!)
    }

    //To get Video path with extension
    private fun getVideoPath(uri: Uri): String? {
        val data = arrayOf(MediaStore.Video.Media.DATA)
        val loader =
            CursorLoader(applicationContext, uri, data, null, null, null)
        val cursor: Cursor? = loader.loadInBackground()
        val column_index: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        cursor?.moveToFirst()
        return cursor?.getString(column_index!!)
    }

    //Audio Conversion
    private fun startAudioConversion(currentAudioPath: String){
       val currentAudioFile = File(currentAudioPath)
        val callback: IConvertCallback = object : IConvertCallback {
            override fun onSuccess(convertedFile: File) {
                // So fast? Love it!
                Timber.e("Converted Aud Absolute Path: ${convertedFile.absolutePath}")
                audioPath = convertedFile.path
                Timber.e("Converted Aud Path: ${convertedFile.path}")
            }

            override fun onFailure(error: java.lang.Exception) {
                // Oops! Something went wrong
                Timber.e("Oops! Conversion FAILED")
            }
        }
        AndroidAudioConverter.with(this) // Your current audio file
            .setFile(currentAudioFile) // Your desired audio format
            .setFormat(AudioFormat.AAC) // An callback to know when conversion is finished
            .setCallback(callback) // Start conversion
            .convert()
    }

    //Start Muxing Audio/Video
    private fun startMuxing(audioPath: String?, videoPath: String?) {

        var root = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        var audio = audioPath
        var video = videoPath
        var output : String = "$root/output.mp4"

        var file = File(audio)
        Timber.e("is exist : ${file.exists()}")
        Timber.e("audioPath : $audio, video : $video, output : $output")
        Merge_Video_Audio(this).apply {
            this.doInBackground(audio, video, output)
        }
    }
}
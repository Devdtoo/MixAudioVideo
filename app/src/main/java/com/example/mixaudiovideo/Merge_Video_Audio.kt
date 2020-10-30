package com.example.mixaudiovideo


import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import com.coremedia.iso.IsoFile
import com.coremedia.iso.boxes.Container
import com.googlecode.mp4parser.FileDataSourceImpl
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


/**
 * Created by AQEEL on 2/15/2019.
 */
// this is the class which will add the selected soung to the created video
class Merge_Video_Audio(var context: Context) :
    AsyncTask<String?, String?, String?>() {
    var progressDialog: ProgressDialog
    var audio: String? = null
    var video: String? = null
    var output: String? = null
    override fun onPreExecute() {
        super.onPreExecute()
    }

    public override fun doInBackground(vararg params: String?): String? {
        try {
            progressDialog.show()
        } catch (e: Exception) {
        }
        audio = params[0]
        video = params[1]
        output = params[2]
        Log.d("resp", "$audio----$video-----$output")
        val thread = Thread(runnable)
        thread.start()
        return null
    }

    override fun onPostExecute(s: String?) {
        super.onPostExecute(s)
    }

    fun Go_To_preview_Activity() {
//        val intent = Intent(context, Preview_Video_A::class.java)
//        intent.putExtra("path", Variables.root.toString() + "/output2.mp4")
//        context.startActivity(intent)
    }

    fun CropAudio(videopath: String?, fullAudio: Track): Track {
        try {
            val isoFile = IsoFile(videopath)
            val lengthInSeconds: Double =
                (isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                        isoFile.getMovieBox().getMovieHeaderBox().getTimescale()).toDouble()
            val audioTrack: Track = fullAudio as Track
            val startTime1 = 0.0
            var currentSample: Long = 0
            var currentTime : Double= 0.0
            var lastTime : Double= -1.0
            var startSample1: Long = -1
            var endSample1: Long = -1
            for (i in 0 until audioTrack.getSampleDurations().size) {
                val delta: Long = audioTrack.getSampleDurations().get(i)
                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample
                }
                if (currentTime > lastTime && currentTime <= lengthInSeconds) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample
                }
                lastTime = currentTime
                currentTime += (delta.toDouble()/ audioTrack.getTrackMetaData()
                    .getTimescale().toDouble())
                currentSample++
            }
            return CroppedTrack(fullAudio, startSample1, endSample1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fullAudio
    }

    var runnable = Runnable {
        try {
            val m: Movie = MovieCreator.build(video)
            val nuTracks: MutableList<Track> = ArrayList<Track>()
            for (t in m.tracks) {
                if ("soun" != t.getHandler()) {
                    nuTracks.add(t)
                }
            }
            val nuAudio: Track = AACTrackImpl(FileDataSourceImpl(audio))
            val crop_track: Track = CropAudio(video, nuAudio)
            nuTracks.add(crop_track)

            m.tracks = nuTracks
            val mp4file: Container = DefaultMp4Builder().build(m)
            val fc =
                FileOutputStream(File(output)).channel
            mp4file.writeContainer(fc)
            fc.close()
            try {

                Timber.e("final processing : ")
//                progressDialog.dismiss()
            } catch (e: Exception) {
                Timber.e("resp: ${e.toString()}")
            } finally {
                Go_To_preview_Activity()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.e("resp: ${e.toString()}")
        }
    }

    init {
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait...")
    }




}

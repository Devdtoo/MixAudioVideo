package com.example.mixaudiovideo

import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.util.Matrix
import java.util.*

/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
class Movie {
    var matrix = Matrix.ROTATE_0

    constructor() {}
    constructor(tracks: MutableList<Track>) {
        this.tracks = tracks
    }

    var tracks: MutableList<Track> =
        LinkedList()



    fun addTrack(nuTrack: Track) {
        // do some checking
        // perhaps the movie needs to get longer!
        if (getTrackByTrackId(nuTrack.trackMetaData.trackId) != null) {
            // We already have a track with that trackId. Create a new one
            nuTrack.trackMetaData.trackId = nextTrackId
        }
        tracks.add(nuTrack)
    }

    override fun toString(): String {
        var s = "Movie{ "
        for (track in tracks) {
            s += "track_" + track.trackMetaData.trackId + " (" + track.handler + ") "
        }
        s += '}'
        return s
    }

    val nextTrackId: Long
        get() {
            var nextTrackId: Long = 0
            for (track in tracks) {
                nextTrackId =
                    if (nextTrackId < track.trackMetaData.trackId) track.trackMetaData
                        .trackId else nextTrackId
            }
            return ++nextTrackId
        }

    fun getTrackByTrackId(trackId: Long): Track? {
        for (track in tracks) {
            if (track.trackMetaData.trackId == trackId) {
                return track
            }
        }
        return null
    }

    val timescale: Long
        get() {
            var timescale =
                tracks.iterator().next().trackMetaData.timescale
            for (track in tracks) {
                timescale = Movie.gcd(
                    track.trackMetaData.timescale, timescale
                )
            }
            return timescale
        }

    companion object {
        fun gcd(a: Long, b: Long): Long {
            return if (b == 0L) {
                a
            } else Movie.gcd(b, a % b)
        }
    }
}
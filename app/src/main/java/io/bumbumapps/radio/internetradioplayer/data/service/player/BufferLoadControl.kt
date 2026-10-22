package io.bumbumapps.radio.internetradioplayer.data.service.player

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.upstream.Allocator
import com.google.android.exoplayer2.upstream.DefaultAllocator

/**
 * Shared base for the app's load controls (replaces subclassing DefaultLoadControl, whose
 * constructor and hooks changed in ExoPlayer 2.1x).
 */
abstract class BufferLoadControl : LoadControl {

    private val defaultAllocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE)
    protected var targetBufferSize = DefaultLoadControl.DEFAULT_AUDIO_BUFFER_SIZE

    override fun getAllocator(): Allocator = defaultAllocator

    override fun getBackBufferDurationUs(): Long = 0

    override fun retainBackBufferFromKeyframe(): Boolean = false

    override fun onTracksSelected(renderers: Array<out Renderer>, trackGroups: TrackGroupArray,
                                  trackSelections: Array<out ExoTrackSelection>) {
        targetBufferSize = calculateTargetBufferSize(renderers, trackSelections)
        defaultAllocator.setTargetBufferSize(targetBufferSize)
    }

    override fun onPrepared() = reset(false)

    override fun onStopped() = reset(true)

    override fun onReleased() = reset(true)

    protected open fun reset(resetAllocator: Boolean) {
        targetBufferSize = 0
        if (resetAllocator) defaultAllocator.reset()
    }

    companion object {
        fun calculateTargetBufferSize(renderers: Array<out Renderer>,
                                      trackSelections: Array<out ExoTrackSelection?>): Int {
            var size = 0
            for (i in renderers.indices) {
                if (i < trackSelections.size && trackSelections[i] != null) {
                    size += if (renderers[i].trackType == C.TRACK_TYPE_AUDIO) {
                        DefaultLoadControl.DEFAULT_AUDIO_BUFFER_SIZE
                    } else {
                        DefaultLoadControl.DEFAULT_MUXED_BUFFER_SIZE / 10
                    }
                }
            }
            return maxOf(DefaultLoadControl.DEFAULT_MIN_BUFFER_SIZE, size)
        }
    }
}

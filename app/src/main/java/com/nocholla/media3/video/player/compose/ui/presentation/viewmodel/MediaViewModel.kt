package com.nocholla.media3.video.player.compose.ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaViewModel : ViewModel() {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var player: ExoPlayer? = null

    fun initializePlayer(context: android.content.Context) {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        viewModelScope.launch {
                            _playerState.value = _playerState.value.copy(
                                isPlaying = playbackState == Player.STATE_READY && playWhenReady,
                                isLoading = playbackState == Player.STATE_BUFFERING
                            )
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        viewModelScope.launch {
                            _playerState.value = _playerState.value.copy(
                                error = error.message ?: "Playback error"
                            )
                        }
                    }
                })
            }
        }
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
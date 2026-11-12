package com.hmgappchallenge.kidsrhythm

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hmgappchallenge.kidsrhythm.archive.FileSongArchive
import com.hmgappchallenge.kidsrhythm.audio.BeatPlayer
import com.hmgappchallenge.kidsrhythm.audio.InstrumentSounds
import com.hmgappchallenge.kidsrhythm.audio.PcmRecorder
import com.hmgappchallenge.kidsrhythm.audio.SongPlayer
import com.hmgappchallenge.kidsrhythm.audio.VoiceGuide
import com.hmgappchallenge.kidsrhythm.character.CharacterStore
import com.hmgappchallenge.kidsrhythm.character.SharedPreferencesCharacterStore
import com.hmgappchallenge.kidsrhythm.family.FamilyRepository
import com.hmgappchallenge.kidsrhythm.family.PrefsFamilyRepository
import com.hmgappchallenge.kidsrhythm.server.SongApi
import com.hmgappchallenge.kidsrhythm.server.SongResult
import com.hmgappchallenge.kidsrhythm.server.SongService
import com.hmgappchallenge.kidsrhythm.vehicle.CarVehicleSignals
import com.hmgappchallenge.kidsrhythm.vehicle.VehicleSignals
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** S05에서 받은 완성곡을 S06으로 넘긴다. */
data class CompletedSong(val songId: String, val result: SongResult, val audioFile: File, val createdAtMillis: Long)

/** 앱 전체에서 같이 쓰는 것들. Activity가 하나라 여기서 만들고 정리한다. */
class AppServices(context: Context) {
    private val appContext = context.applicationContext

    val songService: SongService = SongApi()
    val archive = FileSongArchive(File(appContext.filesDir, "songs"))
    val family: FamilyRepository = PrefsFamilyRepository(appContext)

    /** 같이 놀 친구 (S01-1 친구 고르기). 홈과 S06 완성에 나온다. */
    val characterStore: CharacterStore = SharedPreferencesCharacterStore(appContext)
    val vehicle: VehicleSignals = CarVehicleSignals(appContext)
    val beatPlayer = BeatPlayer(appContext)
    val sounds = InstrumentSounds(appContext)
    val player = SongPlayer()

    /** S02 시작 음성 안내. 엔진 준비에 시간이 걸려서 앱을 켤 때 만들어 둔다. */
    val voiceGuide = VoiceGuide(appContext)

    /** 화면이 닫혀도 끝까지 해야 하는 일 (서버에 지우기 요청 등). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var completedSong: CompletedSong? by mutableStateOf(null)

    fun newRecorder() = PcmRecorder(appContext)

    /** 받아 둔 완성곡 mp3 자리. 보관함에 저장하면 앱 저장 공간으로 옮긴다. */
    fun cacheFileFor(songId: String) = File(File(appContext.cacheDir, "songs"), "$songId.mp3")

    fun release() {
        player.release()
        beatPlayer.release()
        sounds.release()
        voiceGuide.release()
        appScope.cancel()
    }
}

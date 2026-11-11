package com.hmgappchallenge.kidsrhythm.character

import android.content.Context
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter

/** 같이 놀 친구(S01-1). 홈과 완성 화면에 나오는 캐릭터이며, 앱을 다시 켜도 남는다. */
interface CharacterStore {
    val selected: MascotCharacter
    fun select(character: MascotCharacter)
}

/** 아무도 고르지 않았을 때 나오는 친구. */
val DefaultCharacter = MascotCharacter.LION

class SharedPreferencesCharacterStore(context: Context) : CharacterStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override val selected: MascotCharacter
        get() = characterOf(prefs.getString(KEY_SELECTED, null))

    override fun select(character: MascotCharacter) {
        prefs.edit().putString(KEY_SELECTED, character.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "character"
        const val KEY_SELECTED = "selected"
    }
}

/** 저장된 이름으로 캐릭터를 찾는다. 없거나 모르는 이름이면 기본 친구다. */
internal fun characterOf(saved: String?): MascotCharacter =
    MascotCharacter.entries.firstOrNull { it.name == saved } ?: DefaultCharacter

package com.hmgappchallenge.kidsrhythm.consent

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 보호자 동의 여부. 첫 실행에 한 번 묻고 기기에 남긴다. */
interface ConsentStore {
    val isAgreed: Boolean
    /** 동의한 시각. 동의 전이면 null. */
    val agreedAtMillis: Long?
    fun agree()
    /** 동의를 철회한다. 다음에 노래를 만들려면 다시 동의해야 한다. */
    fun withdraw()
}

class SharedPreferencesConsentStore(context: Context) : ConsentStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override val isAgreed: Boolean get() = prefs.getBoolean(KEY_AGREED, false)

    override val agreedAtMillis: Long?
        get() = prefs.getLong(KEY_AGREED_AT, 0L).takeIf { isAgreed && it > 0L }

    override fun agree() {
        prefs.edit().putBoolean(KEY_AGREED, true).putLong(KEY_AGREED_AT, System.currentTimeMillis()).apply()
    }

    override fun withdraw() {
        prefs.edit().remove(KEY_AGREED).remove(KEY_AGREED_AT).apply()
    }

    private companion object {
        const val PREFS_NAME = "consent"
        const val KEY_AGREED = "guardian_agreed"
        const val KEY_AGREED_AT = "guardian_agreed_at"
    }
}

/**
 * 홈 화면의 동의 팝업 표시 규칙.
 * - 동의 전이면 처음 들어올 때 팝업을 띄운다
 * - "나중에"로 닫아도 노래 만들기를 누르면 다시 띄운다 (동의 없이는 시작하지 않는다)
 */
class ConsentGate(private val store: ConsentStore) {
    var isDialogVisible: Boolean by mutableStateOf(!store.isAgreed)
        private set

    fun agree() {
        store.agree()
        isDialogVisible = false
    }

    fun dismiss() {
        isDialogVisible = false
    }

    /** S08 동의 철회. 팝업은 다음에 노래 만들기를 누를 때 다시 뜬다. */
    fun withdraw() {
        store.withdraw()
        isDialogVisible = false
    }

    /** 노래 만들기를 시작해도 되면 true. 동의 전이면 팝업을 다시 띄우고 false. */
    fun requestStart(): Boolean {
        if (store.isAgreed) return true
        isDialogVisible = true
        return false
    }
}

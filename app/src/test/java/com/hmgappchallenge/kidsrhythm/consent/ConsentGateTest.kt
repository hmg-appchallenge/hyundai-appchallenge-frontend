package com.hmgappchallenge.kidsrhythm.consent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class MemoryConsentStore(override var isAgreed: Boolean = false) : ConsentStore {
    override var agreedAtMillis: Long? = null

    override fun agree() {
        isAgreed = true
        agreedAtMillis = 1L
    }

    override fun withdraw() {
        isAgreed = false
        agreedAtMillis = null
    }
}

class ConsentGateTest {
    @Test
    fun `동의 전이면 처음 들어올 때 동의 팝업을 띄운다`() {
        assertTrue(ConsentGate(MemoryConsentStore()).isDialogVisible)
    }

    @Test
    fun `이미 동의했으면 팝업 없이 바로 시작할 수 있다`() {
        val gate = ConsentGate(MemoryConsentStore(isAgreed = true))
        assertFalse(gate.isDialogVisible)
        assertTrue(gate.requestStart())
    }

    @Test
    fun `동의하고 시작하기를 누르면 동의가 저장되고 팝업이 닫힌다`() {
        val store = MemoryConsentStore()
        val gate = ConsentGate(store)
        gate.agree()
        assertTrue(store.isAgreed)
        assertFalse(gate.isDialogVisible)
    }

    @Test
    fun `나중에로 닫아도 노래 만들기를 누르면 팝업을 다시 띄우고 시작하지 않는다`() {
        val gate = ConsentGate(MemoryConsentStore())
        gate.dismiss()
        assertFalse(gate.isDialogVisible)
        assertFalse(gate.requestStart())
        assertTrue(gate.isDialogVisible)
    }

    @Test
    fun `동의를 철회하면 노래 만들기를 누를 때 동의 팝업을 다시 띄운다`() {
        val gate = ConsentGate(MemoryConsentStore(isAgreed = true))
        gate.withdraw()
        assertFalse(gate.isDialogVisible)
        assertFalse(gate.requestStart())
        assertTrue(gate.isDialogVisible)
    }
}

package byx.matcher

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PerformanceTest {
    @Test
    fun test1() {
        // (a*)*
        val m = 'a'.many().many()
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(1000)}b"))
    }

    @Test
    fun test2() {
        // (a+)+
        val m = 'a'.many1().many1()
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(1000)}b"))
    }

    @Test
    fun test3() {
        // (((((((((a*)*)*)*)*)*)*)*)*)*
        val m = 'a'.many().many().many().many().many().many().many().many().many().many()
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(10)}b"))
    }

    @Test
    fun test4() {
        // (((((((((a+)+)+)+)+)+)+)+)+)+
        val m = ch('a').many1().many1().many1().many1().many1().many1().many1().many1().many1().many1()
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(10)}b"))
    }

    @Test
    fun test5() {
        // (((((((((a*)+)*)+)*)+)*)+)*)+
        val m = ch('a').many().many1().many().many1().many().many1().many().many1().many().many1()
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(10)}b"))
    }

    @Test
    fun test6() {
        // (((((((((a+)*)+)*)+)*)+)*)+)*
        val m = ch('a').many1().many().many1().many().many1().many().many1().many().many1().many()
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(10)}b"))
    }

    @Test
    fun test7() {
        // .*.*=.*
        val m = any.many() and any.many() and '=' and any.many()
        assertTrue(m.match("a=${"b".repeat(1000)}"))
    }

    @Test
    fun test8() {
        // X(.+)+X
        val m = 'X' and any.many1().many1() and 'X'
        assertTrue(m.match("X========================================================================X"))
        assertFalse(m.match("X======================================================================X="))
    }

    @Test
    fun test9() {
        // ((0|1)+)+b
        val m = chs('0', '1').many1().many1() and 'b'
        assertTrue(m.match("${"01".repeat(1000)}b"))
        assertFalse(m.match("01".repeat(50)))
    }

    @Test
    fun test10() {
        // (a|aa)*
        val m = strs("a", "aa").many()
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(1000)}b"))
    }

    @Test
    fun test11() {
        val m = ch('a').repeat(10, 10000).repeat(10, 10000).repeat(10, 10000)
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("a".repeat(100)))
        assertFalse(m.match("${"a".repeat(100)}b"))
    }

    @Test
    fun test12() {
        val m = 'a'.many().many().many().many().many().repeat(10)
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(20)}b"))
    }

    @Test
    fun test13() {
        val m = 'a'.many1().many1().many1().many1().many1().repeat(10)
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("${"a".repeat(20)}b"))
    }
}
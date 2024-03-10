package byx.matcher

import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MatcherCombinatorTest {
    @Test
    fun testCh() {
        val m = ch('a')
        assertTrue(m.match("a"))
        assertFalse(m.match(""))
        assertFalse(m.match("b"))
        assertFalse(m.match("aa"))
        assertFalse(m.match("xy"))
    }

    @Test
    fun testChs1() {
        val m = chs('a', 'b', 'c')
        assertTrue(m.match("a"))
        assertTrue(m.match("b"))
        assertTrue(m.match("c"))
        assertFalse(m.match("d"))
        assertFalse(m.match("1"))
    }

    @Test
    fun testChs2() {
        val arr = arrayOf('a', 'b', 'c').toCharArray()
        val m = chs(*arr)
        assertTrue(m.match("a"))
        assertTrue(m.match("b"))
        assertTrue(m.match("c"))
        assertFalse(m.match("d"))
        assertFalse(m.match("1"))
    }

    @Test
    fun testAny() {
        val m = any
        assertTrue(m.match("a"))
        assertTrue(m.match("b"))
        assertFalse(m.match(""))
        assertFalse(m.match("xyz"))
    }

    @Test
    fun testRange() {
        val m = range('0', '9')
        assertTrue(m.match("0"))
        assertTrue(m.match("5"))
        assertTrue(m.match("9"))
        assertFalse(m.match(""))
        assertFalse(m.match("a"))
    }

    @Test
    fun testNot() {
        val m = not('a')
        assertTrue(m.match("b"))
        assertFalse(m.match("a"))
    }

    @Test
    fun testStr() {
        val m = str("abc")
        assertTrue(m.match("abc"))
        assertFalse(m.match(""))
        assertFalse(m.match("a"))
        assertFalse(m.match("ab"))
        assertFalse(m.match("ax"))
        assertFalse(m.match("abx"))
        assertFalse(m.match("abcx"))
    }

    @Test
    fun testAnd() {
        val m = ch('a').and(ch('b'))
        assertTrue(m.match("ab"))
        assertFalse(m.match("a"))
        assertFalse(m.match("abc"))
        assertFalse(m.match("ba"))
        assertFalse(m.match("x"))
        assertFalse(m.match("xy"))
        assertFalse(m.match(""))
    }

    @Test
    fun testOr() {
        val m = ch('a').or(ch('b'))
        assertTrue(m.match("a"))
        assertTrue(m.match("b"))
        assertFalse(m.match("x"))
        assertFalse(m.match("ax"))
        assertFalse(m.match("by"))
        assertFalse(m.match("mn"))
        assertFalse(m.match(""))
    }

    @Test
    fun testRepeat1() {
        val m: Matcher = ch('a').repeat(3, 5)
        assertTrue(m.match("aaa"))
        assertTrue(m.match("aaaa"))
        assertTrue(m.match("aaaaa"))
        assertFalse(m.match(""))
        assertFalse(m.match("a"))
        assertFalse(m.match("aa"))
        assertFalse(m.match("aaaaaa"))
        assertFalse(m.match("aaaaaaa"))
    }

    @Test
    fun testRepeat2() {
        val m: Matcher = ch('a').repeat(3)
        assertTrue(m.match("aaa"))
        assertFalse(m.match(""))
        assertFalse(m.match("a"))
        assertFalse(m.match("aa"))
        assertFalse(m.match("aaaa"))
        assertFalse(m.match("aaaaa"))
    }

    @Test
    fun testRepeat3() {
        val m = ch('a').repeat(3, Integer.MAX_VALUE)
        assertTrue(m.match("aaa"))
        assertTrue(m.match("aaaa"))
        assertTrue(m.match("a".repeat(1000)))
        assertFalse(m.match("aa"))
    }

    @Test
    fun testMany() {
        val m = ch('a').many()
        assertTrue(m.match(""))
        assertTrue(m.match("a"))
        assertTrue(m.match("aaaaa"))
        assertFalse(m.match("b"))
        assertFalse(m.match("bbbb"))
        assertFalse(m.match("aaab"))
        assertFalse(m.match("aaabaaaa"))
    }

    @Test
    fun testMany1() {
        val m = ch('a').many1()
        assertTrue(m.match("a"))
        assertTrue(m.match("aaaaa"))
        assertFalse(m.match(""))
        assertFalse(m.match("b"))
        assertFalse(m.match("bbbb"))
        assertFalse(m.match("aaab"))
        assertFalse(m.match("aaabaaaa"))
    }

    @Test
    fun testMany2() {
        val m = ch('a').many(3)
        assertFalse(m.match(""))
        assertFalse(m.match("a"))
        assertFalse(m.match("aa"))
        assertTrue(m.match("aaa"))
        assertTrue(m.match("aaaa"))
        assertTrue(m.match("aaaaa"))
    }

    @Test
    fun testFlatMap1() {
        val m = not(' ').many1().flatMap { s -> ch(' ').and(str("xxx ")).and(str(s)) }
        assertTrue(m.match("m xxx m"))
        assertTrue(m.match("aaa xxx aaa"))
        assertTrue(m.match("bbbb xxx bbbb"))
        assertFalse(m.match("aaa xxx bbb"))
        assertFalse(m.match("aaaa xxx aaa"))
        assertFalse(m.match("aaa xxx aaaa"))
    }

    @Test
    fun testFlatMap2() {
        val m: Matcher = any.many1().flatMap { s -> any.repeat(s.length) }
        assertTrue(m.match("aaabbb"))
        assertTrue(m.match("aaaabbbb"))
        assertTrue(m.match("xxxxxyyyyy"))
        assertFalse(m.match("aaabbbb"))
        assertFalse(m.match("xxxxyyy"))
        assertFalse(m.match("mmm"))
    }

    @Test
    fun testLazy() {
        val i = AtomicInteger(123)
        val m = lazy {
            i.set(456)
            ch('a')
        }
        assertEquals(123, i.get())
        assertTrue(m.match("a"))
        assertEquals(456, i.get())
    }
}
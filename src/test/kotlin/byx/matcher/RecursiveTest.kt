package byx.matcher

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 括号匹配校验
 * expr = term+
 * term = "()"
 *      | '(' expr ')'
 */
internal object BracketMatcher {
    private val term = oneOf(
        str("()"),
        '(' and lazy { expr } and ')'
    )
    private val expr: Matcher = term.many1()

    fun isBracketMatch(s: String): Boolean {
        return expr.match(s)
    }
}

/**
 * 算数表达式校验
 * expr = term ('+'|'-' term)+
 * term = fact ('*'|'/' fact)+
 * fact = [0-9]+
 *      | '-' fact
 *      | '(' expr ')'
 */
internal object ArithmeticExprValidator {
    private val fact: Matcher = oneOf(
        range('0', '9').many1(),
        lazy { negExpr },
        '(' and lazy { expr } and ')'
    )
    private val negExpr = '-' and fact
    private val term: Matcher = fact and (chs('*', '/') and fact).many()
    private val expr: Matcher = term and (chs('+', '-') and term).many()

    fun isValidExpr(s: String): Boolean {
        return expr.match(s)
    }
}

/**
 * json字符串校验
 * jsonObj = number | string | bool | arr | obj
 * number  = integer | decimal
 * integer = [0-9]+
 * decimal = [0-9]+ '.' [0-9]+
 * string  = '"' (.*) '"'
 * bool    = "true" | "false"
 * arr     = "[]"
 *         | '[' jsonObj (',' jsonObj)* ']'
 * field   = string ':' jsonObj
 * obj     = "{}"
 *         | '{' field (',' field)* '}'
 */
internal object JsonValidator {
    private val blank = chs(' ', '\t', '\n', '\r').many()
    private val objStart = ch('{').withBlank
    private val objEnd = ch('}').withBlank
    private val arrStart = ch('[').withBlank
    private val arrEnd = ch(']').withBlank
    private val colon = ch(':').withBlank
    private val comma = ch(',').withBlank

    private val jsonObj: Matcher = oneOf(
        lazy { number },
        lazy { string },
        lazy { bool },
        lazy { arr },
        lazy { obj }
    )
    private val digits = range('0', '9').many1()
    private val integer = digits
    private val decimal = digits and '.' and digits
    private val number = integer or decimal
    private val string = '"' and not('"').many() and '"'
    private val bool = strs("true", "false")
    private val arr = oneOf(
        arrStart and arrEnd,
        arrStart and jsonObj.and(comma.and(jsonObj).many()) and arrEnd
    )
    private val field = string and colon and jsonObj
    private val obj = oneOf(
        objStart and objEnd,
        objStart and field.and(comma.and(field).many()) and objEnd
    )

    private val Matcher.withBlank: Matcher
        get() = blank and this and blank

    fun isValidJson(s: String): Boolean {
        return jsonObj.match(s)
    }
}

class RecursiveTest {
    @Test
    fun testBracketMatcher() {
        assertFalse(BracketMatcher.isBracketMatch(""))
        assertFalse(BracketMatcher.isBracketMatch("("))
        assertFalse(BracketMatcher.isBracketMatch(")"))
        assertTrue(BracketMatcher.isBracketMatch("()"))
        assertFalse(BracketMatcher.isBracketMatch(")("))
        assertFalse(BracketMatcher.isBracketMatch("(("))
        assertFalse(BracketMatcher.isBracketMatch("))"))
        assertTrue(BracketMatcher.isBracketMatch("()()"))
        assertTrue(BracketMatcher.isBracketMatch("(())"))
        assertFalse(BracketMatcher.isBracketMatch("(()"))
        assertFalse(BracketMatcher.isBracketMatch("())"))
        assertTrue(BracketMatcher.isBracketMatch("()()()"))
        assertTrue(BracketMatcher.isBracketMatch("()(())"))
        assertTrue(BracketMatcher.isBracketMatch("(())()"))
        assertTrue(BracketMatcher.isBracketMatch("(()())()"))
        assertTrue(BracketMatcher.isBracketMatch("(())()((()))()"))
        assertFalse(BracketMatcher.isBracketMatch("(())()((())()"))
        assertFalse(BracketMatcher.isBracketMatch("(())()(()))()"))
    }

    @Test
    fun testArithmeticExprValidator() {
        assertFalse(ArithmeticExprValidator.isValidExpr(""))
        assertTrue(ArithmeticExprValidator.isValidExpr("123"))
        assertTrue(ArithmeticExprValidator.isValidExpr("-6"))
        assertTrue(ArithmeticExprValidator.isValidExpr("2*(3+4)"))
        assertFalse(ArithmeticExprValidator.isValidExpr("abc"))
        assertFalse(ArithmeticExprValidator.isValidExpr("12+"))
        assertFalse(ArithmeticExprValidator.isValidExpr("12*"))
        assertFalse(ArithmeticExprValidator.isValidExpr("+3"))
        assertFalse(ArithmeticExprValidator.isValidExpr("/6"))
        assertFalse(ArithmeticExprValidator.isValidExpr("6+3-"))
        assertTrue(ArithmeticExprValidator.isValidExpr("(12+345)*(67-890)+10/6"))
        assertTrue(ArithmeticExprValidator.isValidExpr("-6*18+(-3/978)"))
        assertTrue(ArithmeticExprValidator.isValidExpr("24/5774*(6/357+637)-2*7/52+5"))
        assertFalse(ArithmeticExprValidator.isValidExpr("24/5774*(6/357+637-2*7/52+5"))
        assertTrue(ArithmeticExprValidator.isValidExpr("7758*(6/314+552234)-2*61/(10+2/(40-38*5))"))
        assertFalse(ArithmeticExprValidator.isValidExpr("7758*(6/314+552234)-2*61/(10+2/40-38*5))"))
    }

    @Test
    fun testJsonValidator() {
        assertTrue(
            JsonValidator.isValidJson(
                """
            {
                "a": 123,
                "b": 3.14,
                "c": "hello",
                "d": {
                    "x": 100,
                    "y": "world!"
                },
                "e": [
                    12,
                    34.56,
                    {
                        "name": "Xiao Ming",
                        "age": 18,
                        "score": [99.8, 87.5, 60.0]
                    },
                    "abc"
                ],
                "f": [],
                "g": {},
                "h": [true, {"m": false}]
            }
            """
            )
        )
        assertTrue(JsonValidator.isValidJson("123"))
        assertTrue(JsonValidator.isValidJson("34.56"))
        assertTrue(JsonValidator.isValidJson("\"hello\""))
        assertTrue(JsonValidator.isValidJson("true"))
        assertTrue(JsonValidator.isValidJson("false"))
        assertTrue(JsonValidator.isValidJson("{}"))
        assertTrue(JsonValidator.isValidJson("[]"))
        assertTrue(JsonValidator.isValidJson("[{}]"))
        assertFalse(JsonValidator.isValidJson(""))
        assertFalse(JsonValidator.isValidJson("{"))
        assertFalse(JsonValidator.isValidJson("}"))
        assertFalse(JsonValidator.isValidJson("{}}"))
        assertFalse(JsonValidator.isValidJson("[1, 2 3]"))
        assertFalse(JsonValidator.isValidJson("{1, 2, 3}"))
    }
}
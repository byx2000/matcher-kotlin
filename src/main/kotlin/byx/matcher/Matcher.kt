package byx.matcher

fun interface Matcher {
    /**
     * 从[index]位置开始解析字符串[s]，返回解析后的位置集合，用Sequence表示
     */
    fun parse(s: String, index: Int): Sequence<Int>

    /**
     * 判断字符串[s]是否与当前模式匹配
     */
    fun match(s: String) = parse(s, 0).any { i -> i == s.length }

    /**
     * 连接两个Matcher
     */
    infix fun and(rhs: Matcher) = Matcher { s, index ->
        parse(s, index).flatMap { i -> rhs.parse(s, i) }.distinct()
    }

    /**
     * 将字符串[s]转换成Matcher，并与当前Matcher相连
     */
    infix fun and(s: String) = and(str(s))

    /**
     * 将字符[c]转换成Matcher，并与当前Matcher相连
     */
    infix fun and(c: Char) = and(ch(c))

    /**
     * 使用or连接两个Matcher
     */
    infix fun or(rhs: Matcher) = Matcher { s, index ->
        (parse(s, index) + rhs.parse(s, index)).distinct()
    }

    /**
     * 将字符串[s]转换成Matcher，并使用or与当前Matcher相连
     */
    infix fun or(s: String) = or(str(s))

    /**
     * 将字符[c]转换成Matcher，并使用or与当前Matcher相连
     */
    infix fun or(c: Char) = or(ch(c))

    /**
     * 将当前Matcher连续应用至少[minTimes]次
     */
    fun many(minTimes: Int = 0) = Matcher { s, index ->
        sequence {
            // 应用minTimes次
            var seq = sequenceOf(index)
            repeat(minTimes) {
                seq = seq.flatMap { i -> parse(s, i) }.distinct()
            }

            val visited = HashSet<Int>()
            seq.forEach { i ->
                yield(i)
                visited.add(i)
            }

            // 继续应用当前解析器，直到没有新的位置产生
            val queue = ArrayDeque(visited)
            while (!queue.isEmpty()) {
                for (i in parse(s, queue.removeFirst())) {
                    if (!visited.contains(i)) {
                        yield(i)
                        visited.add(i)
                        queue.addLast(i)
                    }
                }
            }
        }
    }

    /**
     * 将当前Matcher连续应用1次或多次
     */
    fun many1() = many(1)

    /**
     * 将当前Matcher重复最少[minTimes]次，最多[maxTimes]次
     */
    fun repeat(minTimes: Int, maxTimes: Int) = Matcher { s, index ->
        sequence {
            // 应用minTimes次
            var set = sequenceOf(index)
            repeat(minTimes) {
                set = set.flatMap { i -> parse(s, i) }.distinct()
            }

            val visited = HashSet<Int>()
            set.forEach { i ->
                yield(i)
                visited.add(i)
            }

            // 继续应用直到maxTimes次
            val queue = ArrayDeque(visited)
            var times = minTimes
            while (!queue.isEmpty() && times < maxTimes) {
                val cnt = queue.size
                repeat(cnt) {
                    for (i in parse(s, queue.removeFirst())) {
                        if (!visited.contains(i)) {
                            yield(i)
                            visited.add(i)
                            queue.addLast(i)
                        }
                    }
                }
                times++
            }
        }
    }

    /**
     * 将当前解析器重复[times]次
     */
    fun repeat(times: Int) = repeat(times, times)

    /**
     * 应用当前Matcher，并调用[mapper]生成下一个Matcher，继续应用下一个Mapper
     */
    fun flatMap(mapper: (String) -> Matcher) = Matcher { s, index ->
        parse(s, index).flatMap { i ->
            val matchStr = s.substring(index..<i)
            mapper(matchStr).parse(s, i)
        }.distinct()
    }
}

/**
 * 匹配满足条件的单个字符，条件由[predicate]指定
 */
fun ch(predicate: (Char) -> Boolean) = Matcher { s, index ->
    if (index < s.length && predicate(s[index])) {
        sequenceOf(index + 1)
    } else {
        emptySequence()
    }
}

/**
 * 匹配字符集[chs]内的字符
 */
fun chs(vararg chs: Char): Matcher {
    val set = chs.toSet()
    return ch { c -> set.contains(c) }
}

/**
 * 匹配任意字符
 */
val any = ch { true }

/**
 * 匹配字符[c]
 */
fun ch(c: Char) = ch { ch -> ch == c }

/**
 * 匹配不等于[c]的字符
 */
fun not(c: Char) = ch { ch -> ch != c }

/**
 * 匹配范围[[c1], [c2]]内的字符
 */
fun range(c1: Char, c2: Char) = ch { c -> (c - c1) * (c - c2) <= 0 }

/**
 * 匹配指定字符串[str]
 */
fun str(str: String) = Matcher { s, index ->
    if (s.startsWith(str, index)) {
        sequenceOf(index + str.length)
    } else {
        emptySequence()
    }
}

/**
 * 匹配集合[strs]内的字符串
 */
fun strs(vararg strs: String) = strs.map(::str).reduce(Matcher::or)

/**
 * 将多个Matcher用and连接
 */
fun seq(vararg matchers: Matcher) = matchers.reduce(Matcher::and)

/**
 * 将多个Matcher用or连接
 */
fun oneOf(vararg matchers: Matcher) = matchers.reduce(Matcher::or)

/**
 * 惰性Matcher，解析时调用[matcherSupplier]获取Matcher实例并执行
 */
fun lazy(matcherSupplier: () -> Matcher) = Matcher { s, index -> matcherSupplier().parse(s, index) }

fun Char.many(minTimes: Int = 0) = ch(this).many(minTimes)
fun Char.many1() = ch(this).many1()
infix fun Char.and(matcher: Matcher) = ch(this).and(matcher)
infix fun Char.or(matcher: Matcher) = ch(this).or(matcher)

fun String.many(minTimes: Int = 0) = str(this).many(minTimes)
fun String.many1() = str(this).many1()
infix fun String.and(matcher: Matcher) = str(this).and(matcher)
infix fun String.or(matcher: Matcher) = str(this).or(matcher)

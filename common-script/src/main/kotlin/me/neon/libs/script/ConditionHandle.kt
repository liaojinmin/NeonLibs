package me.neon.libs.script

import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern

/**
 * NeonLibs
 * PACKAGE_NAME
 *
 * @author 老廖
 * @since 2024/4/28 15:36
 */


fun main() {
    val teams = listOf("Team A", "Team B", "Team C", "Team D")
    val cache: MutableSet<Pair<String, String>> = mutableSetOf()

    // 生成所有可能的对战组合
    val allPossibleMatches = mutableSetOf<Pair<String, String>>()
    for (i in teams.indices) {
        for (j in i + 1 until teams.size) {
            allPossibleMatches.add(teams[i] to teams[j])
        }
    }

    // 生成一回合的对战队伍
    fun generateOneRound(): List<Pair<String, String>> {
        val teamsInCurrentMatches = mutableSetOf<String>()
        val roundMatches = mutableListOf<Pair<String, String>>()

        // 从所有可能的对战组合中选择对战队伍
        var availableMatches: List<Pair<String, String>> = allPossibleMatches.filter { !roundMatches.contains(it) && !cache.contains(it) }
        if (availableMatches.isEmpty()) {
            cache.clear()
            availableMatches = allPossibleMatches.filter { !roundMatches.contains(it) && !cache.contains(it) }
        }

        for (match in availableMatches) {
            val (team1, team2) = match
            if (team1 !in teamsInCurrentMatches && team2 !in teamsInCurrentMatches) {
                roundMatches.add(match)
                cache.add(match)
                teamsInCurrentMatches.add(team1)
                teamsInCurrentMatches.add(team2)
                if (roundMatches.size == teams.size / 2) break
            }
        }
        val unpairedTeams = teams - teamsInCurrentMatches
        if (unpairedTeams.isNotEmpty()) {
            println("Unpaired teams in this round: ${unpairedTeams.first()}")

        }

        return roundMatches
    }

    // 获取一回合的对战队伍
    fun simulateOneRound() {
        val oneRoundMatches = generateOneRound()

        // 输出这一回合的对战队伍
        println("One round of matches:")
        for ((team1, team2) in oneRoundMatches) {
            println("$team1 vs $team2")
        }

        // 计算未参与战斗的队伍
        val allTeams = teams.toSet()
        val pairedTeams = oneRoundMatches.flatMap { listOf(it.first, it.second) }.toSet()
        val unpairedTeams = allTeams - pairedTeams

        if (unpairedTeams.isNotEmpty()) {
            println("Unpaired teams in this round:")
            for (team in unpairedTeams) {
                println(team)
            }
        }
    }

    repeat(3) {
        simulateOneRound()
    }
}


/*
fun main() {
    val teams = listOf("Team A", "Team B", "Team C")

    // 生成所有可能的对战组合
    val allPossibleMatches = mutableSetOf<Pair<String, String>>()
    for (i in teams.indices) {
        for (j in i + 1 until teams.size) {
            allPossibleMatches.add(teams[i] to teams[j])
        }
    }

    // 生成对战轮次
    fun generateRounds(): List<List<Pair<String, String>>> {
        val rounds = mutableListOf<List<Pair<String, String>>>()
        val usedMatches = mutableSetOf<Pair<String, String>>()

        while (usedMatches.size < allPossibleMatches.size) {
            val currentMatches = mutableListOf<Pair<String, String>>()
            val teamsInCurrentMatches = mutableSetOf<String>()

            val availableMatches = allPossibleMatches.filter { !usedMatches.contains(it) }

            // 尝试创建一个新的回合
            for (match in availableMatches) {
                val (team1, team2) = match
                if (team1 !in teamsInCurrentMatches && team2 !in teamsInCurrentMatches) {
                    currentMatches.add(match)
                    teamsInCurrentMatches.add(team1)
                    teamsInCurrentMatches.add(team2)
                    usedMatches.add(match)
                    if (currentMatches.size == teams.size / 2) break
                }
            }

            // 如果当前轮次配对不满，需要将未配对的队伍打印出来
            val unpairedTeams = teams - teamsInCurrentMatches
            if (unpairedTeams.isNotEmpty()) {
                println("Unpaired teams in this round: ${unpairedTeams.first()}")

            }

            rounds.add(currentMatches)
        }

        return rounds
    }

    // 模拟并输出所有轮次
    fun simulateRounds() {
        val allRounds = generateRounds()

        // 输出所有轮次的对阵
        for ((index, round) in allRounds.withIndex()) {
            println("Round ${index + 1}:")
            for ((team1, team2) in round) {
                println("$team1 vs $team2")
            }
            println()
        }
    }

    simulateRounds()
}

 */


/*
fun main() {
    val teams = listOf("Team A", "Team B", "Team C", "Team D")

    // 生成所有可能的对战组合
    val allPossibleMatches = mutableSetOf<Pair<String, String>>()
    for (i in teams.indices) {
        for (j in i + 1 until teams.size) {
            allPossibleMatches.add(teams[i] to teams[j])
        }
    }

    // 生成对战轮次
    fun generateRounds(): List<List<Pair<String, String>>> {
        val rounds = mutableListOf<List<Pair<String, String>>>()
        val usedMatches = mutableSetOf<Pair<String, String>>()

        while (usedMatches.size < allPossibleMatches.size) {
            val currentMatches = mutableListOf<Pair<String, String>>()
            val teamsInCurrentMatches = mutableSetOf<String>()

            val availableMatches = allPossibleMatches.filter { !usedMatches.contains(it) }

            for (match in availableMatches) {
                val (team1, team2) = match
                if (team1 !in teamsInCurrentMatches && team2 !in teamsInCurrentMatches) {
                    currentMatches.add(match)
                    teamsInCurrentMatches.add(team1)
                    teamsInCurrentMatches.add(team2)
                    usedMatches.add(match)
                    if (currentMatches.size == teams.size / 2) {
                       // println("idea team >>> ${availableMatches.last()}")
                        break
                    }
                }
            }

            // 如果当前轮次配对不满，需要将未配对的队伍打印出来
            if (currentMatches.size < teams.size / 2) {
                val pairedTeams = currentMatches.flatMap { listOf(it.first, it.second) }.toSet()
                val unpairedTeams = teams.filter { it !in pairedTeams }
                if (unpairedTeams.isNotEmpty()) {
                    println("Unpaired teams in this round:")
                    for (team in unpairedTeams) {
                        println(team)
                    }
                }
                break
            }

            if (currentMatches.size == teams.size / 2) {
                rounds.add(currentMatches.toList())
            }
        }

        return rounds
    }

    // 模拟并输出所有轮次以及未成功配对的队伍
    fun simulateRounds() {
        val allRounds = generateRounds()

        // 输出所有轮次的对阵
        for ((index, round) in allRounds.withIndex()) {
            println("Round ${index + 1}:")
            val allTeams = teams.toSet()

            for ((team1, team2) in round) {
                println("$team1 vs $team2")

                // 计算未参与战斗的队伍
                val pairedTeams = setOf(team1, team2)
                val unpairedTeams = allTeams - pairedTeams

                if (unpairedTeams.isNotEmpty()) {
                    println("simulateRounds by Unpaired teams in this round:")
                    for (team in unpairedTeams) {
                        println(team)
                    }
                }
            }
            println()
        }
    }

    simulateRounds()
}

 */



/*
fun main() {
    val teams = listOf("Team A", "Team B", "Team C", "Team D")
    val allPossibleMatches = mutableSetOf<Pair<String, String>>()
    val previousMatches = mutableMapOf<String, String?>()

    // 初始化队伍对手映射和所有可能的对战组合
    for (team in teams) {
        previousMatches[team] = null
    }
    for (i in teams.indices) {
        for (j in i + 1 until teams.size) {
            allPossibleMatches.add(teams[i] to teams[j])
        }
    }

    // 生成所有可能的对战轮次
    fun generateAllRounds(): List<List<Pair<String, String>>> {
        val rounds = mutableListOf<List<Pair<String, String>>>()
        val usedMatches = mutableSetOf<Pair<String, String>>()

        fun getAvailableMatches(): List<Pair<String, String>> {
            return allPossibleMatches.filter { match ->
                val (team1, team2) = match
                previousMatches[team1] != team2 && previousMatches[team2] != team1 && !usedMatches.contains(match)
            }
        }

        while (usedMatches.size < allPossibleMatches.size) {
            val availableMatches = getAvailableMatches().shuffled() // 打乱顺序以增加随机性
            val roundMatches = mutableListOf<Pair<String, String>>()
            val usedTeams = mutableSetOf<String>()

            for (match in availableMatches) {
                val (team1, team2) = match
                if (team1 !in usedTeams && team2 !in usedTeams) {
                    roundMatches.add(match)
                    usedTeams.add(team1)
                    usedTeams.add(team2)
                    usedMatches.add(match)
                    if (usedTeams.size == teams.size) break
                }
            }

            if (roundMatches.size == teams.size / 2) {
                rounds.add(roundMatches)
                for ((team1, team2) in roundMatches) {
                    previousMatches[team1] = team2
                    previousMatches[team2] = team1
                }
            } else {
                break // 如果无法生成完整的轮次，则停止生成
            }
        }

        return rounds
    }

    // 模拟并输出所有轮次
    fun simulateRounds() {
        val allRounds = generateAllRounds()

        // 输出所有轮次的对阵
        for ((index, round) in allRounds.withIndex()) {
            println("Round ${index + 1}:")
            for ((team1, team2) in round) {
                println("$team1 vs $team2")
            }
            println()
        }
    }

    simulateRounds()
}

 */


object ConditionHandle {
    private val split =
        Pattern.compile("(?<!\\w)-?\\w+(\\.\\w+)?|[>=<|!&()]{1,2}")
    private val split2 =
        Pattern.compile("(?<![\\w\\u4E00-\\u9FA5])-?[\\w\\u4E00-\\u9FA5]+(\\.[\\w\\u4E00-\\u9FA5]+)?|[>=<|!&()]{1,2}")

    private val symbol =
        Pattern.compile("[>=<\\|!&()]{1,2}")


    fun parseCondition(expression: String): Boolean {
        val stack = Stack<Any>()
        val operator = Stack<String?>()
        operator.push(null)
        val matcher = split2.matcher(expression)
        var skipNext = false // 用于跳过下一个条件表达式的标志
        var skipLock = false
        while (matcher.find()) {
            val temp = matcher.group()
            if (skipNext) {
                if (skipLock && temp.equals(")", ignoreCase = true)) {
                    operator.pop()
                    skipNext = false
                }
                continue
            }
            if (symbol.matcher(temp).find()) {
                when (temp) {
                    "||" -> {
                        while (!operator.isEmpty() && getPriority(temp) <= getPriority(operator.peek())) {
                            val end = stack.pop()
                            val start = stack.pop()
                            stack.push(evalCondition(start, end, operator.pop()))
                        }
                        if (stack.peek() == "true") {
                            skipNext = true
                        } else {
                            operator.push(temp)
                        }
                    }

                    "&&" -> {
                        while (!operator.isEmpty() && getPriority(temp) <= getPriority(operator.peek())) {
                            val end = stack.pop()
                            val start = stack.pop()
                            stack.push(evalCondition(start, end, operator.pop()))
                        }
                        if (stack.peek() == "false") {
                            skipNext = true
                            if (!operator.isEmpty() && operator.peek() != null && operator.peek()
                                    .equals("(", ignoreCase = true)
                            ) {
                                skipLock = true
                            }
                        } else {
                            operator.push(temp)
                        }
                    }

                    "(" -> {
                        operator.push(temp)
                    }

                    ")" -> {
                        if (skipLock) {
                            skipLock = false
                        } else {
                            while (!operator.isEmpty() && operator.peek() != "(") {
                                val end = stack.pop()
                                val start = stack.pop()
                                stack.push(evalCondition(start, end, operator.pop()))
                            }
                            operator.pop()
                        }
                    }

                    else -> {
                        while (!operator.isEmpty() && getPriority(temp) <= getPriority(operator.peek())) {
                            val end = stack.pop()
                            val start = stack.pop()
                            stack.push(evalCondition(start, end, operator.pop()))
                        }
                        operator.push(temp)
                    }
                }
            } else {
                stack.push(temp)
            }
        }
        while (!operator.isEmpty() && operator.peek() != null) {
            val end = stack.pop()
            val start = stack.pop()
            stack.push(evalCondition(start, end, operator.pop()))
        }
        return java.lang.Boolean.parseBoolean(stack.pop().toString())
    }

    private fun evalCondition(start: Any, end: Any, operator: String?): Boolean {
        //println("  eval $start $operator $end")
        if (operator.equals("&&", ignoreCase = true)) {
            return start == true && end == true
        } else if (operator.equals("||", ignoreCase = true)) {
            return start == true || end == true
        } else if (operator.equals("!=", ignoreCase = true)) {
            return start != end
        } else if (operator.equals("==", ignoreCase = true)) {
            return start == end
        }
        val i = BigDecimal(start.toString()).compareTo(BigDecimal(end.toString()))
        return when (operator) {
            "<=" -> i <= 0
            ">=" -> i >= 0
            "<" -> i < 0
            ">" -> i > 0
            else -> false
        }
    }

    private fun getPriority(s: String?): Int {
        return if (s == null) 0 else when (s) {
            "(", ")" -> 1
            "&", "&&", "||", "|" -> 2
            ">", "<", ">=", "<=", "==", "!=" -> 3
            else -> 0
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val timer = System.nanoTime()
            val t2 = System.currentTimeMillis()
            println("out " + parseCondition("(100 >= 88 && 99 < 100) && 19416 != 1561"))
            val ti = (System.nanoTime() - timer) / 1000000.0
            println("timer " + String.format("%.2f", ti) + "ms")
            println("timer2 " + (System.currentTimeMillis() - t2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
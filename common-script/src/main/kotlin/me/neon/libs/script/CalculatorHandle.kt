package me.neon.libs.script

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * NeonLibs
 * me.neon.libs.script
 *
 * @author 老廖
 * @since 2024/3/15 11:52
 */
object CalculatorHandle {

    /**
     * 计算器工具
     * 来源: CSDN
     * 方法来源于网站: https://blog.csdn.net/qq_37969433/article/details/81200872
     * 标题: （算法）java完成解析数学算式（计算器）三 —— 用栈解析
     *
     * @author Sirm23333
     */
    private val p: Pattern = Pattern.compile("(?<!\\d)-?\\d+(\\.\\d+)?|[+\\-*/%()]") // 这个正则为匹配表达式中的数字或运算符
    private val bp: Regex = Regex("[+\\-*/%()]")

    fun getResult(exp: String): Number {
        var expr = exp
        var intTransform = false
        if (expr.startsWith("int")) {
            intTransform = true
            expr = expr.substring(3)
        }
        /*数字栈*/
        val number: Stack<Double> = Stack()
        /*符号栈*/
        val operator: Stack<String> = Stack()
        operator.push(null) // 在栈顶压人一个null，配合它的优先级，目的是减少下面程序的判断

        /* 将expr打散为运算数和运算符 */
        val m: Matcher = p.matcher(expr)
        while (m.find()) {
            val temp: String = m.group()
            if (temp.matches(bp)) { //遇到符号
                when (temp) {
                    "(" -> operator.push(temp) //遇到左括号，直接入符号栈
                    ")" -> {
                        //遇到右括号，"符号栈弹栈取栈顶符号b，数字栈弹栈取栈顶数字a1，数字栈弹栈取栈顶数字a2，计算a2 b a1 ,将结果压入数字栈"，重复引号步骤至取栈顶为左括号，将左括号弹出
                        var b: String
                        while (operator.pop().also { b = it } != "(") {
                            number.push(doubleCal(number.pop(), number.pop(), b[0]))
                        }
                    }
                    else -> {
                        //遇到运算符，满足该运算符的优先级大于栈顶元素的优先级压栈；否则计算后压栈
                        while (getPriority(temp) <= getPriority(operator.peek())) {
                            number.push(doubleCal(number.pop(), number.pop(), operator.pop()[0]))
                        }
                        operator.push(temp)
                    }
                }
            } else {
                //遇到数字，直接压入数字栈
                number.push(temp.toDouble())
            }
        }
        while (operator.peek() != null) { //遍历结束后，符号栈数字栈依次弹栈计算，并将结果压入数字栈
            val a1: Double = number.pop()
            val a2: Double = number.pop()
            val b: String = operator.pop()
            number.push(doubleCal(a2, a1, b[0]))
        }
        return if (intTransform) Math.round(number.pop()) else number.pop()
    }


    private fun doubleCal(a1: Double, a2: Double, operator: Char): Double {
       return when (operator) {
            '+' -> a1 + a2
            '-' -> a1 - a2
            '*' -> a1 * a2
            '/' -> a1 / a2
            '%' -> a1 % a2
            else -> throw Exception("illegal operator!")
        }
    }

    private fun getPriority(s: String?): Int {
        if (s == null) {
            return 0
        }
        return when (s) {
            "(" -> 1
            "+", "-" -> 2
            "*", "%", "/" -> 3
            else -> throw Exception("illegal operator!")
        }
    }
}
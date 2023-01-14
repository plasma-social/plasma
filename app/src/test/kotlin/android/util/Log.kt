@file:JvmName("Log")

package android.util

fun e(tag: String, msg: String, t: Throwable): Int {
    println("ERROR: $tag: $msg")
    t.printStackTrace()
    return 0
}

fun e(tag: String, msg: String): Int {
    println("ERROR: $tag: $msg")
    return 0
}

fun d(tag: String, msg: String): Int {
    println("DEBUG: $tag: $msg")
    return 0
}

fun d(tag: String, msg: String, t: Throwable): Int {
    println("DEBUG: $tag: $msg")
    t.printStackTrace(System.out)
    return 0
}


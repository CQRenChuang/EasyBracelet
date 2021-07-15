package com.ocm.smartrobot.receiver.JPush

data class JPushModel(
    val msg_no: String,
    val param1: String,
    val type: String
)

enum class JPushType(val code: String) {
    CHECKUPGRADE("13"),
    UPLOADLOG("111"),
    RESTART("112"),
    UPLOADIMG("113"),
    LIVENESS("114"),
    TEST("115"),
    UPGRADE("116"),
    CONFIG("117"),
    SCORE("118"),;
}
package me.neon.libs.region


/**
 * NeonLibs
 * me.neon.libs.region
 *
 * @author 老廖
 * @since 2025/12/26 19:46
 */
enum class RegionAction(val display: String) {

    BREAK("破坏区域方块"),
    PLACE("放置区域方块"),
    INTERACT("交互区域方块"),
    JOIN("加入区域"),
    QUIT("离开区域"),
    JOIN_CHILD("加入子区域"),
    QUIT_CHILD("离开子区域")
}
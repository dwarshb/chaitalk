package com.dwarshb.chaitalk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
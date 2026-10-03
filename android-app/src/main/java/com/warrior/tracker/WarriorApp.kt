package com.warrior.tracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Hilt root (sec.5). No internet, no analytics — Local-first by design (sec.17). */
@HiltAndroidApp
class WarriorApp : Application()

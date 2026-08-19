package com.mixnote

import android.app.Application
import com.mixnote.data.AppDatabase

class MixNoteApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}

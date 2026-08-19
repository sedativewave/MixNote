package com.mixnote.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mixnote_prefs", Context.MODE_PRIVATE)

    var themeColor: Int
        get() = prefs.getInt("theme_color", -16776961) // По умолчанию фиолетовый
        set(value) = prefs.edit().putInt("theme_color", value).apply()
}

package com.app.storyapp.data.local

import android.content.Context
import com.app.storyapp.data.dataclass.UserData

internal class SharedPrefs(context: Context) {
    companion object {
        private const val PREFS_NAME = "auth_pref"
        private const val USER_ID = "user_id"
        private const val NAME = "name"
        private const val TOKEN = "token"
    }

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setUser(value: UserData) {
        val editor = preferences.edit()
        editor.putString(USER_ID, value.userId)
        editor.putString(NAME, value.name)
        editor.putString(TOKEN, value.token)
        editor.apply()
    }

    fun getUser(): UserData {
        val user = UserData()
        user.userId = preferences.getString(USER_ID, "")
        user.name = preferences.getString(NAME, "")
        user.token = preferences.getString(TOKEN, "")
        return user
    }
}
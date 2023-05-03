package com.app.storyapp.data.local

import android.content.Context
import com.app.storyapp.data.dataclass.UserData

internal class SharedPrefs(context: Context) {
    private val authPreferences = context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)

    fun setUser(value: UserData) {
        val editor = authPreferences.edit()
        editor.putString(AUTH_USER_ID, value.userId)
        editor.putString(AUTH_NAME, value.name)
        editor.putString(AUTH_TOKEN, value.token)
        editor.apply()
    }

    fun removeUser() {
        val editor = authPreferences.edit()
        editor.remove(AUTH_NAME)
        editor.remove(AUTH_USER_ID)
        editor.remove(AUTH_TOKEN)
        editor.apply()
    }

    fun getUser(): UserData {
        val user = UserData()
        user.userId = authPreferences.getString(AUTH_USER_ID, "")
        user.name = authPreferences.getString(AUTH_NAME, "")
        user.token = authPreferences.getString(AUTH_TOKEN, "")
        return user
    }

    companion object {
        const val AUTH_PREFS_NAME = "auth_pref"
        const val AUTH_USER_ID = "user_id"
        const val AUTH_NAME = "name"
        const val AUTH_TOKEN = "token"
    }
}
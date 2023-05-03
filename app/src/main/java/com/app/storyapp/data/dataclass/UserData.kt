package com.app.storyapp.data.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    var userId: String? = null,
    var name: String? = null,
    var token: String? = null
) : Parcelable

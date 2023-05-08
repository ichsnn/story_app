package com.app.storyapp.exception

class UnauthorizedTokenException(statusCode: Int): Exception("$statusCode Unauthorized")
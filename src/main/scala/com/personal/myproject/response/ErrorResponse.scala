package com.personal.myproject.response

case class ErrorResponse(version: Long, request: String, errors: List[String]) extends Response(version, request)
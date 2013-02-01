package com.personal.myproject.response


case class SuccessResponse[T](version: Long,
                                    request: String,
                                    size: Long,
                                    requestParams: Option[Map[String, _]],
                                    content: List[T]) extends Response(version, request)
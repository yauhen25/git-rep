package com.personal.myproject.model

import com.novus.salat.annotations.raw.{Ignore, Salat}
import org.bson.types.ObjectId

case class ResourceWrapper(_id: Option[ObjectId],
                                       version: Long,
                                       content: List[Resource])
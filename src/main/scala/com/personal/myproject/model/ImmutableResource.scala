package com.personal.myproject.model

import org.bson.types.ObjectId
import com.novus.salat.annotations.raw.Ignore
import com.novus.salat.annotations.raw.Salat
import com.novus.salat.annotations.raw.{Ignore, Salat}

case class ImmutableResource(@Ignore id: Option[ObjectId], name: String, description: String)
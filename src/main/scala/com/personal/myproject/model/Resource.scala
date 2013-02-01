package com.personal.myproject.model

import org.bson.types.ObjectId
import com.novus.salat.annotations.raw.{Ignore, Salat}

case class Resource(@Ignore id: Option[ObjectId], 
    name: String, 
    description: String, 
    nestedObject: Option[NestedObject], 
    enabled: Boolean)
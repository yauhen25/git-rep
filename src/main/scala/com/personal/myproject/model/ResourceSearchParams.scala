package com.personal.myproject.model

import com.mongodb.casbah.Imports._

/**
 * @author chris_carrier
 * @version 9/30/11
 */

object ResourceSearchParams {

  implicit def toDbo(p: ResourceSearchParams): MongoDBObject = {

    val query = MongoDBObject()
    
    if (p.name != None) {
      query += "content.name" -> p.name.get
    }


    if (p.description != None) {
      query += "content.description" -> p.description.get
    }

    query
  }
}

case class ResourceSearchParams(name: Option[String], description: Option[String])
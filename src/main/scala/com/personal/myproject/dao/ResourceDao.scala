package com.personal.myproject.dao

import com.mongodb.casbah.Imports._
import akka.dispatch.Future
import java.util.Date
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.ServerAddress
import com.mongodb.DBObject
import com.mongodb.{ServerAddress, DBObject}
import com.mongodb.casbah.{MongoDB, MongoConnection}
import com.mongodb.casbah.commons.MongoDBObject
import com.personal.myproject._
import com.personal.myproject.model._

/**
 * @author chris carrier
 */

class ResourceDao(mongoCollection: MongoCollection) extends Dao {

  def getResource(key: ObjectId) = {
    Future {
      val q = MongoDBObject("_id" -> key)
      val dbo = mongoCollection.findOne(q)
      dbo.map(f => grater[ResourceWrapper].asObject(f))
    }
  }

  def createResource(modelWrapper: ResourceWrapper) = {
    Future {
      val dbo = grater[ResourceWrapper].asDBObject(modelWrapper)
      mongoCollection += dbo
      Some(modelWrapper.copy(_id = dbo.getAs[org.bson.types.ObjectId]("_id"))) 
    }
  }

  def updateResource(key: ObjectId, model: Resource) = {
    Future {
      val query = MongoDBObject("_id" -> key)
      val update = $addToSet("content" -> model)

      mongoCollection.update(query, update, false, false, WriteConcern.Safe)

      val dbo = mongoCollection.findOne(query)
      dbo.map(f => grater[ResourceWrapper].asObject(f))
    }
  }

  def searchResource(searchObj: MongoDBObject) = {
    Future {
      val data = mongoCollection.find(searchObj)
      val dataList = data.map(f => grater[ResourceWrapper].asObject(f).content).flatten.toList

      if (dataList.isEmpty) {
        None
      }
      else {
        Some(dataList)
      }
    }
  }
}

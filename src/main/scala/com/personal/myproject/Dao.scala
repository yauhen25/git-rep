package com.personal.myproject

import akka.dispatch.Future
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.personal.myproject.model._

trait Dao {
  def getResource(key: ObjectId): Future[Option[ResourceWrapper]]
  def createResource(modelWrapper: ResourceWrapper): Future[Option[ResourceWrapper]]
  def updateResource(key: ObjectId, model: Resource): Future[Option[ResourceWrapper]]
  def searchResource(searchObj: MongoDBObject): Future[Option[List[Resource]]]
}

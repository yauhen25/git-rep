package com.personal.myproject.boot

import org.slf4j.LoggerFactory
import akka.dispatch._
import javax.servlet._
import akka.util.AkkaLoader
import akka.dispatch.Dispatchers
import akka.actor.{Supervisor, Actor, Scheduler}
import akka.config.Supervision
import Supervision._
import cc.spray.connectors.Initializer
import akka.actor.Actor._
import cc.spray.utils.ActorHelpers._
import cc.spray.HttpService._
import com.mongodb.ServerAddress
import com.mongodb.casbah.{MongoDB, MongoConnection}
import cc.spray.{HttpService, RootService}
import com.personal.myproject._
import com.personal.myproject.dao._

/**
 * @author chris_carrier
 */


class ServiceInitializer extends Initializer {
  
  val logger = LoggerFactory.getLogger("com.personal.Resource.boot.ServiceInitializer");
  logger.info("Running Initializer")

  val akkaConfig = akka.config.Config.config

  val mongoUrl = akkaConfig.getString("mongodb.mongoUrl", "localhost")
  val mongoDbName = akkaConfig.getString("mongodb.database", "")
  val collection = akkaConfig.getString("mongodb.collection", "Resources")

  val urlList = mongoUrl.split(",").toList.map(new ServerAddress(_))
  val db = urlList match {
    case List(s) => MongoConnection(s)(mongoDbName)
    case s: List[String] => MongoConnection(s)(mongoDbName)
  }

  // ///////////// INDEXES for collections go here (include all lookup fields)
  //  configsCollection.ensureIndex(MongoDBObject("customerId" -> 1), "idx_customerId")
  val mainModule = new ResourceEndpoint {
    val service = new ResourceDao(db(collection))
  }

  val httpService = actorOf(new HttpService(mainModule.restService))
  val rootService = actorOf(new RootService(httpService))

  // Start all actors that need supervision, including the root service actor.
  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        Supervise(httpService, Permanent),
        Supervise(rootService, Permanent)
      )
    )
  )

}
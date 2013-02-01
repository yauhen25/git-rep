package com.personal.myproject

import cc.spray.Directives
import model.{ ResourceSearchParams, ResourceWrapper, Resource }
import net.liftweb.json.JsonParser._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization._
import org.bson.types.ObjectId
import akka.event.EventHandler
import cc.spray.http._
import MediaTypes._
import com.personal.myproject.model._
import com.personal.myproject.response._
import util._
import com.personal.myproject.directives._

trait ResourceEndpoint extends Directives with ValidationDirectives {
  implicit val formats = DefaultFormats + new ObjectIdSerializer

  final val NOT_FOUND_MESSAGE = "resource not found"
  final val INTERNAL_ERROR_MESSAGE = "error"

  def JsonContent(content: String) = HttpContent(ContentType(`application/json`), content)

  EventHandler.info(this, "Starting actor.")
  val service: Dao

  val restService = {
    // Debugging: /ping -> pong
    path("ping") {
      get {
        _.complete("pong")
      }
    } ~
      // Service implementation.
      pathPrefix("Resources") {
        path("^[a-f0-9]+$".r) {
          resourceId =>
            get {
              ctx =>
                try {
                  service.getResource(new ObjectId(resourceId)).onComplete(f => {
                    f.result.get match {
                      case Some(ResourceWrapper(oid, version, content)) => ctx.complete(write(SuccessResponse[Resource](version, ctx.request.path, 1, None, content.map(x => x.copy(id = oid)))))
                      case None => ctx.fail(StatusCodes.NotFound, write(ErrorResponse(1l, ctx.request.path, List(NOT_FOUND_MESSAGE))))
                    }
                  })
                } catch {
                  case e: IllegalArgumentException => {
                    ctx.fail(StatusCodes.NotFound, write(ErrorResponse(1l, ctx.request.path, List(NOT_FOUND_MESSAGE))))
                  }
                }
            } ~
              requiringStrings(List("name", "description")) {
                put {
                  ctx =>
                    try {
                      val content = new String(ctx.request.content.get.buffer)

                      val resource = parse(content).extract[Resource]

                      service.updateResource(new ObjectId(resourceId), resource).onTimeout(f => {
                        ctx.fail(StatusCodes.InternalServerError, write(ErrorResponse(1, ctx.request.path, List(INTERNAL_ERROR_MESSAGE))))
                      }).onComplete(f => {
                        f.result.get match {
                          case Some(ResourceWrapper(oid, version, content)) => ctx.complete(write(SuccessResponse[Resource](version, ctx.request.path, 1, None, content.map(x => x.copy(id = oid)))))
                          case None => ctx.fail(StatusCodes.NotFound, write(ErrorResponse(1, ctx.request.path, List(NOT_FOUND_MESSAGE))))
                        }
                      }).onException {
                        case e => {
                          ctx.fail(StatusCodes.InternalServerError, write(ErrorResponse(1, ctx.request.path, List(e.getMessage))))
                        }
                      }

                    } catch {
                      case e: IllegalArgumentException => {
                        ctx.fail(StatusCodes.NotFound, write(ErrorResponse(1l, ctx.request.path, List(NOT_FOUND_MESSAGE))))
                      }
                    }
                }
              }
        } ~
          path("") {
            requiringStrings(List("name", "description")) {
              post {
                ctx =>
                  val content = new String(ctx.request.content.get.buffer)

                  val resource = parse(content).extract[Resource]
                  val resourceWrapper = ResourceWrapper(None, 1, List(resource))

                  service.createResource(resourceWrapper).onTimeout(f => {
                    ctx.fail(StatusCodes.InternalServerError, write(ErrorResponse(1, ctx.request.path, List(INTERNAL_ERROR_MESSAGE))))
                    EventHandler.info(this, "Timed out")
                  }).onComplete(f => {
                    f.result.get match {
                      case Some(ResourceWrapper(oid, version, content)) => ctx.complete(HttpResponse(StatusCodes.Created, JsonContent(write(SuccessResponse[Resource](version, ctx.request.path, 1, None, content.map(x => x.copy(id = oid)))))))
                      case None => ctx.fail(StatusCodes.BadRequest, write(ErrorResponse(1, ctx.request.path, List(NOT_FOUND_MESSAGE))))
                    }
                  }).onException {
                    case e => {
                      EventHandler.info(this, "Excepted: " + e)
                      ctx.fail(StatusCodes.InternalServerError, write(ErrorResponse(1, ctx.request.path, List(e.getMessage))))
                    }
                  }
              }
            } ~
              parameters('name?, 'description?) { (name, description) =>
                get { ctx =>
                  service.searchResource(ResourceSearchParams(name, description)).onTimeout(f => {
                    ctx.fail(StatusCodes.InternalServerError, write(ErrorResponse(1, ctx.request.path, List(INTERNAL_ERROR_MESSAGE))))
                  }).onComplete(f => {
                    f.result.get match {
                      case content: Some[List[Resource]] => ctx.complete(write(SuccessResponse[Resource](1, ctx.request.path, content.get.length, None, content.get)))
                      case None => ctx.fail(StatusCodes.NotFound, write(ErrorResponse(1, ctx.request.path, List(NOT_FOUND_MESSAGE))))
                    }
                  }).onException {
                    case e => {
                      e.printStackTrace()
                      ctx.fail(StatusCodes.InternalServerError, write(ErrorResponse(1, ctx.request.path, List(e.getMessage))))
                    }
                  }
                }
              }
          }
      }
  }

}

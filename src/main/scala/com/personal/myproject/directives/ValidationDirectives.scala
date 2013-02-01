package com.personal.myproject.directives

import cc.spray.directives._
import cc.spray.Directives
import net.liftweb.json.JsonParser._
import net.liftweb.json.Formats


trait ValidationDirectives extends Directives {

  implicit val formats: Formats

  def requiringStrings(fieldNames: List[String]): SprayRoute0 = filter {
    ctx =>
      ctx.request.content match {
        case Some(httpContent) => {
          val json = parse(new String(httpContent.buffer))
          fieldNames.map{ xs =>
            val field = json \ xs
            field.extractOpt[String] match {
              case Some(x) => //Do nothing
              case None => xs
            }
          }
          Pass()
        }
        case _ => Reject(ValidationRejection("body.required"))
      }
  }


}

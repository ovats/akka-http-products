package com.cc.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.{failWith, path, provide}

import java.util.UUID
import scala.util.{Failure, Success, Try}

trait CustomDirectives {

  def extractUUID: Directive1[UUID] = {
    path(Segment).flatMap { maybeuuid =>
      Try(UUID.fromString(maybeuuid)) match {
        case Success(result) => provide(result)
        case Failure(error) =>
          complete(StatusCodes.BadRequest, s"Invalid uuid: $maybeuuid (getUUID Directive)") //failWith(error)
      }
    }
  }

  //TODO remove
//  val rr2: Route = pathPrefix("customer2") {
//    extractUUID { uuid =>
//      complete(StatusCodes.OK, s"Valid uuid: $uuid (getUUID)")
//    }
//  }

}

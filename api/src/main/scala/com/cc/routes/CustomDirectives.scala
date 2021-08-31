package com.cc.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._

import java.util.UUID
import scala.util.{Failure, Success, Try}

trait CustomDirectives extends ResponseHandler {

  def extractUUID: Directive1[UUID] = {
    path(Segment).flatMap { maybeuuid =>
      Try(UUID.fromString(maybeuuid)) match {
        case Success(result) => provide(result)
        case Failure(_) =>
          complete(StatusCodes.BadRequest, s"Invalid uuid: $maybeuuid")
      }
    }
  }

  // Directives for CRUD
  def postWithUUID: Directive1[UUID]   = post & extractUUID
  def getWithUUID: Directive1[UUID]    = get & extractUUID
  def putWithUUID: Directive1[UUID]    = put & extractUUID
  def deleteWithUUID: Directive1[UUID] = delete & extractUUID

}

package com.cc.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.cc.services.ServiceResponse
import com.cc.services.ServiceResponse._
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

trait ResponseHandler extends LazyLogging with FailFastCirceSupport {
  def handleServiceResponse(response: ServiceResponse, entity: String): StandardRoute = {
    response match {
      case ValidationErrors(errors) =>
        val errorMessage = s"Validation errors ($entity): ${errors.toString}"
        logger.debug(errorMessage)
        complete(StatusCodes.BadRequest, ErrorsResponse(errors))
      case UnknownError(errorMsg) =>
        val errorMessage = s"Error ($entity)"
        logger.error(s"$errorMessage: $errorMsg")
        complete(StatusCodes.InternalServerError, ErrorsResponse(List(errorMessage)))
      case r =>
        val errorMessage = s"Unexpected response ($entity): ${r.toString}"
        logger.error(errorMessage)
        complete(StatusCodes.InternalServerError, ErrorsResponse(List(errorMessage)))

    }
  }

  def handleFailure(e: Throwable, entity: String): StandardRoute = {
    val errorMessage = s"Error when processing request ($entity)"
    logger.error(errorMessage, e)
    complete(StatusCodes.InternalServerError, ErrorsResponse(List(errorMessage)))
  }
}

final case class ErrorsResponse(errors: Seq[String])

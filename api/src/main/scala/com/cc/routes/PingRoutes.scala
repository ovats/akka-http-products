package com.cc.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging

class PingRoutes extends LazyLogging {

  val routes: Route = path("ping") {
    get {
      logger.debug(s"GET /ping")
      complete("pong")
    }
  }

}

package com.cc.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.cc.test.BaseSpec

class PingRoutesSpec extends BaseSpec with ScalatestRouteTest {

  val pingRoutes = new PingRoutes()

  s"GET /ping" should "return pong" in {
    val request = Get(uri = s"/ping")
    request ~> pingRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      entityAs[String] shouldBe "pong"
    }
  }
}

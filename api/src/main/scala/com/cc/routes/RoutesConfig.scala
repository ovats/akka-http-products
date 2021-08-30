package com.cc.routes

import com.cc.view.RestView
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Printer

trait RoutesConfig extends FailFastCirceSupport with RestView {
  implicit val customPrinter: Printer = Printer.spaces2.copy(dropNullValues = true)
}

package com.cc.view

import java.time.LocalDate

final case class ProductView(name: String, vendor: String, price: BigDecimal, expirationDate: Option[LocalDate])

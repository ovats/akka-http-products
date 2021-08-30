package com.cc.domain

import java.time.LocalDate
import java.util.UUID

final case class Product(
    uuid: UUID,
    name: String,
    vendor: String,
    price: BigDecimal,
    expirationDate: Option[LocalDate],
)

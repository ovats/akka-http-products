package com.cc.validator

import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId
import com.cc.domain.DomainConstants._

import java.time.LocalDate

final case class Invalid(resource: String, field: String, details: String)

object ProductValidator {

  type ValidationResult[A] = ValidatedNel[Invalid, A]

  def validateName(name: String): ValidationResult[String] = {
    if (name.isEmpty) Invalid("product", "name", "Field name can not be empty").invalidNel
    else if (name.length > productNameMaxSize)
      Invalid("product", "name", s"Field name too big, max size is $productNameMaxSize chars").invalidNel
    else name.validNel
  }
  def validateVendor(vendor: String): ValidationResult[String] = {
    if (vendor.isEmpty) Invalid("product", "vendor", "Field vendor can not be empty").invalidNel
    else if (vendor.length > productVendorMaxSize)
      Invalid("product", "vendor", s"Field vendor too big, max size is $productNameMaxSize chars").invalidNel
    else vendor.validNel
  }

  def validatePrice(price: BigDecimal): ValidationResult[BigDecimal] = {
    if (price > 0) price.validNel
    else Invalid("product", "price", "Field price must be > 0").invalidNel
  }
  def validateExpirationDate(expiration: Option[LocalDate]): ValidationResult[Option[LocalDate]] = {
    expiration match {
      case None => None.validNel
      case Some(expDate) =>
        if (expDate.isAfter(LocalDate.now()))
          Some(expDate).validNel
        else
          Invalid("product", "expiration date", "Field expiration date must be in the future").invalidNel
    }
  }
}

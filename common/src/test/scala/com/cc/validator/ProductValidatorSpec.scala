package com.cc.validator

import cats.implicits.catsSyntaxValidatedId
import com.cc.domain.DomainConstants
import com.cc.test.BaseSpec
import com.cc.validator.ProductValidator._

import java.time.LocalDate

class ProductValidatorSpec extends BaseSpec {

  "validateName" should "validate name correctly" in {
    validateName("") shouldBe Invalid("product", "name", "Field name can not be empty").invalidNel
    validateName("abc") shouldBe "abc".validNel

    val max         = DomainConstants.productNameMaxSize
    val invalidName = "a" * (max + 1)
    validateName(invalidName) shouldBe Invalid(
      "product",
      "name",
      s"Field name too big, max size is $max chars",
    ).invalidNel
    val validName = "a" * max
    validateName(validName) shouldBe validName.validNel
  }

  "validateVendor" should "validate vendor name correctly" in {
    validateVendor("") shouldBe Invalid("product", "vendor", "Field vendor can not be empty").invalidNel
    validateVendor("abc") shouldBe "abc".validNel

    val max               = DomainConstants.productVendorMaxSize
    val invalidVendorName = "a" * (max + 1)
    validateVendor(invalidVendorName) shouldBe Invalid(
      "product",
      "vendor",
      s"Field vendor too big, max size is $max chars",
    ).invalidNel
    val validVendorName = "a" * max
    validateVendor(validVendorName) shouldBe validVendorName.validNel
  }

  "validatePrice" should "validate price correctly" in {
    validatePrice(0) shouldBe Invalid("product", "price", "Field price must be > 0").invalidNel
    validatePrice(-1) shouldBe Invalid("product", "price", "Field price must be > 0").invalidNel
    validatePrice(-1.5) shouldBe Invalid("product", "price", "Field price must be > 0").invalidNel
    validatePrice(-1000) shouldBe Invalid("product", "price", "Field price must be > 0").invalidNel
    validatePrice(1) shouldBe 1.validNel
    validatePrice(0.5) shouldBe 0.5.validNel
    validatePrice(100) shouldBe 100.validNel
    validatePrice(1000) shouldBe 1000.validNel
  }

  "validateExpirationDate" should "validate expiration date correctly" in {
    val ed1 = None
    validateExpirationDate(ed1) shouldBe ed1.validNel
    val ed2 = Option(LocalDate.now().plusDays(1))
    validateExpirationDate(ed2) shouldBe ed2.validNel
    val ed3 = Option(LocalDate.now().plusDays(15))
    validateExpirationDate(ed3) shouldBe ed3.validNel
    val ed4 = Option(LocalDate.now().plusWeeks(2))
    validateExpirationDate(ed4) shouldBe ed4.validNel
    val ed5 = Option(LocalDate.now().plusMonths(1))
    validateExpirationDate(ed5) shouldBe ed5.validNel

    val ed6 = Option(LocalDate.now())
    validateExpirationDate(ed6) shouldBe Invalid(
      "product",
      "expiration date",
      "Field expiration date must be in the future",
    ).invalidNel

    val ed7 = Option(LocalDate.now().minusDays(1))
    validateExpirationDate(ed7) shouldBe Invalid(
      "product",
      "expiration date",
      "Field expiration date must be in the future",
    ).invalidNel

    val ed8 = Option(LocalDate.now().minusWeeks(2))
    validateExpirationDate(ed8) shouldBe
      Invalid(
        "product",
        "expiration date",
        "Field expiration date must be in the future",
      ).invalidNel

    val ed9 = Option(LocalDate.now().minusYears(3))
    validateExpirationDate(ed9) shouldBe Invalid(
      "product",
      "expiration date",
      "Field expiration date must be in the future",
    ).invalidNel

  }

}

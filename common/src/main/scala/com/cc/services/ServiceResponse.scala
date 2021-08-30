package com.cc.services

import com.cc.domain.{Product, Statistics, VendorProducts}

sealed trait ServiceResponse

object ServiceResponse {

  // When a product is created without errors
  final case class ProductCreated(id: String)       extends ServiceResponse
  final case class ProductUpdated(product: Product) extends ServiceResponse
  final case object ProductDeleted                  extends ServiceResponse

  // If price is invalid, name of product is invalid, etc. will return a list of errors:
  final case class ValidationErrors(errors: Seq[String]) extends ServiceResponse

  // Product found/not found/...
  final case class ProductFound(product: Product) extends ServiceResponse
  final case object ProductNotFound               extends ServiceResponse
  final case object ProductAlreadyExists          extends ServiceResponse

  // List of products
  final case class ProductList(products: Seq[Product])         extends ServiceResponse
  final case class ProductsByVendor(list: Seq[VendorProducts]) extends ServiceResponse
  final case class ProductStatistics(statistics: Statistics)   extends ServiceResponse

  // Unknown error
  final case class UnknownError(errorMessage: String) extends ServiceResponse
}

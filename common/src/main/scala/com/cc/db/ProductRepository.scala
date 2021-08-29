package com.cc.db

import com.cc.domain.{Product, VendorProducts}

import scala.concurrent.Future

trait ProductRepository[K, T] {

  // Standard CRUD
  def create(entity: T): Future[K]
  def update(entity: T): Future[T]
  def findById(key: K): Future[Option[T]]
  def delete(key: K): Future[Unit]
  def findAll: Future[Seq[T]]

  def findByVendor(vendorName: String, caseSensitive: Boolean): Future[Seq[Product]]
  def findAllGroupedByVendor(): Future[Seq[VendorProducts]]
  def deleteAll(): Future[Unit]

}

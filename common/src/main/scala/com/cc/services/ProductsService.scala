package com.cc.services

import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import com.cc.db.InMemoryProductRepository
import com.cc.domain.{Product, Statistics}
import com.cc.services.ServiceResponse._
import com.cc.validator.ProductValidator._
import com.cc.view.ProductView
import com.typesafe.scalalogging.LazyLogging

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ProductsService(productRepository: InMemoryProductRepository)(implicit ec: ExecutionContext) extends LazyLogging {

  def addProduct(uuid: UUID, productData: ProductView): Future[ServiceResponse] = {
    productRepository.findById(uuid).flatMap {
      case Some(_) => Future.successful(ProductAlreadyExists)
      case None =>
        val newProductValidated = maybeValidProduct(
          uuid = uuid,
          name = productData.name,
          vendor = productData.vendor,
          price = productData.price,
          expirationDate = productData.expirationDate,
        )
        newProductValidated match {
          case Validated.Valid(product) =>
            logger.debug("Product data validation ok.")
            productRepository
              .create(product)
              .map(newId => ProductCreated(newId.toString))
              .recoverWith(handleExceptions(Some(uuid), "creating"))
          case Validated.Invalid(errors) =>
            logger.debug(s"Product data validation with errors: ${errors.toList.map(_.details)}")
            Future.successful(ValidationErrors(errors.toList.map(_.details)))
        }
    }
  }

  def updateProduct(uuid: UUID, productData: ProductView): Future[ServiceResponse] = {
    productRepository.findById(uuid).flatMap {
      case None => Future.successful(ProductNotFound)
      case Some(_) =>
        val productValidated = maybeValidProduct(
          uuid = uuid,
          name = productData.name,
          vendor = productData.vendor,
          price = productData.price,
          expirationDate = productData.expirationDate,
        )
        productValidated match {
          case Validated.Valid(product) =>
            logger.debug("Product data validation ok.")
            productRepository
              .update(product)
              .map(ProductUpdated)
              .recoverWith(handleExceptions(Some(uuid), "updating"))
          case Validated.Invalid(errors) =>
            logger.debug(s"Product data validation with errors: ${errors.toList.map(_.details)}")
            Future.successful(ValidationErrors(errors.toList.map(_.details)))
        }
    }
  }

  def deleteProduct(uuid: UUID): Future[ServiceResponse] = {
    productRepository.findById(uuid).flatMap {
      case None => Future.successful(ProductNotFound)
      case Some(_) =>
        productRepository
          .delete(uuid)
          .map(_ => ProductDeleted)
          .recoverWith(handleExceptions(Some(uuid), "deleting"))
    }
  }

  def getProductByUUID(uuid: UUID): Future[ServiceResponse] = {
    productRepository
      .findById(uuid)
      .flatMap {
        case Some(productFound) => Future.successful(ProductFound(productFound))
        case None               => Future.successful(ProductNotFound)
      }
      .recoverWith(handleExceptions(Some(uuid), "retrieving"))
  }

  def getProductsFilteredByVendor(vendorName: String, caseSensitive: String): Future[ServiceResponse] = {
    val cs                           = if (caseSensitive == "no") false else true
    val result: Future[Seq[Product]] = productRepository.findByVendor(vendorName, cs)
    result
      .map(l => ProductList(l.toList))
      .recoverWith(handleExceptions(None, "retrieving list of products"))
  }

  def getAllProducts(): Future[ServiceResponse] = {
    val result: Future[Seq[Product]] = productRepository.findAll
    result
      .map(l => ProductList(l.toList))
      .recoverWith(handleExceptions(None, "retrieving list of products"))
  }

  def getProductsGroupedByVendor(): Future[ServiceResponse] = {
    productRepository
      .findAllGroupedByVendor()
      .map(ProductsByVendor)
      .recoverWith(handleExceptions(None, "retrieving list of products"))
  }

  def getProductsStatistics(): Future[ServiceResponse] = {
    val result: Future[Seq[Product]] = productRepository.findAll
    result
      .map { products =>
        val today                    = LocalDate.now()
        val totalProducts            = products.length
        val totalVendors             = products.map(_.vendor).groupBy(_.toLowerCase).keys.toList.length
        val averagePrice: BigDecimal = if (totalProducts > 0) products.map(_.price).sum / totalProducts else 0
        val due                      = products.filter(p => p.expirationDate.exists(_.isBefore(today)))
        ProductStatistics(
          Statistics(
            numberOfProducts = totalProducts,
            numberOfVendors = totalVendors,
            averagePrice = averagePrice,
            dueProducts = due.length,
          )
        )
      }
      .recoverWith(handleExceptions(None, "retrieving statistics"))

  }

  private def maybeValidProduct(
      uuid: UUID,
      name: String,
      vendor: String,
      price: BigDecimal,
      expirationDate: Option[LocalDate],
  ) = {
    (
      validateName(name),
      validateVendor(vendor),
      validatePrice(price),
      validateExpirationDate(expirationDate),
    ).mapN { (validName, validVendor, validPrice, validExpirationDate) =>
      Product(
        uuid = uuid,
        name = validName,
        vendor = validVendor,
        price = validPrice,
        expirationDate = validExpirationDate,
      )
    }
  }

  private def handleExceptions(
      maybeUuid: Option[UUID],
      action: String,
  ): PartialFunction[Throwable, Future[ServiceResponse]] = {
    case e: Throwable =>
      val uuid     = maybeUuid.map(x => s"product uuid ${x.toString}").getOrElse("")
      val errorMsg = s"Error when $action $uuid: ${e.getMessage}"
      logger.error(errorMsg, e)
      Future.successful(UnknownError(errorMsg))
  }

}

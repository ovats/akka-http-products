package com.cc.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.cc.services.ServiceResponse._
import com.cc.services.{ProductsService, ServiceResponse}
import com.cc.view.ProductView
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ProductRoutes(productsService: ProductsService)
    extends LazyLogging
    with RoutesConfig
    with ResponseHandler
    with CustomDirectives {

  private val createProduct =
    postWithUUID { productUUID =>
      logger.debug(s"POST /products/$productUUID")
      entity(as[ProductView]) { productData =>
        val fut = productsService.addProduct(productUUID, productData)
        onComplete(fut) {
          case Success(ProductCreated(_)) => complete(StatusCodes.OK)
          case Success(ProductAlreadyExists) =>
            complete(StatusCodes.Conflict, ErrorsResponse(List(s"Product already exists with uuid $productUUID")))
          case Success(response) => handleServiceResponse(response, "products")
          case Failure(ex)       => handleFailure(ex, "products")
        }
      }
    }

  private val updateProduct =
    putWithUUID { productUUID =>
      logger.debug(s"PUT /products/$productUUID")
      entity(as[ProductView]) { productData =>
        val fut = productsService.updateProduct(productUUID, productData)
        onComplete(fut) {
          case Success(ProductUpdated(p)) => complete(StatusCodes.OK, p.toRestView)
          case Success(ProductNotFound) =>
            complete(StatusCodes.NotFound, ErrorsResponse(List(s"Product does not exists, uuid $productUUID")))
          case Success(response) => handleServiceResponse(response, "products")
          case Failure(ex)       => handleFailure(ex, "products")
        }
      }
    }

  private val deleteProduct =
    deleteWithUUID { productUUID =>
      logger.debug(s"DELETE /products/$productUUID")
      val fut = productsService.deleteProduct(productUUID)
      onComplete(fut) {
        case Success(ProductDeleted) => complete(StatusCodes.NoContent)
        case Success(ProductNotFound) =>
          complete(StatusCodes.NotFound, ErrorsResponse(List(s"Product does not exists, uuid $productUUID")))
        case Success(response) => handleServiceResponse(response, "products")
        case Failure(ex)       => handleFailure(ex, "products")
      }
    }

  private val findProducts = get {
    parameters("vendor".as[String], "case".as[String].withDefault("no")) { (vendorName, caseSensitive) =>
      logger.debug(s"GET /products?vendor=$vendorName&case=$caseSensitive")
      val fut: Future[ServiceResponse] = productsService.getProductsFilteredByVendor(vendorName, caseSensitive)
      onComplete(fut) {
        case Success(ProductList(products)) =>
          complete(StatusCodes.OK, products.map(_.toRestView))
        case Success(response) => handleServiceResponse(response, "products")
        case Failure(ex)       => handleFailure(ex, "products")
      }
    } ~ pathEndOrSingleSlash {
      logger.debug(s"GET /products")
      val fut: Future[ServiceResponse] = productsService.getAllProducts()
      onComplete(fut) {
        case Success(ProductList(products)) =>
          complete(StatusCodes.OK, products.map(_.toRestView))
        case Success(response) => handleServiceResponse(response, "products")
        case Failure(ex)       => handleFailure(ex, "products")
      }
    } ~ path(Segment) { productUUID =>
      logger.debug(s"GET /products/$productUUID")
      Try(UUID.fromString(productUUID)) match {
        case Failure(_) => complete(StatusCodes.BadRequest, ErrorsResponse(List(s"Invalid uuid: $productUUID")))
        case Success(validUuid) =>
          val fut = productsService.getProductByUUID(validUuid)
          onComplete(fut) {
            case Success(ProductFound(p)) => complete(StatusCodes.OK, p.toRestView)
            case Success(ProductNotFound) =>
              complete(StatusCodes.NotFound, ErrorsResponse(List(s"Not found uuid: $productUUID")))
            case Success(response) => handleServiceResponse(response, "products")
            case Failure(ex)       => handleFailure(ex, "products")
          }
      }
    }
  }

  private val findProductsByVendor: Route = get {
    logger.debug(s"GET /products-by-vendor")
    val fut: Future[ServiceResponse] = productsService.getProductsGroupedByVendor()
    onComplete(fut) {
      case Success(ProductsByVendor(products)) => complete(StatusCodes.OK, products.map(_.toRestView))
      case Success(response)                   => handleServiceResponse(response, "products")
      case Failure(ex)                         => handleFailure(ex, "products")
    }
  }

  private val productsStatistics: Route = get {
    logger.debug(s"GET /products-statistics")
    val fut: Future[ServiceResponse] = productsService.getProductsStatistics()
    onComplete(fut) {
      case Success(ProductStatistics(s)) => complete(StatusCodes.OK, s)
      case Success(response)             => handleServiceResponse(response, "products")
      case Failure(ex)                   => handleFailure(ex, "products")
    }
  }

  val routes: Route = pathPrefix("products") {
      findProducts ~ createProduct ~ updateProduct ~ deleteProduct
    } ~ pathPrefix("products-by-vendor") {
          findProductsByVendor
        } ~ pathPrefix("products-statistics") {
          productsStatistics
        }

}

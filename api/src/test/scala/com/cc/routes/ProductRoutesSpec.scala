package com.cc.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.cc.db.InMemoryProductRepository
import com.cc.domain.{DomainConstants, Statistics}
import com.cc.services.ProductsService
import com.cc.services.ServiceResponse.ValidationErrors
import com.cc.test.BaseSpec
import com.cc.view.{ProductView, VendorProductsView}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

import java.time.LocalDate
import java.util.UUID

class ProductRoutesSpec extends BaseSpec with ScalatestRouteTest with FailFastCirceSupport {

  private val inMemoryProductRepository = new InMemoryProductRepository()
  private val productsService           = new ProductsService(inMemoryProductRepository)
  private val productRoutes             = new ProductRoutes(productsService)

  "POST /products/{uuid}" should "return 200 Ok when adding a new product" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val productRequest =
      ProductView(name = "name", vendor = "vendor", price = 10.99, expirationDate = Option(expirationDate))
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should "return 200 Ok when adding a new product without expiration date" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val productRequest =
      ProductView(name = "name", vendor = "vendor", price = 10.99, expirationDate = Option(expirationDate))
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should "return 400 Bad Request when uuid is not valid" in {
    val expirationDate = LocalDate.now().plusDays(1)
    val productRequest =
      ProductView(name = "name", vendor = "vendor", price = 10.99, expirationDate = Option(expirationDate))
    val request = Post(uri = s"/products/1234", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  it should "return 400 Bad Request when name empty" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val productRequest =
      ProductView(name = "", vendor = "vendor", price = 10.99, expirationDate = Option(expirationDate))
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[ValidationErrors] shouldBe ValidationErrors(List("Field name can not be empty"))
    }
  }

  it should "return 400 Bad Request when name length is greater than maximum allowed" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val maxLength      = DomainConstants.productNameMaxSize
    val productRequest =
      ProductView(
        name = "1" * (maxLength + 1),
        vendor = "vendor",
        price = 10.99,
        expirationDate = Option(expirationDate),
      )
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[ValidationErrors] shouldBe ValidationErrors(List(s"Field name too big, max size is $maxLength chars"))
    }
  }

  it should "return 400 Bad Request when vendor empty" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val productRequest =
      ProductView(name = "name", vendor = "", price = 10.99, expirationDate = Option(expirationDate))
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[ValidationErrors] shouldBe ValidationErrors(List("Field vendor can not be empty"))
    }
  }

  it should "return 400 Bad Request when vendor length is greater than maximum allowed" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val maxLength      = DomainConstants.productVendorMaxSize
    val productRequest =
      ProductView(
        name = "name",
        vendor = "1" * (maxLength + 1),
        price = 10.99,
        expirationDate = Option(expirationDate),
      )
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[ValidationErrors] shouldBe ValidationErrors(
        List(s"Field vendor too big, max size is $maxLength chars")
      )
    }
  }

  it should "return 400 Bad Request when expiration date is before today" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(-5)
    val productRequest =
      ProductView(
        name = "name",
        vendor = "vendor",
        price = 10.99,
        expirationDate = Option(expirationDate),
      )
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[ValidationErrors] shouldBe ValidationErrors(
        List("Field expiration date must be in the future")
      )
    }
  }

  it should "return 400 Bad Request when price is zero" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(+5)
    val productRequest =
      ProductView(
        name = "name",
        vendor = "vendor",
        price = 0,
        expirationDate = Option(expirationDate),
      )
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[ValidationErrors] shouldBe ValidationErrors(
        List("Field price must be > 0")
      )
    }
  }

  it should "return 400 Bad Request when price is negative" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(+5)
    val productRequest =
      ProductView(
        name = "name",
        vendor = "vendor",
        price = -50.5,
        expirationDate = Option(expirationDate),
      )
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[ValidationErrors] shouldBe ValidationErrors(
        List("Field price must be > 0")
      )
    }
  }

  it should "return 400 Bad Request and list of error when product data is invalid" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(-15)
    val productRequest =
      ProductView(
        name = "123456789012",
        vendor = "123456789012",
        price = -50.5,
        expirationDate = Option(expirationDate),
      )
    val request = Post(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[ValidationErrors] shouldBe ValidationErrors(
        List(
          s"Field name too big, max size is ${DomainConstants.productNameMaxSize} chars",
          s"Field vendor too big, max size is ${DomainConstants.productVendorMaxSize} chars",
          "Field price must be > 0",
          "Field expiration date must be in the future",
        )
      )
    }
  }

  it should "return 409 Conflict when trying to add a product with an uuid already existing" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val productRequest1 =
      ProductView(name = "name", vendor = "vendor", price = 10.99, expirationDate = Option(expirationDate))
    val request1 = Post(uri = s"/products/$uuid", productRequest1)
    request1 ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
    }

    val productRequest2 =
      ProductView(name = "name2", vendor = "vendor2", price = 28.99, expirationDate = Option(expirationDate))
    val request2 = Post(uri = s"/products/$uuid", productRequest2)
    request2 ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.Conflict
    }

  }

  "PUT /products/{uuid}" should "return 200 Ok when updating a product" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val product =
      addProduct(uuid = uuid, name = "name", vendor = "vendor", price = 100, expirationDate = Option(expirationDate))
    val productRequest = product.copy(name = "name2", vendor = "vendor2", price = 101)
    val request        = Put(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[ProductView] shouldBe productRequest
    }
  }

  it should "return 404 Not Found when updating a product that does not exists" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val productRequest = ProductView(name = "name", vendor = "vendor", price = 100, Option(expirationDate))
    val request        = Put(uri = s"/products/$uuid", productRequest)
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }

  "DELETE /products/{uuid}" should "return 204 No Content when deleting a product" in {
    val uuid           = UUID.randomUUID()
    val expirationDate = LocalDate.now().plusDays(1)
    val product =
      addProduct(uuid = uuid, name = "name", vendor = "vendor", price = 100, expirationDate = Option(expirationDate))
    val request = Delete(uri = s"/products/$uuid")
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }

  it should "return 404 Not Found when deleting a product" in {
    val uuid    = UUID.randomUUID()
    val request = Delete(uri = s"/products/$uuid")
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }

  "GET /products?vendor=..." should "return the correct list of products" in {
    val expirationDate = LocalDate.now().plusYears(2)
    val pr1 = addProduct(
      UUID.randomUUID(),
      name = "iphone 6",
      vendor = "apple",
      price = 199.99,
      expirationDate = Option(expirationDate),
    )
    val pr2 = addProduct(
      UUID.randomUUID(),
      name = "iphone 7",
      vendor = "apple",
      price = 399.99,
      expirationDate = Option(expirationDate),
    )
    val pr3 = addProduct(
      UUID.randomUUID(),
      name = "iphone 8",
      vendor = "apple",
      price = 699.99,
      expirationDate = Option(expirationDate),
    )
    val pr4 = addProduct(
      UUID.randomUUID(),
      name = "notebook",
      vendor = "dell",
      price = 699.99,
      expirationDate = Option(expirationDate),
    )

    val request1 = Get(uri = s"/products?vendor=apple")
    request1 ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      val appleList = responseAs[List[ProductView]]
      appleList should contain theSameElementsAs List(pr1, pr2, pr3)
      appleList.length shouldBe 3
    }

    val request2 = Get(uri = s"/products?vendor=dell")
    request2 ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      val appleList = responseAs[List[ProductView]]
      appleList shouldBe List(pr4)
      appleList.length shouldBe 1
    }

    val request3 = Get(uri = s"/products?vendor=bmw")
    request3 ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      val appleList = responseAs[List[ProductView]]
      appleList shouldBe List()
      appleList.length shouldBe 0
    }

  }

  it should "return an empty list when there is no product with that vendor" in {
    val expirationDate = LocalDate.now().plusYears(2)
    val pr1 = addProduct(
      UUID.randomUUID(),
      name = "iphone 6",
      vendor = "apple",
      price = 199.99,
      expirationDate = Option(expirationDate),
    )
    val pr2 = addProduct(
      UUID.randomUUID(),
      name = "iphone 7",
      vendor = "apple",
      price = 399.99,
      expirationDate = Option(expirationDate),
    )
    val pr3 = addProduct(
      UUID.randomUUID(),
      name = "iphone 8",
      vendor = "apple",
      price = 699.99,
      expirationDate = Option(expirationDate),
    )
    val pr4 = addProduct(
      UUID.randomUUID(),
      name = "notebook",
      vendor = "dell",
      price = 699.99,
      expirationDate = Option(expirationDate),
    )

    val request = Get(uri = s"/products?vendor=bmw")
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      val appleList = responseAs[List[ProductView]]
      appleList shouldBe List()
      appleList.length shouldBe 0
    }

  }

  "GET /products" should "return the list of all products" in {
    val expirationDate = LocalDate.now().plusYears(2)
    val pr1 = addProduct(
      UUID.randomUUID(),
      name = "iphone 6",
      vendor = "apple",
      price = 199.99,
      expirationDate = Option(expirationDate),
    )
    val pr2 = addProduct(
      UUID.randomUUID(),
      name = "iphone 7",
      vendor = "apple",
      price = 399.99,
      expirationDate = Option(expirationDate),
    )
    val pr3 = addProduct(
      UUID.randomUUID(),
      name = "iphone 8",
      vendor = "apple",
      price = 699.99,
      expirationDate = Option(expirationDate),
    )

    val request = Get(uri = s"/products")
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      val appleList = responseAs[List[ProductView]]
      appleList should contain theSameElementsAs List(pr1, pr2, pr3)
      appleList.length shouldBe 3
    }
  }

  it should "return an empty list when there are not products" in {
    Get(uri = s"/products") ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      val list = responseAs[List[ProductView]]
      list shouldBe List()
      list.length shouldBe 0
    }
  }

  "GET /products/{uuid}" should "return the correct product" in {
    val expirationDate = LocalDate.now().plusYears(2)
    val uuid1          = UUID.randomUUID()
    val uuid2          = UUID.randomUUID()
    val uuid3          = UUID.randomUUID()
    val pr1 = addProduct(
      uuid1,
      name = "iphone 6",
      vendor = "apple",
      price = 199.99,
      expirationDate = Option(expirationDate),
    )
    val pr2 = addProduct(
      uuid2,
      name = "iphone 7",
      vendor = "apple",
      price = 399.99,
      expirationDate = Option(expirationDate),
    )
    val pr3 = addProduct(
      uuid3,
      name = "iphone 8",
      vendor = "apple",
      price = 699.99,
      expirationDate = Option(expirationDate),
    )

    val request1 = Get(uri = s"/products/$uuid1")
    request1 ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[ProductView] shouldBe pr1
    }

    val request2 = Get(uri = s"/products/$uuid2")
    request2 ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[ProductView] shouldBe pr2
    }

    val request3 = Get(uri = s"/products/$uuid3")
    request3 ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[ProductView] shouldBe pr3
    }
  }

  it should "return 404 Not found when the product does not exist" in {
    val expirationDate = LocalDate.now().plusYears(2)
    val uuid1          = UUID.randomUUID()
    val uuid2          = UUID.randomUUID()
    val uuid3          = UUID.randomUUID()
    val pr1 = addProduct(
      uuid1,
      name = "iphone 6",
      vendor = "apple",
      price = 199.99,
      expirationDate = Option(expirationDate),
    )
    val pr2 = addProduct(
      uuid2,
      name = "iphone 7",
      vendor = "apple",
      price = 399.99,
      expirationDate = Option(expirationDate),
    )
    val pr3 = addProduct(
      uuid3,
      name = "iphone 8",
      vendor = "apple",
      price = 699.99,
      expirationDate = Option(expirationDate),
    )

    val request = Get(uri = s"/products/${UUID.randomUUID()}")
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.NotFound
    }

  }

  "GET /products-by-vendor" should "return the list products grouped by vendor" in {
    val expirationDate = LocalDate.now().plusYears(2)
    val pr1 = addProduct(
      UUID.randomUUID(),
      name = "iphone 6",
      vendor = "apple",
      price = 199.99,
      expirationDate = Option(expirationDate),
    )
    val pr2 = addProduct(
      UUID.randomUUID(),
      name = "iphone 7",
      vendor = "apple",
      price = 399.99,
      expirationDate = Option(expirationDate),
    )
    val pr3 = addProduct(
      UUID.randomUUID(),
      name = "notebook",
      vendor = "dell",
      price = 899.99,
      expirationDate = Option(expirationDate),
    )
    val expectedList =
      List(
        VendorProductsView(vendor = "apple", List(pr1, pr2)),
        VendorProductsView(vendor = "dell", List(pr3)),
      )

    val request = Get(uri = s"/products-by-vendor")
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      val products = responseAs[List[VendorProductsView]]
      products.length shouldBe 2
      products.filter(_.vendor == "apple").head.products should contain theSameElementsAs expectedList
        .filter(_.vendor == "apple")
        .head
        .products
      products.filter(_.vendor == "dell").head.products should contain theSameElementsAs expectedList
        .filter(_.vendor == "dell")
        .head
        .products
    }
  }

  "GET /products-statistics" should "return the products statistics" in {
    val expirationDate = LocalDate.now().plusYears(2)
    val pr1 = addProduct(
      UUID.randomUUID(),
      name = "iphone 6",
      vendor = "apple",
      price = 100,
      expirationDate = Option(expirationDate),
    )
    val pr2 = addProduct(
      UUID.randomUUID(),
      name = "iphone 7",
      vendor = "apple",
      price = 200,
      expirationDate = Option(expirationDate),
    )
    val pr3 = addProduct(
      UUID.randomUUID(),
      name = "notebook",
      vendor = "dell",
      price = 300,
      expirationDate = Option(expirationDate),
    )
    val request = Get(uri = s"/products-statistics")
    request ~> productRoutes.routes ~> check {
      status shouldBe StatusCodes.OK
      val statistics = responseAs[Statistics]
      statistics.numberOfProducts shouldBe 3
      statistics.numberOfVendors shouldBe 2
      statistics.averagePrice shouldBe 200
      statistics.dueProducts shouldBe 0
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    inMemoryProductRepository.deleteAll
  }

  private def addProduct(
      uuid: UUID,
      name: String,
      vendor: String,
      price: BigDecimal,
      expirationDate: Option[LocalDate],
  ) = {
    val newProduct = ProductView(name = name, vendor = vendor, price = price, expirationDate = expirationDate)
    Post(uri = s"/products/${uuid.toString}", newProduct) ~> productRoutes.routes
    newProduct
  }
}

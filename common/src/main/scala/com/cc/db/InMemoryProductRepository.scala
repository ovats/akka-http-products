package com.cc.db

import com.cc.domain.{Product, VendorProducts}

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

class InMemoryProductRepository()(implicit ec: ExecutionContext) extends ProductRepository[UUID, Product] {

  private val repo: TrieMap[UUID, Product] = TrieMap.empty

  override def create(product: Product): Future[UUID] =
    Future {
      repo.update(product.uuid, product)
      product.uuid
    }

  override def update(product: Product): Future[Product] =
    Future {
      repo.update(product.uuid, product)
      product
    }

  override def findById(key: UUID): Future[Option[Product]] =
    Future {
      repo.get(key)
    }

  override def delete(key: UUID): Future[Unit] =
    Future {
      repo.remove(key)
    }

  override def findAll: Future[Seq[Product]] =
    Future {
      repo.toList.map(_._2)
    }

  def findByVendor(vendorName: String, caseSensitive: Boolean): Future[Seq[Product]] =
    Future {
      val caseFilter =
        if (caseSensitive) (p: (UUID, Product)) => p._2.vendor == vendorName
        else (p: (UUID, Product)) => p._2.vendor.toLowerCase() == vendorName.toLowerCase()
      repo.filter(caseFilter).toList.map(_._2)
    }

  def findAllGroupedByVendor(): Future[List[VendorProducts]] =
    Future {
      val groupedByVendor: List[(String, TrieMap[UUID, Product])] = repo.groupBy(x => x._2.vendor.toLowerCase).toList
      val result = groupedByVendor.map { e =>
        VendorProducts(
          vendor = e._1,
          products = e._2.toList.map(x => x._2),
        )
      }
      result
    }

  override def deleteAll(): Future[Unit] =
    Future {
      repo.clear()
    }
}

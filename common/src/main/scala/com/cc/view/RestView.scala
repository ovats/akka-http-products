package com.cc.view

import com.cc.domain.{Product, VendorProducts}

trait RestView {

  implicit class ProductViewConverter(product: Product) {
    def toRestView: ProductView =
      ProductView(
        name = product.name,
        vendor = product.vendor,
        price = product.price,
        expirationDate = product.expirationDate,
      )
  }

  implicit class VendorProductsViewConverter(vendorProducts: VendorProducts) {
    def toRestView: VendorProductsView =
      VendorProductsView(vendorProducts.vendor, vendorProducts.products.map(_.toRestView))
  }

}

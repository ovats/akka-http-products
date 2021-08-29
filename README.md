# akka-http-products

## Overview

The project contains two subprojects:

- api: code related for API Rest.
- common: all common classes, objects and traits.

The API is used to store, maintain and retrieve data and statistics of products.

Every product is defined by:

- uuid: an unique identifier for each product
- name: name/description of the product
- vendor: manufacturer of the product
- price: cost of the product
- expiration date: optional, expiration date of the product

Product constraints are described later in `POST /products/{uuid}`.

## API

List of implemented endpoints:

- `POST /products/{uuid}` => add new products
- `PUT /products/{uuid}` => update product data
- `DELETE /products/{uuid}` => delete product
- `GET /products/{uuid}` => get product data of a given uuid
- `GET /products?vendor=name` => retrieve the list of products filtered by vendor
- `GET /products` => get the list of all products stored
- `GET /products-by-vendor` => get the list of products grouped by vendor
- `GET /products-statistics` => get products statistics
- `GET /ping`

When adding or updating a product the following validations are implemented:

- product name can not be empty
- product name max length is 10 chars
- vendor name (manufacturer) can not be empty
- vendor name max length is 10 chars
- price should be > 10
- expiration date if specified must be in the future

### POST /products/{uuid}

```
curl -X POST \
  http://localhost:8080/products/52ae4ed7-d318-44ac-8c33-a4ad6a3c0d35 \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "iphone",
    "vendor": "apple",
    "price": 100,
    "expirationDate": "2021-05-05"
}'
```

The field `expirationDate` is optional, so the following request is also valid:

```
curl -X POST \
  http://localhost:8080/products/a96ff2e7-5453-4946-9ffe-492521222a5e \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "macbook pro",
    "vendor": "apple",
    "price": 200
}'
```

### PUT /products/{uuid}

```
curl -X PUT \
  http://localhost:8080/products/52ae4ed7-d318-44ac-8c33-a4ad6a3c0d35 \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "iphone",
    "vendor": "apple",
    "price": 100,
    "expirationDate": "2021-05-05"
}'
```

### DELETE /products/{uuid}

``` 
curl -X DELETE http://localhost:8080/products/52ae4ed7-d318-44ac-8c33-a4ad6a3c0d35
```

### GET /products/{uuid}

Get the data of a product given its `uuid`:

```
curl -X GET http://localhost:8080/products/uuid/a96ff2e7-5453-4946-9ffe-492521222a5e \
```


### GET /products?vendor=name&case=no

Get the list of products of a given `vendor`:

```
curl -X GET 'http://localhost:8080/products?vendor=apple&case=no' 
```

The query parameter `case` indicates if the search is case sensitive or not.
Valid values are`yes` or `no`. 
Default is `no ` (not case sensitive).

### GET /products

Get the list of all products:

```
curl -X GET http://localhost:8080/products 
```

### GET /products-by-vendor

Get the list of vendors and its list of products:

```
curl -X GET http://localhost:8080/products-by-vendor 
```

### GET /products-statistics

Get products statistics:

- total number of products
- total number of vendors 
- average products price
- total number of expired products

```
curl -X GET http://localhost:8080/products-statistics
```

### GET /ping

This is just a health check endpoint.

```
curl -X GET  http://localhost:8080/ping 
```

## Configuration in application.conf and environment variables

File `application.conf` is very small:

```
http {
  host = "0.0.0.0"
  host = ${?HTTP_INTERFACE}
  port = 8080
  port = ${?HTTP_PORT}
}

akka {
  loglevel = "DEBUG"
  loglevel = ${?LOG_LEVEL}
}
```

Section `http` it's about `interface` and `port` for the API.
Default values are `0.0.0.0` and `80880`.
To set a different value the file can be updated or two environment variables can be created to override default values: `HTTP-INTERFACE` and `HTTP_PORT`.

Section `akka` only set the loglevel value (default `DEBUG`) and it can be overrided setting the the value in environment variable `LOG_LEVEL`.  

## Run service in local environment

From a console/terminal just run:

```
sbt api/run
```

It's possible to add plugins for creating docker images (`sbt-native-packager` for example).
This is not implemented in this solution.


## Possible improvements

- For OAS 3.0 API documentation it can be generated manually or by using [Tapir](https://github.com/softwaremill/tapir) library.
This library allows to create, declare and implement all endpoints and then the documentation is generated automatically (no manual `json` or `yaml` generation).
- Instead of using an "in memory repository" [Slick](https://scala-slick.org/) library and [H2](http://h2database.com/html/main.html) database can be used. 
Then H2 driver can be replaced by other drivers for databases like Postgres, MySql, etc.
- Of course there are other databases libraries like for example [Doobie](https://tpolecat.github.io/doobie/).
- If project is using a database instead of an "in memory repository" then endpoint `/products?vendor=name` can be improved using sql `LIKE`.
- Unit tests were implemented for: API routes and product data validations using Cats.Validated.
- For test coverage [sbt-coverage](https://github.com/scoverage/sbt-scoverage) can be added to the project.
- ServiceResponse trait could be improved using: type parameters, Either.
- Product data validation can be simplified to use just `String` to return errors (instead of using `case class Invalid`).


# Notes 

## comentarios/notas

- ver mis notas de Pull Request


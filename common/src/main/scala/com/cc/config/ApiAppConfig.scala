package com.cc.config

import com.cc.config.ApiAppConfig.HttpConfig
import com.typesafe.scalalogging.LazyLogging
import pureconfig.ConfigSource
import pureconfig.generic.auto._

final case class ApiAppConfig(http: HttpConfig)

object ApiAppConfig extends LazyLogging {

  final case class HttpConfig(host: String, port: Int)

  def apply(resource: String = "application.conf"): ApiAppConfig = {
    ConfigSource.resources(resource).load[ApiAppConfig] match {
      case Left(errors) =>
        val msg = s"Unable to load service configuration (ApiConfig)"
        logger.error(
          s"$msg \n${errors.toList.map(_.description).mkString("* ", "\n*", "")}"
        )
        throw new IllegalStateException(msg)

      case Right(config) =>
        logger.debug(s"Successfully loaded configuration (ApiConfig), $config")
        config
    }
  }

}

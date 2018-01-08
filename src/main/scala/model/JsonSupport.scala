package model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemJsonFormat = jsonFormat9(Story)
}

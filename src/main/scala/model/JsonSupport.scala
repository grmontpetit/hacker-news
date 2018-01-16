package model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

/**
  * Object used by Akka Http to deserialize Json into
  * objects that can be used.
  */
object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val typeJsonFormat = new EnumJsonConverter(Types)
  implicit val commentJsonFormat = jsonFormat7(Comment)
  implicit val storyJsonFormat = jsonFormat10(Story)
}

class EnumJsonConverter[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
  override def write(obj: T#Value): JsValue = JsString(obj.toString)

  override def read(json: JsValue): T#Value = {
    json match {
      case JsString(txt) => enu.withName(txt)
      case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
    }
  }
}
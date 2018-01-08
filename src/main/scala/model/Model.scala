package model

sealed trait Model

case class Item(by: String,
                id: Int,
                kids: List[Int],
                parent: Int,
                text: String,
                time: Long,
                `type`: String,
                url: Option[String] = None) extends Model

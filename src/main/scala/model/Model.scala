package model

sealed trait Model

case class Story(by: String,
                 descendants: Int,
                 id: Int,
                 kids: List[Int],
                 score: Int,
                 time: Long,
                 title: String,
                 `type`: String,
                 url: String) extends Model

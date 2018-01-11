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

case class Comment(by: String,
                   id: Int,
                   parent: Int,
                   text: String,
                   time: Long,
                   `type`: String)

case class StoryReply(by: String,
                     descendants: Int,
                     id: Int,
                     kids: List[Comment],
                     score: Int,
                     time: Long,
                     title: String,
                     `type`: String,
                     url: String) extends Model
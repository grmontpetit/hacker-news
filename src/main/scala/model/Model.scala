package model

import model.Types.`type`

sealed trait Model

/**
  * Represents a story in hacker-news.
  * @param by The alias of the person who created the story.
  * @param descendants The nb. of descendent stories.
  * @param id The id of the story.
  * @param kids The comments of the stories.
  * @param score The voted score of the story.
  * @param time The creation time of the story.
  * @param title The title of the story.
  * @param `type` The [[Types]]
  * @param url The URL of the story
  * @param comments The comments of the story (left empty by default).
  */
case class Story(by: String,
                 descendants: Option[Int],
                 id: Int,
                 kids: Option[List[Int]],
                 score: Int,
                 time: Long,
                 title: String,
                 `type`: `type`,
                 url: Option[String],
                 comments: Option[List[Comment]] = None) extends Model

/**
  * Represents a comment in hacker-news.
  * @param by The alias of the person who created the comment.
  * @param id The id of the comment.
  * @param kids The kids of the comment.
  * @param parent The parent story/comment of the comment
  * @param text The content of the comment itself.
  * @param time The time of the comment.
  * @param `type` The [[Types]]
  */
case class Comment(by: Option[String],
                   id: Int,
                   kids: Option[List[Int]],
                   parent: Int,
                   text: Option[String],
                   time: Long,
                   `type`: `type`)

object Types extends Enumeration {
  val story, comment, job, poll, pollopt = Value
  type `type` = Value
}
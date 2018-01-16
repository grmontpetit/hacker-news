package actors

import model.{Comment, Story}

sealed trait Message

case class GetStory(id: Int) extends Message
case class StoryReply(story: Story) extends Message

case class GetComments(story: Story, item: Int) extends Message
case class CommentsReply(story: Story, comment: Comment) extends Message

case object StartWork extends Message
case object StopWork extends Message
case class WorkComplete(comments: List[CommentsReply]) extends Message
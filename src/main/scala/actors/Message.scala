package actors

import model.Story

sealed trait Message

case class Work(storyId: Int) extends Message
case object StartWork extends Message

case class Reply(item: Story)
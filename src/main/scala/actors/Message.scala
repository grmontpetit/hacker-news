package actors

import model.Item

sealed trait Message

case class Work(storyId: Int) extends Message
case object StartWork extends Message

case class Reply(id: Int, execTime: Int, item: Item)
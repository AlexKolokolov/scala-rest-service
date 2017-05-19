package org.kolokolov.model

/**
  * Created by Kolokolov on 17.05.2017.
  */
case class Comment(text: String, messageId: Int, authorId: Int, id: Int = 0) extends Identifiable

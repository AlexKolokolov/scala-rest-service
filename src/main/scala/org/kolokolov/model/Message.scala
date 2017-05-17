package org.kolokolov.model

import org.kolokolov.repo.Identifiable

/**
  * Created by Kolokolov on 16.05.2017.
  */
case class Message(text: String, authorId: Int, id: Int = 0) extends Identifiable

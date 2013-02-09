package com.october

import com.twitter.util._
import com.twitter.logging.Logger
import october.Post
import october.PostList
import october.Recommender

class RecHandler extends october.Recommender.FutureIface {
    private val logger = Logger.get()

    override def ping(): Future[String] = {
        logger.info("Ping received.")
        Future.value("Pong")
    }

    override def recPosts(userId: Long): Future[PostList] = {
        Future.value(new SPostList(Option(0.5), Seq()))
    }
}

class SPostList(val confidence: Option[Double],
                val posts: Seq[SPost]) extends october.PostList {
}

class SPost(val weight: Option[Double],
            val postId: Long) extends october.Post {
}

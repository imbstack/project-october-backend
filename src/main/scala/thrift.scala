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

    override def recPosts(userId: Long): Future[SPostList] = {
        Future.value(new SPostList(Option(0.5),
            for (i <- 1 until 11) yield new SPost(Option(1.0/i), i)))
    }
}

class SPostList(val confidence: Option[Double],
                val posts: Seq[SPost]) extends october.PostList with Seq[SPost] {

    def apply(idx: Int): SPost = {
        posts.apply(idx)
    }

    def iterator: Iterator[SPost] = {
        posts.iterator
    }

    def length: Int = {
        posts.length
    }
}

class SPost(val weight: Option[Double],
            val postId: Long) extends october.Post {
}

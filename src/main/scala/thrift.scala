package com.october

import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion
import com.thinkaurelius.titan.core._

import com.twitter.util._
import com.twitter.logging.Logger
import october.Action
import october.User
import october.Post
import october.PostList
import october.Recommender

class RecHandler(graph: TitanGraph) extends october.Recommender.FutureIface {
    private val logger = Logger.get()

    override def ping(): Future[String] = {
        logger.info("Ping received.")
        Future.value("Pong")
    }

    override def recPosts(userId: Long): Future[PostList] = {
        Future.value(PostList.apply(Option(0.5),
            for (i <- 1 until 11) yield Post.apply(i, Option(1.0/i))))
    }

    override def userVPost(userId: Long, verb: october.Action, postId: Long) : Future[Unit] = {
        logger.info("user did something to post")
        Future.value(false)
    }

    override def userVComment(userId: Long, verb: october.Action, commentId: Long) : Future[Unit] = {
        logger.info("user did something to comment")
        Future.value(false)
    }

    override def addUser(userId: Long) : Future[Boolean] = {
        if (graph.getVertices("userId", userId).iterator().hasNext())
            throw new IllegalArgumentException("User already exists: " + userId)
        val u = graph.addVertex(null)
        u.setProperty("userId", userId)
        graph.stopTransaction(Conclusion.SUCCESS)
        Future.value(true)
    }
}


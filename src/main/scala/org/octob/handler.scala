package org.octob

import october._

import com.twitter.cassie._

import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion
import com.thinkaurelius.titan.core._

import com.twitter.util._
import com.twitter.logging.Logger

class RecHandler(graph: TitanGraph, posts: Keyspace) extends october.Recommender.FutureIface {
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

    override def addPost(userId: Long, postId: Long, rawTokens: Seq[Token]) : Future[Boolean] = {
        // TODO: Do something with the userId here
        val docVector = posts.columnFamily[LongCodec, Utf8Codec, DoubleCodec] // TODO: make a double codec.  also comment this section a lot
        val max = rawTokens.map(_.f).reduceLeft (_ max _)
        for (token <- rawTokens) {
            // TODO: take each token and: 1. increment the document frequency of that token 2. calculate tf/idf and stick it in the docment
            // value with that token as key
        }
        Future.value(true)
    }
}


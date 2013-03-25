package org.octob

import october._
import org.octob.codecs.DoubleCodec

import com.twitter.cassie._
import com.twitter.cassie.codecs._

import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion
import com.thinkaurelius.titan.core._

import com.twitter.util._
import com.twitter.logging.Logger

class RecHandler(graph: TitanGraph, posts: Keyspace) extends october.Recommender.FutureIface {
    private val logger = Logger.get(getClass)

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
        // TODO: Maybe shouldn't be creating columnfamily in the function?  performance should be investigated
        // TODO: Consider changing consistency of columnfamilies from quorum...
        val docVector = posts.columnFamily("docvector", LongCodec, Utf8Codec, LongCodec)
        val tokenData = posts.columnFamily("tokendata", Utf8Codec, Utf8Codec, LongCodec)
        val max = if (rawTokens.length > 0) rawTokens.map(_.f).reduceLeft (_ max _) else 0
        for (token <- rawTokens) {
            // TODO: take each token and: 1. increment the document frequency of that token 2. calculate tf/idf and stick it in the docment
            // value with that token as key
            // TODO: Make sure this is synchronized properly so that read and write operate on same value
            logger.info(token.t)
            //logger.info(classOf[tokenData.getColumn(token.t, "df")].toString)
            //val count = tokenData.getColumn(token.t, "df") map { _ match {
            //    case Future[Option[Column[String, Long]]] => 0
            //    case None => 0 }
            //} handle { case e => 0 }
            //logger.info(count.get.toString)
            //tokenData.insert(token.t, Column("df", token.f))
        }
        Future.value(true)
    }
}


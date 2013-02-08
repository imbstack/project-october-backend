package com.october

import com.typesafe.config._
import org.apache.thrift.transport.TServerSocket
import org.apache.thrift.server.TSimpleServer
import org.apache.thrift.server.TServer.Args
import org.apache.thrift.protocol.TBinaryProtocol
import java.net.InetSocketAddress
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
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

object RecServer {
    private val logger = Logger.get(getClass)

    def main(args: Array[String]) {
        val config = ConfigFactory.load()
        val protocol = new TBinaryProtocol.Factory()
        val handler = new RecHandler()
        val service = new Recommender.FinagledService(handler, protocol)
        val address = new InetSocketAddress(config.getString("server.host"),
            config.getInt("server.port"))
        logger.info("Server going up!")
        var builder = ServerBuilder()
            .codec(ThriftServerFramedCodec())
            .name("recommender_services")
            .bindTo(address)
            .build(service)
         println("Press Ctrl+C to exit")
    }
}

package com.october

import com.typesafe.config._
import org.apache.thrift.transport.TServerSocket
import org.apache.thrift.server.TSimpleServer
import org.apache.thrift.server.TServer.Args
import org.apache.thrift.protocol.TBinaryProtocol
import java.net.InetSocketAddress
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import october.Recommender
import com.twitter.logging.Logger

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

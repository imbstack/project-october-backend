package org.octob

import october.Recommender

import com.mongodb.casbah.Imports._

import com.typesafe.config._
import org.apache.thrift.transport.TServerSocket
import org.apache.thrift.server.TSimpleServer
import org.apache.thrift.server.TServer.Args
import org.apache.thrift.protocol.TBinaryProtocol
import java.net.InetSocketAddress
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec

import grizzled.slf4j.Logging

object RecServer extends Logging {
    val config = getConfig()
    var mongo = MongoClient(config.getString("mongo.host"), config.getInt("mongo.port"))(config.getString("mongo.db"))

    def getConfig(): Config = {
        val config = ConfigFactory.load().getConfig("base")
        def merge(name: String) = ConfigFactory.load().getConfig(name).withFallback(config)
        sys.env.get("OCTOBER_ENV") match {
            case Some("production") => merge("production")
            case Some("test") => merge("test")
            case Some("remote") => merge("remote")
            case Some("development") | _ => merge("development")
        }
    }

    def main(args: Array[String]) {

        // Now set up Thrift server and listen
        val protocol = new TBinaryProtocol.Factory()
        val handler = RecHandler
        val service = new Recommender.FinagledService(handler, protocol)
        val address = new InetSocketAddress(config.getString("server.host"),
            config.getInt("server.port"))
        info("October Recommender Service starting in: " + config.getString("name") + " on " +
            config.getString("server.host") +":"+ config.getInt("server.port"))
        var builder = ServerBuilder()
            .codec(ThriftServerFramedCodec())
            .name("recommender_service")
            .bindTo(address)
            .build(service)
    }
}

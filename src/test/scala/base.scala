package org.octob.test

import org.octob._

import com.mongodb.casbah.Imports._

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import com.twitter.logging.config._
import com.twitter.logging.Logger

class BaseTest extends FunSuite with BeforeAndAfter {

    val logconf = new LoggerConfig { 
        node = ""
        level = Logger.ERROR
    }
    logconf()

    var mongo: MongoDB = _

    before {
        RecServer.mongo =  MongoClient()("october-test")
        mongo = RecServer.mongo
        mongo.dropDatabase() // Clear out from other testing
        dobefore
    }

    after {
        mongo.dropDatabase()
    }

    /**
     * override to do suite-specific setup
     */
    def dobefore {}
}

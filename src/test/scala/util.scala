package org.octob.test

import org.octob.Util

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import com.twitter.logging.config._
import com.twitter.logging.Logger

class UtilSuite extends FunSuite with BeforeAndAfter {

    val logconf = new LoggerConfig { 
        node = ""
        level = Logger.ERROR
    }
    logconf()

    val VMap = Map[String,Double] _

    test("dot products accept empty maps") {
        val map1 = VMap()
        val map2 = VMap()
        assert(Util.dotProduct(map1, map2) == 0)
    }

    test("dot products accept non-empty maps with disjoint terms") {
        val map1 = VMap("a" -> 1.3, "b" -> 2.4)
        val map2 = VMap("c" -> 3.1, "d" -> 4.2)
        assert(Util.dotProduct(map1, map2) == 0)
    }

    test("dot products produces proper result for magnitude 1 vectors") {
        val map1 = VMap("a" -> 1.5)
        val map2 = VMap("a" -> 2.0)
        assert(Util.dotProduct(map1, map2) == 3.0)
    }
}

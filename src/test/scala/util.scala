package org.octob.test

import org.scalatest.matchers.ShouldMatchers
import org.octob._
import org.scala_tools.time.Imports._

class UtilSuite extends BaseTest with ShouldMatchers {

    val VMap = Map[String,Double] _
    val LMap = Map[String,Long] _

    test("dot products accept empty maps") {
        val map1 = VMap()
        val map2 = VMap()
        expect(0.0) {
            Util.dotProduct(map1, map2)
        }
    }

    test("dot products accept non-empty maps with disjoint terms") {
        val map1 = VMap("a" -> 1.3, "b" -> 2.4)
        val map2 = VMap("c" -> 3.1, "d" -> 4.2)
        expect(0.0){
            Util.dotProduct(map1, map2)
        }
    }

    test("dot products produces proper result for magnitude 1 vectors") {
        val map1 = VMap("a" -> 1.5)
        val map2 = VMap("a" -> 2.0)
        expect(3.0) {
            Util.dotProduct(map1, map2)
        }
    }

    test("tfidf works in expected case") {
        val userMap = LMap("a" -> 10, "b" -> 15, "c" -> 12)
        val docCountMap = LMap("a" -> 2, "b" -> 12, "c" -> 1)
        expect(Map("a" -> 8.333333333333332, "b" -> 2.0833333333333335, "c" -> 20.0)) {
            Util.tfIdfVec(userMap, 25, docCountMap)
        }
    }

    test("timescaling works within flat period") {
        expect(0.5) {
            Util.timeScale(0.5, DateTime.now)
        }
    }

    test("timescaling works within logarithmic period") {
        Util.timeScale(0.5, DateTime.now - 2.days) should be (0.136479207020669 plusOrMinus 0.0000005)
    }
}

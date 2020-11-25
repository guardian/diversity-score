package lib

import lib.NameService.batchText
import org.scalatest.{FlatSpec, Matchers}

class NameServiceTest extends FlatSpec with Matchers {

  "batchText" should "return a single chunk for a string < 5000 bytes" in {
    batchText("Hello Amy").length shouldEqual 1
  }

  "batchText" should "return multiple chunks for a string > 5000 bytes" in {
    val largeString = List.fill(12000)("a").mkString("")
    println(s"length: ${batchText(largeString).length}")
    batchText(largeString).length should be > 1
  }

}

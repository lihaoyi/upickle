package upickle.core

import utest._

import scala.util.Try

object UtilTests extends TestSuite {

  val tests = Tests {

    test("parseLong - valid") {
      test("0")(Util.parseLong("0", 0, 1) ==> 0L)
      test("42")(Util.parseLong("42", 0, 2) ==> 42L)
      test("x0")(Util.parseLong("x0", 1, 2) ==> 0L)
      test("x0x")(Util.parseLong("x0x", 1, 2) ==> 0L)
      test("x-1x")(Util.parseLong("x-1x", 1, 3) ==> -1L)
    }

    test("parseLong - invalid") {
      def invalid(input: String)(start: Int = 0, end: Int = input.length) = {
        val e = intercept[NumberFormatException] {
          Util.parseLong(input, start, end)
        }
        e.getMessage ==> input.substring(start, end)
      }

      test("a")(invalid("a")())
      test("-")(invalid("-")())
      test("᥌")(invalid("᥌")())
      test("too long")(invalid(Long.MaxValue.toString + "1")())
      test("10x")(invalid("x10x")(1, 4))
      test("x10")(invalid("x10x")(0, 3))
    }

    test("parseLong - bounds") {
      for {
        start <- -1 to 5
          end <- -1 to 5
          if start != end // NumberFormatException, not IndexOutOfBoundsException
      } {
        val s = "123"
        if (Try(s.substring(start, end)).isSuccess) {
          Util.parseLong(s, start, end)
        } else {
          intercept[IndexOutOfBoundsException](Util.parseLong(s, start, end))
        }
      }
    }
  }
}

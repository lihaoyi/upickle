package upickle
import utest._
import upickle.default.{read, write, readBinary, writeBinary, writeMsg, transform}
import TestUtil._

object NamedTupleTests extends TestSuite {

  def tests = Tests {
    test("Unit"){
      rw((), "null", "{}", upack.Null, upack.Obj())
    }

  }
}

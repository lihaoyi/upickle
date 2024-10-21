package upack
import java.io.ByteArrayOutputStream

import utest._

import scala.collection.mutable
object MsgPackJvmTests extends TestSuite{
  def readBytes(path: String) = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path))
  def readMsgs(path: String) = {
    val bytes = readBytes(path)
//    println(s"readMsgs($path) bytes.length:${bytes.length}")
    val output = mutable.ArrayBuffer.empty[Msg]
    val p = new MsgPackReader(bytes)
    while(p.getIndex < bytes.length){
//      println("---msg---")
      output.append(p.parse(Msg))
    }
    upack.Arr(output)
  }
  val tests = Tests{
    test("hello"){

      // Taken from:
      // https://github.com/msgpack/msgpack-ruby/tree/a22d8268f82e0f2ae95f038285af43ce5971810e/spec
      val resources = sys.env("MILL_TEST_RESOURCE_DIR")
      val casesJson = resources + "/cases.json"
      val casesMsg = resources + "/cases.msg"
      val casesCompactMsg = resources + "/cases_compact.msg"
      val expectedJson = ujson.read(readBytes(casesJson))
//      println("---msgs---")
      val msg = readMsgs(casesMsg)
//      println("---msgsCompact---")
      val msgCompact = readMsgs(casesCompactMsg)
//      println("---")
      val jsonMsg = upack.transform(msg, ujson.Value)
      val jsonMsgCompact = upack.transform(msgCompact, ujson.Value)
//      val writtenMsg = Util.bytesToString(upack.write(msg))
//      val rewrittenMsg = Util.bytesToString(
//        upack.write(upack.read(upack.write(msg)))
//      )
//      val writtenMsgCompact = Util.bytesToString(upack.write(msgCompact))
//      val rewrittenMsgCompact = Util.bytesToString(
//        upack.write(upack.read(upack.write(msgCompact)))
//      )
      assert(
        expectedJson == jsonMsg,
        expectedJson == jsonMsgCompact
//        Still doesn't pass:
//
//        writtenMsg == rewrittenMsg,
//        writtenMsgCompact == rewrittenMsgCompact
      )
    }
  }
}

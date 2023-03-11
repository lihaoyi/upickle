package upickle.implicits

import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.ClassTag
import upickle.core.{Visitor, Abort, ArrVisitor, ObjVisitor, NoOpVisitor}

/**
* Basic functionality to be able to read and write objects. Kept as a trait so
* other internal files can use it, while also mixing it into the `upickle`
* package to form the public API1
*/
trait TupleCaseClassReadWriters extends upickle.core.Types{


  class TupleNWriter[V](val writers: Array[Writer[_]], val f: V => Array[Any]) extends Writer[V]{
    def write0[R](out: Visitor[_, R], v: V): R = {
      if (v == null) out.visitNull(-1)
      else{
        val ctx = out.visitArray(writers.length, -1)
        val vs = f(v)
        var i = 0
        while(i < writers.length){
          ctx.visitValue(
            writers(i).asInstanceOf[Writer[Any]].write(
              ctx.subVisitor.asInstanceOf[Visitor[Any, Nothing]],
              vs(i)
            ),
            -1
          )
          i += 1
        }
        ctx.visitEnd(-1)
      }
    }
  }

  class TupleNReader[V](val readers: Array[Reader[_]], val f: Array[Any] => V) extends SimpleReader[V]{

    override def expectedMsg = "expected sequence"
    override def visitArray(length: Int, index: Int) = new ArrVisitor[Any, V] {
      val b = new Array[Any](readers.length)
      var facadesIndex = 0

      var start = facadesIndex
      def visitValue(v: Any, index: Int): Unit = {
        b(facadesIndex % readers.length) = v
        facadesIndex = facadesIndex + 1
      }

      def visitEnd(index: Int) = {
        val lengthSoFar = facadesIndex - start
        if (lengthSoFar != readers.length) {
          throw new Abort(
            "expected " + readers.length + " items in sequence, found " + lengthSoFar
          )
        }
        start = facadesIndex

        f(b)

      }

      def subVisitor: Visitor[_, _] = {
        readers(facadesIndex % readers.length)
      }
    }
  }

  abstract class CaseClassReader[V] extends SimpleReader[V]{
    override def expectedMsg = "expected dictionary"
    override def visitString(s: CharSequence, index: Int) = visitObject(0, true, index).visitEnd(index)
  }
  trait CaseClassWriter[V] extends ObjectWriter[V]{
    def length(v: V): Int
    def writeToObject[R](ctx: ObjVisitor[_, R], v: V): Unit
    def write0[R](out: Visitor[_, R], v: V): R = {
      if (v == null) out.visitNull(-1)
      else{
        val ctx = out.visitObject(length(v), true, -1)
        writeToObject(ctx, v)
        ctx.visitEnd(-1)
      }
    }
    def writeSnippet[R, V](objectAttributeKeyWriteMap: CharSequence => CharSequence,
                           ctx: _root_.upickle.core.ObjVisitor[_, R],
                           mappedArgsI: String,
                           w: Any,
                           value: Any) = {
      val keyVisitor = ctx.visitKey(-1)
      ctx.visitKeyValue(
        keyVisitor.visitString(objectAttributeKeyWriteMap(mappedArgsI), -1)
      )
      ctx.narrow.visitValue(w.asInstanceOf[Writer[Any]].write(ctx.subVisitor, value), -1)
    }
  }
  class SingletonReader[T](t: T) extends CaseClassReader[T]{
    override def expectedMsg = "expected string or dictionary"

    override def visitString(s: CharSequence, index: Int) = t

    override def visitObject(length: Int, jsonableKeys: Boolean, index: Int) = new ObjVisitor[Any, T] {
      def subVisitor = NoOpVisitor

      def visitKey(index: Int) = NoOpVisitor
      def visitKeyValue(s: Any) = ()

      def visitValue(v: Any, index: Int): Unit = ()

      def visitEnd(index: Int) = t
    }
  }
  class SingletonWriter[T](f: T) extends CaseClassWriter[T] {
    def length(v: T) = 0
    def writeToObject[R](ctx: ObjVisitor[_, R], v: T): Unit = () // do nothing
  }
}

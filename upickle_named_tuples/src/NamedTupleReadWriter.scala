package upickle.nt

import scala.NamedTuple.NamedTuple
import upickle.default.*
import ujson.Value

object NamedTupleReadWriter:
  inline given [N <: Tuple, V <: Tuple, T <: NamedTuple[N, V]]: ReadWriter[T] =
    val names = scala.compiletime.constValueTuple[N].toList.asInstanceOf[List[String]]
    val readWriters = scala.compiletime.summonAll[Tuple.Map[V, ReadWriter]].toList.asInstanceOf[List[ReadWriter[?]]]

    def toMap(t: T): Map[String, Value] =
      val values = t.toTuple.productIterator.toList
      (0.until(names.length)).foldLeft(Map.empty[String, Value]): (map, i) =>
        val readWriter = readWriters(i).asInstanceOf[ReadWriter[Any]]
        map + (names(i) -> transform(values(i))(using readWriter).to(ujson.Value))
    end toMap

    def fromMap(map: Map[String, Value]): T =
      val namesAndRWs = names.zip(readWriters)
      val valueList = namesAndRWs.map: (name, rw) =>
        map.get(name) match
          case None => throw new Exception(s"Missing field: $name")
          case Some(v) =>
            read(v)(using rw.asInstanceOf[ReadWriter[Any]])

      val tuple = valueList.foldRight(EmptyTuple: Tuple): (v, tuple) =>
        v *: tuple

      tuple.asInstanceOf[T]
    end fromMap

    summon[ReadWriter[Map[String, Value]]].bimap(toMap, fromMap)
  end given
end NamedTupleReadWriter

package ujson

@deprecated
sealed trait ParsingFailedException extends Exception
@deprecated
case class ParseException(clue: String, index: Int)
  extends Exception(clue + " at index " + index) with ParsingFailedException
@deprecated
case class IncompleteParseException(msg: String)
  extends Exception(msg) with ParsingFailedException

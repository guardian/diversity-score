package lib.nameService

case class Name(name: String) extends AnyVal

case class NamesServiceError(underlying: Throwable) extends Exception {
  override def toString: String = underlying.toString
}
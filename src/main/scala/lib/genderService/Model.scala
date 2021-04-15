package lib.genderService

sealed trait Gender {
  def toString: String
}

case object Male extends Gender {
  override def toString = "Male"
}
case object NonMale extends Gender {
  override def toString = "NonMale"
}

case class NameWithGender(name: String, gender: Option[Gender])

case object GenderServiceError extends Exception
case object GenderDecodeError extends Exception

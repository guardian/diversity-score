package lib

import cats.effect.IO
import io.circe.parser.decode
import sttp.client.{HttpURLConnectionBackend, basicRequest}
import sttp.client._
import io.circe.generic.auto._

sealed trait Gender {
  def toString: String
}

case object Male extends Gender {
  override def toString = "Male"
}
case object NonMale extends Gender {
  override def toString = "NonMale"
}

case class GenderResult(name: Name, gender: Option[Gender])

case object GenderServiceError extends Exception
case object GenderDecodeError extends Exception

trait GenderService {
  def genders(name: Name): IO[GenderResult]
}

object GenderService extends GenderService {
  case class GenderizeResult(name: String, gender: Option[String], probability: Double)

  implicit lazy val backend = HttpURLConnectionBackend()

  def genders(name: Name): IO[GenderResult] = {
    val request = basicRequest.get(uri"https://api.genderize.io/?name=${name.name}")

    def getResult(): IO[String] = IO.fromEither(request.send().body.left.map(_ => GenderServiceError))
    def decodeGender(json: String): IO[GenderizeResult] = IO.fromEither(decode[GenderizeResult](json))
    // what is the difference between IO.pure and IO.apply??
    def getGender(genderRes: GenderizeResult): IO[GenderResult] = IO{
      val genderOpt: Option[Gender] = genderRes.gender.flatMap {
        case "male" => Some(Male)
        case "female" => Some(NonMale)
        case _ => None
      }
      GenderResult(name, genderOpt)
    }

    for {
      json <- getResult()
      decoded <- decodeGender(json)
      gender <- getGender(decoded)
    } yield {
      gender
    }
  }
}

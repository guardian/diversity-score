package lib

import cats.effect.IO
import io.circe.parser.decode
import sttp.client.{HttpURLConnectionBackend, basicRequest}
import sttp.client._
import io.circe.generic.auto._
import sttp.model.Uri

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
    //TODO: fix bug where when text ends with name, get %00%00%00%00%00%0... added to end of uri
    val nm = name.name
    val uri: Uri = uri"https://api.genderize.io/".param("name", nm.toString)
//    println(s"uri: $uri")

    val request = basicRequest.get(uri)
//    println(s"request uri: ${request.uri}")

    def getResult(): IO[String] = IO.fromEither(request.send().body.left.map{e =>
      println(s"err: $e")
      GenderServiceError
    })
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

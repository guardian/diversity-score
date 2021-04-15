package lib.genderService

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.parser.decode
import sttp.client.{HttpURLConnectionBackend, basicRequest, _}
import sttp.model.Uri

trait GenderService {
  def genders(name: String): IO[NameWithGender]
}

object GenderService extends GenderService {
  case class GenderizeResult(name: String, gender: Option[String], probability: Double)

  implicit lazy val backend = HttpURLConnectionBackend()

  def genders(name: String): IO[NameWithGender] = {
    //TODO: fix bug where when text ends with name, get %00%00%00%00%00%0... added to end of uri
    val uri: Uri = uri"https://api.genderize.io/".param("name", name)
    println(s"uri: $uri")

    val request = basicRequest.get(uri)
//    println(s"request uri: ${request.uri}")

    def getResult(): IO[String] = IO.fromEither(request.send().body.left.map{e =>
      println(s"err: $e")
      GenderServiceError
    })
    def decodeGender(json: String): IO[GenderizeResult] = IO.fromEither(decode[GenderizeResult](json))
    // what is the difference between IO.pure and IO.apply??
    def getGender(genderRes: GenderizeResult): IO[NameWithGender] = IO{
      val genderOpt: Option[Gender] = genderRes.gender.flatMap {
        case "male" => Some(Male)
        case "female" => Some(NonMale)
        case _ => None
      }
      NameWithGender(name, genderOpt)
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

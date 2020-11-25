package lib

import java.net.URI

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import io.circe.{Decoder, Encoder, HCursor, Json, JsonFloat}
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client.{HttpURLConnectionBackend, basicRequest}
import sttp.client._
import sttp.model.Uri
import cats.effect._
import org.http4s.{EntityBody, EntityDecoder, HttpRoutes, Response, _}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext.global

//how do you constrain value to sit between 0 - 1 - Value Class?? - this is runtime
//Step 1 - names
//https://docs.aws.amazon.com/comprehend/latest/dg/how-entities.html
//Step 2 - genders
//https://gender-api.com/
//https://genderize.io/

trait ContentService {
  def getText(url: URI): IO[String]
}

object CAPIContentService extends ContentService {
  case class ArticleBody(body: String)

  implicit val decodeArticleBody: Decoder[ArticleBody] = new Decoder[ArticleBody] {
    final def apply(c: HCursor) = c.downField("response").downField("content").downField("fields").downField("body").as[String].map(ArticleBody)
  }

  //TODO: share one backend for PROD
  implicit lazy val backend = HttpURLConnectionBackend()

  def getText(url: URI): IO[String] = {
    if(url.getHost == "www.theguardian.com") {
      val path = url.getPath.tail
      println(s"path: $path")
      val uri = new URI(s"https://content.guardianapis.com${url.getPath}?api-key=${apiKey}&show-fields=body")
      val request = basicRequest.get(Uri.apply(uri))
      println(s"request: $request")
      IO.fromEither(request.send.body.left.map(new Throwable(_))).map(ArticleBody).map(_.body)
    } else IO.raiseError(new Throwable(s"URL host non-guardian: ${url.getHost}"))
  }
}

object Main extends IOApp {
  import org.http4s.Request
  val scoringService = new ScoringService(NameService, GenderService)

  implicit val encodeGender: Encoder[Gender] = new Encoder[Gender] {
    final def apply(g: Gender): Json = Json.fromString(g.toString)
  }

  implicit val encodeName: Encoder[Name] = new Encoder[Name] {
    final def apply(n: Name): Json = Json.fromString(n.name)
  }

  implicit val encodeScore: Encoder[Score] = new Encoder[Score] {
    final def apply(s: Score): Json = Json.fromFloat(s.scoreVal).getOrElse(Json.fromString("n/a"))
  }

  implicit val encodeScoreResult: Encoder[ScoreResult] = Encoder
    .forProduct2("score", "entities")(sr => (sr.score, sr.genderResults))

  //TODO: add GET endpoint for testing

  val service: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "score" =>
      println(s"req: ${req}")
      EntityDecoder.decodeText(req).flatMap{ b =>
        scoringService.score(b).flatMap { s =>
          val scoreJson = s.asJson
          Ok(s"$scoreJson")
        }.handleErrorWith{ e =>
          println(s"error: $e, message: ${e.toString}")
          IO.pure(new Response(status = Status.InternalServerError))
        }
      }
    case req @ GET -> Root / "healthcheck" =>
      Ok(s"Running")
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(9000, "0.0.0.0")
      .withHttpApp(service)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
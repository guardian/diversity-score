package lib

import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import cats.effect.IO
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain, EC2ContainerCredentialsProviderWrapper, EnvironmentVariableCredentialsProvider, SystemPropertiesCredentialsProvider}
import com.amazonaws.regions.Regions
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder
import com.amazonaws.services.comprehend.model.{BatchDetectEntitiesRequest, DetectEntitiesRequest, EntityType}

import scala.jdk.CollectionConverters._
import cats.implicits._
import cats.instances.byte

import scala.collection.mutable.ListBuffer

case class Name(name: String) extends AnyVal

case class NamesServiceError(underlying: Throwable) extends Exception {
  override def toString: String = underlying.toString
}

trait NameService {
  def names(s: String): IO[Set[Name]]
}

object NameService extends NameService {

  lazy val credentialsChain = new AWSCredentialsProviderChain(new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider,
    new SystemPropertiesCredentialsProvider,
    new ProfileCredentialsProvider("targeting"),
    new EC2ContainerCredentialsProviderWrapper)
  )

  lazy val comprehend = AmazonComprehendClientBuilder.standard()
    .withCredentials(credentialsChain)
    .withRegion(Regions.EU_WEST_1)
    .build()

  //Comprehend max length of request text allowed is 5000 bytes
//TODO: is there a more functional way of doing this?
  def batchText(text: String) = {
    val in = new ByteArrayInputStream(text.getBytes(Charset.defaultCharset()))
    val buffer = new Array[Byte](5000)
    val batchedText = ListBuffer.empty[String]

    while (in.read(buffer) > 0) {
      val textChunk = new String(buffer, Charset.defaultCharset())
      batchedText += textChunk
    }

    batchedText.toList
  }

  //TODO: handle this error
  def names(s: String): IO[Set[Name]] = IO {
    comprehend
      .batchDetectEntities(new BatchDetectEntitiesRequest().withTextList(batchText(s):_*).withLanguageCode("en"))
  }.map{ res =>
    val entities = res.getResultList.asScala.flatMap(r => r.getEntities.asScala)
      .toList
      .filter(e => e.getType == EntityType.PERSON.toString && e.getScore > 0.9)
    println(s"entities: $entities")
    val names = entities
    .map(entity => Name(entity.getText.split(" ").head))
    .toSet
    println(s"names: $names")
    names
  }.handleErrorWith(err => IO.raiseError(NamesServiceError(err)))
}

package lib

import lib.genderService.{NameWithGender, Male}
import lib.scoringService.{Score, ScoreResult}
import org.scalatest.{FlatSpec, Matchers}
import cats.data.NonEmptyList

class ScoreTest extends FlatSpec with Matchers {
  it should "return json with a score object and value when a score can be determined" in {
    val scoreResult = ScoreResult(Score(1), NonEmptyList(NameWithGender("Thomas", Some(Male))))

  }
}
//
//  object MockNameService extends NameService {
//    def names(text: String): Set[NameService] = {
//      val names = gendersForName.keySet.map(_.name)
//      val words = text.split(" ").toSet
//      words.intersect(names).map(Name)
//    }
//  }
//
//  private val gendersForName = Map(
//  Name("Amy") -> NonMale,
//  Name("Thomas") -> Male,
//  Name("Jon") -> Male,
//  Name("Patricio") -> Male
//  )
//
//  object MockGenderService extends GenderService {
//    def genders(name: NameService): Option[Gender] = {
//      gendersForName.get(name)
//    }
//  }
//
//  val scoring = new ScoringService(MockNameService, MockGenderService)
//
//  it should "return a relevant exception if score is called for an empty string" in {
//
//    scoring.score("") shouldBe Left(EmptyInputException)
//  }
//
//  //what do we expect to give the lookup, and what would we expect to get back?
//  it should "return a relevant exception if the string does not contain a recognised name" in {
//    scoring.score("hello") shouldBe Left(NoNameException)
//  }
//
//  it should "return a score of zero if only one gender is present" in {
//    scoring.score("Amy") shouldBe Right(Score(0f))
//  }
//
//  it should "return a score of 0.5 if we have 3 male, 1 non-male" in {
//    scoring.score("Thomas Jon Patricio Amy") shouldBe Right(Score(0.5f))
//  }
//
//  it should "return a score of 1.0 if we have gender parity" in {
//    scoring.score("Thomas Thomas Amy Amy") shouldBe Right(Score(1.0f))
//  }
//
//  it should "return a score of 1.0 if we have gender parity, with unequal occurrences of a name" in {
//    scoring.score("Thomas Thomas Thomas Amy") shouldBe Right(Score(1.0f))
//  }
//
//
//
//}

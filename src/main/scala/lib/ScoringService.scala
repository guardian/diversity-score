package lib

import cats.Monoid
import cats.effect.IO
import cats.implicits._

case class Score(scoreVal: Float) extends AnyVal
case class ScoreResult(score: Score, genderResults: List[GenderResult])

object ScoreException {
  object EmptyInputException extends Exception
  object NoNameException extends Exception
}

class ScoringService(nameService: NameService, genderService: GenderService) {

  // use scalacheck to check returned score between zero and one
  // import cats. Make Population extend Monoid. Use a fold like method on Monoid to reduce the score method.
  // how do you use refine? Is this more pain than it's worth?

  case class Population(male: Int, nonMale: Int)

  object Population {
    def score(population: Population): Score = {
      Score(1 - Math.abs(population.male - population.nonMale).toFloat/(population.male + population.nonMale))
    }

    def apply(gender: Gender): Population = if(gender == Male) Population(1, 0) else Population(0, 1)

    implicit val populationMonoid: Monoid[Population] = new Monoid[Population] {
      def combine(p1: Population, p2: Population): Population = Population(p1.male + p2.male, p1.nonMale + p2.nonMale)
      def empty = Population(0, 0)
    }
  }

  private def score(genders: List[Gender]): Score = {
    Population.score(Monoid.combineAll(genders.map(Population.apply(_))))
  }

  case object NoGendersException extends Exception
  def nonEmpty(text: String): IO[String] = if(text.isEmpty) IO.raiseError(ScoreException.EmptyInputException) else IO.pure(text)
  def nonEmptyGenders(genders: List[Gender]): IO[List[Gender]] = if(genders.isEmpty) IO.raiseError(NoGendersException) else IO.pure(genders)

  def score(text: String): IO[ScoreResult] = {
    for {
      nonEmptyText <- nonEmpty(text)
      _ = println(s"nonEmptyText: $nonEmptyText")
      names <- nameService.names(nonEmptyText)
      _ = println(s"names: $names")
      genders <- names.map(genderService.genders).toList.sequence
      _ = println(s"genders: $genders")
      nonEmptyGenders <- nonEmptyGenders(genders.flatMap(_.gender))
    } yield ScoreResult(score(nonEmptyGenders), genders)
  }

}

package lib.scoringService

import cats.Monoid
import cats.data.{NonEmptyChain, NonEmptyList}
import cats.effect.IO
import cats.implicits._
import lib.genderService.{Gender, NameWithGender, GenderService, Male}
import lib.nameService.NameService
import lib.scoringService.ScoreException.NoNamesException

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

  private def score(genders: NonEmptyList[Gender]): Score = {
    Population.score(Monoid.combineAll(genders.toList.map(Population.apply(_))))
  }

  case object NoGendersException extends Exception
  def nonEmpty(text: String): IO[String] = if(text.isEmpty) IO.raiseError(ScoreException.EmptyInputException) else IO.pure(text)
  //TODO: generalise to one function, paramaterising the Error
  def namesWithGenders(namesWithGenders: List[NameWithGender]): IO[NonEmptyList[NameWithGender]] =
    NonEmptyList.fromList(namesWithGenders).map(IO.apply(_)).getOrElse(IO.raiseError(NoNamesException))
  def nonEmptyGenders(genders: List[Gender]): IO[NonEmptyList[Gender]] = NonEmptyList.fromList(genders).map(IO(_)).getOrElse(IO.raiseError(NoGendersException))

  def score(text: String): IO[ScoreResult] = for {
    nonEmptyText <- nonEmpty(text)
    names <- nameService.names(nonEmptyText)
    namesWithMaybeGenders <- names.map(n => genderService.genders(n.name)).toList.sequence
    namesWithGenders <- namesWithGenders(namesWithMaybeGenders)
    genders <- nonEmptyGenders(namesWithGenders.map(_.gender).toList.flatten)
  } yield ScoreResult(score(genders), namesWithGenders)

}

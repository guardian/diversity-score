package lib.scoringService

import lib.genderService.NameWithGender
import cats.data.NonEmptyList

case class Score(scoreVal: Float) extends AnyVal

case class ScoreResult(score: Score, genderResults: NonEmptyList[NameWithGender])

object ScoreException {
  object EmptyInputException extends Exception
  object NoNamesException extends Exception
}
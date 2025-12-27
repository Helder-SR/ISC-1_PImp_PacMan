package Backend.Entities

import Backend.Entities.Directions.Directions

class Player extends Entity {
  protected var score: Int = 0;

  def Score: Int = score;

  def ChangeDirection(direction: Directions): Unit = {
    this.direction = direction
  }
  def addScore(points: Int): Unit = {
    score += points;
  }
  def resetScore(): Unit = {
    score = 0;
  }
}

package Backend.Entities

import Backend.Entities.Directions.Directions

class Player extends Entity {
  protected var score: Int = 0;
  protected var lives: Int = 3

  def Score: Int = score;
  def Lives: Int = lives

  def ChangeDirection(direction: Directions): Unit = {
    this.direction = direction
  }
  def addScore(points: Int): Unit = {
    score += points;
  }
  def resetScore(): Unit = {
    score = 0;
  }
  def loseLife(): Unit = {
    lives -= 1
    println(s"Lives: $lives")
  }
  override def toString: String = "Player"
}

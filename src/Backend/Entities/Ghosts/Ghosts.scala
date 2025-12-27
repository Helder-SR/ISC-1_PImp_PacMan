package Backend.Entities.Ghosts

import Backend.Cases.Case
import Backend.Entities.Entity

import java.awt.Color

abstract class Ghosts(val MainColor: Color) extends Entity {
  protected var isVulnerable: Boolean = false;
  protected var isBlinking: Boolean = false;
  protected var isAlive: Boolean = false;

  def IsVulnerable = isVulnerable;
  def IsBlinking = isBlinking;
  def IsAlive = isAlive;

  def kill: Unit = { isAlive = false; }
  def revive: Unit = { isAlive = true; }

  def takeDecision(map: Array[Array[Case]]): Unit;
}
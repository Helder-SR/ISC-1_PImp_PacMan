package Backend.Entities.Ghosts

import Backend.Cases.{CaseType, RoadCase}
import Backend.Entities.Directions.Directions
import Backend.Entities.{Directions, Entity}
import Backend.Logical

import java.awt.Color
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.util.Random

abstract class Ghosts(val MainColor: Color) extends Entity {
  direction = Directions.Right

  protected var isVulnerable: Boolean = false;
  protected var isBlinking: Boolean = false;
  protected var isAlive: Boolean = false;

  def IsVulnerable = isVulnerable;
  def IsBlinking = isBlinking;
  def IsAlive = isAlive;

  def kill: Unit = { isAlive = false; }
  def revive: Unit = { isAlive = true; }

  private val ex: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  private val makeBlinkTask = new Runnable {
    override def run(): Unit = {
      if(IsVulnerable && !IsBlinking) {
          isBlinking = true
          return;
      }
      isVulnerable = false;
      isBlinking = false;
      if(!ex.isShutdown) ex.shutdown();
      println("makeBlinkTaskEnd")
    }
  }

  def makeVulnerable(): Unit = {
    // TO DO : Dynamic calcul timing of vulnerability
    isVulnerable = true;
    ex.scheduleAtFixedRate(makeBlinkTask, 5, 5, TimeUnit.SECONDS)
  }

  def resetVulnerability(): Unit = {
    isVulnerable = false;
    isBlinking = false;
  }

  protected var isLastCaseDoor = false;
  def takeDecision(logical: Logical): Unit = {
    // DEFAULT MOVEMENT (RANDOM)

    println(s"$this taking decision...")
    if(!logical.IsPointInTheMap(x, y)) return;
    val currentCase = logical.Map(y)(x);
    val isLastADoor = isLastCaseDoor
    isLastCaseDoor = currentCase.CaseType == CaseType.Door
    if(isLastCaseDoor) return;
    if(!currentCase.isInstanceOf[RoadCase]) return;
    val currentRoad = currentCase.asInstanceOf[RoadCase];
    if(!isLastADoor && !currentRoad.IsIntersection || currentRoad.isGhostsSpawn) return;

    var dir: Directions = Directions.Right
    var ny: Int = -1
    var nx: Int = -1
    val (dx, dy) = Directions.getDeltaByDirection(Direction)
    val (lx, ly) = (x-dx, y-dy)
    do {
      dir = Directions(Random.nextInt(Directions.maxId))
      val (deltaX, deltaY) = Directions.getDeltaByDirection(dir);
      ny = y + deltaY;
      nx = x + deltaX;
    } while (
      !logical.IsPointInTheMap(nx, ny) ||
        (lx == nx && ly == ny) ||
        logical.Map(ny)(nx).CaseType != CaseType.Road
    )
    direction = dir;

    println(s"$this taked decision $direction")
  };
}
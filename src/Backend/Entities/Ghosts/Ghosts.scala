package Backend.Entities.Ghosts

import Backend.Cases.{Case, CaseType, RoadCase}
import Backend.Entities.Directions.Directions
import Backend.Entities.{Directions, Entity}
import Backend.Logical

import java.awt.Color
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

abstract class Ghosts(val MainColor: Color) extends Entity {
  direction = Directions.Up

  private var isVulnerable: Boolean = false;
  private var isBlinking: Boolean = false;

  private var wayToHome: Array[Array[Int]] = Array.empty
  private var isWayToHomeCalculated = false;

  def IsVulnerable = isVulnerable;
  def IsBlinking = isBlinking;

  final override def revive: Unit = {
    super.revive
    isWayToHomeCalculated = false;
    resetVulnerability()
  }

  def makeBlinking(): Unit = {
    if(IsVulnerable)
      isBlinking = true
  }

  def makeVulnerable(): Unit = {
    isVulnerable = true;
    isBlinking = false;
  }

  def resetVulnerability(): Unit = {
    isVulnerable = false;
    isBlinking = false;
  }

  protected var isLastCaseDoor = false;
  final def takeDecision(logical: Logical): Unit = {
    if(!IsAlive && !checkRevive(logical)) goHome(logical)
    else computeAI(logical)
  };

  protected def computeAI(logical: Logical): Unit = {
    // DEFAULT MOVEMENT (RANDOM)
    println(s"$this taking decision...")
    if(!logical.IsPointInTheMap(x, y)) return;
    val currentCase = logical.Map(y)(x);
    val isLastADoor = isLastCaseDoor
    isLastCaseDoor = currentCase.CaseType == CaseType.Door
    if(isLastCaseDoor) return;
    if(!currentCase.isInstanceOf[RoadCase]) return;
    val currentRoad = currentCase.asInstanceOf[RoadCase];
    if(!isLastADoor && !currentRoad.IsIntersection && !currentRoad.isGhostsSpawn) return;

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
      !(
        logical.Map(ny)(nx).CaseType == CaseType.Road ||
        logical.Map(ny)(nx).CaseType == CaseType.Door &&
        currentRoad.isGhostsSpawn
      )
    )
    direction = dir;

    println(s"$this taked decision $direction")
  };

  def checkRevive(logical: Logical): Boolean = {
    logical.Map(Y)(X) match {
      case roadCase: RoadCase if roadCase.isGhostsSpawn =>
        revive
        true
      case _ =>
        false
    }
  }

  def goHome(logical: Logical): Unit = {
    if(!isWayToHomeCalculated) foundPathToHome(logical)
    val (current, up, down, left, right) = (
      wayToHome(Y)(X),
      wayToHome(Y-1)(X),
      wayToHome(Y+1)(X),
      wayToHome(Y)(X-1),
      wayToHome(Y)(X+1)
    )

    direction = if(up < current) Directions.Up
      else if (down < current) Directions.Down
      else if (left < current) Directions.Left
      else if (right < current) Directions.Right
      else direction
  }

  def foundPathToHome(logical: Logical): Unit = {
    var isPathFound = false
    val (mapHeight, mapWidth) = (logical.Map.length, logical.Map(0).length);
    wayToHome  = Array.fill(mapHeight, mapWidth)(500)
    var lastCases: ArrayBuffer[Case] = ArrayBuffer.empty;
    var idx = 0;
    logical.GhostsSpawn.foreach(s => {
      wayToHome(s.Y)(s.X) = idx;
      lastCases += s;
    })

    do {
      idx += 1;
      val lstCases = lastCases.clone()
      lastCases = ArrayBuffer.empty;
      lstCases.foreach(c => {
        for(i <- -1 to 1 by 2) {
          // vertical
          val (vx, vy) = (c.X, c.Y + i)
          if(
            vy >= 0 && vy < mapHeight &&
            vx >= 0 && vx < mapWidth &&
              wayToHome(vy)(vx) > idx &&
            (
              logical.Map(vy)(vx).CaseType == CaseType.Road ||
              logical.Map(vy)(vx).CaseType == CaseType.Door
            )
          ) {
            wayToHome(vy)(vx) = idx
            lastCases += logical.Map(vy)(vx)
            if(vy == Y && vx == X) {
              isPathFound = true
            }
          }
          // horizontal
          val (hx, hy) = (c.X+i, c.Y)
          if(
            hy >= 0 && hy < mapHeight &&
            hx >= 0 && hx < mapWidth &&
            wayToHome(hy)(hx) > idx &&
            (
              logical.Map(hy)(hx).CaseType == CaseType.Road ||
              logical.Map(hy)(hx).CaseType == CaseType.Door
            )
          ) {
            wayToHome(hy)(hx) = idx
            lastCases += logical.Map(hy)(hx)
            if(hy == Y && hx == X) {
              isPathFound = true
            }
          }
        }
      })
    }while(!isPathFound && idx < (mapHeight * mapWidth))
    isWayToHomeCalculated=true
  }

}
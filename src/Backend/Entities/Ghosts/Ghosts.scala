package Backend.Entities.Ghosts

import Backend.Cases.{Case, CaseType, RoadCase}
import Backend.Entities.Directions.Directions
import Backend.Entities.{Directions, Entity}
import Backend.Logical

import java.awt.Color
import scala.collection.mutable.ArrayBuffer
import scala.math.{pow, sqrt}
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

  // Will get the target case based on the ghost behavior
  def getTarget(logical: Logical): (Int, Int)

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

    val (targetX, targetY) = getTarget(logical)
    val distanceMap = createDistanceMap(logical, targetX, targetY)

    var bestDirection: Directions = direction
    var minDistance: Double = 999
    var maxDistance: Double = -1

    // Check which direction is the best based on the closest distance between the ghost & the target point
    for(d <- Directions.values){
      val (dx, dy) = Directions.getDeltaByDirection(d)
      val nextX = x + dx
      val nextY = y + dy

      if(logical.IsPointInTheMap(nextX, nextY)){

        val nextCase = logical.Map(nextY)(nextX)
        val isWalkable = nextCase.CaseType == CaseType.Road || (nextCase.CaseType == CaseType.Door && currentRoad.isGhostsSpawn)
        if (isWalkable){
          var dist = distanceMap(nextY)(nextX)
          if(IsVulnerable){
            if (dist == 999) dist = -1
            if (dist > maxDistance){
              maxDistance = dist
              bestDirection = d
            }
          } else {
            if (dist < minDistance){
              minDistance = dist
              bestDirection = d
            }
          }
        }
      }
    }

    println(s"$this taked decision $direction")
    direction = bestDirection
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

  // Target at 0, walls at 999. Ghost choose the direction where the case is the lowest of its neighbors
  private def createDistanceMap(logical: Logical, targetX: Int, targetY: Int): Array[Array[Int]] = {
    val height = logical.Map.length
    val width = logical.Map(0).length
    val distances = Array.fill(height, width)(999)

    distances(targetY)(targetX) = 0

    var hasChanged = true
    while (hasChanged) {
      hasChanged = false

      for (y <- 0 until height) {
        for (x <- 0 until width) {
          if (logical.Map(y)(x).CaseType != CaseType.Wall) {
            val currentVal = distances(y)(x)
            var minNeighbor = 999

            for (dir <- Directions.values) {
              val (dx, dy) = Directions.getDeltaByDirection(dir)
              val nx = x + dx
              val ny = y + dy

              if (logical.IsPointInTheMap(nx, ny)) {
                val neighborVal = distances(ny)(nx)
                if (neighborVal < minNeighbor) {
                  minNeighbor = neighborVal
                }
              }
            }

            if (minNeighbor + 1 < currentVal) {
              distances(y)(x) = minNeighbor + 1
              hasChanged = true // so neighbors can have a new value
            }
          }
        }
      }
    }

    return distances
  }

  protected def randomTarget(logical: Logical): (Int, Int) = {
    var x = 0
    var y = 0
    var attempts = 0

    do {
      x = Random.nextInt(logical.Map(0).length)
      y = Random.nextInt(logical.Map.length)
      attempts += 1
    } while (!logical.IsPointInTheMap(x, y) && attempts < 10)

    (x, y)
  }
}
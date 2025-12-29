package Backend

import Backend.Cases.{Case, CaseType, DoorCase, EmptyCase, Items, RoadCase, WallCase}
import Backend.Entities.Directions.Directions
import Backend.Entities.Ghosts.{Blinky, Clyde, Ghosts, Inky, Pinky}
import Backend.Entities.{Directions, Entity, Player}
import Backend.global.Levels

import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class Logical {
  private var map: Array[Array[Case]] = Array.empty;
  private val player = new Player;
  private val ghosts = Array(
    Blinky.INSTANCE,
    Clyde.INSTANCE,
    Inky.INSTANCE,
    Pinky.INSTANCE
  );

  private var isGamePlaying = false;

  private var playerSpawn: RoadCase = null;
  private var ghostsSpawn: Array[RoadCase] = Array.empty;
  private var itemsSpawn: RoadCase = null;

  private val subscriptions: ArrayBuffer[Logical => Unit] = ArrayBuffer.empty;
  private val ex: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  private val task = new Runnable {
    override def run(): Unit = {
      notifyListener()
    }
  }
  private val infiniteNotifier = ex.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS)


  ghosts.foreach(g => subscribeCycle(g.takeDecision))

  def Map = map;
  def Player = player;
  def Ghosts: Array[Ghosts] = ghosts;

  def IsGamePlaying = isGamePlaying;

  def subscribeCycle(callback: Logical => Unit): Unit = {
    subscriptions += callback;
  }

  def notifyListener(): Unit = {
    for(s <- subscriptions) s(this)
  }

  def startGame() = {
    isGamePlaying = true;
  }

  def pauseGame() = {
    isGamePlaying = false;
  }

  def LoadLevel(map: Array[String]): Unit = {
    this.map = Array.ofDim(map.length, map(0).length);

    for((l, y) <- map.zipWithIndex) {
      for((c, x) <- l.zipWithIndex) {
        this.map(y)(x) = c match {
          case ' ' => new EmptyCase(x, y);
          case 'w' => new WallCase(x, y);
          case 'r' => new RoadCase(x, y, isCaseIntersection(map, x, y));
          case 'd' => {
            val d = new RoadCase(x, y, isCaseIntersection(map, x, y));
            d.Item = Items.PacDot;
            d
          };
          case 'D' => {
            val D = new RoadCase(x, y, isCaseIntersection(map, x, y));
            D.Item = Items.PowerPellet;
            D
          };
          case 'i' => {
            val i = new RoadCase(x, y, isCaseIntersection(map, x, y))
            itemsSpawn = i;
            i
          };
          case 'v' => new DoorCase(x, y);
          case 'P' => {
            val P = new RoadCase(x, y, isCaseIntersection(map, x, y));
            playerSpawn = P;
            P
          };
          case 'G' => {
            val G = new RoadCase(x, y, isCaseIntersection(map, x, y))
            G.isGhostsSpawn = true;
            ghostsSpawn :+= G;
            G
          };
        }
      }
    }

    if(player == null) throw new Exception("No spawn for the player in the level!")
    playerSpawn.Entities += player;
    player.definePosition(playerSpawn.X, playerSpawn.Y)

    if(ghostsSpawn.length <= 0) throw new Exception("No spawn for the ghosts in the level !")
    for(g <- ghosts) {
      val spw = ghostsSpawn(Random.nextInt(ghostsSpawn.length));
      spw.Entities += g;
      g.definePosition(spw.X, spw.Y)
    }
  }

  def ChangePlayerDirection(direction: Directions): Unit = {
    val (deltaX, deltaY) = Directions.getDeltaByDirection(direction);
    val (nx, ny) = CorrectNextPoint(player.X + deltaX, player.Y + deltaY)
    if(!IsPointInTheMap(nx, ny)) {
      println("Cannot change direction for nowhere")
      return;
    }
    val nextCase = map(ny)(nx)
    if(nextCase.CaseType == CaseType.Road) player.ChangeDirection(direction)
    else println("Cannot change direction for a wall")
  }

  def IsPointInTheMap(x: Int, y: Int): Boolean = {
    Map.length > y && y >= 0 && Map(0).length > x && x >= 0
  }

  def CorrectNextPoint(x: Int, y: Int): (Int, Int) = {
    (
      if(x >= map(0).length) 0 else if (x < 0) map(0).length-1 else x,
      if(y >= map.length) 0 else if (y < 0) map.length-1 else y
    )
  }

  private def isCaseIntersection(map: Array[String], x: Int, y: Int): Boolean = {
    var (counterX, counterY) = (0, 0);
    for(d <- -1 to 1 by 2) {
      val (nx, ny) = (x+d, y+d)
      if(map.length > ny && ny >= 0)
        counterY += (if(Levels.ROAD_CHAR.contains(map(ny)(x))) 1 else 0);
      if(map(0).length > nx && nx >= 0)
        counterX += (if(Levels.ROAD_CHAR.contains(map(y)(nx))) 1 else 0);
    }
    counterX > 0 && counterY > 0
  }

  subscriptions += calculateFrame;
  private def calculateFrame(logical: Logical): Unit = {
    moveEntity(Player)
    ghosts.foreach(g => moveEntity(g, true))
    eatCaseByPlayer()

    // LAB
    ChangePlayerDirection(Directions(Random.nextInt(Directions.maxId)));
  }

  private def moveEntity(entity: Entity, isGhosts: Boolean = false): Unit = {
    if(!isGamePlaying) return;
    val (deltaX, deltaY) = Directions.getDeltaByDirection(entity.Direction);

    var (nx, ny) = CorrectNextPoint(entity.X + deltaX, entity.Y + deltaY)

    if(!IsPointInTheMap(nx, ny)) {
      println("Error, case doesn't exist on the map")
      return;
    }

    val nextCase = map(ny)(nx)
    val currentCase = map(entity.Y)(entity.X);

    if(nextCase.CaseType == CaseType.Road || (isGhosts && nextCase.CaseType == CaseType.Door)) {
      val nextRoad = nextCase;
      currentCase.Entities.remove(currentCase.Entities.indexOf(entity));
      nextRoad.Entities += entity;
      nextRoad.definePositionOf(entity)
    } else {
      println(s"$entity can't go forward, next case not a road")
    }
  }

  private def eatCaseByPlayer() {
    if(!isGamePlaying) return;
    val currentCase = map(player.Y)(player.X);
    if(!currentCase.isInstanceOf[RoadCase]) return;
    val currentRoad = currentCase.asInstanceOf[RoadCase]
    player.addScore(Items.GetValue(currentRoad.Item));
    if(currentRoad.Item == Items.PowerPellet) makeGhostsVulnarable()
    currentRoad.Item = Items.None;
  }

  private def makeGhostsVulnarable(): Unit = {
    ghosts.foreach(g => g.makeVulnerable())
  }
}

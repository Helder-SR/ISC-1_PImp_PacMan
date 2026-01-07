package Backend

import Backend.Cases.{Case, CaseType, DoorCase, EmptyCase, Items, RoadCase, WallCase}
import Backend.Entities.Directions.Directions
import Backend.Entities.Ghosts.{Blinky, Clyde, Ghosts, Inky, Pinky}
import Backend.Entities.{Directions, Entity, Player}
import Backend.global.Levels

import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class Logical {
  val GAME_SPEED_FRAME_MS = 100

  private var map: Array[Array[Case]] = Array.empty;
  private val player = new Player;
  private val ghosts = Array(
    Blinky.INSTANCE,
    Clyde.INSTANCE,
    Inky.INSTANCE,
    Pinky.INSTANCE
  );

  private var isGamePlaying = false;

  private var isDirectionWaiting = false;
  private var nextDirection = Directions.Up;

  private var playerSpawn: RoadCase = null;
  private var ghostsSpawn: Array[RoadCase] = Array.empty;
  private var itemsSpawn: RoadCase = null;

  private val subscriptions: ArrayBuffer[Logical => Unit] = ArrayBuffer.empty;
  private val mainLoopThreadExecutor: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  private val task = new Runnable {
    override def run(): Unit = {
      notifyListener()
    }
  }
  private val resetThreadExecutor: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  private val resetPositionTask = new Runnable {
    override def run(): Unit = {
      ResetPosition()
      startGame()
    }
  }
  mainLoopThreadExecutor.scheduleAtFixedRate(task, GAME_SPEED_FRAME_MS, GAME_SPEED_FRAME_MS, TimeUnit.MILLISECONDS)

  ghosts.foreach(g => subscribeCycle(g.takeDecision))

  def Map = map;
  def Player = player;
  def Ghosts: Array[Ghosts] = ghosts;

  def GhostsSpawn: Array[RoadCase] = ghostsSpawn;

  def IsGamePlaying = isGamePlaying;

  def subscribeCycle(callback: Logical => Unit): Unit = {
    subscriptions += callback;
  }

  def notifyListener(): Unit = {
    for(s <- subscriptions) s(this)
  }

  def startGame() = resumeGame()

  def resumeGame(): Unit = {
    isGamePlaying = true;
  }

  def pauseGame() = {
    isGamePlaying = false;
  }

  def ResetPosition(): Unit = {
    resetEntityPosition(Player, playerSpawn)
    Player.revive

    ghosts.foreach(g => {
      resetEntityPosition(g, ghostsSpawn(Random.nextInt(ghostsSpawn.length)))
      g.revive
    })

    if(itemsSpawn != null) itemsSpawn.Item = Items.None
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
    nextDirection = direction
    isDirectionWaiting = true;
    val (deltaX, deltaY) = Directions.getDeltaByDirection(direction);
    val (nx, ny) = CorrectPoint(player.X + deltaX, player.Y + deltaY)
    if(!IsPointInTheMap(nx, ny)) {
      println("Cannot change direction for nowhere")
      return;
    }
    val nextCase = map(ny)(nx)
    if(nextCase.CaseType == CaseType.Road) {
      player.ChangeDirection(direction)
      isDirectionWaiting = false;
    }
    else println("Cannot change direction for a wall")
  }

  def IsPointInTheMap(x: Int, y: Int): Boolean = {
    Map.length > y && y >= 0 && Map(0).length > x && x >= 0
  }

  def CorrectPoint(x: Int, y: Int): (Int, Int) = {
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
    if (isDirectionWaiting) ChangePlayerDirection(nextDirection);
    moveEntity(Player)
    ghosts.foreach(g => moveEntity(g, true))
    eatCaseByPlayer()
    checkColision()

    if (!player.IsAlive) {
      pauseGame();
      mainLoopThreadExecutor.schedule(resetPositionTask, 5, TimeUnit.SECONDS)
    }
    // Game flow
    calculateItemSpawn()
    // LAB
    ChangePlayerDirection(Directions(Random.nextInt(Directions.maxId)));
  }

  private def moveEntity(entity: Entity, isGhosts: Boolean = false): Unit = {
    if(!isGamePlaying) return;
    val (deltaX, deltaY) = Directions.getDeltaByDirection(entity.Direction);

    val (nx, ny) = CorrectPoint(entity.X + deltaX, entity.Y + deltaY)

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
    else if(currentRoad.Item == Items.PacDot) pacDotEatenCounter += 1
    else if(currentRoad.Item.id >= Items.Cherry.id && currentRoad.Item.id <= Items.Key.id) itemEatenEvent()
    currentRoad.Item = Items.None;
  }

  private var ghostsEated = 0;

  private val makeBlinkThreadExecutor: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  private var currentMakeBlinkTask: Option[ScheduledFuture[_]] = None;
  private val makeBlinkTask = new Runnable {
    override def run(): Unit = {
      ghosts.foreach(g => g.makeBlinking())
    }
  }
  private val resetVulnerableThreadExecutor: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  private var currentResetVulnerabilityTask: Option[ScheduledFuture[_]] = None;
  private val resetVulnerabilityTask = new Runnable {
    override def run(): Unit = {
      ghosts.foreach(g => g.resetVulnerability())
      ghostsEated = 0;
    }
  }

  private def makeGhostsVulnarable(): Unit = {
    if(currentResetVulnerabilityTask.isDefined) currentResetVulnerabilityTask.get.cancel(true)
    if(currentMakeBlinkTask.isDefined) currentMakeBlinkTask.get.cancel(true)
    ghosts.foreach(g => g.makeVulnerable())
    currentMakeBlinkTask = Some(makeBlinkThreadExecutor.schedule(makeBlinkTask, 5, TimeUnit.SECONDS))
    currentResetVulnerabilityTask = Some(resetVulnerableThreadExecutor.schedule(resetVulnerabilityTask, 10, TimeUnit.SECONDS))
  }

  private def checkColision(): Unit = {
    val (cx, cy) = (Player.X, Player.Y)
    val (dx, dy) = Directions.getDeltaByDirection(Player.Direction)
    val (lx, ly) = CorrectPoint(Player.X - dx, Player.Y - dy)

    Ghosts.foreach(g => {
      if(cx == g.X && cy == g.Y && g.IsAlive) {
        if(g.IsVulnerable) killGhosts(g)
        else Player.kill;
      } else if (lx == g.X && ly == g.Y) {
        val (dgx, dgy) = Directions.getDeltaByDirection(g.Direction)
        val (lgx, lgy) = CorrectPoint(g.X - dgx, g.Y - dgy)
        if(lgx == cx && lgy == cy && g.IsAlive) {
          if(g.IsVulnerable) killGhosts(g);
          else Player.kill;
        }
      }
    })
  }

  private def killGhosts(ghosts: Ghosts): Unit = {
    ghosts.kill
    ghostsEated += 1;
    player.addScore(math.pow(2, ghostsEated).toInt * 100)
  }

  private def resetEntityPosition(entity: Entity, cse: Case): Unit = {
    if(IsPointInTheMap(entity.X, entity.Y)) {
      val lc = map(entity.Y)(entity.X)
      if(lc.Entities.contains(entity)) {
        lc.Entities.remove(lc.Entities.indexOf(entity))
      }
    }
    cse.Entities += entity;
    cse.definePositionOf(entity)
  }

  // Items section
  private var pacDotEatenCounter = 0;
  private var nextItem = Items.Cherry;

  private def calculateItemSpawn(): Unit = {
    if(itemsSpawn == null) return;
    if(itemsSpawn.Item == Items.None && (pacDotEatenCounter == 70 || pacDotEatenCounter == 170)){
      itemsSpawn.Item = nextItem;
    }
  }

  private def itemEatenEvent() {
    val nxtItm = nextItem.id + 1
    if(nxtItm >= Items.maxId) return;
    nextItem = Items(nxtItm)
  }
}

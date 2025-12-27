package Backend

import Backend.Cases.{Case, DoorCase, EmptyCase, Items, RoadCase, WallCase}
import Backend.Entities.Ghosts.{Blinky, Clyde, Ghosts, Inky, Pinky}
import Backend.Entities.Player
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

  def Map = map;
  def Player = player;
  def Ghosts: Array[Ghosts] = ghosts;

  def subscribeCycle(callback: Logical => Unit): Unit = {
    subscriptions += callback;
  }

  def notifyListener(): Unit = {
    for(s <- subscriptions) s(this)
  }

  def LoadLevel(map: Array[String]): Unit = {
    this.map = Array.ofDim(map.length, map(0).length);

    for((l, y) <- map.zipWithIndex) {
      for((c, x) <- l.zipWithIndex) {
        this.map(y)(x) = c match {
          case ' ' => new EmptyCase();
          case 'w' => new WallCase();
          case 'r' => new RoadCase();
          case 'd' => {
            val d = new RoadCase();
            d.Item = Items.PacDot;
            d
          };
          case 'D' => {
            val D = new RoadCase();
            D.Item = Items.PowerPellet;
            D
          };
          case 'i' => {
            val i = new RoadCase()
            itemsSpawn = i;
            i
          };
          case 'v' => new DoorCase();
          case 'P' => {
            val P = new RoadCase();
            playerSpawn = P;
            P
          };
          case 'G' => {
            val G = new RoadCase()
            ghostsSpawn :+= G;
            G
          };
        }
      }
    }

    if(player == null) throw new Exception("No spawn for the player in the level!")
    playerSpawn.Entities += player;

    if(ghostsSpawn.length <= 0) throw new Exception("No spawn for the ghosts in the level !")
    for(g <- ghosts) {
      ghostsSpawn(Random.nextInt(ghostsSpawn.length)).Entities += g;
    }
  }
}

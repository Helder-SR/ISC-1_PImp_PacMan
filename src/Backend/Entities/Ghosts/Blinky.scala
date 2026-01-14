package Backend.Entities.Ghosts

import Backend.Logical
import Backend.global.Position

import java.awt.Color
import scala.util.Random

class Blinky extends Ghosts(Color.RED) {
  override def toString: String = "Blinky"
  var isInCycle = false;
  val scatterPath = Map(
    new Position(21, 1) -> new Position(26, 1),
    new Position(26, 1) -> new Position(26, 5),
    new Position(26, 5) -> new Position(21, 5),
    new Position(21, 5) -> new Position(21, 1),
  );
  val DEFAULT_SCATTER_TARGET = new Position(23, 1)
  var scatterTarget: Position = DEFAULT_SCATTER_TARGET;

  // Targets directly the player but has 1/2 chance to take a random direction
  override def getTarget(logical: Logical): (Int, Int) = {
    if(Mode == GhostsMode.Frightened) {
      if (Random.nextInt(2) != 0) (logical.Player.X, logical.Player.Y) else randomTarget(logical)
    }
    else if(Mode == GhostsMode.Chase) {
      if(isInCycle) {
        isInCycle = false;
        scatterTarget = DEFAULT_SCATTER_TARGET
      }
      (logical.Player.X, logical.Player.Y)
    } else {
      var res: Option[Position] = None;
      scatterPath.foreach(p => if(p._1.X == this.X && p._1.Y == this.Y) res = Option(p._2))
      if(res.isDefined) {
        isInCycle = true
        scatterTarget = res.get
      }
      (scatterTarget.X, scatterTarget.Y)
    }
  }
}

object Blinky {
  val INSTANCE = new Blinky();
}

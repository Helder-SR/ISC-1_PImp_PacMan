package Backend.Cases

import Backend.Cases.Items.Items
import Backend.Entities.Ghosts.Ghosts
import Backend.Entities.{Entity, Player}

import scala.collection.mutable.ArrayBuffer

class RoadCase(posX: Int, posY: Int, val IsIntersection: Boolean = false) extends Case(CaseType.Road, posX, posY) {
  var Item: Items = Items.None;
  var isGhostsSpawn: Boolean = false;

  override def toString: String = {
    if(!Entities.isEmpty)
      if(Entities.exists(e => e.isInstanceOf[Player])) "o";
      else if (Entities.exists(e => e.isInstanceOf[Ghosts]))
        if(Entities.exists(e => e.asInstanceOf[Ghosts].IsBlinking)) "Y"
        else if(Entities.exists(e => e.asInstanceOf[Ghosts].IsVulnerable)) "X"
        else "U"
      else "?"
    else Item match {
      case Items.PacDot       => "."
      case Items.PowerPellet  => "•"
      case Items.Cherry       => "🍒"
      case Items.Strawberry   => "🍓"
      case Items.Orange       => "🍊"
      case Items.Apple        => "🍎"
      case Items.Melon        => "🍈"
      case Items.Galaxian     => "🛸" // Le Galaxian Boss est souvent représenté ainsi
      case Items.Bell         => "🔔"
      case Items.Key          => "🔑"
      case Items.None         => " "
      case _                  => " "
    }
  };
}
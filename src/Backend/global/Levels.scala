package Backend.global

/*
* This object define different level. Every level is defined by an array of string.
* Each char correspond to a différent status of the case. Here is the definition :
*
* -   : Space is nothing (A place where no entity can go)
* - w : It's a Wall
* - r : It's an empty Road (where entities can go, but no items are present)
* - d : It's an PacDot Road
* - D : It's an PowerPellet Road
* - i : It's the Items spawn Road (Where the items like Cherry will spawn) (only one per map)
* - v : It's a Door (Where Ghosts can pass but not the player)
* - P : It's the player spawn Road (only one per map)
* - G : It's the Ghosts spawn zone Road
*
* */

object Levels {
  val ROAD_CHAR: String = "rdDiPG";
  val Level1: Array[String] = Array(
    "wwwwwwwwwwwwwwwwwwwwwwwwwwww",
    "wddddddddddddwwddddddddddddw",
    "wdwwwwdwwwwwdwwdwwwwwdwwwwdw",
    "wDwwwwdwwwwwdwwdwwwwwdwwwwDw",
    "wdwwwwdwwwwwdwwdwwwwwdwwwwdw",
    "wddddddddddddddddddddddddddw",
    "wdwwwwdwwdwwwwwwwwdwwdwwwwdw",
    "wdwwwwdwwdwwwwwwwwdwwdwwwwdw",
    "wddddddwwddddwwddddwwddddddw",
    "wwwwwwdwwwwwrwwrwwwwwdwwwwww",
    "     wdwwwwwrwwrwwwwwdw     ",
    "     wdwwrrrrrrrrrrwwdw     ",
    "     wdwwrwwvvvvwwrwwdw     ",
    "wwwwwwdwwrwwGGGGwwrwwdwwwwww",
    "rrrrrrdrrrwwGGGGwwrrrdrrrrrr",
    "wwwwwwdwwrwwGGGGwwrwwdwwwwww",
    "     wdwwrwwwwwwwwrwwdw     ",
    "     wdwwrrrrrirrrrwwdw     ",
    "     wdwwrwwwwwwwwrwwdw     ",
    "wwwwwwdwwrwwwwwwwwrwwdwwwwww",
    "wddddddddddddwwddddddddddddw",
    "wdwwwwdwwwwwdwwdwwwwwdwwwwdw",
    "wdwwwwdwwwwwdwwdwwwwwdwwwwdw",
    "wDddwwddddddddPdddddddwwddDw",
    "wwwdwwdwwdwwwwwwwwdwwdwwdwww",
    "wwwdwwdwwdwwwwwwwwdwwdwwdwww",
    "wddddddwwddddwwddddwwddddddw",
    "wdwwwwwwwwwwdwwdwwwwwwwwwwdw",
    "wdwwwwwwwwwwdwwdwwwwwwwwwwdw",
    "wddddddddddddddddddddddddddw",
    "wwwwwwwwwwwwwwwwwwwwwwwwwwww"
  )

  var LabLevel: Array[String] = Array(
    "wwwwwwwwwwwwwwwwwwwwwwwwwwww",
    "dddddddddddddddddddddddddddd",
    "wdwwwwwwwwwwwwwwwwwwwwwwwwww",
    "wdwwwwwwwwwwwwwwwwwwwwwwwwww",
    "wdwwwwwwwwwwwwwwwwwwwwwwwwww",
    "dddddddddddddiddddddddddddPd",
    "wwwwwwwwwwwwwwwwwwwwwwwwwwww",
    "DdDdGGGGDdDdDdDdDdDdDdDdDdDd"
  )
}

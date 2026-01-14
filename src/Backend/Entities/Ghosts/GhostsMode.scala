package Backend.Entities.Ghosts

object GhostsMode extends Enumeration {
  type GhostsMode = Value;
  val Chase, Scatter, Frightened = Value;
}

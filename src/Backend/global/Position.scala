package Backend.global

class Position {
  protected var x: Int = -1;
  protected var y: Int = -1;

  protected def definePosition(x: Int, y: Int): Unit = {
    this.x = x;
    this.y = y;
  }

  def definePositionOf(pos: Position) = {
    pos.definePosition(x, y);
  }

  def X: Int = x
  def Y: Int = y;
}

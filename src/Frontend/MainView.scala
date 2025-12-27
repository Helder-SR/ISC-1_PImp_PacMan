package Frontend

import Backend.Logical

class MainView(private val Logical: Logical) {
  // TO DO : User interface of the game
  Logical.subscribeCycle(refreshUserInterface)

  def refreshUserInterface(logical: Logical): Unit = {
    print(logical.Map.map(l => l.mkString).mkString("\n"))
  }
}

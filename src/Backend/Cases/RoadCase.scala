package Backend.Cases

import Backend.Cases.Items.Items
import Backend.Entities.Entity

class RoadCase extends Case(CaseType.Road) {
  var Item: Items = Items.PacDot;
  var Entities: Array[Entity] = Array.empty;
}

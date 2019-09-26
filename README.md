# Traffic-flow-Cellular-Automata
An application displaying a traffic model based on cellular automata

   


<img src="images/How.gif" width="500" title="How it looks like">




## <h2>Cellular automata</h2>
  
  
[Cellular automata](https://en.wikipedia.org/wiki/Cellular_automaton) was the main tools for modeling.
A cellular automaton consists of a regular grid of cells, each in one of a finite number of states.

In my case the grid of cells contains such states as CeLLType, directions, movePermision, velocities:
States = **[cellType, directions, movePermision, velocities]**

CellType is what kind of cell it represents. It can be *wall, traffic light, road, car*. **Wall** is a a state of cells which a car can't go through.
**Road** is a state of cells which a car can move. **Car** CellType for a car, **traffic light** for a traffic light.

<img src="images/CellType.jpg" width="300">

**Directions** show the way a car can move. I did it with vectors where the first index is y-coordinate, the second is x-coordinate -> (y,x). The reference point is in the top right corner, y-axis is directed to down, x-axis is directed to right. 
<img src="images/ReferencePoint.JPG" width=300>|<img src="images/Directions.jpg" width="300">



Lior Shtaimberg 313418469 ליאור שטיימברג
Dor Wolfgor דור וולפגור

Overview

```
The Program, as expected, creates and handles a target graph that a task can run on
The Menu Options are classes in the ui module that use the engine methods to execute tha action and the ui methods to log the results or ask for user input.
```

The UI module handles:

```
    1. all the fileHandling using the `FileHandler` util class
    2. the menu showing and logic of choosing and executing an action using the `Menu` Class and all the Options classes that implement the Option Interface
    3. the console and user interfacing using the UI utils class
```

The Engine module handles:

```
    1. the graph methods and logic where the directed graph is represented by the `AdjMap` Class that is a Dictionery of targets names to their childeren in the graph
    2. the Engine static utils class that works on the static targetGraph instance
    3. the Task interface and the Simulation implementation
```

Task running on the graph implementation

```
In the TargetGraph class we have two AdjMap graphs
* the Original that was loaded from the xml file
* the graph that we would be running the task on

We start from the RunTask_Option class getting all the user input
from there in case the task already ran on the graph and the user wants to run it from the last point
we create a new graph and set the `targetGraphToRunOn` as that or as the original one

we create a Queue with the leafs and Independent targets of the relevant graph
then we go through the queue and run the task on the targets that we pop from it
then we check all the dads of this target and if all their childeren finished successfully we add it to the queue
if one them failed or was skipped we set it as skipped

eventually the targets that remained frozen were skipped so we set them as that
and finnaly print all the neccecery post task running info
```

Bonuses

```
- finding a circuit option no 6
    like finding a path from a target to itself
- saving the graph option no 7
    adding the result of targets to the targets element in the xml
```

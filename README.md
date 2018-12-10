# nine-mens-morris

Nine men's morris game ("Mühle"-Spiel)

The game control state machine:

```java
  return StateMachine.beginStateMachine(MillGameState.class, MillGameEvent.class, Match.BY_EQUALITY)

  .description("MillGameControl")
  .initialState(STARTING)

  .states()

    .state(STARTING)
      .onEntry(this::resetGame)

    .state(PLACING)
      .onTick(this::tryToPlaceStone)

    .state(PLACING_REMOVING)
      .onEntry(this::startRemoving)
      .onTick(this::tryToRemoveStone)

    .state(MOVING)
      .onTick(this::updateMove)

    .state(MOVING_REMOVING)
      .onEntry(this::startRemoving)
      .onTick(this::tryToRemoveStone)

    .state(GAME_OVER)
      .onEntry(this::onGameOver)

  .transitions()

    .when(STARTING).then(PLACING)

    .when(PLACING).then(PLACING_REMOVING)
      .on(STONE_PLACED_IN_MILL)
      .act(this::onMillClosedByPlacing)

    .when(PLACING).then(MOVING)
      .on(STONE_PLACED)
      .condition(this::areAllStonesPlaced)
      .act(this::switchMoving)

    .stay(PLACING)
      .on(STONE_PLACED)
      .act(this::switchPlacing)

    .when(PLACING_REMOVING).then(MOVING)
      .on(STONE_REMOVED)
      .condition(this::areAllStonesPlaced)
      .act(this::switchMoving)

    .when(PLACING_REMOVING).then(PLACING)
      .on(STONE_REMOVED)
      .act(this::switchPlacing)

    .when(MOVING).then(GAME_OVER)
      .condition(this::isGameOver)

    .when(MOVING).then(MOVING_REMOVING)
      .condition(this::isMillClosedByMove)

    .stay(MOVING)
      .condition(this::isMoveComplete)
      .act(this::switchMoving)

    .when(MOVING_REMOVING).then(MOVING)
      .on(STONE_REMOVED)
      .act(this::switchMoving)

    .when(GAME_OVER).then(STARTING)
      .act(this::shallStartNewGame)
		
.endStateMachine();
```

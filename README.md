# PacMan

## Context

This repository was developed as a student assignment for the course **101 - Imperative Programming** at HES‑SO Valais‑Wallis (ISC section). It was created at the end of the first semester to practice core imperative programming concepts (control structures, data structures, input/output), basic game mechanics implementation, and simple AI behaviors.

## Description

Our game is a replication of Pac-Man, but instead of Pac-Man, we have placed our dear professor, Mr. Mudry. He must flee from various things that people hate in IT, such as Windows, WinDev, Office, and Python, and eat the bonuses that appear on the map (for example Vim, Git, etc.).

The goal of the game is to avoid the ghosts while gathering the most points, and to eat a power pellet to be able to eat the ghosts.

## Screenshots

In the image below, you can see how the game looks during gameplay with our graphical interface.

![game](/res/game.png)

## How to Play

* Use the arrow keys to move Pac-Man up, down, left, and right.
* Eat all of the dots on the screen to advance to the next level.
* If a ghost touches you, you lose a life (you have 3 lives).
* Eating a power pellet will temporarily turn the ghosts green (a sequence of 0 and 1), allowing you to eat them for extra points.
* Eat bonuses to get extra points.

## Code Structure

The project is organized into two main packages: `Frontend` and `Backend`.

* **`Frontend`**: This package contains all of the code related to the user interface and graphics.
    * `Renderer.scala`: Handles the rendering of the game state to the screen.
    * `MainView.scala`: The main view of the application, which contains the game loop.
    * `Sprite`: This directory contains the handling of sprites for the game.

* **`Backend`**: This package contains all of the game logic.
    * `Logical.scala`: Contains the main game logic, such as moving the characters and checking for collisions.
    * `Cases`: This directory contains the different types of cases that make up the game board.
    * `Entities`: This directory contains the classes for the game's characters (player and the ghosts).
    * `global`: This directory contains the levels and global logics.

* **`PacMan.scala`**: The main entry point for the game.
* **`Launcher.scala`**: Launches the game.

## Requirements

* **FunGraphics**: Version 1.5.20
* **JDK**: Version 25 (Oracle OpenJDK 25.0.1)
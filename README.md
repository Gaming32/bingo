<img alt="icon.png" width="280" align="right" src="common/src/main/resources/icon.png">

# Bingo

A Bingo mod that displays the bingo board on players' screens and tracks progress in real time. 

## Features

- A bingo board rendered in the corner of the screen: 

    ![board.png](images/board.png)

- Different gamemodes:

  ![board.png](images/gamemodes.png)

- Tracking the progress of goals:

  ![board.png](images/progress.png)

- Vanilla friendly:

  ![board.png](images/vanilla.png)

## Installation

### Fabric

1. [Install Fabric](https://fabricmc.net/use/).
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and [Architectury API](https://modrinth.com/mod/architectury-api) and put them in your mods folder.
3. Download the Bingo mod from [Modrinth](https://modrinth.com/mod/bingo-mod), and put it in your mods folder.

### NeoForge

1. [Install NeoForge](https://neoforged.net/).
2. Download [Architectury API](https://modrinth.com/mod/architectury-api) and put it in your mods folder.
3. Download the Bingo mod from [Modrinth](https://modrinth.com/mod/bingo-mod), and put it in your mods folder.

## Usage

The Bingo mod works with vanilla teams. Use `/team add <name>` to create a new team, and `/team join [player]` to add a player to a team.

To start a game, use `/bingo start [options...] <teams...>`. For example, if you want to start a bingo game between two teams called `red` and `blue` with default settings,
you can use `/bingo start red blue`. 
Here is a more complicated example with a smaller board and easier goals: `/bingo start --difficulty bingo:easy --size 3 red blue`.

## Contributing

The process of contributing is the same as for any other Minecraft mod. Please ask before making any large changes.

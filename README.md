# 🐜 Ant Colony Simulation – Backend

## 💡 Overview

This is the **backend** of the Ant Colony Simulation project. It powers the core simulation logic: generating terrain, managing ants and pheromones, and persisting state in a MongoDB database. It’s built with **Java 17**, **Spring Boot**, and communicates through a RESTful API.

---

## 🧬 Features

- 🐜 Ant movement simulation using probabilistic models
- 🧪 Dual pheromone system (exploration & return)
- 🪨 Procedural terrain generation with Perlin-like noise
- 🗂️ MongoDB integration for persistent simulation state
- 🧵 Step-by-step simulation updates via API
- ⚙️ Docker support for local development

---

## 🐜 Ant Logic

Each ant in the simulation:

- Has a `Vector` position and a movement history
- Switches behavior between:
  - **Exploring** (looking for food)
  - **Returning** (carrying food home)
- Leaves appropriate pheromones depending on behavior
- Avoids moving back to its immediate previous position
- Decides movement based on local pheromone concentration and passability

Pheromone levels decay over time (`evaporatePheromones()` in `Anthill.java`).

---

## 🪨 Terrain System

Terrain is a 3D voxel grid (`Cell[][][]`) generated with Perlin-like noise. Cell types:

- `AirCell` – walkable
- `WallCell` – non-walkable
- `FoodCell` – randomly placed food
- `ColonyCell` – home base (only one)

The terrain generation algorithm:

- Uses a `voxelThreshold` to determine if a cell is walkable
- Places food and the colony randomly in valid locations
- Identifies accessible neighbors for movement calculations

---

## 🗃️ Database (MongoDB)

This project uses MongoDB to store:

- **Ants** – positions, mode (exploring/returning), food carried
- **Anthill** – 3D grid of cells, pheromone maps, food sources
- **Users** – optional, for multi-user setups

Spring Boot interacts with MongoDB using repository interfaces:
- `AntRepository`
- `AnthillRepository`
- `UserRepository`

All changes during simulation steps are persisted via these interfaces.

## ⚙️ Command Tools

To work with this project locally or in a containerized environment, use the following commands:

```bash
docker-compose up # 🐳 Run with Docker (backend + frontend)
```

## 🧠 Tech Stack
<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=java,spring,docker,mongo,maven" />
  </a>
</p>

# Connections — Client/Server Word-Grouping Game (CLI)

This repository contains a Java client–server implementation of **Connections**, a word-grouping game inspired by *The New York Times Connections*.

In each game, players receive **16 words** that belong to **4 hidden thematic groups** (4 words per group). Players submit proposals (groups of 4 words). The server validates proposals, tracks mistakes and score, and manages a **single global active game** for all logged-in players within a time window.

## What the system provides (per project spec)

### Client (CLI)
- User management:
  - Register
  - Login (joining the currently active game and receiving current state)
  - Update credentials
  - Logout
- Game operations (when logged in):
  - Submit a 4-word proposal
  - Request game info/status (current or by game id)
  - Request game statistics (current or by game id)
- General info (when logged in):
  - Request leaderboard (full / top-K / position of a player)
  - Request personal statistics (NYT-like summary and histogram-style breakdown)
- Receives server **asynchronous notifications** (e.g., end-of-game updates)

### Server
- Central coordinator for the global active game:
  - Creates and rotates games over time
  - Accepts client requests over a persistent TCP connection
  - Evaluates proposals and updates per-player state (correct groups, mistakes, score)
  - Sends asynchronous notifications (e.g., when a game ends)
- Loads game data (words and correct groupings) from a JSON file provided with the project
- Periodically persists users and game-related data in JSON to support restart recovery

## Scoring rules (summary)

- +6 / +12 / +18 bonus for exactly 1 / 2 / 3 correct groups (3 correct groups means the player wins)
- −4 for each wrong proposal (up to 4 wrong proposals)
- 0 points if participating without sending any proposal

## Build and run (Windows)

- `build.bat` — compile
- `run-server.bat` — start server
- `run-client.bat` — start client

## Repository layout (high level)

- `src/` — Java sources (client, server, shared models)
- `test/` — tests (if present)
- `build.bat`, `run-server.bat`, `run-client.bat` — helper scripts

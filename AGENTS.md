# Toolbox Economy Curves

These instructions apply to this repository, including when it is opened without the parent mod workspace.
The workspace `AGENTS.md` one folder up still governs releases, packaging, Git identity and communication.

## Notion

Tasks, history and test lists: the **Toolbox Economy Curves board** in Andrzej's Notion, under
**Starsector mods** (`collection://c2853687-d128-4f5a-adf3-651780f31e8a`). Working rules and game
API notes: **Claude Information** (`collection://7bf9b1d6-b9e5-4b62-87f1-757bbaa51919`), starting
with "Start here: working on Starsector mods through Notion". Ticket ids start with TEC. Update the
board during the work, not only at release time.

## Build

`./gradlew releaseZip test` compiles against the game, LunaLib and Theory of Toolbox 1.0.13
(`gradle.properties` names the folders), runs the checks and tests, and writes
`ToolboxEconomyCurves.zip` in this folder.

## Layout

- `src/toolboxeconomycurves/Curve` - the size curve, no game classes, tested in `test/`.
  `factor(size)` replaces Theory of Toolbox's 2^(size - 1). Every shape but Theory of Toolbox's
  own is pinned to it at the anchor size.
- `src/toolboxeconomycurves/CurveScript` - transient every-frame script. Adds a multiplier under
  `tec_size_curve` on top of Theory of Toolbox's `aotd_new_scale` (colony food and goods demand)
  and `aotd_toolbox_food_prod` (farm food flat, farm machinery mult), equal to wanted / theirs.
  Divides out Astral Ascension Renewed's `aar_toolbox_cap`. Makers get `tec_size_curve_makers`.
  Skips frames while Theory of Toolbox's worker threads run.
- `src/toolboxeconomycurves/Ledger` - every multiplier with its undo, swept each frame and
  released before every save.
- `src/toolboxeconomycurves/raids` - swaps Theory of Toolbox's commodity raid targets for
  `FairCommodityRaid` at listener priority 1.
- `src/toolboxeconomycurves/Settings` - LunaLib reads with defaults.

Why a multiplier and not a replaced class: Theory of Toolbox's economy step reapplies every colony
and reads the amounts right after, and a replaced industry class would not reach colonies already
in a save. A modifier under our own id survives the reapply.

# Toolbox Economy Curves

A Starsector 0.98a mod for AoTD - Theory of Toolbox. It lets you choose how colony size scales
four amounts that Theory of Toolbox doubles with every size step:

- colony food demand
- colony domestic goods demand
- farm food output
- farm heavy machinery demand

Theory of Toolbox doubles all four, but Light Industry and Heavy Industry output does not double.
Big colonies therefore run short of domestic goods, and their farms run short of heavy machinery.

## Settings (LunaLib)

- **Size curve**:
  - Theory of Toolbox (doubling)
  - Bend: Theory of Toolbox up to the bend size (default 7), then a gentler growth per size
    (default x1.4). The Sector's own colonies keep Theory of Toolbox's balance.
  - Linear
  - Gentle: x1.2, x1.5, x2.25, x3.5, x5, x7.5, x10 for sizes 2 to 8, then about x1.3 per size
  - Custom growth
  - Custom list
- **Anchor size** (default 6): Linear, Gentle and the custom curves give Theory of Toolbox's
  amounts at this size.
- **Stop growing at size** (default 15): the curve stays flat past this size. Works with colonies
  up to size 15, for example from Astral Ascension Renewed. While this mod is on, this setting
  replaces Astral Ascension Renewed's own hold at size 10.
- **Producers follow the curve** (default: domestic goods): Light Industry, or Light Industry and
  Heavy Industry, grow their output along the same curve. They make the same as in Theory of
  Toolbox at the anchor size.
- **Upkeep grows with output** (default: square root): farms, and Light and Heavy Industry when
  they follow the curve, pay more upkeep when the curve makes them produce more. Three times the
  output costs 1.7 times the upkeep. Upkeep never drops below normal.
- **Loot per marine group** (default 1 percent): the share of a commodity's stock that one marine
  group takes in a raid. A full raid uses 10 groups, so it takes 10 percent by default. With
  Theory of Toolbox alone, a full raid takes the colony's whole surplus.

## Requirements

- AoTD - Theory of Toolbox 1.0.13
- LunaLib

## Compatibility

- Works with the AoTD - Theory of Toolbox Scheduler Fork (StarsectorPrepatcher): colonies whose
  numbers this mod changes are reported to the fork, so it recalculates them.

## Saves

Nothing from this mod is written into a save, so you can add it to a game in progress or remove
it from one.

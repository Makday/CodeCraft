# Complete List of Minecraft DSL Atomic Functions

Based on your original DSL use cases, here are all the unique atomic functions extracted and classified.

## Movement Actions (3 functions)
Control player position and orientation:

1. `move_forward(n)` - Move player forward by n blocks
2. `turn_right()` - Rotate player 90 degrees clockwise
3. `turn_left()` - Rotate player 90 degrees counter-clockwise

## Inventory Actions (5 functions)
Manage player's inventory and items:

4. `open_inventory()` - Open the player's inventory screen
5. `close_inventory()` - Close the player's inventory screen
6. `drop_item(row, col)` - Drop item from inventory slot (row, col)
7. `move_item(from_row, from_col, to_row, to_col)` - Move item between inventory slots
8. `tool_bar(slot)` - Select hotbar slot (0-8)

## Player/Item Actions (2 functions)
Interact with items and player state:

9. `use()` - Use or activate currently held item (right-click action)
10. `eat()` - Eat currently held food item

## Summary by Category

| Category | Count | Functions |
|----------|-------|-----------|
| Movement | 3 | `move_forward`, `turn_right`, `turn_left` |
| Inventory | 5 | `open_inventory`, `close_inventory`, `drop_item`, `move_item`, `tool_bar` |
| Player/Item | 2 | `use`, `eat` |
| Total | 10 | All atomic actions for your DSL |

## Implementation Status

- Movement actions: implemented
- Inventory actions: implemented
- Player/item actions: pending


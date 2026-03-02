# DSL Usage Examples

## Use case 1 — Farming Automation

```python
rows = 5
cols = 10
crop = items.wheat_seeds

for rows times
    for cols times
        tool_bar(1)        -- select first slot of tool bar with hoe
        use()
        tool_bar(2)        -- select second slot of tool bar with seeds
        use()
        move_forward(1)

    turn_right()
    move_forward(1)
    turn_right()

    for cols times
        tool_bar(1)        -- select first slot of tool bar with hoe
        use()
        tool_bar(2)        -- select second slot of tool bar with seeds
        use()
        move_forward(1)

    -- reposition again
    turn_left()
    move_forward(1)
    turn_left()

print("Farm planting complete!")
```

---

## Use case 2 — Inventory Manager

```python
open_inventory()
drop_item(0, 0)
drop_item(0, 1)

if item_at(0, 2) == items.cobblestone
    drop_item(0, 2)

move_item(0, 0, 0, 2)
move_item(3, 4, 2, 3)

for row from 0 to 3
    for col from 0 to 9
        if item_at(row, col) == items.dirt
            drop_item(row, col)

close_inventory()
```

---

## Use case 3 — React to Conditions

```python
hunger_limit = 6

if state.hunger <= hunger_limit
    open_inventory()
    found = false

    for row from 0 to 3
        for col from 0 to 9
            if is_edible(item_at(row, col))
                found = true
                move_item(row, col, 3, 0)
                stop

    if found
        stop

    tool_bar(0)  -- corresponds to position (3, 0) of inventory

    while state.hunger <= hunger_limit
        if not is_empty(item_at(row, col))
            eat()

    close_inventory()
```
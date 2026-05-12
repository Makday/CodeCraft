# DSL Usage Examples

## Use case 1 — Farming Automation

```python
rows = 9
cols = 9
crop = items.wheat_seeds

center()		-- align player's camera to the closest cardinal direction (e. g. face SOUTH/NORTH/EAST/WEST)

for row from 0 to 4
    for col from 0 to 9
        current_item = item_at(row, col)
        if current_item == items.netherite_hoe
            move_item(row, col, 3, 0)	-- move hoe to inventory
        if current_item == crop
            move_item(row, col, 3, 1)	-- move seeds to inventory

for row from 0 to rows
    for col from 0 to cols
        tool_bar(0)		-- select hoe
        use()
        tool_bar(1)		-- select seeds
        use()
        if col < cols - 1
            move_forward(1)

    if row < rows - 1
        if row % 2 == 1 	-- odd rows
            turn_right()
            move_forward(1)
            turn_right()
        else			-- even rows
            turn_left()
            move_forward(1)
            turn_left()

print("Farm planting complete!")
```

---

## Use case 2 — Inventory Manager

```python
open_inventory()

for row from 0 to 3
    for col from 0 to 9
        if item_at(row, col) == items.netherite_sword
            move_item(row, col, 3, 0)
            
        if item_at(row, col) == items.netherite_axe
            move_item(row, col, 3, 1)
                    
        if item_at(row, col) == items.netherite_pickaxe
            move_item(row, col, 3, 2)
                                
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

    close_inventory()

    tool_bar(0)  -- corresponds to position (3, 0) of inventory

    for 10 times
        use()
```
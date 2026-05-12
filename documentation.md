# CodeCraft DSL Reference Guide

This document provides a comprehensive reference for all available functionalities in the CodeCraft Domain-Specific Language (DSL), including syntax, functions, control structures, and special objects.

---

## Table of Contents

1. [Language Syntax](#language-syntax)
2. [Data Types](#data-types)
3. [Variables and Operators](#variables-and-operators)
4. [Control Flow](#control-flow)
5. [Built-in Functions](#built-in-functions)
6. [Special Objects](#special-objects)
7. [Examples](#examples)
8. [How to run CodeCraft programs](#running-dsl-programs)

---

## Language Syntax

### Comments

Lines beginning with a double dash (`--`) are treated as comments and are ignored by the interpreter.

```codecraft
-- This is a comment
x = 5  -- Comments can also appear at the end of a line
```

### Indentation

CodeCraft uses **Python-like indentation** to define code blocks. Proper indentation is required for blocks within control flow statements.

```codecraft
for 3 times
    move_forward(1)  -- Indented code inside the loop
    turn_left()
```

---

## Data Types

CodeCraft supports three primitive data types:

### Numbers

Integer values used for numeric operations and function arguments.

```codecraft
x = 10
y = -5
z = 0
```

### Strings

Text values enclosed in double quotes. String concatenation is supported via the `+` operator.

```codecraft
message = "Hello"
greeting = message + " World"
```

### Booleans

Logical values: `true` and `false`.

```codecraft
is_ready = true
condition = false
```

### Implicit Conversions

Values are automatically converted between types when needed:
- Numbers convert to `true` (if non-zero) or `false` (if zero)
- Strings convert to `true` (if non-empty) or `false` (if empty)
- Strings can be converted to numbers for arithmetic operations

---

## Variables and Operators

### Variable Assignment

Variables are created and updated using the assignment operator (`=`).

```codecraft
x = 10
name = "Player"
is_ready = true
```

Variables are dynamically typed and can change type during execution.

### Arithmetic Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `+` | Addition or string concatenation | `x + 5` or `"Hello" + " World"` |
| `-` | Subtraction | `x - 3` |
| `*` | Multiplication | `x * 2` |
| `/` | Integer division | `x / 2` |
| `%` | Modulo (remainder) | `x % 3` |

```codecraft
a = 10 + 5      -- 15
b = 20 - 8      -- 12
c = 4 * 3       -- 12
d = 16 / 4      -- 4
e = 10 % 3      -- 1
text = "Farm " + "complete"  -- "Farm complete"
```

### Comparison Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `==` | Equal to | `x == 5` |
| `!=` | Not equal to | `x != 0` |
| `<` | Less than | `x < 10` |
| `>` | Greater than | `x > 5` |
| `<=` | Less than or equal to | `x <= 10` |
| `>=` | Greater than or equal to | `x >= 5` |

```codecraft
if x == 10
    log("x is 10")

if inventory_full != true
    move_forward(1)
```

### Logical Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `and` | Logical AND (short-circuit) | `x > 0 and y < 10` |
| `or` | Logical OR (short-circuit) | `x == 0 or y == 0` |
| `not` | Logical NOT | `not condition` |

```codecraft
if state.hunger > 5 and state.health > 10
    log("Player is healthy")

if not is_tired
    move_forward(5)
```

### Operator Precedence

Operators are evaluated in the following order (highest to lowest):
1. Unary negation (`-`)
2. Multiplication, division, modulo (`*`, `/`, `%`)
3. Addition, subtraction (`+`, `-`)
4. Comparison (`==`, `!=`, `<`, `>`, `<=`, `>=`)
5. Logical AND (`and`)
6. Logical OR (`or`)

Use parentheses to override precedence:

```codecraft
x = 2 + 3 * 4      -- 14 (multiplication first)
y = (2 + 3) * 4    -- 20 (addition first)
```

---

## Control Flow

### If-Else Statements

Execute code conditionally based on a boolean condition.

```codecraft
if condition
    -- Code executed if condition is true

if condition
    -- Code if true
else
    -- Code if false
```

**Example:**

```codecraft
if state.hunger > 10
    log("Player is hungry")
else
    log("Player is not hungry")
```

### For Count Loop

Repeat a block of code a fixed number of times.

```codecraft
for COUNT times
    -- Code block executed COUNT times
```

**Example:**

```codecraft
for 5 times
    move_forward(1)
    log("Moved forward")
```

### For Range Loop

Iterate over a range of numbers, with the loop variable accessible within the block.

```codecraft
for VARIABLE from START to END
    -- Code block executed for each value
    -- VARIABLE contains the current iteration value
    -- Range is [START, END) - END is exclusive
```

**Example:**

```codecraft
for i from 0 to 5
    log(i)  -- Prints 0, 1, 2, 3, 4

for col from 0 to 9
    move_forward(1)
```

### While Loop

Repeat a block of code as long as a condition remains true.

```codecraft
while condition
    -- Code block executed while condition is true
```

**Example:**

```codecraft
x = 0
while x < 5
    log(x)
    x = x + 1
```

### Stop Statement

Immediately terminate execution of any loop or the entire program.

```codecraft
for 10 times
    if x > 5
        stop      -- Exit the loop
    x = x + 1
```

---

## Built-in Functions

### I/O Functions

#### `log(...args)`
Log a message to the console. Can accept multiple arguments which are joined with spaces.

```codecraft
log("Hello")
log("Player at", x, y, z)
log(state.hunger)
```

#### `print(...args)`
Display a message in the game chat. Can accept multiple arguments which are joined with spaces.

```codecraft
print("Farm planting complete!")
print("Items collected:", item_count)
```

---

### Movement Functions

#### `move_forward(blocks)`
Move the player forward by the specified number of blocks.

- **Arguments:** `blocks` (number) - Number of blocks to move forward
- **Returns:** None

```codecraft
move_forward(5)   -- Move forward 5 blocks
move_forward(1)   -- Move forward 1 block
```

#### `turn_left()`
Turn the player 90 degrees horizontally to the left and snap to the closest cardinal direction.

- **Arguments:** None
- **Returns:** None

```codecraft
turn_left()
move_forward(5)
turn_left()
```

#### `turn_right()`
Turn the player 90 degrees horizontally to the right and snap to the closest cardinal direction.

- **Arguments:** None
- **Returns:** None

```codecraft
turn_right()
move_forward(3)
turn_right()
```

#### `center()`
Reset the player's horizontal orientation to face the closest cardinal direction.

- **Arguments:** None
- **Returns:** None

```codecraft
center()  -- Face north
```

---

### Player Actions

#### `use()`
Activate a block or item in front of the player. Can be called with or without block coordinates.

**Signature 1: Use front block**
```codecraft
use()  -- Activate the block directly in front
```

**Signature 2: Use specific block**
```codecraft
use(relX, relY, relZ)  -- Activate block at relative position
-- relX: relative left-right offset
-- relY: relative up-down offset
-- relZ: relative front-back offset
-- the origin (0, 0, 0) is the block where the player's legs reside
-- maximum reachable distance is 4 blocks
```

**Example:**

```codecraft
use()          -- Use block in front
use(0, 0, 1)   -- Use block 1 unit away
```

#### `break()`
Break a block in front of the player. Can be called with or without block coordinates.

**Signature 1: Break front block**
```codecraft
break()  -- Break the block directly in front
```

**Signature 2: Break specific block**
```codecraft
break(relX, relY, relZ)  -- Break block at relative position
-- relX: relative left-right offset
-- relY: relative up-down offset
-- relZ: relative front-back offset
-- the origin (0, 0, 0) is the block where the player's legs reside
-- maximum reachable distance is 4 blocks
```

**Example:**

```codecraft
break()        -- Break block in front
break(0, 0, 1) -- Break block 1 unit away
```

#### `tool_bar(slot)`
Select an item from the player's hotbar.

- **Arguments:** `slot` (number) - Hotbar slot index (0-8)
- **Returns:** None

```codecraft
tool_bar(0)  -- Select first hotbar slot
tool_bar(1)  -- Select second hotbar slot
```

---

### Inventory Management

#### `open_inventory()`
Open the player's inventory screen.

- **Arguments:** None
- **Returns:** None

```codecraft
open_inventory()
-- Inventory is now open
```

#### `close_inventory()`
Close any GUI screen such as inventory, chest inventory, etc.

- **Arguments:** None
- **Returns:** None

```codecraft
close_inventory()
-- Inventory is now closed
```

#### `item_at(row, col)`
Query the item at a specific inventory slot.

- **Arguments:**
  - `row` (number) - Row index (0-3 for main inventory rows)
  - `col` (number) - Column index (0-8 for main inventory columns)
- **Returns:** Item identifier string (e.g., `items.diamond_sword`) or `null` if empty

```codecraft
item = item_at(0, 0)
if item == items.netherite_hoe
    log("Found a hoe!")
```

#### `move_item(fromRow, fromCol, toRow, toCol)`
Move an item from one inventory slot to another.

- **Arguments:**
  - `fromRow` (number) - Source row (0-3)
  - `fromCol` (number) - Source column (0-8)
  - `toRow` (number) - Destination row (0-3)
  - `toCol` (number) - Destination column (0-8)
- **Returns:** None

```codecraft
move_item(0, 0, 3, 0)  -- Move item from (0,0) to hotbar slot (3,0)
move_item(1, 2, 2, 5)  -- Move item between inventory slots
```

#### `drop_item(row, col)`
Drop an item from an inventory slot onto the ground.

- **Arguments:**
  - `row` (number) - Row index (0-3)
  - `col` (number) - Column index (0-8)
- **Returns:** None

```codecraft
drop_item(0, 0)  -- Drop item at slot (0,0)
drop_item(2, 5)  -- Drop item at slot (2,5)
```

---

### Inventory Query Functions

#### `is_empty(value)`
Check if a value is empty (falsy).

- **Arguments:** `value` - Any value to check
- **Returns:** `true` if value is empty/falsy, `false` otherwise

```codecraft
if is_empty(item_at(0, 0))
    log("Slot is empty")
```

#### `is_item(symbol)`
Check if a value is a valid Minecraft item symbol.

- **Arguments:** `symbol` (string) - Item identifier to validate
- **Returns:** `true` if valid item, `false` otherwise

```codecraft
if is_item(items.diamond)
    log("Valid item")
```

#### `is_edible(symbol)`
Check if an item symbol represents a food item.

- **Arguments:** `symbol` (string) - Item identifier to check
- **Returns:** `true` if the item is edible, `false` otherwise

**Edible items include:**
Apple, baked potato, beetroot, bread, cake, carrot, cookie, melon slice, potato, cooked beef, cooked chicken, cooked cod, cooked mutton, cooked porkchop, and more.

```codecraft
if is_edible(items.apple)
    log("Apples are edible")
```

---

### Timing Functions

#### `wait(ticks)`
Pause execution for the specified number of game ticks.

- **Arguments:** `ticks` (number) - Number of ticks to wait (1 tick = 50ms)
- **Returns:** None

```codecraft
wait(10)   -- Wait 0.5 seconds
wait(20)   -- Wait 1 second
```

#### `wait_ticks(ticks)`
Alias for `wait()`. Pause execution for the specified number of ticks.

```codecraft
wait_ticks(10)
```

---

## Special Objects

Special objects provide access to player and world state using dot notation. They are resolved at runtime and return dynamic values.

### `items` Object

Access item identifiers for use in inventory operations and comparisons.

#### Syntax
```codecraft
items.ITEM_NAME
```

**Common items:**
- `items.diamond`
- `items.dirt`
- `items.netherite_sword`
- `items.netherite_pickaxe`
- `items.netherite_hoe`
- `items.netherite_axe`
- `items.wheat_seeds`
- `items.apple`
- And all other Minecraft items...

**Examples:**

```codecraft
if item_at(0, 0) == items.netherite_hoe
    log("Found a hoe")

tool_bar(0)
use()
crop = items.wheat_seeds
```

### `state` Object

Access the current player and world state.

#### Available Fields

| Field | Type | Description |
|-------|------|-------------|
| `state.hunger` | number | Player's current hunger level (0-20) |
| `state.food` | number | Alias for `state.hunger` |
| `state.health` | number | Player's current health (0-20) |
| `state.x` | number | Player's X coordinate |
| `state.y` | number | Player's Y coordinate |
| `state.z` | number | Player's Z coordinate |
| `state.inventory_open` | boolean | Whether inventory screen is open |

**Examples:**

```codecraft
if state.hunger > 10
    log("Player is hungry")

if state.health <= 5
    log("Player is critically injured")

log("Player position:", state.x, state.y, state.z)

if state.inventory_open
    close_inventory()
```

---

## Examples

### Example 1: Simple Movement Pattern

```codecraft
-- Move in a square pattern
for 4 times
    move_forward(5)
    turn_right()

print("Square complete!")
```

### Example 2: Inventory Organization

```codecraft
open_inventory()
wait(10)

-- Organize items by type
for row from 0 to 3
    for col from 0 to 9
        item = item_at(row, col)
        
        if item == items.netherite_sword
            move_item(row, col, 3, 0)
        
        if item == items.netherite_pickaxe
            move_item(row, col, 3, 1)
        
        if item == items.dirt
            drop_item(row, col)

close_inventory()
print("Inventory organized!")
```

### Example 3: Conditional Movement

```codecraft
x = 0
center()

while x < 10
    if x % 2 == 0
        turn_right()
    else
        turn_left()
    
    move_forward(1)
    x = x + 1

log("Movement pattern complete")
```

### Example 4: Farm Automation

```codecraft
rows = 9
cols = 9

center()

-- Plant crops in a grid
for row from 0 to rows
    for col from 0 to cols
        tool_bar(0)  -- Select hoe
        use()        -- Till the soil
        tool_bar(1)  -- Select seeds
        use()        -- Plant seeds
        
        if col < cols - 1
            move_forward(1)
    
    -- Move to next row
    if row < rows - 1
        if row % 2 == 1
            turn_right()
            move_forward(1)
            turn_right()
        else
            turn_left()
            move_forward(1)
            turn_left()

print("Farm complete!")
```

### Example 5: Monitoring Player State

```codecraft
-- Monitor player state and react
for 100 times
    log("Hunger:", state.hunger, "Health:", state.health)
    
    if state.hunger < 5
        log("ALERT: Player is starving!")
        -- Take action
    
    if state.health < 3
        log("ALERT: Player health critical!")
        stop
    
    wait(20)
```

### Example 6: Item Detection and Movement

```codecraft
open_inventory()
wait(10)

found_count = 0

-- Search for specific items
for row from 0 to 3
    for col from 0 to 9
        item = item_at(row, col)
        
        if is_edible(item)
            found_count = found_count + 1
            move_item(row, col, 2, found_count)

log("Found", found_count, "edible items")
close_inventory()
```

---

## Running DSL Programs

For a program to be executed, it must be saved in a file with the `.codecraft` extension. 
You can create and edit these files using any text editor.

To execute a DSL program in the game, use the command:

```
/execute "<file_path>"
```

This command accepts 2 types of file paths:
1. **Relative Path:** A path relative to the `codecraft` directory inside Minecraft root folder (You have to create the /codecraft folder). For example, if your file is located at `minecraft/codecraft/my_scripts/farm_automation.codecraft`, you can run it with:

```
/execute "my_scripts/farm_automation.codecraft"
```

2. **Absolute Path:** A full path from the root of the filesystem. For example:

```
/execute "C:/Users/Username/Documents/my_scripts/farm_automation.codecraft"
```

---

## Additional Resources

- **Language Grammar:** See the parser implementation for syntax details
- **Interpreter Source:** Check `StandardLibrary.java` for function implementations
- **Examples:** Refer to `.codecraft` files in the `docs/code_examples/` directory
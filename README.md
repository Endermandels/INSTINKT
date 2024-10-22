# INSTINKT

## How to Play

## Controls

### Enemy Wave/Cooldown
    Movement:       WASD/Arrow keys
    HUD Toggle:     ~

### Enemy Wave
    Spray:          Space
    Eat Berry:      E

### Cooldown
    Start Wave:     Space
    Plant Berry:    E
    Open Shop:      Q

### Shop
    Select Upgrade: Space/E
    Close Shop:     Q



## Cheat Codes

### HUD
    next:       Skip the current enemy wave and go to the next cooldown phase
    berries:    Set the player's berry count to specified amount
    tl:         Show the tile location of all moving entities.
                Also shows the Dijkstra distances of each tile.
                The top left number is the distance to the player 
                (more black closer to player, more red away from player).
                The bottom left number (purple) is the distance to the berry pile
                (modified by closeness to the player).
                The bottom right number (white) is the distance to an exit
                (modified by closeness to the player).
    hp:         Set the player's max HP to specified amount (also restores HP to full)
    atk:        Set the player's attack to specified amount
    smax:       Set the player's max spray count to specified amount
    stats:      Toggle the player and enemy HP stats between always and in console    
    srad:       Set the player's spray radius to specified amount
    slen:       Set the player's spray length to specified amount
    sdur:       Set the player's spray duration to specified amount (in seconds)
    scld:       Set the player's spray cooldown to specified amount (in seconds)
    fps:        Toggle fps visibility between always and in console
    p:          Pause the game
    s:          Step through the next update cycle while the game is paused
    u:          Unpause the game

### Other
    ESC:        Pause game during enemy wave
    LSHIFT+ESC: Kill player during enemy wave (used to reset game)



## Features

### Low-Bar

    Movement (Complete)
        Complete    Tile-based movement
    Spray (Complete)
        Complete    Spray creates a line from the player, marking hit enemies
        Complete    Spray creates a AOE ring from the hit enemy/tile which repels enemies
        Complete    User has limited number of sprays
    Melee (Complete)
        Complete    Player and enemies receive damage on collision
    Collisions (Complete)
        Complete    All entities are allowed to move through each other
        Complete    All entities are not allowed to move through obstacles
    Berries (Complete)
        Complete    Player can plant berry seedlings, which grow into berry bushes after one wave
        Complete    Berry bushes produce a random amount of berries at the start of the cooldown phase
    Upgrade Stats (Complete)
        Complete    Player can purchase upgrades (e.g. more health, spray ammo, etc.) using berries
    Enemies (Complete)
        Complete    Fox targets player, is fast and has high attack
        Complete    Squirrel targets berry pile, is fast and weak
        Complete    Cobra targets player, is slow and slows the player down on melee
    Gameplay (Complete)
        Complete    Broken into two sections: Enemy Wave and Cooldown Phase
    Enemy Wave (Complete)
        Complete    Enemies spawn at one of the three entrances
        Complete    Player is able to die from melee attacks
        Complete    Player can replenish HP and spray by eating berries, which also clears slowness
    Cooldown (Complete)
        Complete    Berries from berry bushes are gathered
        Complete    Player's HP and spray are replenished
        Complete    Player can plant berry bushes
        Complete    Player can open a shop to upgrade their stats in exchange for berries
        Complete    Player can switch to next enemy wave

### Extra

    Animated Shop GUI
        Animated shop with text describing powerups

    Extra Enemy
        Twig Blight enemy with extra health and attack

    Animated Stats GUI
        Animated healthbar, spraybar, wave, and berry stats

## License

This work is licensed under CC BY-NC-SA 4.0

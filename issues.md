## Resolved Issues ##

- **[1]** Resize Game Screen
- **[2]** Create Tile class and grid
- **[3]** Draw tile grid using default tile sprites
- **[4]** Create Player class and draw to screen at a tile location
- **[5]** Create Animation Handler class and add one to the player
- **[6]** Fix HUD
- **[7]** Handle Keyboard Input for Player movement
- **[8]** Add Game Background Music
- **[9]** Create Obstacle class and spawn test instances
- **[10]** Handle collisions between Player and Obstacle
- **[11]** Spawn tiles using text file
- **[12]** Spawn obstacles using text file
- **[13]** Add farmland tiles, berry bushes, berry pile
- **[14]** Design Level
- **[15]** Add shadows to obstacles and Player
- **[16]** Create enemy class
- **[17]** Enemy spawn locations and basic movement
- **[18]** Add enemy pathfinding using Dijkstra's Algorithm
- **[19]** Stop enemy when attacking the player
- **[20]** Visually debug Entity Tile Locations (HUD)
- **[21]** Visually debug enemy pathfinding (HUD)
- **[22]** Add melee damage and HP system
- **[23]** Create BASIC enemy spawn formations
- **[24]** Create Squirrel enemy
- **[25]** Create Cobra enemy
- **[26]** Handle Player death (Game Over and restart)
- **[27]** Entity death animation
- **[28]** Create Player HP display
- **[29]** Add HUD command for setting player stats (HP, atk, etc.)
- **[30]** Create visual feedback for taking damage (hurt animation)
- **[31]** Lower Opacity of foreground obstacles
- **[32]** Change Entity Tile locations to center of Tile, instead of bottom left?
- **[33]** Sound Effects
- **[34]** Create Spray class
- **[35]** Shoot spray in opposite direction of player's last moved direction (variable length)
- **[36]** Spray logistics (limited number, cooldown)
- **[37]** Create AOE effect on a sprayed tile and enemy which repels enemies
- **[38]** Add Spray HUD commands
- **[39]** Play player spray animation, stop player movement
- **[40]** Add enemy SFX reaction to spray
- **[41]** Phase transitions (cooldown and enemy wave)
- **[42]** Add berry mechanic after every enemy wave
- **[43]** Spray count GUI
- **[44]** Refill Spray and HP at start of cooldown phase
- **[45]** Handle weird Spray and HP counts in GUI
- **[46]** Regain Spray and HP by eating berries
- **[47]** HUD command for complete enemy wave.
- **[48]** Implement Goal: survive 10 waves.
- **[49]** Fix Spray bar shake at beginning of enemy wave
- **[50]** Switch up enemy formations between waves.
- **[51]** Squirrel mechanic (steal berries and then run) (also avoid player)
- **[52]** Shop Mechanic and basic UI
- **[53]** Change AOE effect shape from square to circle
- **[54]** Add Icon and make EXE
- **[55]** Wear off stink on sprayed enemies
- **[56]** Bias enemies moving vertically before horizontally
- **[57]** Add Twig Blight leader (Tanky, targets player, immune to stink
disappear when all other normal enemies have been defeated)

## Open Issues ##

- **[58]** Cobra slows player
- **[59]** Refactor level design to have more horizontal enemy movement
- **[60]** In enemy_formations.txt, when a number is encountered in the same enemy chain,
change the frequency (Introduces longer waves)

## Backlog ##

- **[]** Balance Player, Enemy, and World stats
- **[]** Enemies run away from stinky tiles
- **[]** 
- **[]** 
- **[]** 
- **[]** Add Druid leader
(Makes waves harder to defeat while on screen, fragile, stays back, throws projectiles)
- **[]** 
- **[]** 
- **[]** 
- **[]** How to Play Screen
- **[]** Press and Hold space to move to next enemy wave
- **[]** Stop enemy when first smelling stink (hurt animation).
- **[]** Make shop upgrades more interesting
- **[]** 
- **[]** 
- **[]** Use Regeneration animation for health and spray
- **[]** 
- **[]** 
- **[]** Separate enemies in their own classes
- **[]** Enemies run a certain amount of distance before fainting from smell
- **[]** Fade out AOE effect
- **[]** Improve AOE effect texture
- **[]** Fix player movement stickiness
- **[]** Once the spray cooldown drops below 0.1 seconds,
Change SFX and animation to be a constant spray sound while spraying

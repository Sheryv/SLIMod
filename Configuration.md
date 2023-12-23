Configuration
====

## Mob spawn limits

File `SLI-SpawnLimitIncrease.toml` in config directory.

Spawn_Limits section of that file can be reloaded with command `slimod reload`. 
To kill all mobs around player use command `slimod kill <all|passive|hostile|entity>` e.g. `slimod kill all`

#### Example

Config below increases max limits for monsters and animals. 
For example by increasing `monsterLimit` to 210 you can triple max limit of monsters in area surrounding player (it will not affect spawn rate - only max limit)

```toml

[General]
    enableLogging = false

#Here you can change vanilla spawn limits. 
#Limits are applied per mob category and defines how many mobs can be spawned in certain area. 
#It affects attempts of spawn of animals and monsters but it does not increase spawn rate
[Spawn_Limits]
	#When false spawn limits are not changed
	enableLimitModification = true
	#Limit for ambient - bats...
	#Vanilla default: 15
	#Range: 1 ~ 2000
	ambientLimit = 15
	#Limit for water ambient - fish...
	#Vanilla default: 20
	#Range: 1 ~ 2000
	waterAmbientLimit = 20
	#Limit for creatures - passive mobs, animals
	#Vanilla default: 10
	#Range: 1 ~ 2000
	creatureLimit = 300
	#Limit for monsters - hostile mobs
	#Vanilla default: 70
	#Range: 1 ~ 2000
	monsterLimit = 230
	#Limit for water creatures - dolphins, squids...
	#Vanilla default: 5
	#Range: 1 ~ 2000
	waterCreatureLimit = 15

```

## Mob spawners

Mob spawners (that is number of mobs per chunk per successful spawn) are configured via datapacks. 
See example at [data/sli_mcmod/forge/biome_modifier/slimod_spawners_overwrite.json](src/main/resources/data/sli_mcmod/forge/biome_modifier/slimod_spawners_overwrite.json)


**By default**, it increases animal number per group in grassy biomes and birch forest. It also makes endermans more frequent in hot biomes.


## Animal spawn rate

Animals in Minecraft are never respawn - they only generate with terrain. 
To make them more common this mod provides config that allows to increase game hardcoded chance of animal spawn when a chunk is generated (called spawn probability).


Animal spawn chance multipliers are configured via datapacks. 
See example at [data/sli_mcmod/forge/biome_modifier/slimod_probabilities_modifier.json](src/main/resources/data/sli_mcmod/forge/biome_modifier/slimod_probabilities_modifier.json)
**By default**, it increases animal spawn chance by 150% - 300% depending on biome


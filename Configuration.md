Configuration
====
------
# **OUTDATED**

------

File `SLI-SpawnLimitIncrease.toml` in config directory.

Spawn_Limits section of that file can be reloaded with command `slimod reload`. 
To kill all mobs around player use command `slimod kill <all|passive|hostile|entity>` e.g. `slimod kill all`

### Example

Config below increases animal number and max limits for monsters. It also makes endermans more frequent

```toml

[General]
    enableLogging = false

#Allows to change spawner configuration for each biome. You can change weight or add new mob to some biomes.
#It is not intended to block spawning - use InControl mod for advanced controlling of spawning
#
#Fields:
#	entity          <- entity to configure eg. "minecraft:cow"
#	addIfMissing    <- if true add mob spawning to configured biome, if false modifies only vanilla spawning (for example: if false cows won't spawn in desert but only in biomes where vanilla spawn is configured), default false
#	affectedBiomes  <- biomes list where change should be applied, empty list means all biomes eg. ["minecraft:plains"]
#	weight          <- defines proportions/probability of spawning, every mob category has own pool (for zombie it is 100, for cow 10) eg. 10
#	groupMaxSize    <- max group size for single spawn eg. 4
#	groupMinSize    <- min group size for single spawn eg. 2
#
#Example for cow that increases spawn group size in plains to 10:
#	[[Spawner_Properties.entries]]
#		entity = "minecraft:cow"
#		affectedBiomes = ["minecraft:plains"]
#		weight = 10
#		groupMaxSize = 10
#		groupMinSize = 10
#
#More info on: https://github.com/Sheryv/SLIMod
[Spawner_Properties]
	#When false spawners are not changed
	enableSpawnerModification = true

	[[Spawner_Properties.entries]]
		addIfMissing = false
		groupMinSize = 5
		weight = 5
		affectedBiomes = []
		groupMaxSize = 7
        forbiddenBiomeCategories = []
		entity = "minecraft:cow"

	[[Spawner_Properties.entries]]
		addIfMissing = false
		groupMinSize = 5
		weight = 6
		affectedBiomes = []
		groupMaxSize = 8
        forbiddenBiomeCategories = []
		entity = "minecraft:chicken"

	[[Spawner_Properties.entries]]
		addIfMissing = false
		groupMinSize = 4
		weight = 6
		affectedBiomes = []
		groupMaxSize = 6
        forbiddenBiomeCategories = []
		entity = "minecraft:sheep"

	[[Spawner_Properties.entries]]
		addIfMissing = false
		groupMinSize = 5
		weight = 6
		affectedBiomes = []
		groupMaxSize = 8
        forbiddenBiomeCategories = []
		entity = "minecraft:pig"

	[[Spawner_Properties.entries]]
		addIfMissing = false
		groupMinSize = 4
		weight = 5
		affectedBiomes = []
		groupMaxSize = 6
        forbiddenBiomeCategories = []
		entity = "minecraft:horse"

	[[Spawner_Properties.entries]]
		addIfMissing = false
		groupMinSize = 4
		weight = 25
		affectedBiomes = []
		groupMaxSize = 4
        forbiddenBiomeCategories = []
		entity = "minecraft:enderman"

	[[Spawner_Properties.entries]]
		addIfMissing = false
		groupMinSize = 1
		weight = 80
		affectedBiomes = []
		groupMaxSize = 4
        forbiddenBiomeCategories = ["NETHER", "THEEND", "PLAINS"]
		entity = "minecraft:enderman"

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

[Spawn_Attempts]
	#Change probability of spawn of creature for each attempt.
	#It is calculated as <vanilla value> * <provided here>, e.g.: 2.0 means two times higher probability.
	#It only affects passive mobs that are generated with terrain and may decrease performance.
	#Values higher than 3 are not recommended. Vanilla default: 1.0.
	#Value 1.0 also disables this feature. To refresh this value world reload is required
	#Range: 0.01 ~ 5.0
	defaultCreatureSpawnProbabilityMultiplier = 2.5

	#Defines list of multipliers that override default value. List is loaded from top to bottom. Example is provided
	[[Spawn_Attempts.entries]]
		creatureSpawnProbabilityMultiplier = 4.0
		affectedBiomes = ["minecraft:plains", "biomesoplenty:shrubland", "biomesoplenty:shrubland_hills", "biomesoplenty:scrubland", "biomesoplenty:wooded_scrubland", "miencraft:savanna", "biomesoplenty:highland", "biomesoplenty:highland_moor", "biomesoplenty:prairie", "biomesoplenty:golden_prairie"]
```

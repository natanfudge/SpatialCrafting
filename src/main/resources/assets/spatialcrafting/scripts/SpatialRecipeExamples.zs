import mods.spatialcrafting;

val stone = <minecraft:stone:*>;
val x2Crafter = <spatialcrafting:x2crafter_block>;
val x3Crafter = <spatialcrafting:x3crafter_block>;

# Recipe for the 3x3x3 crafter block
spatialcrafting.addRecipe([
	[
		[stone, stone],
		[stone, stone]
	],
	[
		[x2Crafter, x2Crafter],
		[x2Crafter, x2Crafter]
	],
],	x3Crafter * 9);



val ironBlock = <minecraft:iron_block>;
val x4Crafter = <spatialcrafting:x4crafter_block>;

# Recipe for the 4x4x4 crafter block
mods.spatialcrafting.addRecipe([
	[
		[ironBlock, null, ironBlock],
		[null, null, null],
		[ironBlock, null, ironBlock]
	],
	[
		[ironBlock, null, ironBlock],
		[null, null, null],
		[ironBlock, null, ironBlock]
	],
	[
		[x3Crafter, x3Crafter, x3Crafter],
		[x3Crafter, x3Crafter, x3Crafter],
		[x3Crafter, x3Crafter, x3Crafter]
	],
],	x4Crafter * 16);



val diamBlock = <minecraft:diamond_block>;
val emerBlock = <minecraft:emerald_block>;
val x5Crafter = <spatialcrafting:x5crafter_block>;

# Recipe for the 5x5x5 crafter block
mods.spatialcrafting.addRecipe([
	[
		[diamBlock, emerBlock, emerBlock,diamBlock],
		[emerBlock, null, null, emerBlock],
		[emerBlock, null, null , emerBlock],
		[diamBlock, emerBlock, emerBlock, diamBlock]
	],
	[
		[ null, diamBlock, diamBlock, null],
		[diamBlock, null, null, diamBlock],
		[diamBlock, null, null, diamBlock],
		[null, diamBlock, diamBlock, null]
	],
	[
		[null, diamBlock, diamBlock, null],
		[diamBlock, null, null, diamBlock],
		[diamBlock, null, null, diamBlock],
		[null, diamBlock, diamBlock, null]
	],
	[
		[x4Crafter, x4Crafter, x4Crafter, x4Crafter],
		[x4Crafter, x4Crafter, x4Crafter, x4Crafter],
		[x4Crafter, x4Crafter, x4Crafter, x4Crafter],
		[x4Crafter, x4Crafter, x4Crafter, x4Crafter]
	],
],	x5Crafter * 25);






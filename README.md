# SpatialCrafting
The code base of best mod.

## Getting Started

The first thing you wanna do is get 4 <b>Wood-carved Crafters</b>. Place 4 of them in a 2x2 grid, and the 2x2 crafter will appear.
In order to make bigger recipes, you will need the next levels of crafters - look up "crafter" in JEI!

## Crafting Items

To craft items, insert the required items for the spatial recipe in the holograms by right clicking on them. You can take items out by either right clicking with an open hand, or left clicking.  
For this step JEI is highly recommended! Simply look up the recipe (for the bigger crafters, for example) in JEI. When near a crafter, you can <b>press "start crafting help" to automaticly configure to holograms to allow for easy inserations</b> for that recipe.  
JEI shows each layer of the recipe individually (from bottom to top - layer 1 will be the lowest layer and so on). <b>Use the buttons near the recipe to traverse the layers of the recipe</b>. This will also configure the nearest crafter to activate the relevent layer.

## Adding Recipes

You may have noticed there are not that many spatial recipes. This is because Spatial Crafting is designed to provide custom recipes to various modded items. For this reason adding recipes is extremely easy:  
First, insert a nearby crafter with the items that you want to serve as the input. (Tip: <b>type /sc layer [layerNumber] to activate the layer that you want to insert items into.)</b>  
Then, hold the item (or any amount of that item) that you want to be the output for the recipe in your main hand.  
Finally, <b>type /sc addrecipe (or /sc ar) to add the recipe</b>.  

If you want to add a shapeless recipe type "/sc addshapeless" or "/sc as" instead.

## Config
By default Spatial Crafting does not require energy. Energy cost can be enabled in the config, and there are already recipes that support it. 
  
## Adding More Advanced Recipes

Adding recipes using the "ar" commands has some limitations. You cannot choose how much time it takes to craft the item, how much energy the recipe costs (if energy usage is enabled), and some other things that CraftTweaker enables. 
A better way of adding recipes is by writing a [CraftTweaker](https://crafttweaker.readthedocs.io/en/latest/) script. Go into your minecraft folder, then to scripts/spatialcrafting. You will see a file named SpatialRecipeExamples.zs with some examples of how to add recipes. You can either add extra recipes to this file or create another file in the scripts folder with a .zs extension to add recipes to.

This process might be difficult for some, so if you are experiencing any problems you can ask questions in the [Spatial Crafting Discord Server](https://discord.gg/CFaCu97).
To add a recipe, the file needs to have this at the top:
```
import mods.spatialcrafting;
```
Then decide what ingredients you will use in your recipe, for example:
```
val stone = <minecraft:stone:*>;
val ironBlock = <minecraft:iron_block>;
```
Then add the actual command that will add the recipe. The syntax for it goes like this: (Parameters enclosed with [] are optional)
```
spatialcrafting.addRecipe(<recipe input array>, <recipe output>, [recipe craft time], [recipe energy cost]);
```
For example:
```
spatialcrafting.addRecipe([
	[
		[stone, stone],
		[stone, stone]
	],
	[
		[ironBlock, stone],
		[stone, ironBlock]
	]
],	stone * 9, 10.5,20000);
```
This will add a 2x2 recipe in which the first layer contains 4 stone, and the second layer contains 2 iron blocks and 2 stone. The output will be 9 stone, it will take 10.5 seconds to craft and cost 20,000 FE.

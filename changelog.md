# 1.0.0
Released
## 1.1.0
Updated REI to 3.0 (thanks Daniel <3)
### 1.1.1
Fixed sounds and particles not aligning correctly to what is actually happening.
## 1.2.0
- Crafters will now accept items and insert them into the holograms. 
If recipe help is active, the items will be inserted into the correct places.
- Now depends on Working Scheduler.
### 1.2.1
- Fixed inserting into slots more than the allowed amount for that specific item.
### 1.2.3 
- Improved crash diagnostics.
### 1.2.4
- Fixed 2 crash cases.
## 1.3.0
- Items that have remainders (like buckets and bottles) will now drop their remainders when used as crafting ingredients (Thanks Juuz!).
- The recipe creator GUI is not longer broken.
- You can now hold Y (by default) to make holograms smaller. This makes inserting into inner holograms without REI easier. (You should still use REI though.)
### 1.3.1
- Fixed holograms being replaceable by water.
## 1.14.0
- The _modpack_ variant no longer requires **LibGUI**, **Lib Block Attributes**, **Fabric Drawer**, and **Working Scheduler**. You now only need **Fabric API** and **Fabric Language Kotlin**.
- The _standalone_ variant no longer bundles **Roughly Enough Items**. It now only bundles **Fabric Language Kotlin**. 
### 1.4.1
- Fixed issues regarding holograms.
### 1.4.2
- Fixed multiblocks not being placed properly on dedicated servers.
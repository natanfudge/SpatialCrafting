package spatialcrafting.ticker

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateManager
import org.apache.logging.log4j.LogManager

fun <T : PersistentState> PersistentStateManager.getOrCreate(id: String, creator: () -> T): T = getOrCreate(creator, id)
fun CompoundTag.putIdentifier(key: String, identifier: Identifier) = putString(key, identifier.toString())
fun CompoundTag.getIdentifier(key: String) = Identifier(getString(key))

private val Logger = LogManager.getLogger("Working Ticker")

fun logWarning(warning: String) = Logger.warn(warning)

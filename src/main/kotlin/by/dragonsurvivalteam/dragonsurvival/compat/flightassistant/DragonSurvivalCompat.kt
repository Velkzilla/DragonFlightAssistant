package by.dragonsurvivalteam.dragonsurvival.compat.flightassistant

import dev.architectury.platform.Platform
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.player.Player
import ru.octol1ttle.flightassistant.FlightAssistant

/**
 * DragonSurvival compatibility layer for FlightAssistant.
 * Provides utilities to detect dragon flight state.
 */
object DragonSurvivalCompat {
    private const val DRAGON_SURVIVAL_MOD_ID = "dragonsurvival"
    private var isDragonSurvivalLoaded: Boolean = false

    // Reflection caches for DragonSurvival classes/methods
    private var dragonStateProviderClass: Class<*>? = null
    private var flightDataClass: Class<*>? = null
    private var dsDataAttachmentsClass: Class<*>? = null
    private var getDataMethod: ((Player) -> Any)? = null
    private var isDragonMethod: ((LocalPlayer) -> Boolean)? = null
    private var flightAttachmentField: Any? = null

    fun init() {
        isDragonSurvivalLoaded = Platform.isModLoaded(DRAGON_SURVIVAL_MOD_ID)
        FlightAssistant.logger.info("DragonSurvival loaded: $isDragonSurvivalLoaded")
        if (!isDragonSurvivalLoaded) {
            return
        }

        FlightAssistant.logger.info("Initializing support for DragonSurvival")

        try {
            // Cache DragonStateProvider class
            dragonStateProviderClass = Class.forName("by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider")
            FlightAssistant.logger.info("Found DragonStateProvider class")
            
            // Cache FlightData class
            flightDataClass = Class.forName("by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData")
            FlightAssistant.logger.info("Found FlightData class")
            
            // Cache DSDataAttachments class
            dsDataAttachmentsClass = Class.forName("by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments")
            FlightAssistant.logger.info("Found DSDataAttachments class")
            
            // Cache methods - isDragon takes Entity, getData takes Player
            val getDataMethodHandle = flightDataClass!!.getMethod("getData", Player::class.java)
            getDataMethod = { player -> getDataMethodHandle.invoke(null, player) }
            FlightAssistant.logger.info("Cached getData method")

            val isDragonMethodHandle = dragonStateProviderClass!!.getMethod("isDragon", net.minecraft.world.entity.Entity::class.java)
            isDragonMethod = { player -> isDragonMethodHandle.invoke(null, player) as Boolean }
            FlightAssistant.logger.info("Cached isDragon method")
            
            // Cache FLIGHT attachment field from DSDataAttachments
            flightAttachmentField = dsDataAttachmentsClass!!.getField("FLIGHT").get(null)
            FlightAssistant.logger.info("Cached FLIGHT attachment")
            
            FlightAssistant.logger.info("DragonSurvival support initialized successfully")
        } catch (e: Exception) {
            FlightAssistant.logger.error("Failed to initialize DragonSurvival support", e)
            isDragonSurvivalLoaded = false
        }
    }

    /**
     * Checks if the player is a dragon and currently flying/gliding.
     * This is the DragonSurvival equivalent of isFallFlying().
     */
    fun isDragonFlying(player: LocalPlayer): Boolean {
        if (!isDragonSurvivalLoaded) {
            return false
        }

        try {
            // Check if player is a dragon
            val isDragon = isDragonMethod?.invoke(player) ?: return false
            FlightAssistant.logger.debug("DragonSurvival: isDragon=$isDragon")
            if (!isDragon) {
                return false
            }

            // Get flight data using Player interface
            val flightData = getDataMethod?.invoke(player) ?: return false
            FlightAssistant.logger.debug("DragonSurvival: got flightData")

            // Access public fields directly
            val hasFlightField = flightDataClass!!.getField("hasFlight")
            val hasFlight = hasFlightField.get(flightData) as Boolean
            FlightAssistant.logger.debug("DragonSurvival: hasFlight=$hasFlight")
            if (!hasFlight) {
                return false
            }

            val areWingsSpreadField = flightDataClass!!.getField("areWingsSpread")
            val areWingsSpread = areWingsSpreadField.get(flightData) as Boolean
            FlightAssistant.logger.debug("DragonSurvival: areWingsSpread=$areWingsSpread")
            
            // Dragon must not be on ground or in fluids to be considered flying
            val onGround = player.onGround()
            val inWater = player.isInWater
            val inLava = player.isInLava
            val isPassenger = player.isPassenger
            FlightAssistant.logger.debug("DragonSurvival: onGround=$onGround, inWater=$inWater, inLava=$inLava, isPassenger=$isPassenger")

            val result = areWingsSpread && !onGround && !inWater && !inLava && !isPassenger
            FlightAssistant.logger.debug("DragonSurvival: isDragonFlying=$result")
            return result
        } catch (e: Exception) {
            FlightAssistant.logger.warn("Error checking dragon flight state", e)
            return false
        }
    }

    /**
     * Returns true if DragonSurvival is loaded.
     */
    fun isLoaded(): Boolean = isDragonSurvivalLoaded
}

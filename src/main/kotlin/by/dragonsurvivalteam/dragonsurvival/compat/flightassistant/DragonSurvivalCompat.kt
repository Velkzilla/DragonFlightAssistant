// ============================================================================
// DragonSurvival 兼容性层
// DragonSurvival Compatibility Layer
// ============================================================================
//
// 【目的 / Purpose】
// 为 FlightAssistant 提供 DragonSurvival 模组龙飞行状态的检测能力。
// Provides dragon flight state detection capabilities for FlightAssistant mod.
//
// 【问题背景 / Background】
// FlightAssistant 使用 player.isFallFlying() 检测飞行状态，但 DragonSurvival 的龙
// 使用 FlightData.areWingsSpread 和 hasFlight 字段，导致：
// - 龙飞行时 HUD 不显示（使用 notFlyingNoElytra 配置）
// - 龙无法享受 FlightAssistant 的飞行辅助功能
//
// FlightAssistant uses player.isFallFlying() to detect flight state, but
// DragonSurvival's dragons use FlightData.areWingsSpread and hasFlight fields,
// causing:
// - HUD not displaying during dragon flight (uses notFlyingNoElytra config)
// - Dragons cannot use FlightAssistant's flight assistance features
//
// 【实现方式 / Implementation】
// 使用反射访问 DragonSurvival 的内部 API，避免硬编码依赖：
// 1. DragonStateProvider.isDragon(Entity) - 检测是否为龙
// 2. FlightData.getData(Player) - 获取龙飞行数据
// 3. FlightData.hasFlight / areWingsSpread - 检查飞行状态
//
// Uses reflection to access DragonSurvival's internal API, avoiding hard-coded
// dependencies:
// 1. DragonStateProvider.isDragon(Entity) - Check if entity is a dragon
// 2. FlightData.getData(Player) - Get dragon flight data
// 3. FlightData.hasFlight / areWingsSpread - Check flight state
//
// 【注意事项 / Caveats】
// 1. isDragon() 方法接受 Entity 类型参数，不是 Player
//    isDragon() method accepts Entity parameter, not Player
// 2. getData() 方法接受 Player 类型参数
//    getData() method accepts Player parameter
// 3. 飞行状态检测包含额外条件：不在地面、不在水中/熔岩、不是乘客
//    Flight state detection includes additional conditions: not on ground,
//    not in water/lava, not passenger
// 4. 如果 DragonSurvival API 变更，需要更新反射方法签名
//    If DragonSurvival API changes, reflection method signatures need updating
//
// 【维护者提示 / Maintainer Notes】
// - 此兼容层在 FlightAssistantForge 和 FlightAssistantFabric 中初始化
//   This compatibility layer is initialized in both FlightAssistantForge
//   and FlightAssistantFabric
// - 如果 DragonSurvival 变为硬依赖，可以移除此兼容层直接使用 API
//   If DragonSurvival becomes a hard dependency, this layer can be removed
//   to use the API directly
// - 调试日志在初始化时会输出详细信息
//   Debug logs output detailed information during initialization
// ============================================================================

package by.dragonsurvivalteam.dragonsurvival.compat.flightassistant

import dev.architectury.platform.Platform
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.player.Player
import ru.octol1ttle.flightassistant.FlightAssistant

/**
 * DragonSurvival 兼容性工具类
 * DragonSurvival compatibility utilities.
 *
 * 提供龙飞行状态检测，等效于原版的 isFallFlying()
 * Provides dragon flight state detection, equivalent to vanilla isFallFlying()
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

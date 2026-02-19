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
// 5. 使用懒加载 + 缓存避免性能问题和初始化死锁
//    Uses lazy loading + caching to avoid performance issues and initialization deadlocks
//
// 【维护者提示 / Maintainer Notes】
// - 此兼容层在 FlightAssistantForge 和 FlightAssistantFabric 中初始化
//   This compatibility layer is initialized in both FlightAssistantForge
//   and FlightAssistantFabric
// - 如果 DragonSurvival 变为硬依赖，可以移除此兼容层直接使用 API
//   If DragonSurvival becomes a hard dependency, this layer can be removed
//   to use the API directly
// - 初始化使用懒加载，避免模组加载顺序问题
//   Initialization uses lazy loading to avoid mod loading order issues
// ============================================================================

package by.dragonsurvivalteam.dragonsurvival.compat.flightassistant

import dev.architectury.platform.Platform
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import ru.octol1ttle.flightassistant.FlightAssistant
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * DragonSurvival 兼容性工具类
 * DragonSurvival compatibility utilities.
 *
 * 提供龙飞行状态检测，等效于原版的 isFallFlying()
 * Provides dragon flight state detection, equivalent to vanilla isFallFlying()
 *
 * 【性能优化 / Performance Optimization】
 * - 所有反射对象在首次调用时懒加载并缓存
 * - isDragonFlying() 每 tick 调用，但只使用缓存的 Method/Field，无反射开销
 * - 初始化失败时静默降级，不影响主模组功能
 *
 * All reflection objects are lazily loaded and cached on first call
 * isDragonFlying() is called every tick, but only uses cached Method/Field, no reflection overhead
 * Silently degrades on initialization failure, doesn't affect main mod functionality
 */
object DragonSurvivalCompat {
    private const val DRAGON_SURVIVAL_MOD_ID = "dragonsurvival"
    
    // 懒加载初始化标志（避免死锁）/ Lazy init flag (avoid deadlock)
    private var isInitialized: Boolean = false
    private var isDragonSurvivalLoaded: Boolean = false

    // Reflection caches - 缓存 Method 和 Field 对象，避免每 tick 反射
    // Cache Method and Field objects to avoid per-tick reflection
    private var dragonStateProviderClass: Class<*>? = null
    private var flightDataClass: Class<*>? = null
    private var isDragonMethod: Method? = null
    private var getDataMethod: Method? = null
    private var hasFlightField: Field? = null
    private var areWingsSpreadField: Field? = null

    /**
     * 懒加载初始化（首次调用时执行）
     * Lazy initialization (executes on first call)
     * 
     * 【为什么用懒加载 / Why lazy loading?】
     * 1. 避免模组加载顺序问题 - DragonSurvival 可能还没完成初始化
     * 2. 避免死锁 - 在 FlightAssistant 初始化时不强制加载 DragonSurvival
     * 3. 性能优化 - 只有龙玩家在线时才会初始化
     * 
     * 1. Avoids mod loading order issues - DragonSurvival may not be fully initialized
     * 2. Avoids deadlocks - Doesn't force load DragonSurvival during FlightAssistant init
     * 3. Performance optimization - Only initializes when dragon player is online
     */
    private fun ensureInitialized() {
        if (isInitialized) return
        
        isInitialized = true
        isDragonSurvivalLoaded = Platform.isModLoaded(DRAGON_SURVIVAL_MOD_ID)
        
        if (!isDragonSurvivalLoaded) {
            return
        }

        try {
            // Cache classes
            dragonStateProviderClass = Class.forName("by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider")
            flightDataClass = Class.forName("by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData")
            
            // Cache methods - 注意参数类型 / Note parameter types
            isDragonMethod = dragonStateProviderClass!!.getMethod("isDragon", Entity::class.java)
            getDataMethod = flightDataClass!!.getMethod("getData", Player::class.java)
            
            // Cache fields - 直接访问公共字段 / Access public fields directly
            hasFlightField = flightDataClass!!.getField("hasFlight")
            areWingsSpreadField = flightDataClass!!.getField("areWingsSpread")
            
            FlightAssistant.logger.info("DragonSurvival compatibility initialized successfully")
        } catch (e: Exception) {
            FlightAssistant.logger.error("Failed to initialize DragonSurvival compatibility", e)
            isDragonSurvivalLoaded = false
        }
    }

    /**
     * 检测玩家是否为龙且正在飞行/滑翔
     * Check if player is a dragon and currently flying/gliding
     * 
     * 【性能 / Performance】
     * - 每 tick 调用，但只使用缓存的反射对象，开销极小
     * - Called every tick, but only uses cached reflection objects, minimal overhead
     * 
     * @param player 要检测的玩家 / Player to check
     * @return 如果是龙且在飞行 / True if dragon and flying
     */
    fun isDragonFlying(player: LocalPlayer): Boolean {
        // 懒加载初始化 / Lazy init
        ensureInitialized()
        
        if (!isDragonSurvivalLoaded) {
            return false
        }

        try {
            // Check if player is a dragon (cached method, no reflection overhead)
            val isDragon = isDragonMethod?.invoke(null, player) as? Boolean ?: return false
            if (!isDragon) {
                return false
            }

            // Get flight data (cached method, no reflection overhead)
            val flightData = getDataMethod?.invoke(null, player) ?: return false

            // Check flight capability (cached field access, no reflection overhead)
            val hasFlight = hasFlightField?.getBoolean(flightData) ?: return false
            if (!hasFlight) {
                return false
            }

            // Check if wings are spread (cached field access, no reflection overhead)
            val areWingsSpread = areWingsSpreadField?.getBoolean(flightData) ?: return false
            
            // Dragon must not be on ground or in fluids to be considered flying
            val onGround = player.onGround()
            val inWater = player.isInWater
            val inLava = player.isInLava
            val isPassenger = player.isPassenger

            return areWingsSpread && !onGround && !inWater && !inLava && !isPassenger
        } catch (e: Exception) {
            // 静默失败，避免刷屏日志 / Silent failure to avoid log spam
            return false
        }
    }

    /**
     * 返回 DragonSurvival 是否已加载
     * Returns true if DragonSurvival is loaded
     */
    fun isLoaded(): Boolean {
        ensureInitialized()
        return isDragonSurvivalLoaded
    }
}

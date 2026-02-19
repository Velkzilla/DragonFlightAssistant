package ru.octol1ttle.flightassistant.impl.computer.data

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.max
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.DamageTypeTags
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.phys.Vec3
import ru.octol1ttle.flightassistant.FAKeyMappings
import ru.octol1ttle.flightassistant.FlightAssistant
import ru.octol1ttle.flightassistant.api.computer.Computer
import ru.octol1ttle.flightassistant.api.computer.ComputerBus
import ru.octol1ttle.flightassistant.api.util.degrees
import ru.octol1ttle.flightassistant.api.util.extensions.bottomY
import ru.octol1ttle.flightassistant.api.util.extensions.getLerpedDeltaMovement
import ru.octol1ttle.flightassistant.api.util.extensions.perSecond
import ru.octol1ttle.flightassistant.api.util.throwIfNotInRange
import ru.octol1ttle.flightassistant.config.FAConfig
import by.dragonsurvivalteam.dragonsurvival.compat.flightassistant.DragonSurvivalCompat

class AirDataComputer(computers: ComputerBus, private val mc: Minecraft) : Computer(computers) {
    val player: LocalPlayer
        get() = checkNotNull(mc.player)
    val flying: Boolean
        get() = player.isFallFlying || DragonSurvivalCompat.isDragonFlying(player)
    val level: ClientLevel
        get() = checkNotNull(mc.level)

    val position: Vec3
        get() = player.position()
    val x: Double
        get() = position.x
    val z: Double
        get() = position.z
    val altitude: Double
        get() = position.y
    val voidY: Int
        get() = level.bottomY - 64

    private val fallDistance: Double
        get() =
            if (computers.gpws.groundY == null || computers.gpws.groundY!! == Double.MAX_VALUE) Double.MAX_VALUE
//? if >=1.21.5 {
            /*else max(player.fallDistance, altitude - computers.gpws.groundY!!)
*///?} else
            else max(player.fallDistance.toDouble(), altitude - computers.gpws.groundY!!)

    val fallDistanceSafe: Boolean
        get() = player.isInWater || fallDistance <= player.maxFallDistance || isInvulnerableTo(player.damageSources().fall())

    // ============================================================================
    // 速度平滑处理 - 一阶低通滤波器 (FIR Low-pass Filter)
    // Velocity Smoothing - First-order FIR Low-pass Filter
    // ============================================================================
    //
    // 【问题 / Problem】
    // Minecraft 的 deltaMovement 每帧都在变化，导致 HUD 速度读数抖动（例如在 16-17 之间跳变）。
    // 这在 DragonSurvival 龙飞行模式下尤为明显，因为龙的飞行物理计算与原版鞘翅不同。
    //
    // Minecraft's deltaMovement changes every frame, causing HUD speed readings to flicker
    // (e.g., jumping between 16-17). This is especially noticeable in DragonSurvival's
    // dragon flight mode because dragon flight physics differ from vanilla elytra.
    //
    // 【解决方案 / Solution】
    // 使用一阶 FIR 低通滤波器平滑速度数据：
    // y[n] = α × x[n] + (1-α) × y[n-1]
    //
    // Use first-order FIR low-pass filter to smooth velocity data:
    // y[n] = α × x[n] + (1-α) × y[n-1]
    //
    // 其中 / Where:
    //   - y[n] = 当前输出（平滑后的速度）/ current output (smoothed velocity)
    //   - x[n] = 当前输入（原始速度）/ current input (raw velocity)
    //   - y[n-1] = 上次输出 / previous output
    //   - α = 滤波系数 / filter coefficient
    //
    // 【参数选择 / Parameter Selection】
    // velocityFilterAlpha = 0.2
    //   - 截止频率 / Cut-off frequency: fc ≈ 0.8 Hz @ 20 TPS
    //   - 延迟 / Delay: ~0.25 秒 / seconds
    //   - 在响应性和平滑度之间取得平衡
    //   - Balanced between responsiveness and smoothness
    //
    // 【调整指南 / Adjustment Guide】
    //   α = 0.1 → 更平滑，延迟 ~0.5s / Smoother, delay ~0.5s
    //   α = 0.2 → 推荐 / Recommended (平衡 / Balanced)
    //   α = 0.3 → 更响应，延迟 ~0.15s / More responsive, delay ~0.15s
    //
    // 【注意事项 / Caveats】
    // 1. 此滤波会影响所有依赖速度的 HUD 组件（空速仪、飞行轨迹预测等）
    //    This filter affects all velocity-dependent HUD components (airspeed indicator,
    //    flight path predictor, etc.)
    // 2. 延迟是固定的，不会随速度变化
    //    The delay is fixed and does not vary with speed
    // 3. 如果未来添加加速度相关的 HUD，需要考虑滤波对加速度计算的影响
    //    If acceleration-related HUD is added in the future, consider the filter's
    //    impact on acceleration calculations
    // 4. HudDisplayDataComputer 也使用相同的滤波器参数，确保一致性
    //    HudDisplayDataComputer uses the same filter parameters for consistency
    //
    // 【维护者提示 / Maintainer Notes】
    // - 如果发现 HUD 响应太慢，可以增大 alpha，但会增加抖动
    //   If HUD feels too sluggish, increase alpha, but this will increase flickering
    // - 如果抖动严重，可以减小 alpha，但会增加延迟
    //   If flickering is severe, decrease alpha, but this will increase delay
    // - 不要完全移除滤波，除非找到更好的替代方案
    //   Do not remove the filter entirely unless a better alternative is found
    // ============================================================================
    private var smoothedVelocity: Vec3 = Vec3.ZERO
    private val velocityFilterAlpha = 0.2
    
    val velocity: Vec3
        get() = smoothedVelocity
    val velocityPerSecond: Vec3
        get() = velocity.perSecond()
    var forwardVelocity: Vec3 = Vec3.ZERO
        private set
    var forwardVelocityPerSecond: Vec3 = Vec3.ZERO
        private set
    var forwardAcceleration: Double = 0.0
        private set

    val pitch: Float
        get() = -player.xRot.throwIfNotInRange(-90.0f..90.0f)
    val yaw: Float
        get() = Mth.wrapDegrees(player.yRot).throwIfNotInRange(-180.0f..180.0f)
    val heading: Float
        get() = (yaw + 180.0f).throwIfNotInRange(0.0f..360.0f)

    val flightPitch: Float
        get() = degrees(asin(velocity.normalize().y).toFloat())
    val flightYaw: Float
        get() = degrees(atan2(-velocity.x, velocity.z).toFloat())

    override fun tick() {
        // 一阶低通滤波：y[n] = α × x[n] + (1-α) × y[n-1]
        val rawVelocity = player.deltaMovement
        val alpha = velocityFilterAlpha
        val oneMinusAlpha = 1.0 - alpha
        
        smoothedVelocity = Vec3(
            alpha * rawVelocity.x + oneMinusAlpha * smoothedVelocity.x,
            alpha * rawVelocity.y + oneMinusAlpha * smoothedVelocity.y,
            alpha * rawVelocity.z + oneMinusAlpha * smoothedVelocity.z
        )

        forwardVelocity = computeForwardVector(velocity)
        forwardVelocityPerSecond = forwardVelocity.perSecond()
        forwardAcceleration = forwardVelocity.length() - computeForwardVector(player.getLerpedDeltaMovement(0.0f)).length()
    }

    fun automationsAllowed(checkFlying: Boolean = true): Boolean {
        if (FAKeyMappings.globalAutomationOverride.isDown) {
            return false
        }
        return (!checkFlying || flying) && (FAConfig.global.automationsAllowedInOverlays || (mc.screen == null && mc.overlay == null))
    }

    fun isInvulnerableTo(source: DamageSource): Boolean {
        if (!FAConfig.safety.considerInvulnerability) {
            return false
        }
//? if >=1.21.2 {
        /*return (player as ru.octol1ttle.flightassistant.mixin.EntityInvoker).invokeIsInvulnerableToBase(source)
*///?} else
        return player.isInvulnerableTo(source)
                || player.abilities.invulnerable && !source.`is`(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || player.abilities.mayfly && source.`is`(DamageTypeTags.IS_FALL)
    }

    fun computeForwardVector(vector: Vec3): Vec3 {
        val normalizedLookAngle: Vec3 = player.lookAngle.normalize()
        val normalizedVector: Vec3 = vector.normalize()
        return vector.scale(normalizedLookAngle.dot(normalizedVector).coerceAtLeast(0.0))
    }

    override fun reset() {
        forwardVelocity = Vec3.ZERO
        forwardVelocityPerSecond = Vec3.ZERO
        forwardAcceleration = 0.0
        smoothedVelocity = Vec3.ZERO
    }

    companion object {
        val ID: ResourceLocation = FlightAssistant.id("air_data")
    }
}
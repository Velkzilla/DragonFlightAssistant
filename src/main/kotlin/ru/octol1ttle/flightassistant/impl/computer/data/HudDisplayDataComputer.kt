package ru.octol1ttle.flightassistant.impl.computer.data

import kotlin.math.atan2
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import ru.octol1ttle.flightassistant.FlightAssistant
import ru.octol1ttle.flightassistant.api.computer.Computer
import ru.octol1ttle.flightassistant.api.computer.ComputerBus
import ru.octol1ttle.flightassistant.api.util.FATickCounter
import ru.octol1ttle.flightassistant.api.util.RenderMatrices
import ru.octol1ttle.flightassistant.api.util.degrees
import ru.octol1ttle.flightassistant.api.util.extensions.getLerpedDeltaMovement
import ru.octol1ttle.flightassistant.api.util.throwIfNotInRange

class HudDisplayDataComputer(computers: ComputerBus, private val mc: Minecraft) : Computer(computers) {
    val player: LocalPlayer
        get() = checkNotNull(mc.player)

    val isViewMirrored: Boolean
        get() = mc.options.cameraType.isMirrored

    var lerpedPosition: Vec3 = Vec3.ZERO
        private set

    // ============================================================================
    // 速度平滑处理 - 一阶低通滤波器 (FIR Low-pass Filter)
    // Velocity Smoothing - First-order FIR Low-pass Filter
    // ============================================================================
    //
    // 【重要 / IMPORTANT】
    // 此滤波器与 AirDataComputer 使用相同的参数，确保 HUD 显示与内部逻辑一致。
    // This filter uses the same parameters as AirDataComputer to ensure HUD display
    // is consistent with internal logic.
    //
    // 【问题 / Problem】
    // player.getLerpedDeltaMovement() 返回的插值速度每帧都在变化，导致：
    // - 空速仪数值跳动
    // - 飞行轨迹预测线抖动
    // - 垂直速度指示器不稳定
    //
    // Interpolated velocity from player.getLerpedDeltaMovement() changes every frame,
    // causing:
    // - Airspeed indicator values to jump
    // - Flight path predictor line to flicker
    // - Vertical speed indicator to be unstable
    //
    // 【解决方案 / Solution】
    // 使用与 AirDataComputer 相同的 FIR 低通滤波器：
    // y[n] = α × x[n] + (1-α) × y[n-1]
    //
    // Use the same FIR low-pass filter as AirDataComputer:
    // y[n] = α × x[n] + (1-α) × y[n-1]
    //
    // 【参数 / Parameters】
    // velocityFilterAlpha = 0.2
    //   - 必须与 AirDataComputer.velocityFilterAlpha 保持一致
    //   - Must be consistent with AirDataComputer.velocityFilterAlpha
    //   - 截止频率 / Cut-off frequency: fc ≈ 0.8 Hz @ 20 TPS
    //   - 延迟 / Delay: ~0.25 秒 / seconds
    //
    // 【受影响的 HUD 组件 / Affected HUD Components】
    // - SpeedDisplay (空速仪 / Airspeed Indicator)
    // - VelocityComponentsDisplay (速度分量 / Velocity Components)
    // - FlightPathDisplay (飞行轨迹预测 / Flight Path Predictor)
    // - AltitudeDisplay (高度表速度矢量 / Altimeter Velocity Vector)
    //
    // 【注意事项 / Caveats】
    // 1. 此滤波在 renderTick() 中执行，每帧调用一次
    //    This filtering is executed in renderTick(), called once per frame
    // 2. 使用 FATickCounter.partialTick 进行插值，确保与渲染同步
    //    Uses FATickCounter.partialTick for interpolation to sync with rendering
    // 3. 如果修改参数，必须同时修改 AirDataComputer 中的对应参数
    //    If modifying parameters, must also update corresponding parameters
    //    in AirDataComputer
    // 4. 重置时必须清空平滑状态，避免重启后速度异常
    //    Must clear smoothed state on reset to avoid abnormal velocity after restart
    //
    // 【维护者提示 / Maintainer Notes】
    // - 不要直接使用 player.getLerpedDeltaMovement() 作为 HUD 数据源
    //   Do not use player.getLerpedDeltaMovement() directly as HUD data source
    // - 如果未来添加新的速度相关 HUD，使用 lerpedVelocity 而不是原始数据
    //   If adding new velocity-related HUD in future, use lerpedVelocity instead
    //   of raw data
    // - 如果发现显示延迟，可以调整 alpha，但要与 AirDataComputer 保持一致
    //   If display latency is noticed, alpha can be adjusted, but must remain
    //   consistent with AirDataComputer
    // ============================================================================
    private var smoothedLerpedVelocity: Vec3 = Vec3.ZERO
    private val velocityFilterAlpha = 0.2

    var lerpedVelocity: Vec3 = Vec3.ZERO
        private set
    var lerpedForwardVelocity: Vec3 = Vec3.ZERO
        private set

    val lerpedAltitude: Double
        get() = lerpedPosition.y

    val roll: Float
        get() = degrees(atan2(-RenderMatrices.worldSpaceMatrix.m10(), RenderMatrices.worldSpaceMatrix.m11())).throwIfNotInRange(-180.0f..180.0f)

    override fun renderTick() {
        lerpedPosition = player.getPosition(FATickCounter.partialTick)
        
        // 获取原始插值速度并应用低通滤波
        val rawLerpedVelocity = player.getLerpedDeltaMovement(FATickCounter.partialTick)
        val alpha = velocityFilterAlpha
        val oneMinusAlpha = 1.0 - alpha
        
        smoothedLerpedVelocity = Vec3(
            alpha * rawLerpedVelocity.x + oneMinusAlpha * smoothedLerpedVelocity.x,
            alpha * rawLerpedVelocity.y + oneMinusAlpha * smoothedLerpedVelocity.y,
            alpha * rawLerpedVelocity.z + oneMinusAlpha * smoothedLerpedVelocity.z
        )
        lerpedVelocity = smoothedLerpedVelocity
        
        lerpedForwardVelocity = computers.data.computeForwardVector(lerpedVelocity)
    }

    override fun reset() {
        lerpedPosition = Vec3.ZERO
        smoothedLerpedVelocity = Vec3.ZERO
        lerpedVelocity = Vec3.ZERO
        lerpedForwardVelocity = Vec3.ZERO
    }

    companion object {
        val ID: ResourceLocation = FlightAssistant.id("hud_display_data")
    }
}
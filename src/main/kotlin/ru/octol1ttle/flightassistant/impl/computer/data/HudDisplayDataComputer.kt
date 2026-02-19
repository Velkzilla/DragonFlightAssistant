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
    
    // 速度平滑处理 - 一阶低通滤波器 (FIR Low-pass Filter)
    // 与 AirDataComputer 使用相同的参数
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
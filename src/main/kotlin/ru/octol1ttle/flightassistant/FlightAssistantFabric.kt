// ============================================================================
// FlightAssistant Fabric 入口点
// FlightAssistant Fabric Entry Point
// ============================================================================
//
// 【兼容性模组初始化 / Compatibility Mod Initialization】
// 按以下顺序初始化兼容性模组：
// 1. DaBRCompatFA - Do A Barrel Roll 支持（可选）
// 2. DragonSurvivalCompat - DragonSurvival 支持（可选，懒加载）
//
// Initialize compatibility mods in the following order:
// 1. DaBRCompatFA - Do A Barrel Roll support (optional)
// 2. DragonSurvivalCompat - DragonSurvival support (optional, lazy-loaded)
//
// 【注意事项 / Caveats】
// - 兼容层会自动检测目标模组是否存在，不存在时静默跳过
//   Compatibility layers automatically detect if target mods exist,
//   silently skipping if not present
// - DragonSurvivalCompat 使用懒加载，避免模组加载顺序问题
//   DragonSurvivalCompat uses lazy loading to avoid mod loading order issues
// - 如果添加新的兼容层，在此处调用 init()
//   If adding new compatibility layers, call init() here
// ============================================================================

package ru.octol1ttle.flightassistant

//? if fabric {
import dev.architectury.registry.client.keymappings.KeyMappingRegistry
import net.fabricmc.api.ClientModInitializer
import nl.enjarai.doabarrelroll.compat.flightassistant.DaBRCompatFA
import by.dragonsurvivalteam.dragonsurvival.compat.flightassistant.DragonSurvivalCompat

object FlightAssistantFabric : ClientModInitializer {
    override fun onInitializeClient() {
        FlightAssistant.init()
        DaBRCompatFA.init()
        // DragonSurvivalCompat 使用懒加载，不需要显式初始化
        // DragonSurvivalCompat uses lazy loading, no explicit initialization needed
        DragonSurvivalCompat.isLoaded()  // 触发懒加载 / Trigger lazy init
        FAKeyMappings.keyMappings.forEach(KeyMappingRegistry::register)
    }
}
//?}

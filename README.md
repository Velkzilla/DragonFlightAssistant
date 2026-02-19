<!-- ============================================================================ -->
<!-- DragonSurvival 兼容性分支 / DragonSurvival Compatibility Fork                -->
<!-- ============================================================================ -->

> [!IMPORTANT]
> **此分支添加了 DragonSurvival 模组支持** / **This fork adds DragonSurvival mod support**
> 
> 如果你在使用 DragonSurvival 模组，请使用此版本！
> If you are using DragonSurvival mod, use this version!

## 🐉 DragonSurvival 兼容性 / DragonSurvival Compatibility

### 快速预览 / Quick Preview

#### 龙飞行 HUD 演示 / Dragon Flight HUD Demo

> [!TIP]
> **视频播放 / Video Playback:** 
> - GitHub 网页可能无法直接播放视频，请**下载视频文件到本地播放**
> - 或在 GitHub 桌面客户端中查看
> - 或点击文件名在 GitHub 中打开
>
> **Video Playback on GitHub:**
> - GitHub web may not play videos directly, **download for local playback**
> - Or view in GitHub desktop client
> - Or click filename to open on GitHub

| 生存飞行和告警 / Survival Flight & Alerts | 创造滑翔 / Creative Gliding | 创造飞行 / Creative Flight | 地形逃离测试 / Terrain Escape Test |
|------------------------------------------|----------------------------|---------------------------|------------------------------------|
| [📹 生存飞行和告警.mp4](./images/生存飞行和告警.mp4) <br> <video src="./images/生存飞行和告警.mp4" width="200" controls /> | [📹 创造滑翔.mp4](./images/创造滑翔.mp4) <br> <video src="./images/创造滑翔.mp4" width="200" controls /> | [📹 创造飞行.mp4](./images/创造飞行.mp4) <br> <video src="./images/创造飞行.mp4" width="200" controls /> | [📹 地形逃离.mp4](./images/地形逃离.mp4) <br> <video src="./images/地形逃离.mp4" width="200" controls /> |

> **下载链接 / Download Links:**
> - [生存飞行和告警.mp4](./images/生存飞行和告警.mp4) (18.1 MB) - 生存模式飞行 + 告警系统演示
> - [创造滑翔.mp4](./images/创造滑翔.mp4) (13.7 MB) - 创造模式滑翔演示
> - [创造飞行.mp4](./images/创造飞行.mp4) (15.0 MB) - 创造模式飞行演示
> - [地形逃离.mp4](./images/地形逃离.mp4) (25.5 MB) - ✅ 地形逃离测试成功

#### 系统架构图 / System Architecture

![System Architecture](images/diagram.png)

#### 功能特性 / Features

- ✅ **龙飞行状态检测** - 自动识别龙的飞行/滑翔状态
- ✅ **完整 HUD 支持** - 空速仪、高度表、飞行轨迹预测
- ✅ **自动模式切换** - 地面 ↔ 飞行 自动切换显示模式
- ✅ **速度平滑处理** - FIR 滤波器消除 HUD 抖动
- ✅ **无需配置** - 自动检测 DragonSurvival 并启用支持

#### 安装要求 / Requirements

| 必需 / Required | 可选 / Optional |
|----------------|-----------------|
| DragonSurvival | YACL (配置界面) |
| GeckoLib | |
| Architectury API | |
| KFF (NeoForge) / FLK (Fabric) | |

> [!WARNING]
> **不兼容性警告 / Incompatibility Warning**
> - ❌ 不能与原版 FlightAssistant 共存
> - ❌ 服务器必须安装 DragonSurvival 才能显示龙飞行 HUD
> 
> - Cannot coexist with vanilla FlightAssistant
> - Server must have DragonSurvival for dragon flight HUD

---

## 原版 FlightAssistant / Original FlightAssistant

<p align=center>
    <img src="images/logo.png">
</p>

<p align=center>
    <a href="https://modrinth.com/mod/flightassistant">
        <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3.2.0/assets/cozy/available/modrinth_vector.svg"
            alt="Available on Modrinth"></a>
    <a href="https://modrinth.com/mod/fabric-api/">
        <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3.2.0/assets/cozy/requires/fabric-api_vector.svg"
            alt="Requires Fabric API"></a>
    <a href="https://modrinth.com/mod/flightassistant/versions?l=forge&l=neoforge">
        <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3.2.0/assets/cozy/supported/forge_vector.svg"
            alt="Available for Forge"></a>
    <a href="https://discord.gg/5kcBCvnbTp">
        <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3.2.0/assets/cozy/social/discord-plural_vector.svg"
            alt="Chat with us on Discord"></a>
</p>

---

**FlightAssistant** is a powerful client-side mod that adds fully configurable full-fledged avionics, situational awareness tools and advanced automation to Minecraft elytras, while still being friendly to casual users.

## Main Features

- Fully customizable flight HUD ([wiki](https://github.com/Octol1ttle/FlightAssistant/wiki/HUD))
  - An informative display showing flight data in real time
    <img src="https://github.com/Octol1ttle/FlightAssistant/wiki/img/hud/full_screenshot_alt.png">
- Smart flight safety alerts ([wiki](https://github.com/Octol1ttle/FlightAssistant/wiki/Alerts))
  - Be aware of unsafe flight conditions before it's too late
    <img src="https://github.com/Octol1ttle/FlightAssistant/wiki/img/hud/alert_display_alt.png">
- Non-intrusive automatic flight protections ([wiki](https://github.com/Octol1ttle/FlightAssistant/wiki/Flight-protections))
  - When you're not paying attention, FlightAssistant will protect you
  - Examples: ground collision damage avoidance, void damage avoidance, automatic elytra deploying
- Advanced autopilot with flight planning capabilities: fly safely completely hands-free ([wiki](https://github.com/Octol1ttle/FlightAssistant/wiki/Autopilot%3A-overview-and-basics))
  - Flight plans can be saved to disk and loaded later
  - With flight plans, a flight can be performed completely automatically from takeoff to landing
    <img src="https://github.com/Octol1ttle/FlightAssistant/wiki/img/hud/full_screenshot.png">

### Known Issues & Limitations

#### ⚠️ Incompatibilities

1. **Cannot coexist with vanilla FlightAssistant**
   - This fork modifies core flight detection logic
   - Installing both will cause conflicts
   - **Solution:** Use only this fork or the original, not both

2. **Server-side requirements**
   - DragonSurvival must be installed on the server
   - FlightAssistant is client-side only
   - Server without DragonSurvival = no dragon flight HUD

#### ⚠️ Testing Status

**Tested scenarios:**
- ✅ Singleplayer dragon flight
- ✅ Multiplayer dragon flight (DragonSurvival on server)
- ✅ HUD display mode switching
- ✅ Velocity smoothing (FIR filter)

**Untested/Partially tested:**
- ⚠️ Server with mixed dragon/human players
- ⚠️ Dragon flight with elytra equipped
- ⚠️ Compatibility with other flight mods (Do A Barrel Roll, etc.)
- ⚠️ Long-term stability in production servers

#### ⚠️ Technical Limitations

1. **Velocity smoothing delay**
   - FIR low-pass filter introduces ~0.25 second delay
   - This is intentional to prevent HUD flickering
   - **Trade-off:** Smooth display vs. real-time accuracy
   - **Adjustment:** Modify `velocityFilterAlpha` in code (0.1-0.3 range)

2. **Reflection-based compatibility**
   - Uses reflection to access DragonSurvival internals
   - May break if DragonSurvival API changes
   - **Risk:** Updates to DragonSurvival may require updates to this fork

3. **Flight state detection**
   - Dragon flight uses `FlightData.areWingsSpread` + `hasFlight`
   - Additional conditions: not on ground, not in water/lava, not passenger
   - May not detect edge cases (spectator mode, creative flight, etc.)

#### ⚠️ Known Bugs

| Issue | Severity | Workaround |
|-------|----------|------------|
| HUD flickering during rapid flight state changes | Low | FIR filter reduces but doesn't eliminate |
| Velocity display lag (~0.25s) | Low | Intentional design trade-off |
| Server sync delay for dragon flight state | Medium | Depends on server tick rate |
| Possible incompatibility with other flight mods | Unknown | Test before deploying |

### For Maintainers

#### Code Changes

This fork modifies the following core components:

1. **`AirDataComputer.kt`**
   - Added `DragonSurvivalCompat.isDragonFlying()` to flight detection
   - Implemented FIR low-pass filter for velocity smoothing

2. **`HudDisplayDataComputer.kt`**
   - Added independent FIR filter for HUD velocity data
   - Ensures consistency with `AirDataComputer`

3. **`FAConfig.kt`**
   - Updated `display` property to use compound flight detection

4. **`DragonSurvivalCompat.kt`** (new)
   - Reflection-based compatibility layer
   - Detects dragon flight state without hard dependency

#### Parameter Tuning

**Velocity Filter (FIR Low-pass)**
```kotlin
// AirDataComputer.kt & HudDisplayDataComputer.kt
private val velocityFilterAlpha = 0.2  // Adjust this value
```

| Alpha | Cut-off Frequency | Delay | Use Case |
|-------|-------------------|-------|----------|
| 0.1 | ~0.35 Hz | ~0.5s | Maximum smoothness |
| **0.2** | **~0.8 Hz** | **~0.25s** | **Recommended (balanced)** |
| 0.3 | ~1.4 Hz | ~0.15s | Maximum responsiveness |

**Dragon Flight Detection**
```kotlin
// DragonSurvivalCompat.kt
val isDragonFlying = isDragon && hasFlight && areWingsSpread && 
                     !onGround && !inWater && !inLava && !isPassenger
```

#### Testing Checklist

Before merging or releasing:

- [ ] Singleplayer dragon flight HUD
- [ ] Multiplayer dragon flight HUD
- [ ] Elytra flight still works
- [ ] Human player (no dragon) HUD works
- [ ] Flight state transitions (ground ↔ air)
- [ ] Velocity display stability
- [ ] No console errors on startup
- [ ] Server compatibility test

#### Reporting Issues

When reporting bugs, please include:

1. Mod versions (FlightAssistant, DragonSurvival, etc.)
2. Loader (NeoForge/Fabric) and version
3. Minecraft version
4. Singleplayer or multiplayer
5. Log file (especially DragonSurvival compatibility logs)
6. Steps to reproduce

---

## Credits

- **Original FlightAssistant:** [Octol1ttle](https://github.com/Octol1ttle/FlightAssistant)
- **DragonSurvival:** [DragonSurvival Team](https://github.com/DragonSurvivalTeam/DragonSurvival)
- **This Fork:** Community contribution for DragonSurvival compatibility

## License

This fork inherits the LGPLv3 license from the original FlightAssistant mod.
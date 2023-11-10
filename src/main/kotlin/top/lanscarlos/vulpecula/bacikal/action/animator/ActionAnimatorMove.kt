package top.lanscarlos.vulpecula.bacikal.action.animator

import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.action.animator.ActionAnimator.Companion.isValueInPlayerContext
import top.lanscarlos.vulpecula.bacikal.action.animator.ActionAnimator.Companion.retrieveFromPlayerContext
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.roundToInt

object ActionAnimatorMove : ActionAnimator.Resolver {

    override val name = arrayOf("move")

    /**
     * animator move to {location} ( step {count(100)} duration {tick(200)} ) 在 {duration} tick 内将玩家以 {step} 为步长移动到指定位置
     * animator move x {x} y {y} z {z} yaw {yaw} pitch {pitch} ( step {count(100)} duration {tick(200)} ) 在 {duration} tick 内将玩家以 {step} 为步长移动到指定位置
     */
    override fun resolve(reader: ActionAnimator.Reader): Bacikal.Parser<Any?> {
        return reader.run {
            combine(
                source(),
                optional("to", then = location()),
                optional("x", then = double()),
                optional("y", then = double()),
                optional("z", then = double()),
                optional("yaw", then = float()),
                optional("pitch", then = float()),
                argument("step", then = int(), def = 50),
                argument("duration", "time", then = long(), def = 200)
            ) { target, location, x, y, z, yaw, pitch, step, duration ->
                target.forEach { player ->
                    val isInAnimator : Boolean = isValueInPlayerContext(player.uniqueId, "armorStand")
                    if (isInAnimator) {
                        val armorStand : ArmorStand = retrieveFromPlayerContext(player.uniqueId, "armorStand") ?: return@combine

                        // 调用 moveArmorStand 函数，根据可用的参数选择传递 location 或者单独的 x, y, z, yaw, pitch
                        if (location != null) {
                            moveArmorStand(
                                armorStand,
                                targetLocation = location.toBukkitLocation(),
                                targetX = x,
                                targetY = y,
                                targetZ = z,
                                targetYaw = yaw,
                                targetPitch = pitch,
                                steps = step,
                                duration = duration
                            )
                        }
                    } else return@combine
                }
            }
        }
    }

    private fun moveArmorStand(
        armorStand : ArmorStand,
        targetLocation : Location ?= null,
        targetX : Double ?= null,
        targetY : Double ?= null,
        targetZ : Double ?= null,
        targetYaw : Float ?= null,
        targetPitch : Float ?= null,
        steps : Int,
        duration : Long
    ) {
        val initialLocation = armorStand.location.clone()
        val period = max(1, BigDecimal(duration).divide(BigDecimal(steps), RoundingMode.HALF_UP).longValueExact())


        // 用于控制任务执行次数的变量
        var currentStep = 0

        submit(period = period.toLong()) {
            // TODO 很奇怪的问题，会在未达到 maxStep 时就完成了平滑移动，暂未知如何解决与导致原因

            // 插值计算新的位置
            val newX = targetX ?: targetLocation?.x ?: initialLocation.x
            val newY = targetY ?: targetLocation?.y ?: initialLocation.y
            val newZ = targetZ ?: targetLocation?.z ?: initialLocation.z

            val hasMoved = newX != initialLocation.x || newY != initialLocation.y || newZ != initialLocation.z

            if (hasMoved) {
                // 计算线性插值XYZ
                val lerpX = initialLocation.x + (newX - initialLocation.x) * (currentStep / steps.toFloat())
                val lerpY = initialLocation.y + (newY - initialLocation.y) * (currentStep / steps.toFloat())
                val lerpZ = initialLocation.z + (newZ - initialLocation.z) * (currentStep / steps.toFloat())
                // console().sendMessage("lerpX: $lerpX, lerpY: $lerpY, lerpZ: $lerpZ")

                // 应用新的位置
                initialLocation.x = lerpX
                initialLocation.y = lerpY
                initialLocation.z = lerpZ
            }

            // 插值计算新的视角
            val newYaw = targetYaw ?: targetLocation?.yaw ?: initialLocation.yaw
            val newPitch = targetPitch ?: targetLocation?.pitch ?: initialLocation.pitch

            val hasTurned = newYaw != initialLocation.yaw || newPitch != initialLocation.pitch

            if (hasTurned) {
                // 需要确保平滑过渡，特别是当跨越-180到180界限时
                val deltaYaw = Math.floorMod((newYaw - initialLocation.yaw + 180).toInt(), 360) - 180
                val deltaPitch = Math.floorMod((newPitch - initialLocation.pitch + 180).toInt(), 360) - 180

                val lerpYaw = initialLocation.yaw + (deltaYaw) * (currentStep / steps.toFloat())
                val lerpPitch = initialLocation.pitch + (deltaPitch) * (currentStep / steps.toFloat())

                // 应用新的视角
                initialLocation.yaw = lerpYaw
                initialLocation.pitch = lerpPitch.coerceIn(-90f, 90f) // 限制pitch范围

            }

            // 只在位置或视角变化时执行teleport
            if (hasMoved || hasTurned) {
                // 在最后一步直接设置目标位置和视角（为了防止浮点数运算导致的累计误差）
                if (currentStep == steps -1) {
                    targetLocation?.let {
                        armorStand.teleport(it)
                    } ?: run {
                        armorStand.teleport(armorStand.location.apply {
                            x = targetX ?: x
                            y = targetY ?: y
                            z = targetZ ?: z
                            yaw = targetYaw ?: yaw
                            pitch = targetPitch ?: pitch
                        })
                    }
                } else {
                    armorStand.teleport(initialLocation)
                }
            }

            // 更新步数，并检查是否应该取消任务
            currentStep++

            // console().sendMessage("currentStep: $currentStep, steps: $steps, period: $period")

            if (currentStep >= steps) {
                this.cancel()
                return@submit
            }

        }
    }

    // 辅助函数，用于将角度规范化到-180到180的范围内
    private fun normalizeAngle(angle : Float) : Float {
        return ((angle % 360) + 360) % 360 - 180
    }

}
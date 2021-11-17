package me.neznamy.tab.platforms.krypton

import me.lucko.spark.api.Spark
import me.lucko.spark.api.statistic.StatisticWindow.CpuUsage
import me.lucko.spark.api.statistic.StatisticWindow.MillisPerTick
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond
import me.neznamy.tab.api.PlaceholderManager
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.service.provide
import kotlin.math.ceil

class KryptonPlaceholderRegistry(private val plugin: Main) : PlaceholderRegistry {

    override fun registerPlaceholders(manager: PlaceholderManager) {
        manager.registerPlayerPlaceholder("%displayname%", 500) { (it.player as Player).displayName }
        manager.registerPlayerPlaceholder("%vanished%", 1000, TabPlayer::isVanished)
        manager.registerPlayerPlaceholder("%health%", 100) { ceil((it.player as Player).health) }

        if (plugin.server.pluginManager.isLoaded("spark")) {
            val provider = plugin.server.servicesManager.provide(Spark::class.java)
            val spark = provider?.service
            if (spark != null) {
                // TPS
                val tps = spark.tps()!!
                manager.registerServerPlaceholder("%tps_5s%", 5000) { tps.poll(TicksPerSecond.SECONDS_5) }
                manager.registerServerPlaceholder("%tps_10s%", 10000) { tps.poll(TicksPerSecond.SECONDS_10) }
                manager.registerServerPlaceholder("%tps_1m%", 1000000) { tps.poll(TicksPerSecond.MINUTES_1) }
                manager.registerServerPlaceholder("%tps_5m%", 5000000) { tps.poll(TicksPerSecond.MINUTES_5) }
                manager.registerServerPlaceholder("%tps_15m%", 15000000) { tps.poll(TicksPerSecond.MINUTES_15) }

                // MSPT
                val mspt = spark.mspt()!!
                manager.registerServerPlaceholder("%mspt_min_10s%", 10000) { mspt.poll(MillisPerTick.SECONDS_10).min() }
                manager.registerServerPlaceholder("%mspt_max_10s%", 10000) { mspt.poll(MillisPerTick.SECONDS_10).max() }
                manager.registerServerPlaceholder("%mspt_mean_10s%", 10000) { mspt.poll(MillisPerTick.SECONDS_10).mean() }
                manager.registerServerPlaceholder("%mspt_median_10s%", 10000) { mspt.poll(MillisPerTick.SECONDS_10).median() }
                manager.registerServerPlaceholder("%mspt_percentile95_10s%", 10000) {
                    mspt.poll(MillisPerTick.SECONDS_10).percentile95th()
                }
                manager.registerServerPlaceholder("%mspt_min_1m%", 1000000) { mspt.poll(MillisPerTick.MINUTES_1).min() }
                manager.registerServerPlaceholder("%mspt_max_1m%", 1000000) { mspt.poll(MillisPerTick.MINUTES_1).max() }
                manager.registerServerPlaceholder("%mspt_mean_1m%", 1000000) { mspt.poll(MillisPerTick.MINUTES_1).mean() }
                manager.registerServerPlaceholder("%mspt_median_1m%", 1000000) { mspt.poll(MillisPerTick.MINUTES_1).median() }
                manager.registerServerPlaceholder("%mspt_percentile95_1m%", 1000000) {
                    mspt.poll(MillisPerTick.SECONDS_10).percentile95th()
                }

                // CPU process
                val process = spark.cpuProcess()
                manager.registerServerPlaceholder("%cpu_process_10s%", 10000) { process.poll(CpuUsage.SECONDS_10) }
                manager.registerServerPlaceholder("%cpu_process_1m%", 1000000) { process.poll(CpuUsage.MINUTES_1) }
                manager.registerServerPlaceholder("%cpu_process_15m%", 15000000) { process.poll(CpuUsage.MINUTES_15) }

                // CPU system
                val system = spark.cpuSystem()
                manager.registerServerPlaceholder("%cpu_system_10s%", 10000) { system.poll(CpuUsage.SECONDS_10) }
                manager.registerServerPlaceholder("%cpu_system_1m%", 1000000) { system.poll(CpuUsage.MINUTES_1) }
                manager.registerServerPlaceholder("%cpu_system_15m%", 15000000) { system.poll(CpuUsage.MINUTES_15) }
            }
        }
        registerOnlinePlaceholders(manager)
    }

    private fun registerOnlinePlaceholders(manager: PlaceholderManager) {
        manager.registerPlayerPlaceholder("%online%", 2000) { player ->
            TAB.getInstance().onlinePlayers.count { (player.player as Player).canSee(it.player as Player) }
        }
        manager.registerPlayerPlaceholder("%staffonline%", 2000) { player ->
            TAB.getInstance().onlinePlayers.count {
                it.hasPermission("tab.staff") && (player.player as Player).canSee(it.player as Player)
            }
        }
        manager.registerPlayerPlaceholder("%nonstaffonline%", 2000) { player ->
            TAB.getInstance().onlinePlayers.count {
                !it.hasPermission("tab.staff") && (player.player as Player).canSee(it.player as Player)
            }
        }
    }
}
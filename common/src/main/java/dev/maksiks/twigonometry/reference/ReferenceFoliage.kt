package dev.maksiks.twigonometry.reference

import dev.maksiks.twigonometry.api.LeafPlacerContext
import dev.maksiks.twigonometry.api.LeafPlacerContext.LayerPattern
import dev.maksiks.twigonometry.api.LeafPlacerContext.HorizontalLayer
import dev.maksiks.twigonometry.api.LeafPlacerContext.Sector
import net.minecraft.core.BlockPos

/**
 * ### Examples of various features
 *
 * you can mix and match whatever you want to achieve your desired leaf shape
 */
class ReferenceFoliage {
    companion object {
        @JvmStatic
        fun createReferenceFoliage(ctx: LeafPlacerContext, basePos: BlockPos, showDebugPercentageNametagsLag: Boolean) {
            var yOffset = 0
            val spacing = 2
            val radius = 6

            val userDebug = ctx.debug
            ctx.debug = showDebugPercentageNametagsLag

            // square
            ctx.incSquare(basePos.above(yOffset), 100, *Array(radius) { HorizontalLayer(100) })
            yOffset += spacing

            // diamond
            ctx.incDiamond(basePos.above(yOffset), 100, *Array(radius) { HorizontalLayer(100) })
            yOffset += spacing

            // disc (smooth)
            ctx.incDisc(basePos.above(yOffset), 100, smooth = true, *Array(radius) { HorizontalLayer(100) })
            yOffset += spacing

            // disc (non-smooth)
            ctx.incDisc(basePos.above(yOffset), 100, smooth = false, *Array(radius) { HorizontalLayer(100) })
            yOffset += spacing

            yOffset += 1

            LayerPattern.entries.forEach { pattern ->
                ctx.incSquare(basePos.above(yOffset), 100, *Array(radius) { HorizontalLayer(100, pattern = pattern) })
                yOffset += spacing
            }

            yOffset += 1

            // pie charts i mean sectors
            Sector.entries.forEach { sector ->
                ctx.incDisc(
                    basePos.above(yOffset),
                    100,
                    true,
                    *Array(radius) { HorizontalLayer(100, skipSector = sector.skip) })
                yOffset += spacing
            }

            yOffset += 1

            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, skipSector = Sector.N.skip or Sector.S.skip) })
            yOffset += spacing

            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, skipSector = Sector.E.skip or Sector.W.skip) })
            yOffset += spacing

            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) {
                    HorizontalLayer(
                        100,
                        skipSector = Sector.NE.skip or Sector.SE.skip or Sector.SW.skip or Sector.NW.skip
                    )
                })
            yOffset += spacing

            // skip all cardinals
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) {
                    HorizontalLayer(
                        100,
                        skipSector = Sector.N.skip or Sector.E.skip or Sector.S.skip or Sector.W.skip
                    )
                })
            yOffset += spacing

            yOffset += 1

            // more advanced stuff

            // chance variations
            ctx.incSquare(basePos.above(yOffset), 100, *Array(radius) { HorizontalLayer(75) })
            yOffset += spacing

            ctx.incSquare(basePos.above(yOffset), 100, *Array(radius) { HorizontalLayer(50) })
            yOffset += spacing

            ctx.incSquare(basePos.above(yOffset), 100, *Array(radius) { HorizontalLayer(25) })
            yOffset += spacing

            // incremental chances ("fades" outward i guess)
            ctx.incSquare(
                basePos.above(yOffset),
                100,
                HorizontalLayer(100),
                HorizontalLayer(75),
                HorizontalLayer(50),
                HorizontalLayer(25),
                HorizontalLayer(10),
                HorizontalLayer(5)
            )
            yOffset += spacing

            // centricFactor biased toward center/axes (1.0)
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, centricFactor = 1.0) })
            yOffset += spacing

            // centricFactor neutral (0.5)
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, centricFactor = 0.5) })
            yOffset += spacing

            // centricFactor biased toward corners/edges (0.0)
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, centricFactor = 0.0) })
            yOffset += spacing

            // guaranteed 50%
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(25, guaranteed = 50) })
            yOffset += spacing

            // capped 30%
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, cap = 30) })
            yOffset += spacing

            // RING
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, pattern = LayerPattern.RING) })
            yOffset += spacing

            // INNER
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, pattern = LayerPattern.INNER) })
            yOffset += spacing

            yOffset += 1

            // combinations

            // cross pattern with 50% chance
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(50, pattern = LayerPattern.CROSS) })
            yOffset += spacing

            // diamond shape with diagonal pattern
            ctx.incDiamond(
                basePos.above(yOffset), 100,
                *Array(radius) { HorizontalLayer(100, pattern = LayerPattern.DIAGONALS) })
            yOffset += spacing

            // disc with corners only
            ctx.incDisc(
                basePos.above(yOffset), 100, smooth = true,
                *Array(radius) { HorizontalLayer(100, pattern = LayerPattern.CORNERS) })
            yOffset += spacing

            // fading cross
            ctx.incSquare(
                basePos.above(yOffset), 100,
                HorizontalLayer(100, pattern = LayerPattern.CROSS),
                HorizontalLayer(75, pattern = LayerPattern.CROSS),
                HorizontalLayer(50, pattern = LayerPattern.CROSS),
                HorizontalLayer(25, pattern = LayerPattern.CROSS),
                HorizontalLayer(10, pattern = LayerPattern.CROSS),
                HorizontalLayer(5, pattern = LayerPattern.CROSS)
            )
            yOffset += spacing

            // sector skip with centric factor
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) {
                    HorizontalLayer(
                        100,
                        skipSector = Sector.N.skip or Sector.E.skip,
                        centricFactor = 1.0
                    )
                })
            yOffset += spacing

            // arc kinda
            ctx.incSquare(
                basePos.above(yOffset), 100,
                *Array(radius) {
                    HorizontalLayer(
                        100,
                        pattern = LayerPattern.RING,
                        skipSector = Sector.S.skip or Sector.SW.skip or Sector.SE.skip
                    )
                })

            ctx.debug = userDebug
        }
    }
}
package dev.maksiks.twigonometry.internal

import com.google.common.collect.Lists
import com.mojang.datafixers.util.Pair
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.Clearable
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlockContainer
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape
import net.minecraft.world.phys.shapes.DiscreteVoxelShape
import kotlin.math.max
import kotlin.math.min

class QueuedStructureTemplate(private val delegate: StructureTemplate) {
    /**
     * Bastardized version of:
     * [net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.placeInWorld]
     *
     * part of me wants to make it a mixin so i don't have to bother with vanilla updates
     * subtly breaking with some kinda new check, but i think it's better to do it myself
     *
     * entity placement is cut out
     *
     */
    @Internal
    fun placeWithQueue(
        queue: PlacementQueue,
        serverLevel: ServerLevelAccessor,
        offset: BlockPos,
        pos: BlockPos,
        settings: StructurePlaceSettings,
        random: RandomSource,
        flags: Int
    ): Boolean {
        if (delegate.palettes.isEmpty()) {
            return false
        } else {
            val list = settings.getRandomPalette(delegate.palettes, offset).blocks()
            if ((!list.isEmpty() || !settings.isIgnoreEntities && !delegate.entityInfoList.isEmpty())
                && delegate.size.x >= 1 && delegate.size.y >= 1 && delegate.size.z >= 1
            ) {
                val boundingBox = settings.boundingBox
                val list2: MutableList<BlockPos?> =
                    Lists.newArrayListWithCapacity(if (settings.shouldApplyWaterlogging()) list.size else 0)
                val list3: MutableList<BlockPos?> =
                    Lists.newArrayListWithCapacity(if (settings.shouldApplyWaterlogging()) list.size else 0)
                val list4 = ArrayList<Pair<BlockPos?, CompoundTag?>>(list.size)
                var i = Int.MAX_VALUE
                var j = Int.MAX_VALUE
                var k = Int.MAX_VALUE
                var l = Int.MIN_VALUE
                var m = Int.MIN_VALUE
                var n = Int.MIN_VALUE

                for (structureBlockInfo in StructureTemplate.processBlockInfos(
                    serverLevel,
                    offset,
                    pos,
                    settings,
                    list
                )) {
                    val blockPos = structureBlockInfo.pos
                    if (boundingBox == null || boundingBox.isInside(blockPos)) {
                        val blockstate: BlockState =
                            structureBlockInfo.state
                                .mirror(settings.mirror)
                                .rotate(settings.rotation)
                        val fluidState =
                            if (settings.shouldApplyWaterlogging()) serverLevel.getFluidState(blockPos) else null
                        val blockState =
                            structureBlockInfo.state.mirror(settings.mirror).rotate(settings.rotation)
                        if (structureBlockInfo.nbt != null) {
                            val blockEntity = serverLevel.getBlockEntity(blockPos)
                            Clearable.tryClear(blockEntity)

                            // queued here
                            // TODO twig now fix: it always places barriers and only
                            queue.enqueue(blockPos, Blocks.BARRIER.defaultBlockState(), 20)
                        }

                        // TODO Twig cur: check if the lack of a check here breaks something
                        // kind of just assuming successful placement here as it's not critical for trees
                        serverLevel.setBlock(blockPos, blockstate, flags)
                        queue.enqueue(blockPos, blockstate, flags)

                        i = min(i, blockPos.x)
                        j = min(j, blockPos.y)
                        k = min(k, blockPos.z)
                        l = max(l, blockPos.x)
                        m = max(m, blockPos.y)
                        n = max(n, blockPos.z)
                        list4.add(Pair.of<BlockPos?, CompoundTag?>(blockPos, structureBlockInfo.nbt))

                        // the nbt putting here is in PlacementQueue because the placement happens in the future

                        if (fluidState != null) {
                            if (blockState.fluidState.isSource) {
                                list3.add(blockPos)
                            } else if (blockState.block is LiquidBlockContainer) {
                                (blockState.block as LiquidBlockContainer).placeLiquid(
                                    serverLevel,
                                    blockPos,
                                    blockState,
                                    fluidState
                                )
                                if (!fluidState.isSource) {
                                    list2.add(blockPos)
                                }
                            }
                        }
                    }
                }

                var bl = true
                val directions: Array<Direction?> =
                    arrayOf(Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)

                while (bl && !list2.isEmpty()) {
                    bl = false
                    val iterator = list2.iterator()

                    while (iterator.hasNext()) {
                        val blockPos2 = iterator.next() as BlockPos
                        var fluidState2 = serverLevel.getFluidState(blockPos2)

                        var o = 0
                        while (o < directions.size && !fluidState2.isSource) {
                            val blockPos3 = blockPos2.relative(directions[o])
                            val fluidState3 = serverLevel.getFluidState(blockPos3)
                            if (fluidState3.isSource && !list3.contains(blockPos3)) {
                                fluidState2 = fluidState3
                            }
                            o++
                        }

                        if (fluidState2.isSource) {
                            val blockState2 = serverLevel.getBlockState(blockPos2)
                            val block = blockState2.block
                            if (block is LiquidBlockContainer) {
                                (block as LiquidBlockContainer).placeLiquid(
                                    serverLevel,
                                    blockPos2,
                                    blockState2,
                                    fluidState2
                                )
                                bl = true
                                iterator.remove()
                            }
                        }
                    }
                }

                if (i <= l) {
                    if (!settings.knownShape) {
                        val discreteVoxelShape: DiscreteVoxelShape =
                            BitSetDiscreteVoxelShape(l - i + 1, m - j + 1, n - k + 1)
                        val p = i
                        val q = j
                        val ox = k

                        for (pair in list4) {
                            val blockPos4: BlockPos = pair.getFirst()!!
                            discreteVoxelShape.fill(blockPos4.x - p, blockPos4.y - q, blockPos4.z - ox)
                        }

                        // TODO twig cur: queue these setblocks
                        StructureTemplate.updateShapeAtEdge(serverLevel, flags, discreteVoxelShape, p, q, ox)
                    }

                    for (pair2 in list4) {
                        val blockPos5: BlockPos = pair2.getFirst()!!
                        if (!settings.knownShape) {
                            val blockState2 = serverLevel.getBlockState(blockPos5)
                            val blockState3 = Block.updateFromNeighbourShapes(blockState2, serverLevel, blockPos5)

                            // queued here
                            if (blockState2 !== blockState3) {
                                queue.enqueue(blockPos5, blockState3, flags and -2 or 16)
                            }

                            serverLevel.blockUpdated(blockPos5, blockState3.block)
                        }

                        if (pair2.getSecond() != null) {
                            val blockEntity = serverLevel.getBlockEntity(blockPos5)
                            if (blockEntity != null) {
                                blockEntity.setChanged()
                            }
                        }
                    }
                }

                return true
            } else {
                return false
            }
        }
    }

}

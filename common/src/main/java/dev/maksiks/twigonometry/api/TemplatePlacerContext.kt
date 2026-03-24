package dev.maksiks.twigonometry.api

import dev.maksiks.twigonometry.Constants
import dev.maksiks.twigonometry.internal.ITreePlacerContext
import dev.maksiks.twigonometry.internal.QueuedStructureTemplate
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.structure.templatesystem.NopProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import java.util.Optional


// TODO: come up with some better way of masking

/**
 *
 * This lets you place a template structure anywhere you want.
 * You can use this to place the whole tree, or better, parts of it.
 * E.g. you can do the branches in code and add different handmade
 * tree tops etc.
 *
 * Or you can construct your tree out of pre-built pieces and
 * only do some logic to snap them together, the world's
 * your canvas.
 *
 * There are some randomization options for most use cases,
 * these help greatly with with adding slight differences to trees
 * so they don't look the same, which in turn helps greatly with immersion
 * for the player.
 *
 * This is handy as you'd normally counter that by making a bunch of separate
 * slightly different templates, tho these options might feel a bit limiting
 * as they're meant to be pre-made solutions rather than something fully
 * customizable.
 *
 * This accepts an NBT structure, but
 * you can use [https://bloxelizer.com/converter](https://bloxelizer.com/converter) or similar
 * for converting various other formats
 * e.g. .litematica, .schem and others
 * TODO: check if site works
 *
 */
class TemplatePlacerContext(
    superCtx: ITreePlacerContext,
    var debug: Boolean = false
) {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun ctx(
            ctx: ITreePlacerContext,
            debug: Boolean = false
        ): TemplatePlacerContext {
            return TemplatePlacerContext(ctx, debug)
        }
    }

    var queue = superCtx.queue
    val serverLevel: ServerLevel = superCtx.level.level
    val server = serverLevel.server
    val templateManager: StructureTemplateManager = server.structureManager
    val random: RandomSource = superCtx.random

    /**
     *
     * TODO: Twigonometry: splice them in
     * TODO: Twigonometry: settings
     * **Note:** Template context placements are spliced in between Placer context placements in order,
     * so you can use placers to change your template afterwards however you want.
     * E.g. carve out hz layer 3 at relative y 6, then replace it with a 50/50 hz layer of leaves
     * to create some randomization
     *
     */
    @JvmOverloads
    fun place(
        pos: BlockPos,
        loc: ResourceLocation,
        rotation: Rotation = Rotation.NONE,
        mirror: Mirror = Mirror.NONE,
        shuffleSettings: ShuffleSettings? = null,
        randomizationSettings: RandomizationSettings? = null,
    ) {
        val templateVanilla: Optional<StructureTemplate?> = templateManager.get(loc)
        if (!templateVanilla.isPresent) {
            throw IllegalStateException("Twigonometry: NBT template not found $loc")
        }
        val template = QueuedStructureTemplate(templateVanilla.get())

        val settings = StructurePlaceSettings()
            .setRotation(rotation)
            .setMirror(mirror)
            .setIgnoreEntities(false)
            .setRandom(random)
            .addProcessor(NopProcessor.INSTANCE)

        template.placeWithQueue(queue, serverLevel, pos, pos, settings, random, 19)
    }

    /**
     *
     * TODO: Twig: check
     * These let you shuffle the blocks inside your template to randomize them (including air).
     * Essentially like throwing them into a washing machine.
     *
     * If you want to contain the chaos to a specific area and not the whole template
     * (recommended, unless rectangular trees are your thing),
     * you can make a mask structure to limit the area where the shuffle happens.
     * Otherwise pass in an empty [ShuffleSettings].
     *
     * Note: traditionally a mask is more the 'hidden' area but it's kinda different depending on who you ask,
     * for simplicity in these docs mask = to be revealed/applied, NOT mask = to be hidden/excluded.
     * This is the opposite of e.g. Photoshop.
     *
     * @param seed a random seed for the shuffle
     * @param mask the mask structure. Must be the exact same size in blocks as the initial template,
     * and you can use any block you'd like for the masked area while air and structure voids will be excluded.
     *
     */
    data class ShuffleSettings(
        val seed: Long? = null,
        val mask: ResourceLocation? = null,
    )

    /**
     *
     * These are less versetile than [ShuffleSettings] but a lot more customizable.
     *
     * TODO cur: Trigonometry: make and revision the kdoc
     * @param radialGradient removes leaves radially from center to edge of the template
     * @param ditherEdges pre-built edge dithering, starts from the first non air or structure void block
     * in each direction, 1.0 = it's gone except for a few leaves,
     * 0.1 should only barely affect the edges.
     *
     */
    data class RandomizationSettings(
        var radialGradient: Array<Float>? = null,
        var ditherEdges: Float = 0.0f,
        var shuffle: Float = 0.0f,
    ) {
        // recommended Kotlin overrides
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RandomizationSettings

            if (ditherEdges != other.ditherEdges) return false
            if (shuffle != other.shuffle) return false
            if (!radialGradient.contentEquals(other.radialGradient)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ditherEdges.hashCode()
            result = 31 * result + shuffle.hashCode()
            result = 31 * result + (radialGradient?.contentHashCode() ?: 0)
            return result
        }
    }
}
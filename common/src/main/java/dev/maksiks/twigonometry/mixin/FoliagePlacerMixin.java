package dev.maksiks.twigonometry.mixin;

import dev.maksiks.twigonometry.api.WildcardFoliageAttachment;
import dev.maksiks.twigonometry.api.WildcardFoliagePlacer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to allow for wildcard foliage.
 * I honestly feel like my implementation of this is kind of weird, but I couldn't think of anything better
 * that wasn't janky or and keeps type safety.
 * <p>
 * Known issue: this removes offset since it's a part of the abstract class and I can't seem to mixin into it,
 * but this very thing also allows you to add anything you want there so that solves itself.
 * Tho if someone were to PR a way to do this and keep the offset I would appreciate it.
 */
@Mixin(FoliagePlacer.class)
public abstract class FoliagePlacerMixin {

    @Inject(
            method = "createFoliage(Lnet/minecraft/world/level/LevelSimulatedReader;Lnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageSetter;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;ILnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageAttachment;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectWildcardSupport(
            LevelSimulatedReader level, FoliagePlacer.FoliageSetter blockSetter, RandomSource random, TreeConfiguration config, int maxFreeTreeHeight, FoliagePlacer.FoliageAttachment attachment, int foliageHeight, int foliageRadius, CallbackInfo ci
    ) {
        if ((Object) this instanceof WildcardFoliagePlacer placer &&
                attachment instanceof WildcardFoliageAttachment wildcardAttachment) {
            placer.createWildcardFoliage(
                    level, blockSetter, random, config,
                    maxFreeTreeHeight, wildcardAttachment,
                    foliageHeight, foliageRadius
            );
            ci.cancel();
        }
    }
}
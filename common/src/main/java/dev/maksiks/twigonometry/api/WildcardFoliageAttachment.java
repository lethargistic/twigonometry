package dev.maksiks.twigonometry.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WildcardFoliageAttachment extends FoliagePlacer.FoliageAttachment {
    private final Map<String, Object> customDataMap = new HashMap<>();
    private boolean validated = false;

    @SafeVarargs
    public WildcardFoliageAttachment(
            BlockPos pos,
    int radiusOffset,
    boolean doubleTrunk,
    Map.Entry<String, Object>... customArgs
    ) {
        super(pos, radiusOffset, doubleTrunk);
        for (Map.Entry<String, Object> entry : customArgs) {
        customDataMap.put(entry.getKey(), entry.getValue());
    }
    }

    public WildcardFoliageAttachment with(String key, Object value) {
        customDataMap.put(key, value);
        return this;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> T get(String key) {

        return (T) customDataMap.get(key);
    }

    ///
    /// You MUST set your custom keys with {@link this#require(String...)}
    /// for your attachment to be valid.
    ///
    public <T> T getRequired(String key) {
        validateOrThrow();
        T value = get(key);
        if (value == null) {
            throw new IllegalStateException(
                    "Twigonometry: Missing required attachment key '" + key +
                            "' (did you forget to set it in the your trunk/foliage placer?)"
            );
        }
        return value;
    }

    private <T> T getOrDefault(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    public boolean has(String key) {
        return customDataMap.containsKey(key);
    }

    ///
    /// Sets the required keys for this attachment.
    /// Pseudo key-safety.
    ///
    public void require(String... keys) {
        for (String key : keys) {
            if (!customDataMap.containsKey(key)) {
                throw new IllegalStateException(
                        "Twigonometry: Missing required attachment key: " + key
                );
            }
        }
        validated = true;
    }

    private void validateOrThrow() {
        if (!validated) {
            throw new IllegalStateException(
                    "Attachment keys accessed before calling require(...). " +
                            "You must validate the WildcardFoliageAttachment first."
            );
        }
    }
}
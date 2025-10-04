package io.github.gaming32.bingo.test;

import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class MixinApplyTest {
    @Test
    public void testMixins() {
        MixinEnvironment.getCurrentEnvironment().audit();
    }
}

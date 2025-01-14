package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.GoalIconType;

public class DefaultIconRenderers {
    public static void setup() {
        IconRenderers.register(GoalIconType.EMPTY, (icon, graphics, x, y) -> {});
        IconRenderers.register(GoalIconType.BLOCK, new BlockIconRenderer());
        IconRenderers.register(GoalIconType.CYCLE, new CycleIconRenderer());
        IconRenderers.register(GoalIconType.EFFECT, new EffectIconRenderer());
        IconRenderers.register(GoalIconType.ENTITY, new EntityIconRenderer());
        IconRenderers.register(GoalIconType.ENTITY_TYPE_TAG_CYCLE, new EntityTypeTagCycleIconRenderer());
        IconRenderers.register(GoalIconType.INDICATOR, new IndicatorIconRenderer());
        IconRenderers.register(GoalIconType.INSTRUMENT_CYCLE, new InstrumentCycleIconRenderer());
        IconRenderers.register(GoalIconType.ITEM, (icon, graphics, x, y) -> graphics.renderFakeItem(icon.item(), x, y));
        IconRenderers.register(GoalIconType.ITEM_TAG_CYCLE, new ItemTagCycleIconRenderer());
        IconRenderers.register(GoalIconType.SPRITE, new SpriteIconRenderer());
    }
}

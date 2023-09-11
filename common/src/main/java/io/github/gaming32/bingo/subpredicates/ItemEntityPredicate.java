package io.github.gaming32.bingo.subpredicates;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.ext.ItemEntityExt;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemEntityPredicate implements EntitySubPredicate {
    public static final EntitySubPredicate.Type TYPE = ItemEntityPredicate::fromJson;

    private final ItemPredicate item;
    private final MinMaxBounds.Ints age;
    private final EntityPredicate droppedBy;

    public ItemEntityPredicate(ItemPredicate item, MinMaxBounds.Ints age, EntityPredicate droppedBy) {
        this.item = item;
        this.age = age;
        this.droppedBy = droppedBy;
    }

    public static ItemEntityPredicate droppedBy(ItemPredicate item, EntityPredicate droppedBy) {
        return new ItemEntityPredicate(item, MinMaxBounds.Ints.ANY, droppedBy);
    }

    public static ItemEntityPredicate fromJson(JsonObject json) {
        return new ItemEntityPredicate(
            ItemPredicate.fromJson(json.get("item")),
            MinMaxBounds.Ints.fromJson(json.get("age")),
            EntityPredicate.fromJson(json.get("dropped_by"))
        );
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (!(entity instanceof ItemEntity itemEntity)) {
            return false;
        }
        if (!item.matches(itemEntity.getItem())) {
            return false;
        }
        if (!age.matches(itemEntity.getAge())) {
            return false;
        }
        if (!droppedBy.matches(level, position, ((ItemEntityExt)itemEntity).bingo$getDroppedBy())) {
            return false;
        }
        return true;
    }

    @NotNull
    @Override
    public JsonObject serializeCustomData() {
        final JsonObject result = new JsonObject();
        result.add("item", item.serializeToJson());
        result.add("age", age.serializeToJson());
        result.add("dropped_by", droppedBy.serializeToJson());
        return result;
    }

    @NotNull
    @Override
    public Type type() {
        return TYPE;
    }
}

package io.github.gaming32.bingo.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public sealed abstract class ProgressTracker {
    public abstract ResourceLocation getType();
    protected abstract JsonObject doSerialize();

    public final JsonObject serialize() {
        JsonObject json = doSerialize();
        json.addProperty("type", getType().toString());
        return json;
    }

    public static ProgressTracker deserialize(JsonObject json) {
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(json, "type"));
        if (type.equals(Criterion.ID)) {
            return new Criterion(json);
        } else if (type.equals(AchievedRequirements.ID)) {
            return new AchievedRequirements();
        } else {
            throw new JsonParseException("Unknown progress tracker type '" + type + "'");
        }
    }

    public void validate(BingoGoal goal) {
    }

    public static final class Criterion extends ProgressTracker {
        private static final ResourceLocation ID = new ResourceLocation(Bingo.MOD_ID, "criterion");

        public final String criterion;
        public final float scale;

        public Criterion(String criterion, float scale) {
            this.criterion = criterion;
            this.scale = scale;
        }

        public Criterion(JsonObject json) {
            this(GsonHelper.getAsString(json, "criterion"), GsonHelper.getAsFloat(json, "scale", 1));
        }

        @Override
        public ResourceLocation getType() {
            return ID;
        }

        @Override
        protected JsonObject doSerialize() {
            JsonObject json = new JsonObject();
            json.addProperty("criterion", criterion);
            if (scale != 1) {
                json.addProperty("scale", scale);
            }
            return json;
        }

        @Override
        public void validate(BingoGoal goal) {
            if (!goal.getCriteria().containsKey(criterion)) {
                throw new IllegalArgumentException("Specified progress criterion '" + criterion + "' does not exist");
            }
        }
    }

    public static final class AchievedRequirements extends ProgressTracker {
        private static final ResourceLocation ID = new ResourceLocation(Bingo.MOD_ID, "achieved_requirements");

        @Override
        public ResourceLocation getType() {
            return ID;
        }

        @Override
        protected JsonObject doSerialize() {
            return new JsonObject();
        }
    }
}

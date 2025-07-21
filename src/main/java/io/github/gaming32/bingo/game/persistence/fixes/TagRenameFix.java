package io.github.gaming32.bingo.game.persistence.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import io.github.gaming32.bingo.game.persistence.PersistenceTypes;
import io.github.gaming32.bingo.game.persistence.ReferenceOrTag;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.Objects;

import static com.mojang.datafixers.DSL.named;

public class TagRenameFix extends DataFix {
    private final DSL.TypeReference unhashedTypeReference;
    private final DSL.TypeReference hashedTypeReference;
    private final String name;
    private final Map<String, String> renames;

    public TagRenameFix(
        Schema outputSchema,
        DSL.TypeReference unhashedTypeReference,
        DSL.TypeReference hashedTypeReference,
        Map<String, String> renames,
        String name
    ) {
        super(outputSchema, false);
        this.unhashedTypeReference = unhashedTypeReference;
        this.hashedTypeReference = hashedTypeReference;
        this.name = name;
        this.renames = renames;
    }

    public static TagRenameFix items(Schema outputSchema, Map<String, String> renames) {
        return new TagRenameFix(
            outputSchema,
            PersistenceTypes.ITEM_TAG,
            PersistenceTypes.ITEM_TAG_OR_REFERENCE,
            renames,
            "ItemTagRenameFix"
        );
    }

    @Override
    protected TypeRewriteRule makeRule() {
        final var unhashed = named(unhashedTypeReference.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(getInputSchema().getType(unhashedTypeReference), unhashed)) {
            throw new IllegalStateException("Unhashed type was incorrect");
        }
        final var unhashedFix = fixTypeEverywhere(
            "Unhashed " + name,
            unhashed,
            dynamic -> pair -> pair.mapSecond(tag -> renames.getOrDefault(tag, tag))
        );

        final var hashed = named(hashedTypeReference.typeName(), ReferenceOrTag.TYPE);
        if (!Objects.equals(getInputSchema().getType(hashedTypeReference), hashed)) {
            throw new IllegalStateException("Hashed type was incorrect");
        }
        final var hashedFix = fixTypeEverywhere(
            "Hashed " + name,
            hashed,
            dynamic -> pair -> pair.mapSecond(tagOrReference -> {
                if (!tagOrReference.startsWith("#")) {
                    return tagOrReference;
                }
                final var newTag = renames.get(tagOrReference.substring(1));
                return newTag != null ? '#' + newTag : tagOrReference;
            })
        );

        return TypeRewriteRule.seq(unhashedFix, hashedFix);
    }
}

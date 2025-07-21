package io.github.gaming32.bingo.game.persistence;

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import static com.mojang.datafixers.DSL.*;

public class ReferenceOrTag {
    public static final PrimitiveCodec<String> CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<String> read(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input).map(ReferenceOrTag::ensureNamespaced);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, String value) {
            return ops.createString(value);
        }

        @Override
        public String toString() {
            return "ReferenceOrTag";
        }
    };
    public static final Type<String> TYPE = new Const.PrimitiveType<>(CODEC);

    public static String ensureNamespaced(String string) {
        if (string.startsWith("#")) {
            return '#' + NamespacedSchema.ensureNamespaced(string.substring(1));
        }
        return NamespacedSchema.ensureNamespaced(string);
    }

    public static TypeTemplate homogeneous(TypeTemplate type) {
        return or(type, list(constType(NamespacedSchema.namespacedString())));
    }
}

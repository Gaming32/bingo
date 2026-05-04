package io.github.gaming32.bingo.datagen.tag;

import io.github.gaming32.bingo.data.tags.bingo.BingoEntityTypeTags;
import io.github.gaming32.bingo.datagen.BingoDataGenUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BingoEntityTypeTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {
    private static final Map<Class<? extends Entity>, Boolean> CAN_BE_AGE_LOCKED_CACHE = new HashMap<>();

    public BingoEntityTypeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        final var entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);

        final var cannotBeAgeLocked = BingoDataGenUtil.loadVanillaTag(EntityTypeTags.CANNOT_BE_AGE_LOCKED, registries);

        final var canBeAgeLockedBuilder = valueLookupBuilder(BingoEntityTypeTags.CAN_BE_AGE_LOCKED);
        final var passiveBuilder = valueLookupBuilder(BingoEntityTypeTags.PASSIVE);
        final var hostileBuilder = valueLookupBuilder(BingoEntityTypeTags.HOSTILE);

        MutableBoolean anyAgeLockable = new MutableBoolean();

        entityTypes.listElements().forEach(type -> {
            if (isPassive(type.value())) {
                passiveBuilder.add(type.value());
            } else if (!type.value().getCategory().isFriendly()) {
                hostileBuilder.add(type.value());
            }

            Class<? extends Entity> entityClass = BingoDataGenUtil.getEntityTypeClass(type.value());
            if (entityClass != null && canBeAgeLocked(entityClass) && !cannotBeAgeLocked.contains(type)) {
                canBeAgeLockedBuilder.add(type.value());
                anyAgeLockable.setValue(true);
            }
        });

        if (!anyAgeLockable.booleanValue()) {
            throw new IllegalStateException("No age-lockable entities found");
        }
    }

    public static boolean isPassive(EntityType<?> entityType) {
        if (entityType == EntityType.VILLAGER) {
            return true;
        }
        return entityType.getCategory() != MobCategory.MISC && entityType.getCategory().isFriendly();
    }

    private static boolean canBeAgeLocked(Class<? extends Entity> entityClass) {
        Boolean cacheResult = CAN_BE_AGE_LOCKED_CACHE.get(entityClass);
        if (cacheResult != null) {
            return cacheResult;
        }

        ClassNode classNode;
        try {
            classNode = MixinService.getService().getBytecodeProvider().getClassNode(entityClass.getName());
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }

        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (methodInsn.owner.equals(Type.getInternalName(AgeableMob.class)) && methodInsn.name.equals("setAgeLocked")) {
                        CAN_BE_AGE_LOCKED_CACHE.put(entityClass, true);
                        return true;
                    }
                }
            }
        }

        boolean result = entityClass != Entity.class && canBeAgeLocked(entityClass.getSuperclass().asSubclass(Entity.class));
        CAN_BE_AGE_LOCKED_CACHE.put(entityClass, result);
        return result;
    }
}

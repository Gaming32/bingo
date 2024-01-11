package io.github.gaming32.bingo.ext;

public interface CriterionTriggerExt {
    default boolean bingo$requiresClientCode() {
        return false;
    }
}

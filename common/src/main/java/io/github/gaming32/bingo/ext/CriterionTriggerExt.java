package io.github.gaming32.bingo.ext;

public interface CriterionTriggerExt {
    default boolean requiresClientCode() {
        return false;
    }
}

package de.lootz.borderline.core

data class ModuleDescriptor(
    val id: ModuleId,
    val required: Boolean = false,
    val dependsOn: Set<ModuleId> = emptySet()
)

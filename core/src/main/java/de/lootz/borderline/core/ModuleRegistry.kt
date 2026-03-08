package de.lootz.borderline.core

object ModuleRegistry {
    val all = listOf(
        ModuleDescriptor(ModuleId.ACCESSIBILITY, required = true),
        ModuleDescriptor(ModuleId.OVERLAY, dependsOn = setOf(ModuleId.ACCESSIBILITY)),
        ModuleDescriptor(ModuleId.SHORTCUTS, dependsOn = setOf(ModuleId.OVERLAY))
    )

    fun descriptor(id: ModuleId): ModuleDescriptor = all.first { it.id == id }
}

package de.lootz.borderline.core

import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleRegistryTest {
    @Test
    fun overlayDependsOnAccessibility() {
        assertTrue(ModuleRegistry.descriptor(ModuleId.OVERLAY).dependsOn.contains(ModuleId.ACCESSIBILITY))
    }
}

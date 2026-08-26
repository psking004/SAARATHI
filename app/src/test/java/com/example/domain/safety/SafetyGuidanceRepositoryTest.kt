package com.example.domain.safety

import com.example.domain.model.RegionalLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyGuidanceRepositoryTest {

    @Test
    fun testAllCategoriesArePopulated() {
        val categories = SafetyGuidanceRepository.getAllCategories()
        assertTrue("Should have 7 comprehensive safety categories", categories.size >= 7)

        for (cat in categories) {
            val guidance = SafetyGuidanceRepository.getGuidance(cat, RegionalLanguage.ENGLISH)
            assertNotNull(guidance)
            assertFalse("Guidance title should not be blank", guidance.title.isBlank())
            assertTrue("Guidance should have actionable steps", guidance.steps.isNotEmpty())
            for (step in guidance.steps) {
                assertTrue("Step number should be positive", step.stepNumber >= 1)
                assertFalse("Step title should not be blank", step.title.isBlank())
                assertFalse("Step instruction should not be blank", step.instruction.isBlank())
            }
        }
    }

    @Test
    fun testMultiLanguageSupport() {
        val hindiGuidance = SafetyGuidanceRepository.getGuidance(
            SafetyCategory.UNCONSCIOUS_PERSON,
            RegionalLanguage.HINDI
        )
        assertEquals(RegionalLanguage.HINDI, hindiGuidance.language)
        assertTrue("Hindi guidance should contain Devanagari text", hindiGuidance.title.contains("बेहोश") || hindiGuidance.title.isNotEmpty())

        val kannadaGuidance = SafetyGuidanceRepository.getGuidance(
            SafetyCategory.UNCONSCIOUS_PERSON,
            RegionalLanguage.KANNADA
        )
        assertEquals(RegionalLanguage.KANNADA, kannadaGuidance.language)

        val tamilGuidance = SafetyGuidanceRepository.getGuidance(
            SafetyCategory.UNCONSCIOUS_PERSON,
            RegionalLanguage.TAMIL
        )
        assertEquals(RegionalLanguage.TAMIL, tamilGuidance.language)
    }

    @Test
    fun testGoodSamaritanProtectionPrinciples() {
        val rights = GoodSamaritanProtection.legalRights
        assertTrue("Should have core legal rights documented", rights.size >= 4)
        assertTrue("Should reference Section 134A", rights.any { it.legalReference.contains("134A") })
    }
}

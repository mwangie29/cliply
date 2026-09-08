package com.cliply.integration.instagram

import com.cliply.domain.model.ResolutionFailureCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramAccessModelsTest {
    private fun grant(
        configuration: InstagramApiConfiguration = InstagramApiConfiguration.INSTAGRAM_LOGIN,
        accessLevel: InstagramAccessLevel = InstagramAccessLevel.STANDARD,
        review: Boolean = false,
        verification: Boolean = false,
        flag: InstagramFeatureFlag = InstagramFeatureFlag.INTERNAL,
        permissions: Set<String> = setOf("instagram_business_basic")
    ) = InstagramAccessGrant(configuration, "v26.0", "graph.instagram.com", "Instagram User", permissions, accessLevel, review, verification, flag)

    private val account = ConnectedInstagramAccount("account-ref", "ig-user-id", "cliply", "Cliply", true, InstagramAccountStatus.ACTIVE, setOf("instagram_business_basic"))

    @Test fun internalProfessionalAccountCanPassStandardAccessGate() {
        assertTrue(InstagramAccessGate.evaluate(grant(), account, externalAccount = false).allowed)
    }

    @Test fun externalAccountRequiresAdvancedAccessReviewAndVerification() {
        assertFalse(InstagramAccessGate.evaluate(grant(), account, externalAccount = true).allowed)
        assertTrue(InstagramAccessGate.evaluate(grant(accessLevel = InstagramAccessLevel.ADVANCED, review = true, verification = true), account, externalAccount = true).allowed)
    }

    @Test fun disabledFeatureFlagBlocksEvenValidAccount() {
        assertFalse(InstagramAccessGate.evaluate(grant(flag = InstagramFeatureFlag.DISABLED), account, false).allowed)
    }

    @Test fun mismatchedConfigurationHostAndTokenAreRejected() {
        val mismatched = grant(configuration = InstagramApiConfiguration.FACEBOOK_LOGIN_FOR_BUSINESS)
        assertFalse(InstagramAccessGate.evaluate(mismatched, account, false).allowed)
    }

    @Test fun nonProfessionalAndReauthAccountsAreRejected() {
        assertFalse(InstagramAccessGate.evaluate(grant(), account.copy(isProfessional = false), false).allowed)
        assertFalse(InstagramAccessGate.evaluate(grant(), account.copy(status = InstagramAccountStatus.REAUTH_REQUIRED), false).allowed)
    }

    @Test fun missingPermissionIsRejected() {
        assertFalse(InstagramAccessGate.evaluate(grant(permissions = emptySet()), account.copy(grantedPermissions = emptySet()), false).allowed)
    }

    @Test fun providerPolicyAcceptsOfficialCdnAndRejectsArbitraryUrls() {
        assertTrue(InstagramProviderMediaPolicy.validateMediaUrl("https://scontent.cdninstagram.com/v/t51.1-15/example.mp4"))
        assertFalse(InstagramProviderMediaPolicy.validateMediaUrl("https://evil.example/example.mp4"))
        assertFalse(InstagramProviderMediaPolicy.validateMediaUrl("http://scontent.cdninstagram.com/example.mp4"))
        assertFalse(InstagramProviderMediaPolicy.validateMediaUrl("https://scontent.cdninstagram.com@evil.example/example.mp4"))
    }

    @Test fun missingMediaUrlMapsToProviderUnavailableWithoutFallback() {
        val response = InstagramResolutionResponse("resolution", InstagramResolvedMediaPayload("media", null, null, "video/mp4", null, null, null, null, null, false), null)
        val result = InstagramProviderMediaPolicy.normalize(response)
        assertTrue(result is InstagramBackendResult.Failure)
        assertEquals(ResolutionFailureCode.MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER, (result as InstagramBackendResult.Failure).code)
    }
}

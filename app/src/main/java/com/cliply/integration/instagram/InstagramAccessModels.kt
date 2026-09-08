package com.cliply.integration.instagram

/** Official Meta API configuration selected for the integration. */
enum class InstagramApiConfiguration {
    INSTAGRAM_LOGIN,
    FACEBOOK_LOGIN_FOR_BUSINESS
}

enum class InstagramAccessLevel {
    STANDARD,
    ADVANCED
}

enum class InstagramAccountStatus {
    ACTIVE,
    REAUTH_REQUIRED,
    REVOKED,
    DISCONNECTED
}

enum class InstagramFeatureFlag {
    DISABLED,
    INTERNAL,
    ENABLED
}

data class InstagramAccessGrant(
    val configuration: InstagramApiConfiguration,
    val apiVersion: String,
    val apiHost: String,
    val tokenType: String,
    val permissions: Set<String>,
    val accessLevel: InstagramAccessLevel,
    val appReviewApproved: Boolean,
    val businessVerificationComplete: Boolean,
    val featureFlag: InstagramFeatureFlag = InstagramFeatureFlag.DISABLED
)

data class ConnectedInstagramAccount(
    val accountReference: String,
    val providerAccountId: String,
    val username: String?,
    val displayName: String?,
    val isProfessional: Boolean,
    val status: InstagramAccountStatus,
    val grantedPermissions: Set<String>
)

data class InstagramAccessDecision(val allowed: Boolean, val reason: String)

object InstagramAccessGate {
    private const val INSTAGRAM_HOST = "graph.instagram.com"
    private const val FACEBOOK_HOST = "graph.facebook.com"
    private const val INSTAGRAM_LOGIN_TOKEN = "Instagram User"
    private const val FACEBOOK_LOGIN_TOKEN = "Facebook User"
    private const val BASIC_INSTAGRAM_PERMISSION = "instagram_business_basic"
    private const val BASIC_FACEBOOK_PERMISSION = "instagram_basic"

    fun evaluate(grant: InstagramAccessGrant, account: ConnectedInstagramAccount, externalAccount: Boolean): InstagramAccessDecision {
        if (grant.featureFlag == InstagramFeatureFlag.DISABLED) return InstagramAccessDecision(false, "Authorized Instagram resolution is disabled.")
        if (!account.isProfessional) return InstagramAccessDecision(false, "Only Instagram professional accounts are supported.")
        if (account.status != InstagramAccountStatus.ACTIVE) return InstagramAccessDecision(false, "The Instagram account requires reauthorization.")
        val expectedHost = if (grant.configuration == InstagramApiConfiguration.INSTAGRAM_LOGIN) INSTAGRAM_HOST else FACEBOOK_HOST
        if (grant.apiHost != expectedHost) return InstagramAccessDecision(false, "The configured Meta API host does not match the selected login configuration.")
        val expectedToken = if (grant.configuration == InstagramApiConfiguration.INSTAGRAM_LOGIN) INSTAGRAM_LOGIN_TOKEN else FACEBOOK_LOGIN_TOKEN
        if (grant.tokenType != expectedToken) return InstagramAccessDecision(false, "The configured Meta token type does not match the selected login configuration.")
        val requiredPermission = if (grant.configuration == InstagramApiConfiguration.INSTAGRAM_LOGIN) BASIC_INSTAGRAM_PERMISSION else BASIC_FACEBOOK_PERMISSION
        if (requiredPermission !in grant.permissions || requiredPermission !in account.grantedPermissions) return InstagramAccessDecision(false, "The connected account has not granted the required Instagram permission.")
        if (externalAccount && (grant.accessLevel != InstagramAccessLevel.ADVANCED || !grant.appReviewApproved || !grant.businessVerificationComplete)) return InstagramAccessDecision(false, "Advanced Access, App Review, and Business Verification are required for external accounts.")
        return InstagramAccessDecision(true, "Authorized Instagram access is ready for this account.")
    }
}

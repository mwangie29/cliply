# Authorized Instagram Backend Contract

This document defines the backend boundary required before Cliply can enable authorized Instagram resolution. It is an implementation contract, not a claim that Meta access or the backend currently exists.

## Product boundary

The backend may resolve only Instagram professional-account media for an authenticated Cliply user whose account connection is active and authorized. It must reject anonymous arbitrary Reel lookup, raw provider tokens, arbitrary media URLs, webpage scraping, private endpoints, cookies, DRM bypass, and access-control circumvention.

## Internal endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/v1/integrations/instagram/connect` | Return a short-lived backend-owned OAuth start URL |
| GET | `/v1/integrations/instagram/accounts` | Return connected-account summaries without tokens |
| POST | `/v1/integrations/instagram/{accountRef}/disconnect` | Disconnect and revoke/delete token material |
| POST | `/v1/resolutions/instagram` | Resolve an authorized Reel permalink |
| GET | `/v1/resolutions/{resolutionId}` | Read a short-lived resolution record |
| POST | `/v1/download-authorizations` | Create a job-bound short-lived media handoff |

Every endpoint is authenticated using the Cliply application session. The backend independently verifies that `accountRef` belongs to the authenticated Cliply user.

## Resolution request

```json
{
  "accountReference": "cliply-account-ref",
  "permalink": "https://www.instagram.com/reel/example/"
}
```

The request must not accept a Meta access token, media URL, provider media ID supplied by the client, or arbitrary destination URL.

## Successful response

```json
{
  "resolutionId": "resolution-id",
  "source": "AUTHORIZED_INSTAGRAM",
  "media": {
    "id": "provider-media-id",
    "platform": "INSTAGRAM",
    "title": "Optional safe title",
    "thumbnailUrl": null,
    "mimeType": "video/mp4",
    "width": 1080,
    "height": 1920,
    "durationMs": 12000,
    "fileSize": null,
    "quality": "Original",
    "supportsRange": false
  },
  "expiresAt": "2026-09-08T14:00:00Z"
}
```

The response must not expose long-lived credentials. The media handoff should use a short-lived, job-bound authorization or a short-lived provider URL, depending on the approved data-processing model.

## Typed failure response

```json
{
  "code": "MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER",
  "message": "This media is not available for download through the connected Instagram account."
}
```

The backend must map provider failures to safe stable codes:

- `AUTHORIZED_ACCOUNT_SCOPE_REQUIRED`
- `AUTH_REAUTH_REQUIRED`
- `AUTH_PERMISSION_REQUIRED`
- `MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER`
- `MEDIA_NOT_FOUND`
- `PROVIDER_RATE_LIMITED`
- `PROVIDER_UNAVAILABLE`
- `INVALID_MEDIA_RESPONSE`

Raw Meta response bodies, tokens, signed URLs, authorization codes, and internal stack traces must not be returned to Android.

## OAuth and account record

OAuth is backend-owned. The Android app receives a connection URL and later a non-secret account summary. The backend stores a record equivalent to:

```text
AuthorizedAccount
- id
- cliplyUserId
- provider = INSTAGRAM
- providerAccountId
- username/displayName
- accountType = PROFESSIONAL
- grantedScopes
- tokenReference
- tokenExpiresAt
- lastValidatedAt
- status = ACTIVE / REAUTH_REQUIRED / REVOKED / DISCONNECTED
- createdAt
- updatedAt
```

Tokens are encrypted at rest, accessible only to the Meta integration service, excluded from logs and analytics, and deleted or revoked when the user disconnects or policy requires deletion.

## Provider resolution policy

The service should locate the Reel within the authorized account’s permitted media scope and request only approved fields. A successful result requires:

1. HTTPS Instagram permalink with a supported Reel path.
2. Authenticated Cliply user.
3. Account reference owned by that user.
4. Active professional-account authorization.
5. Media object within the authorized account’s permitted scope.
6. Video/Reel media type.
7. Official Meta media URL present, HTTPS, approved-host, valid, and unexpired or refreshable.

If Meta omits `media_url`, the service must return `MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER`. It must not attempt scraping or an unofficial fallback.

## Download authorization

A download authorization must be bound to:

```text
resolutionId
DownloadJob.id
Cliply user ID
provider account reference
expiration time
```

The transfer service must validate scheme, host, expiration, response MIME, response size, and redirect destination. It must reject arbitrary client-provided URLs and prevent cross-user or replayed resolution identifiers.

## Feature flag

Production enablement is controlled by:

```text
instagram_authorized_resolution = disabled | internal | enabled
```

The default is `disabled`. Direct HTTPS media remains available regardless of this flag.

## Required backend verification

Before enabling the feature, test:

- OAuth callback and token lifecycle.
- Professional-account and scope checks.
- Account-reference ownership isolation.
- Permalink matching and pagination.
- `media_url` present and omitted.
- Permission, expiry, revocation, rate-limit, and provider-unavailable errors.
- SSRF and redirect defenses.
- Resolution replay and cross-user access.
- Token and signed-URL redaction.
- Job-bound download authorization.
- Deletion and disconnect behavior.

No live Meta credentials belong in the Android repository or APK.

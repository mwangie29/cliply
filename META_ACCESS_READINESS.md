# Cliply Meta Access Readiness

This document operationalizes the approved architecture plan and records the current boundary for official Instagram integration. It is not evidence that Meta access has been granted.

## Current status

| Gate | Status | Evidence or next action |
|---|---|---|
| Product boundary | Complete | Professional accounts only; no anonymous Reel resolution, scraping, private APIs, cookies, or bypasses |
| Android/backend seam | Complete | Android sends an internal account reference and permalink; provider credentials remain server-side |
| Direct-media MVP path | Preserved | Existing direct HTTPS resolver remains independent of Instagram authorization |
| Meta login configuration | Pending product-owner selection | Choose Instagram Login unless a required approved capability needs Facebook Login for Business |
| Meta app creation | Pending | Create/configure the Cliply app under the correct business portfolio |
| Standard Access test | Pending | Add a designated professional test account and complete OAuth round trip |
| Business Verification | Pending if Advanced Access is required | Prepare legal entity, domain, privacy, deletion, and contact evidence |
| App Review | Pending if serving external accounts | Submit exact permission/use-case mapping and reviewer screencast |
| Advanced Access | Pending if serving external accounts | Do not enable public access until granted |
| Production feature flag | Disabled | `instagram_authorized_resolution = disabled` |

## Required Meta configuration record

Complete this record before any live implementation:

```text
Meta app ID: pending
Business portfolio: pending
Login configuration: pending
API host: pending
Pinned API version: pending
Requested permissions: pending
Access level: pending
Business verification status: pending
App Review status: pending
Privacy policy URL: pending
Data deletion URL: pending
OAuth redirect URI: pending
Feature flag: disabled
```

## Access-granting workflow

1. Create or select the Cliply Meta app under the correct business portfolio.
2. Configure the selected official Instagram login product.
3. Register the HTTPS backend OAuth redirect URI and required domains.
4. Add the minimum permission set required for the approved media-resolution use case.
5. Add internal app roles and a designated Instagram professional test account.
6. Complete the backend-owned OAuth callback and token exchange using test credentials.
7. Verify the account is professional, active, and has the required granted permissions.
8. Confirm account-reference ownership isolation and token redaction.
9. If external users will be supported, complete Business Verification and request Advanced Access/App Review.
10. Provide Meta reviewers with the real flow: connect, authorize, resolve an authorized Reel, download when `media_url` is available, and safely report unavailable media when it is omitted.
11. Keep the public feature disabled until the approved permissions and use case match the implementation.

## Backend contract requirements

The backend must expose only the narrow endpoints in `AUTHORIZED_INSTAGRAM_BACKEND_CONTRACT.md`:

- OAuth start and callback handling.
- Connected-account summaries without tokens.
- Explicit disconnect and token deletion/revocation handling.
- Authorized Reel resolution by account reference and permalink.
- Short-lived resolution lookup.
- Job-bound download authorization.

The backend must reject raw provider tokens, arbitrary media URLs, arbitrary provider media IDs supplied by the client, cross-user account references, and unsupported account types. It must map omitted `media_url` to `MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER` and must not invoke an unofficial fallback.

## Rollout gates

### Internal gate

Standard Access, internal app roles, and designated professional test accounts only. The feature may be exercised through deterministic tests and a controlled backend environment, but it is not a public promise.

### External-user gate

Requires the relevant Advanced Access, Business Verification, and App Review outcomes. The approved permissions, API version, account scope, data retention, privacy policy, deletion flow, and reviewer screencast must match production behavior.

### Production safety gate

Before changing the feature flag from `disabled`, verify OAuth lifecycle, reauthorization, disconnect deletion, account ownership isolation, permalink matching, pagination, media URL validation, expiry refresh, SSRF/redirect defenses, replay protection, job binding, response MIME/size limits, and token/signed-URL redaction.

## Android behavior

Android receives only a safe account summary and internal account reference. It never stores Meta app secrets, authorization codes, long-lived access tokens, or raw provider payloads. A successful provider resolution enters the existing normalized `ResolvedMedia` and `DownloadJob` pipeline. Direct HTTPS downloads continue to work regardless of the Instagram feature flag.

## References

- [Cliply MVP Technical Specification](./Cliply%20MVP%20Technical%20Specification.md)
- [Authorized Instagram Backend Contract](./AUTHORIZED_INSTAGRAM_BACKEND_CONTRACT.md)
- [Approved architecture plan](../../plan.md)

package com.cliply.share

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class ShareIntentParser { fun extractText(intent: Intent): String? = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() }
class ShareReceiverActivity : Activity() {
    private val validator = UrlValidator()
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); val raw = ShareIntentParser().extractText(intent); val result = validator.validate(raw); Toast.makeText(this, if (result.isValid) "Link received by Cliply" else "This link is not supported", Toast.LENGTH_SHORT).show(); finish() }
}

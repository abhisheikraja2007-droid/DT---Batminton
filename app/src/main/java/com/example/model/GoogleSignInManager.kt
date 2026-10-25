package com.example.model

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.json.JSONObject

object GoogleSignInManager {

  private const val TAG = "GoogleSignInManager"
  private const val FALLBACK_CLIENT_ID = "994660975565-brjnoq8khe1su1ambttiibndq3unrnrj.apps.googleusercontent.com"

  fun getWebClientId(context: Context): String {
    return try {
      val jsonString = context.assets.open("firebase-applet-config.json").bufferedReader().use { it.readText() }
      val json = JSONObject(jsonString)
      val clientId = json.optString("oAuthClientId", FALLBACK_CLIENT_ID)
      if (clientId.isNotBlank()) clientId else FALLBACK_CLIENT_ID
    } catch (e: Exception) {
      Log.w(TAG, "Could not load web client ID from assets/firebase-applet-config.json: ${e.message}")
      FALLBACK_CLIENT_ID
    }
  }

  fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val webClientId = getWebClientId(context)
    Log.d(TAG, "Configuring GoogleSignInOptions with Web Client ID: $webClientId")
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(webClientId)
      .requestEmail()
      .build()
    return GoogleSignIn.getClient(context, gso)
  }
}

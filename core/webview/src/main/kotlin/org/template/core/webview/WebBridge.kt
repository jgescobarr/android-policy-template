package org.template.core.webview

import android.webkit.WebView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewCompat.addWebMessageListener
import androidx.webkit.WebViewFeature

/**
 * Puente de comunicación entre contenido web y código nativo.
 *
 * Referencia normativa:
 * - Manual de Arquitectura, sección 2.7 (Gateway) — WebBridge es la
 *   especialización de Gateway para WebView / comunicación híbrida.
 * - Manual de Seguridad, sección 8.1 — "La comunicación ... debe realizarse
 *   exclusivamente mediante WebMessageListener ... Se prohíbe el uso del
 *   método addJavascriptInterface()."
 * - Manual de Seguridad, sección 8.2 — lista blanca explícita de orígenes,
 *   nunca wildcard "*".
 *
 * TRACEABILITY.md > :core:webview >
 *   "Sanitización de todos los mensajes entrantes desde WebView" (Crítico · PR)
 *   "WebViews con acceso a archivos locales deshabilitado" (Menor · PR)
 *
 * Nótese la ausencia total de `addJavascriptInterface` en este archivo —
 * es una omisión deliberada, no un descuido.
 */
class WebBridge(
    private val allowedOrigins: Set<String>,
    private val onValidMessage: (WebBridgeMessage) -> Unit,
    private val onRejectedMessage: (origin: String, reason: String) -> Unit,
) : DefaultLifecycleObserver {

    private var targetWebView: WebView? = null

    init {
        require(allowedOrigins.none { it == "*" }) {
            "WebBridge: lista blanca de orígenes no puede contener wildcard \"*\" (Seg. 8.2)"
        }
    }

    fun attach(webView: WebView, lifecycleOwner: LifecycleOwner? = null) {
        targetWebView = webView
        configureSecureSettings(webView)

        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            onRejectedMessage("", "WebMessageListener no soportado en este WebView")
            return
        }

        addWebMessageListener(
            webView,
            "nativeBridge",
            allowedOrigins,
            WebViewCompat.WebMessageListener { _, message, sourceOrigin, _, _ ->
                handleIncoming(sourceOrigin.toString(), message.data ?: "")
            },
        )

        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        targetWebView?.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        targetWebView?.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        targetWebView?.let { webView ->
            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                WebViewCompat.removeWebMessageListener(webView, "nativeBridge")
            }
            webView.stopLoading()
            webView.destroy()
        }
        targetWebView = null
        owner.lifecycle.removeObserver(this)
    }

    /**
     * Manual de Seguridad, sección 8.5 (Sandboxing de Contenido) —
     * deshabilita acceso a archivos locales y habilita Safe Browsing.
     */
    private fun configureSecureSettings(webView: WebView) {
        webView.settings.apply {
            allowFileAccess = false
            allowContentAccess = false
            javaScriptEnabled = true // requerido para WebMessageListener
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            androidx.webkit.WebSettingsCompat.setSafeBrowsingEnabled(webView.settings, true)
        }
    }

    /**
     * Manual de Seguridad, sección 8.3 (Sanitización de Datos Entrantes) —
     * todo mensaje se trata como input no confiable hasta validarse.
     */
    private fun handleIncoming(origin: String, rawMessage: String) {
        if (origin !in allowedOrigins) {
            onRejectedMessage(origin, "Origen no está en la lista blanca")
            return
        }
        val parsed = WebBridgeMessage.parseOrNull(rawMessage)
        if (parsed == null) {
            onRejectedMessage(origin, "Mensaje malformado o esquema desconocido")
            return
        }
        onValidMessage(parsed)
    }
}

/**
 * Mensaje ya validado y con esquema conocido. El parseo real (JSON schema,
 * campos obligatorios) queda como TODO — depende del contrato específico
 * del contenido web de cada :feature que use WebBridge.
 */
data class WebBridgeMessage(val type: String, val payload: String) {
    companion object {
        fun parseOrNull(raw: String): WebBridgeMessage? {
            // TODO: reemplazar por deserialización real con validación de
            // esquema (kotlinx.serialization) al integrar el primer :feature
            // que consuma WebBridge. Ver TRACEABILITY.md > :core:webview.
            if (raw.isBlank()) return null
            return WebBridgeMessage(type = "unknown", payload = raw)
        }
    }
}

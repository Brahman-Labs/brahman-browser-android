package com.brahmanlabs.browser

import android.content.Context
import android.net.Uri
import java.util.concurrent.atomic.AtomicInteger

class AdBlocker private constructor(private val context: Context) {

    private val prefs = context.getSharedPreferences("brahman_shields", Context.MODE_PRIVATE)
    private val blockedCount = AtomicInteger(0)

    companion object {
        @Volatile
        private var INSTANCE: AdBlocker? = null

        fun getInstance(context: Context): AdBlocker {
            return INSTANCE ?: synchronized(this) {
                AdBlocker(context.applicationContext).also { INSTANCE = it }
            }
        }

        private val BLOCKED_DOMAINS = setOf(
            "doubleclick.net","googleadservices.com","googlesyndication.com",
            "googletagmanager.com","googletagservices.com","google-analytics.com",
            "adservice.google.com","pagead2.googlesyndication.com",
            "connect.facebook.net","graph.facebook.com","pixel.facebook.com",
            "ads.twitter.com","static.ads-twitter.com","analytics.twitter.com",
            "amazon-adsystem.com","advertising.amazon.com",
            "outbrain.com","taboola.com","trc.taboola.com","cdn.taboola.com",
            "criteo.com","criteo.net","bidswitch.net","pubmatic.com",
            "openx.net","openx.com","rubiconproject.com","rubiconproject.net",
            "adnxs.com","ib.adnxs.com","appnexus.com","adsrvr.org",
            "turn.com","quantserve.com","quantcount.com",
            "scorecardresearch.com","comscore.com","moatads.com","moat.com",
            "chartbeat.com","chartbeat.net","static.chartbeat.com",
            "hotjar.com","static.hotjar.com","fullstory.com","rs.fullstory.com",
            "mixpanel.com","cdn.mxpnl.com","amplitude.com","api.amplitude.com",
            "segment.com","segment.io","cdn.segment.com",
            "optimizely.com","cdn.optimizely.com",
            "mouseflow.com","a.mouseflow.com","crazyegg.com","script.crazyegg.com",
            "atdmt.com","microsoftatdmt.com","msads.net","bat.bing.com",
            "ads.yahoo.com","adtech.de","yieldmanager.com","advertising.com",
            "adroll.com","d.adroll.com","s.adroll.com","propellerads.com",
            "bluekai.com","bkrtx.com","buysellads.com","carbonads.com",
            "casalemedia.com","contextweb.com","pulsepoint.com",
            "demdex.net","dpm.demdex.net","doubleverify.com",
            "flashtalking.com","servedby.flashtalking.com",
            "fwmrm.net","lkqd.net","histats.com","s10.histats.com",
            "indexww.com","lijit.com","sovrn.com","liveintent.com",
            "lotame.com","crwdcntrl.net","mathtag.com","cm.mathtag.com",
            "media.net","cn.media.net","mediaplex.com","mediamath.com",
            "mopub.com","ads.mopub.com","newrelic.com","bam.nr-data.net",
            "nielsen.com","imrworldwide.com","omniture.com","omtrdc.net",
            "parsely.com","srv.pixel.parsely.com","permutive.com",
            "revcontent.com","rfihub.com","rlcdn.com","pippio.com",
            "smaato.net","smartadserver.com","smartclip.net",
            "sonobi.com","spotxchange.com","spotx.tv",
            "statcounter.com","c.statcounter.com","tapad.com",
            "tradedoubler.com","triplelift.com","tlx.3lift.com",
            "undertone.com","viglink.com","api.viglink.com",
            "webtrends.com","xaxis.com","yieldmo.com","zedo.com",
            "zeotap.com","zemanta.com","pingdom.com","dataxu.com",
            "an.yandex.ru","mc.yandex.ru","adform.net","track.adform.net",
            "eyeota.net","tns-counter.ru","openstat.net","counter.yadro.ru"
        )
    }

    // ── Shields on/off ──────────────────────────────────────
    fun isShieldsEnabled(pageUrl: String): Boolean {
        val d = domain(pageUrl); if (d.isEmpty()) return false
        return prefs.getBoolean("shield_$d", true)
    }
    fun setShieldsEnabled(pageUrl: String, v: Boolean) = set("shield_${domain(pageUrl)}", v)

    // ── HTTPS Upgrade ───────────────────────────────────────
    fun isHttpsUpgradeEnabled(pageUrl: String): Boolean {
        val d = domain(pageUrl); if (d.isEmpty()) return false
        return prefs.getBoolean("https_$d", true)
    }
    fun setHttpsUpgradeEnabled(pageUrl: String, v: Boolean) = set("https_${domain(pageUrl)}", v)

    // ── Block Scripts ───────────────────────────────────────
    fun isScriptsBlocked(pageUrl: String): Boolean {
        val d = domain(pageUrl); if (d.isEmpty()) return false
        return prefs.getBoolean("scripts_$d", false)
    }
    fun setScriptsBlocked(pageUrl: String, v: Boolean) = set("scripts_${domain(pageUrl)}", v)

    // ── Forget on close ─────────────────────────────────────
    fun isForgetOnClose(pageUrl: String): Boolean {
        val d = domain(pageUrl); if (d.isEmpty()) return false
        return prefs.getBoolean("forget_$d", false)
    }
    fun setForgetOnClose(pageUrl: String, v: Boolean) = set("forget_${domain(pageUrl)}", v)
    fun hasAnyForgetOnClose(): Boolean =
        prefs.all.entries.any { it.key.startsWith("forget_") && it.value == true }

    // ── Block Cookies ───────────────────────────────────────
    fun isCookiesBlocked(pageUrl: String): Boolean {
        val d = domain(pageUrl); if (d.isEmpty()) return false
        return prefs.getBoolean("cookies_$d", false)
    }
    fun setCookiesBlocked(pageUrl: String, v: Boolean) = set("cookies_${domain(pageUrl)}", v)

    // ── Block Fingerprinting ────────────────────────────────
    fun isFingerprintingBlocked(pageUrl: String): Boolean {
        val d = domain(pageUrl); if (d.isEmpty()) return false
        return prefs.getBoolean("fp_$d", true)
    }
    fun setFingerprintingBlocked(pageUrl: String, v: Boolean) = set("fp_${domain(pageUrl)}", v)

    // ── Ad blocking ─────────────────────────────────────────
    fun shouldBlock(requestUrl: String): Boolean {
        return try {
            val h = Uri.parse(requestUrl).host?.lowercase() ?: return false
            BLOCKED_DOMAINS.any { d -> h == d || h.endsWith(".$d") }
        } catch (e: Exception) { false }
    }

    fun incrementBlockedCount() { blockedCount.incrementAndGet() }
    fun getBlockedCount(): Int = blockedCount.get()
    fun resetBlockedCount() { blockedCount.set(0) }

    // ── Helpers ─────────────────────────────────────────────
    private fun set(key: String, v: Boolean) = prefs.edit().putBoolean(key, v).apply()
    fun domain(url: String): String = try {
        Uri.parse(url).host?.removePrefix("www.") ?: ""
    } catch (e: Exception) { "" }
}

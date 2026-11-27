package io.github.orth4x0r.venty.ble

import kotlinx.coroutines.flow.Flow

/** A live connection to a Venty, real or simulated. */
interface VentyLink : AutoCloseable {
    /** Raw notifications from the device. The flow fails with [LinkLostException] when the link drops. */
    val incoming: Flow<ByteArray>

    /** Sends one request packet. Safe to call from several coroutines; sends are serialised. */
    suspend fun send(packet: ByteArray)

    /** Tries to revive a link that is up but silent, e.g. by re-subscribing to notifications. */
    suspend fun nudge() {}

    /** Reads the link's signal strength in dBm, or null if the link can't. */
    suspend fun readRssi(): Int? = null

    /**
     * Describes every GATT service, characteristic and descriptor the device offers, with the value
     * of each readable characteristic, one line each. Empty for links that can't.
     */
    suspend fun describeGatt(): List<String> = emptyList()
}

class LinkLostException(message: String) : Exception(message)

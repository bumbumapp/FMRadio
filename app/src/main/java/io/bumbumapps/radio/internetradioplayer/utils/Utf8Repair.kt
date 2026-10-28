package io.bumbumapps.radio.internetradioplayer.utils

/**
 * Some station directories send UTF-8 text that was decoded as Latin-1 on their side ("HÃ¤gele").
 * Re-decode such strings; anything that does not round-trip cleanly is returned unchanged.
 */
fun String.repairUtf8(): String {
    if (none { it == 'Ã' || it == 'Â' || it == 'Ð' || it == 'Ñ' }) return this
    if (any { it.code > 0xFF }) return this
    val repaired = String(toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
    return if ('�' in repaired) this else repaired
}

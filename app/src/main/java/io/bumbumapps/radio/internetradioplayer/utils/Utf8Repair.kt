package io.bumbumapps.radio.internetradioplayer.utils

/**
 * Some station directories send UTF-8 text that was decoded as Latin-1 / Windows-1252 on their side
 * ("HÃ¤gele", "GrÃ¼ÃŸe"). Re-decode such strings; anything that does not round-trip cleanly is returned
 * unchanged.
 */
fun String.repairUtf8(): String {
    if (none { it == 'Ã' || it == 'Â' || it == 'Ð' || it == 'Ñ' }) return this
    val bytes = ByteArray(length)
    for ((i, c) in withIndex()) {
        val b = if (c.code <= 0xFF) c.code else CP1252_HIGH[c] ?: return this
        bytes[i] = b.toByte()
    }
    val repaired = String(bytes, Charsets.UTF_8)
    return if ('�' in repaired) this else repaired
}

/** Windows-1252 characters in 0x80..0x9F that are not Latin-1 code points. */
private val CP1252_HIGH: Map<Char, Int> = mapOf(
    '€' to 0x80, '‚' to 0x82, 'ƒ' to 0x83, '„' to 0x84, '…' to 0x85, '†' to 0x86, '‡' to 0x87,
    'ˆ' to 0x88, '‰' to 0x89, 'Š' to 0x8A, '‹' to 0x8B, 'Œ' to 0x8C, 'Ž' to 0x8E, '‘' to 0x91,
    '’' to 0x92, '“' to 0x93, '”' to 0x94, '•' to 0x95, '–' to 0x96, '—' to 0x97, '˜' to 0x98,
    '™' to 0x99, 'š' to 0x9A, '›' to 0x9B, 'œ' to 0x9C, 'ž' to 0x9E, 'Ÿ' to 0x9F
)

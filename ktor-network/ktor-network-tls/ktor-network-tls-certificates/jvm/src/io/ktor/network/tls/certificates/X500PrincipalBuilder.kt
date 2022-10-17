/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.tls.certificates

import javax.security.auth.x500.*

/**
 * Builds an [X500Principal], using standard attributes for Distinguished Names defined in
 * [RFC1779 Section 2.3](https://datatracker.ietf.org/doc/html/rfc1779#section-2.3) (Table 1).
 */
public fun buildX500Principal(configure: X500PrincipalBuilder.() -> Unit): X500Principal =
    X500PrincipalBuilder().apply(configure).build()

/**
 * A builder for [X500Principal] objects, using standard attributes for Distinguished Names defined in
 * [RFC1779 Section 2.3](https://datatracker.ietf.org/doc/html/rfc1779#section-2.3) (Table 1).
 */
public class X500PrincipalBuilder {

    public var commonName: String? = null
    public var localityName: String? = null
    public var stateOrProvinceName: String? = null
    public var organizationName: String? = null
    public var organizationalUnitName: String? = null
    public var countryName: String? = null
    public var streetAddress: String? = null

    public fun build(): X500Principal {
        val rdnMap = buildMap {
            commonName?.let { put("CN", it) }
            localityName?.let { put("L", it) }
            stateOrProvinceName?.let { put("ST", it) }
            organizationName?.let { put("O", it) }
            organizationalUnitName?.let { put("OU", it) }
            countryName?.let { put("C", it) }
            streetAddress?.let { put("STREET", it) }
        }
        val dn = rdnMap.entries.joinToString(", ") { (key, value) -> "$key=${value.escapeForDN()}" }
        return X500Principal(dn)
    }

    // see BNF in https://datatracker.ietf.org/doc/html/rfc1779#section-2.3
    private fun String.escapeForDN() = replace(Regex("""[,=\n\r+<>#;\\"]""")) { match -> "\\${match.value}" }
}

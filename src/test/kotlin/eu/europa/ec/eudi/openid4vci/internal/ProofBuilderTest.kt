/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.eudi.openid4vci.internal

import com.nimbusds.jose.JWSAlgorithm
import eu.europa.ec.eudi.openid4vci.CredentialIssuanceError
import eu.europa.ec.eudi.openid4vci.CryptoGenerator
import eu.europa.ec.eudi.openid4vci.ProofType
import eu.europa.ec.eudi.openid4vci.universityDegreeJwt
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test cases for [ProofBuilder].
 */
internal class ProofBuilderTest {

    @Test
    fun `proof is successfully generated when signing algorithm is supported by the issuer`() {
        val signingAlgorithm = JWSAlgorithm.RS256
        val credentialConfiguration = universityDegreeJwt()
        assertTrue { ProofType.JWT in credentialConfiguration.proofTypesSupported }
        assertTrue { signingAlgorithm in credentialConfiguration.proofTypesSupported[ProofType.JWT].orEmpty() }

        val signer = CryptoGenerator.rsaProofSigner(signingAlgorithm)
        ProofBuilder.ofType(ProofType.JWT) {
            iss("https://wallet")
            aud("https://issuer")
            publicKey(signer.getBindingKey())
            credentialSpec(universityDegreeJwt())
            nonce("nonce")
            build(signer)
        }
    }

    @Test
    fun `proof is not generated when signing algorithm is not supported by the issuer`() {
        val signingAlgorithm = JWSAlgorithm.RS512
        val credentialConfiguration = universityDegreeJwt()
        assertTrue { ProofType.JWT in credentialConfiguration.proofTypesSupported }
        assertFalse { signingAlgorithm in credentialConfiguration.proofTypesSupported[ProofType.JWT].orEmpty() }

        val signer = CryptoGenerator.rsaProofSigner(signingAlgorithm)
        assertFailsWith(CredentialIssuanceError.ProofGenerationError.ProofTypeSigningAlgorithmNotSupported::class) {
            ProofBuilder.ofType(ProofType.JWT) {
                iss("https://wallet")
                aud("https://issuer")
                publicKey(signer.getBindingKey())
                credentialSpec(universityDegreeJwt())
                nonce("nonce")
                build(signer)
            }
        }
    }
}
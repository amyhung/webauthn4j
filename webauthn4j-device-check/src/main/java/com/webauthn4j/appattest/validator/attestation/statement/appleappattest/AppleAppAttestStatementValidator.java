/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webauthn4j.appattest.validator.attestation.statement.appleappattest;

import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.appattest.data.attestation.statement.AppleAppAttestAttestationStatement;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.attestation.statement.AttestationType;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.util.MessageDigestUtil;
import com.webauthn4j.validator.CoreRegistrationObject;
import com.webauthn4j.validator.attestation.statement.AbstractStatementValidator;
import com.webauthn4j.validator.exception.BadAaguidException;
import com.webauthn4j.validator.exception.BadAttestationStatementException;
import com.webauthn4j.validator.exception.KeyDescriptionValidationException;
import com.webauthn4j.validator.exception.MaliciousCounterValueException;
import org.apache.kerby.asn1.parse.Asn1Container;
import org.apache.kerby.asn1.parse.Asn1Parser;
import org.apache.kerby.asn1.type.Asn1OctetString;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Validates that the specified {@link AttestationStatement} is a valid Apple App Attest attestation
 */
public class AppleAppAttestStatementValidator extends AbstractStatementValidator<AppleAppAttestAttestationStatement> {
    public static final String APPLE_CRED_CERT_EXTENSION_OID = "1.2.840.113635.100.8.2";
    public static final AAGUID APPLE_APP_ATTEST_ENVIRONMENT_DEVELOPMENT = new AAGUID("appattestdevelop".getBytes());
    public static final AAGUID APPLE_APP_ATTEST_ENVIRONMENT_PRODUCTION = new AAGUID("appattest\0\0\0\0\0\0\0".getBytes());

    @Override
    public AttestationType validate(CoreRegistrationObject registrationObject) {
        if (!supports(registrationObject)) {
            throw new IllegalArgumentException(String.format("Specified format '%s' is not supported by %s.",
                    registrationObject.getAttestationObject().getFormat(), this.getClass().getName()));
        }

        validateNonce(registrationObject);
        validateAuthenticatorData(registrationObject.getAttestationObject().getAuthenticatorData());

        return AttestationType.BASIC;
    }

    private void validateNonce(CoreRegistrationObject registrationObject) {
        AppleAppAttestAttestationStatement attestationStatement = getAttestationStatement(registrationObject);
        X509Certificate credCert = attestationStatement.getX5c().getEndEntityAttestationCertificate().getCertificate();
        byte[] actualNonce = extractNonce(credCert);

        byte[] clientDataHash = registrationObject.getClientDataHash();
        byte[] authenticatorData = registrationObject.getAuthenticatorDataBytes();
        byte[] composite = ByteBuffer.allocate(authenticatorData.length + clientDataHash.length)
                .put(authenticatorData).put(clientDataHash).array();
        byte[] expectedNonce = MessageDigestUtil.createSHA256().digest(composite);

        if (!Arrays.equals(actualNonce, expectedNonce)) {
            throw new KeyDescriptionValidationException("App Attest nonce doesn't match.");
        }
    }

    private void validateAuthenticatorData(AuthenticatorData<RegistrationExtensionAuthenticatorOutput> authenticatorData) {
        if (authenticatorData.getSignCount() != 0) {
            throw new MaliciousCounterValueException("Counter is not zero");
        }

        AAGUID aaguid = authenticatorData.getAttestedCredentialData().getAaguid();
        if (!aaguid.equals(APPLE_APP_ATTEST_ENVIRONMENT_DEVELOPMENT)
                && !aaguid.equals(APPLE_APP_ATTEST_ENVIRONMENT_PRODUCTION)) {
            throw new BadAaguidException("Expected AAGUID of either 'appattestdevelop' or 'appattest'");
        }
    }

    private AppleAppAttestAttestationStatement getAttestationStatement(CoreRegistrationObject registrationObject) {
        AppleAppAttestAttestationStatement attestationStatement =
                (AppleAppAttestAttestationStatement) registrationObject.getAttestationObject().getAttestationStatement();

        if (attestationStatement.getX5c() == null || attestationStatement.getX5c().isEmpty()) {
            throw new BadAttestationStatementException(
                    "No attestation certificate is found in Apple App Attest attestation statement."
            );
        }

        return attestationStatement;
    }

    private byte[] extractNonce(X509Certificate credCert) {
        byte[] attestationExtensionBytes = credCert.getExtensionValue(APPLE_CRED_CERT_EXTENSION_OID);
        if (attestationExtensionBytes == null) {
            throw new BadAttestationStatementException("Apple X.509 extension not found");
        }

        Asn1OctetString envelope = new Asn1OctetString();
        try {
            envelope.decode(attestationExtensionBytes);
            Asn1Container container = (Asn1Container) Asn1Parser.parse(ByteBuffer.wrap(envelope.getValue()));
            Asn1OctetString subEnvelop = new Asn1OctetString();
            subEnvelop.decode(container.getChildren().get(0));
            return subEnvelop.getValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

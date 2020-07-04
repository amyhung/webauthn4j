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

package com.webauthn4j.data.extension.authenticator;

import com.webauthn4j.validator.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HMACCreateSecretExtensionAuthenticatorInputTest {

    @Test
    void getIdentifier_test(){
        HMACCreateSecretExtensionAuthenticatorInput target = new HMACCreateSecretExtensionAuthenticatorInput(true);
        assertThat(target.getIdentifier()).isEqualTo(HMACCreateSecretExtensionAuthenticatorInput.ID);
    }

    @Test
    void getValue_with_valid_key_test(){
        HMACCreateSecretExtensionAuthenticatorInput target = new HMACCreateSecretExtensionAuthenticatorInput(true);
        assertThatCode(()->target.getValue("hmacCreateSecret")).doesNotThrowAnyException();
    }

    @Test
    void getValue_with_invalid_key_test(){
        HMACCreateSecretExtensionAuthenticatorInput target = new HMACCreateSecretExtensionAuthenticatorInput(true);
        assertThatThrownBy(()->target.getValue("invalid")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validate_test(){
        HMACCreateSecretExtensionAuthenticatorInput target = new HMACCreateSecretExtensionAuthenticatorInput(true);
        assertThatCode(target::validate).doesNotThrowAnyException();
    }

    @Test
    void validate_invalid_data_test(){
        HMACCreateSecretExtensionAuthenticatorInput target = new HMACCreateSecretExtensionAuthenticatorInput(null);
        assertThatThrownBy(target::validate).isInstanceOf(ConstraintViolationException.class);
    }
}

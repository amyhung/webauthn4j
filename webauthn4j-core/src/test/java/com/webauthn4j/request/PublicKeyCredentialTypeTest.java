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

package com.webauthn4j.request;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.webauthn4j.registry.Registry;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class PublicKeyCredentialTypeTest {

    @Test
    public void test() throws IOException {
        TestDto testDto = new Registry().getJsonMapper().readValue("{\"type\": \"public-key\"}", TestDto.class);
        assertThat(testDto.getType()).isEqualTo(PublicKeyCredentialType.PUBLIC_KEY);
    }

    @Test(expected = InvalidFormatException.class)
    public void invalid_data_test() throws IOException {
        new Registry().getJsonMapper().readValue("{\"type\": \"invalid-data\"}", TestDto.class);
    }

}
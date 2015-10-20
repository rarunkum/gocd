/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.security;

import java.io.File;
import java.io.IOException;
import javax.crypto.spec.DESKeySpec;

import com.thoughtworks.go.util.ReflectionUtil;
import com.thoughtworks.go.util.SystemEnvironment;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.thoughtworks.go.util.ReflectionUtil.getField;
import static com.thoughtworks.go.util.ReflectionUtil.invoke;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class GoCipherTest {

    private GoCipher goCipher;
    private File cipherFile;

    @Before
    public void setUp() throws IOException {
        ReflectionUtil.setField(new CipherProvider(new SystemEnvironment()), "cachedKey", null);
        cipherFile = new SystemEnvironment().getCipherFile();
        FileUtils.writeStringToFile(cipherFile, "269298bc31c44620");
        goCipher = new GoCipher();
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(cipherFile);
    }

    @Test
    public void shouldDecryptUsingTheSameKeyUsedForEncryption() throws InvalidCipherTextException {
        String input = "user-password!";
        String cipherText = goCipher.encrypt(input);
        assertThat(cipherText, is("mvcX9yrQsM4iPgm1tDxN1A=="));
        String plainText = goCipher.decrypt(cipherText).trim();
        assertThat(plainText, is(input));
    }

    @Test
    public void shouldNotKillLeadingAndTrailingSpacesDuringEncryption() throws InvalidCipherTextException {
        String plainText = "   foo   ";
        String cipherText = goCipher.encrypt(plainText);
        assertThat(goCipher.decrypt(cipherText), is(plainText));
    }

    @Test
    public void shouldGenerateAValidAndSafeDESKey() throws Exception {
        byte[] key = (byte[]) invoke(getField(goCipher, "cipherProvider"), "generateKey");
        assertThat(DESKeySpec.isWeak(key, 0), is(false));
    }

    @Test
    public void shouldErrorOutWhenCipherTextIsTamperedWith() {
        try {
            goCipher.decrypt("some junk that should not decrypt to something sane. I mean, seriously, how could this make sense.");
            fail("Should have thrown DataLengthException");
        }
        catch (Exception e) {
            assertThat(e.getMessage(), is("last block incomplete in decryption"));
        }
    }

    @Test
    public void shouldCreateACipherFileWithTheCipherIfNotFound() throws IOException, InvalidCipherTextException {
        FileUtils.deleteQuietly(cipherFile);
        assertThat(cipherFile.exists(), is(false));
        ReflectionUtil.setField(new CipherProvider(new SystemEnvironment()), "cachedKey", null);
        goCipher = new GoCipher();
        assertThat(cipherFile.exists(), is(true));
        String plainText = goCipher.decrypt(goCipher.encrypt("user-password!"));
        assertThat(plainText, is("user-password!"));
        assertThat(cipherFile.exists(), is(true));
    }

    @Test
    public void shouldWorkEvenAfterCipherFileHasBeenDeleted() throws InvalidCipherTextException {//serialization friendliness
        FileUtils.deleteQuietly(cipherFile);
        assertThat(cipherFile.exists(), is(false));
        String plainText = goCipher.decrypt(goCipher.encrypt("user-password!"));
        assertThat(plainText, is("user-password!"));
        assertThat(cipherFile.exists(), is(false));
    }
}

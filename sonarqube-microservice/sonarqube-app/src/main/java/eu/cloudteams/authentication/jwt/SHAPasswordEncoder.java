/*
 *  Copyright 2016 Arcadia Framework, http://www.arcadia-framework.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.cloudteams.authentication.jwt;

import eu.cloudteams.util.other.Util;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class SHAPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence cs) {
        return cs.toString();
    }

    /**
     *
     * @param cs
     * @param string
     * @return True if the password is matched otherwise false
     */
    @Override
    public boolean matches(CharSequence cs, String string) {
        return Util.createAlgorithm(cs.toString(), Util.ALGORITHM.SHA.toString()).equals(string);
    }

}
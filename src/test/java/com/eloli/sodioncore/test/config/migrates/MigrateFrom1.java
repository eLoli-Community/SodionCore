/*
 * Copyright 2021 Mohist-Community
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

package com.eloli.sodioncore.test.config.migrates;

import com.eloli.sodioncore.config.Migrater;
import com.eloli.sodioncore.test.config.MainConfiguration;

import java.util.Locale;

public class MigrateFrom1 extends Migrater<HistoryConfiguration1, MainConfiguration> {
    @Override
    public void migrate(HistoryConfiguration1 from, MainConfiguration to) {
        to.defaultLang = from.defaultLang.toUpperCase(Locale.ROOT);
    }
}

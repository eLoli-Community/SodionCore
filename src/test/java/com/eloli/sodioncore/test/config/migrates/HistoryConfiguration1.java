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

import com.eloli.sodioncore.config.Configure;
import com.eloli.sodioncore.config.Lore;
import com.google.gson.annotations.Expose;

public class HistoryConfiguration1 extends Configure {
    @Expose(serialize = true, deserialize = false)
    public Integer version = 1;

    @Lore("The default language should message use.")
    @Expose
    public String defaultLang = "en";
}

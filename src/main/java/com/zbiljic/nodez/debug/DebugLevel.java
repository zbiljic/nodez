/*
 * Copyright 2017 Nemanja Zbiljić
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
package com.zbiljic.nodez.debug;

/**
 * Debug levels of {@link DebugManager}.
 */
public enum DebugLevel {

  /**
   * Disable debug logging (no log entries will be output).
   */
  NONE,

  /**
   * Output only basic debug log entries.
   */
  BASIC,

  /**
   * Output detailed debug log entries.
   */
  DETAILED,

  /**
   * Output all debug log entries.
   */
  VERBOSE;

}

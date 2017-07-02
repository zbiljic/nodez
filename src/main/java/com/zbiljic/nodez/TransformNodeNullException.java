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
package com.zbiljic.nodez;

public class TransformNodeNullException extends Exception {

  public final Node transformNode;
  public final Node sourceNode;
  public final Object source;

  public TransformNodeNullException(Node transformNode,
                                    Node sourceNode,
                                    Object source) {
    this.transformNode = transformNode;
    this.sourceNode = sourceNode;
    this.source = source;
  }

  @Override
  public String toString() {
    return String.format("TransformNode [%s] response is null while transforming %s",
      transformNode.getName(),
      String.valueOf(source));
  }
}

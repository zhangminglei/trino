/*
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
package io.trino.metadata;

import io.trino.operator.scalar.ScalarFunctionImplementation;

public class HiveSqlScalarFunction
        extends SqlScalarFunction
{
    private final ScalarFunctionImplementation implementation;

    protected HiveSqlScalarFunction(FunctionMetadata functionMetadata, ScalarFunctionImplementation implementation)
    {
        super(functionMetadata);
        this.implementation = implementation;
    }

    @Override
    public FunctionDependencyDeclaration getFunctionDependencies()
    {
        return FunctionDependencyDeclaration.NO_DEPENDENCIES;
    }

    @Override
    protected ScalarFunctionImplementation specialize(BoundSignature boundSignature)
    {
        return implementation;
    }
}
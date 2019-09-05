/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.cellery.impl;

import io.cellery.BallerinaCelleryException;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import static io.cellery.CelleryUtils.copyResourceToTarget;
import static io.cellery.CelleryUtils.readSwagger;

/**
 * Class to Parse Swagger.
 */
public class ReadSwaggerFile {

    public static MapValue readSwaggerFile(String swaggerFilePath) throws BallerinaCelleryException {
        final String specification;
        try {
            specification = readSwagger(swaggerFilePath, Charset.defaultCharset());
            copyResourceToTarget(swaggerFilePath);
        } catch (IOException e) {
            throw new BallerinaCelleryException(e.getMessage());
        }
        final Swagger swagger = new SwaggerParser().parse(specification);
        String basePath = swagger.getBasePath();
        if (basePath.isEmpty() || "/".equals(basePath)) {
            basePath = "";
        }
        AtomicLong runCount = new AtomicLong(0L);
        String finalBasePath = basePath;
        ArrayValue arrayValue = new ArrayValue();
        MapValue<String, ArrayValue> apiDefinitions = new MapValueImpl<>();
        swagger.getPaths().forEach((path, pathDefinition) ->
                pathDefinition.getOperationMap().forEach((httpMethod, operation) -> {
                    MapValue<String, String> resource = new MapValueImpl<>();
                    resource.put("path", finalBasePath + path);
                    resource.put("method", httpMethod.toString());
                    arrayValue.add(runCount.getAndIncrement(), resource);
                }));
        apiDefinitions.put("resources", arrayValue);
        return apiDefinitions;
    }
}

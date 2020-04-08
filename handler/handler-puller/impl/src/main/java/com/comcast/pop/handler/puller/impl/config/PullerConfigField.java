/*
 * Copyright 2020 Comcast Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.comcast.pop.handler.puller.impl.config;

import com.comast.pop.handler.base.field.retriever.api.NamedField;

public enum PullerConfigField implements NamedField
{
    POP_RESOURCE_POOL_SERVICE_URL("pop.puller.resourcePoolServiceUrl"),
    PULL_WAIT("pop.puller.pullWait"),
    INSIGHT_ID("pop.puller.insightId"),
    AGENDA_REQUEST_COUNT("pop.puller.agendaRequestCount"),
    LOCAL_AGENDA_RELATIVE_PATH("pop.puller.localAgendaRelativePath");

    private final String fieldName;

    PullerConfigField(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}

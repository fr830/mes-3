/*
 * **************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * **************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.importing.parsers;

import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.importing.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.importing.helpers.CellParser;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Component
public class StorageLocationCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND = "qcadooView.validate.field.error.lookupCodeNotFound";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void parse(final String cellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        Entity storageLocation = getStorageLocationByNumber(cellValue);

        if (Objects.isNull(storageLocation)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND);
        } else {
            valueConsumer.accept(storageLocation);
        }
    }

    private Entity getStorageLocationByNumber(final String number) {
        return getStorageLocationDD().find().add(SearchRestrictions.eq(StorageLocationFields.NUMBER, number)).setMaxResults(1)
                .uniqueResult();
    }

    private DataDefinition getStorageLocationDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

}

/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.listeners;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.importing.services.XlsxImportService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.importing.position.PositionCellBinderRegistry;
import com.qcadoo.mes.materialFlowResources.importing.position.PositionXlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class PositionsImportListeners {

    public static final String L_FORM = "form";

    @Autowired
    private PositionXlsxImportService positionXlsxImportService;

    @Autowired
    private PositionCellBinderRegistry positionCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        positionXlsxImportService.downloadImportSchema(view, MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION, XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity document = documentForm.getEntity();

        positionXlsxImportService.processImportFile(view, positionCellBinderRegistry.getCellBinderRegistry(), true,
                MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_POSITION, document,
                PositionFields.DOCUMENT, PositionsImportListeners::createRestrictionForPosition);
    }

    private static SearchCriterion createRestrictionForPosition(final Entity position) {
        return SearchRestrictions.eq(PositionFields.NUMBER, position.getStringField(PositionFields.NUMBER));
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        positionXlsxImportService.redirectToLogs(view, MaterialFlowResourcesConstants.MODEL_POSITION);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        positionXlsxImportService.changeButtonsState(view, false);
    }

}

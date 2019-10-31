package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlowResources.constants.DocumentPositionParametersItemFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParametersMFRListeners {

    private static final String L_GRID = "grid";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void addColumnWithResourceAttribute(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        StringBuilder url = new StringBuilder("../page/materialFlowResources/documentAttributePosition.html");

        view.openModal(url.toString());
    }

    public void addColumns(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        Set<Long> ids = grid.getSelectedEntitiesIds();
        ids.forEach(attrId -> {
            Entity attribute = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.ATTRIBUTE).get(attrId);

            Entity positionItem = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS_ITEM).create();
            positionItem.setField(DocumentPositionParametersItemFields.NAME, attribute.getStringField(AttributeFields.NUMBER));
            positionItem.setField(DocumentPositionParametersItemFields.CHECKED, true);
            positionItem.setField(DocumentPositionParametersItemFields.EDITABLE, true);
            positionItem.setField(DocumentPositionParametersItemFields.PARAMETERS, parameterService.getParameter()
                    .getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS).getId());
            positionItem.setField(DocumentPositionParametersItemFields.FOR_ATTRIBUTE, true);
            positionItem.setField(DocumentPositionParametersItemFields.ATTRIBUTE, attribute.getId());
            positionItem = positionItem.getDataDefinition().save(positionItem);
        });
    }
}

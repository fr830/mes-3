package com.qcadoo.mes.basic.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BaseAttributeValuelHooks {

    private static final String ATTRIBUTE_VALUE = "attributeValue";

    private static final String ATTRIBUTE = "attribute";

    private static final String VALUE = "value";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public boolean validate(final DataDefinition ownerAttributeValueDD, final Entity ownerAttributeValue, final String ownerName,
            final String ownerAttributeValueCollectionName) {
        Entity attribute = ownerAttributeValue.getBelongsToField(ATTRIBUTE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && Objects.isNull(ownerAttributeValue.getBelongsToField(ATTRIBUTE_VALUE))) {
            ownerAttributeValue.addError(ownerAttributeValueDD.getField(ATTRIBUTE_VALUE),
                    "qcadooView.validate.field.error.missing");
            return false;
        }

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    ownerAttributeValue.getStringField(VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                int scale = attribute.getIntegerField(AttributeFields.PRECISION);
                int valueScale = eitherNumber.getRight().get().scale();
                if (valueScale > scale) {
                    ownerAttributeValue.addError(ownerAttributeValueDD.getField(VALUE),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));
                    return false;
                }
            } else {
                ownerAttributeValue.addError(ownerAttributeValueDD.getField(VALUE),
                        "qcadooView.validate.field.error.invalidNumericFormat");
                return false;
            }
            ownerAttributeValue
                    .setField(
                            ProductAttributeValueFields.VALUE,
                            BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                    attribute.getIntegerField(AttributeFields.PRECISION)));
        }
        if (checkIfValueExists(ownerAttributeValueDD, ownerAttributeValue, ownerName, ownerAttributeValueCollectionName)) {
            return false;
        }

        return true;
    }

    private boolean checkIfValueExists(DataDefinition ownerAttributeValueDD, Entity ownerAttributeValue, String ownerName,
            String ownerAttributeValueCollectionName) {
        Entity attribute = ownerAttributeValue.getBelongsToField(ATTRIBUTE);
        Entity owner = ownerAttributeValue.getBelongsToField(ownerName);
        Entity attributeValue = ownerAttributeValue.getBelongsToField(ATTRIBUTE_VALUE);

        List<Entity> values = owner.getHasManyField(ownerAttributeValueCollectionName);

        List sameValue;
        if (Objects.nonNull(attributeValue)) {
            sameValue = values
                    .stream()
                    .filter(val -> val.getBelongsToField(ATTRIBUTE).getId().equals(attribute.getId())
                            && Objects.nonNull(val.getBelongsToField(ATTRIBUTE_VALUE))
                            && val.getBelongsToField(ATTRIBUTE_VALUE).getId().equals(attributeValue.getId()))
                    .filter(val -> !val.getId().equals(ownerAttributeValue.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                ownerAttributeValue.addError(ownerAttributeValueDD.getField(ATTRIBUTE_VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        } else {
            sameValue = values
                    .stream()
                    .filter(val -> val.getBelongsToField(ATTRIBUTE).getId().equals(attribute.getId())
                            && Objects.isNull(val.getBelongsToField(ATTRIBUTE_VALUE))
                            && val.getStringField(VALUE).equals(ownerAttributeValue.getStringField(VALUE)))
                    .filter(val -> !val.getId().equals(ownerAttributeValue.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                ownerAttributeValue.addError(ownerAttributeValueDD.getField(VALUE), "basic.attributeValue.error.valueExists");
                return true;
            }
        }

        return false;
    }

    public void onSave(final DataDefinition attributeValueDD, final Entity attributeValue) {
        Entity attribute = attributeValue.getBelongsToField(ATTRIBUTE);
        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    attributeValue.getStringField(VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                attributeValue.setField(
                        VALUE,
                        BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                attribute.getIntegerField(AttributeFields.PRECISION)));
            }
        }
    }
}

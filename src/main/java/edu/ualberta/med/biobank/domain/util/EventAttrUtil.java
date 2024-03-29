package edu.ualberta.med.biobank.domain.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import edu.ualberta.med.biobank.util.DateUtil;

public class EventAttrUtil {

    public static final String INVALID_STUDY_EVENT_ATTR_SINGLE_VALUE_ERRMSG =
        "Value \"{0}\" is invalid for label \"{1}\", valid is one of \"{2}\".";

    public static final String INVALID_STUDY_EVENT_ATTR_MULTIPLE_VALUE_ERRMSG =
        "Value \"{0}\" (\"{1}\") is invalid for label \"{2}\".";

    public static final String CANNOT_PARSE_DATE_ERRMSG = "Cannot parse date \"{0}\".";

    public static final String UNKNOWN_EVENT_ATTR_TYPE_ERRMSG = "Unknown Event Attribute Type \"{0}\".";

    /**
     * Validates an event attribute value.
     *
     * @param type
     * @param label
     * @param permissibleValues
     * @param value
     *
     * @throws LocalizedException
     */
    public static void validateValue(EventAttrTypeEnum type, String label, String permissibleValues, String value) {

        List<String> permValuesSplit = new ArrayList<String>(0);

        if (type.isSelectType()) {
            if (permissibleValues != null) {
                permValuesSplit = Arrays.asList(permissibleValues.split(";"));
            }
        }

        if (type == EventAttrTypeEnum.SELECT_SINGLE) {
            if (!permValuesSplit.contains(value)) {
                throw new RuntimeException(
                        INVALID_STUDY_EVENT_ATTR_SINGLE_VALUE_ERRMSG.formatted(
                                value, label, permissibleValues));
            }
        } else if (type == EventAttrTypeEnum.SELECT_MULTIPLE) {
            for (String singleVal : value.split(";")) {
                if (!permValuesSplit.contains(singleVal)) {
                    throw new RuntimeException(
                                    INVALID_STUDY_EVENT_ATTR_MULTIPLE_VALUE_ERRMSG.formatted(
                                    singleVal, value, label));
                }
            }
        } else if (type == EventAttrTypeEnum.NUMBER) {
            Double.parseDouble(value);
        } else if (type == EventAttrTypeEnum.DATE_TIME) {
            try {
                DateUtil.parseDate(value);
            } catch (ParseException e) {
                throw new RuntimeException(CANNOT_PARSE_DATE_ERRMSG.formatted(value));
            }
        } else if (type == EventAttrTypeEnum.TEXT) {
            // do nothing
        } else {
            throw new RuntimeException(
                    UNKNOWN_EVENT_ATTR_TYPE_ERRMSG.formatted(type.getName()));
        }

    }

}

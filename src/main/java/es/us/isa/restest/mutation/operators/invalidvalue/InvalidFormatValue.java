package es.us.isa.restest.mutation.operators.invalidvalue;

import static es.us.isa.restest.specification.OpenAPISpecificationVisitor.getParametersFeaturesSubjectToInvalidValueChange;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import es.us.isa.restest.mutation.operators.AbstractMutationOperator;
import es.us.isa.restest.specification.OpenAPIParameter;
import es.us.isa.restest.testcases.TestCase;
import io.swagger.v3.oas.models.Operation;

/**
 * @author Alberto Martin-Lopez
 */
public class InvalidFormatValue extends AbstractMutationOperator {

    protected static final String FLOAT_FORMAT = "float";
    private static final String DOUBLE_FORMAT = "double";
    private static final String INT32_FORMAT = "int32";
    protected static final String INT64_FORMAT = "int64";
    protected static final String DATE_FORMAT = "date";
    protected static final String DATETIME_FORMAT = "datetime";

    /**
     * If possible, inserts an invalid value into some parameter of a test case.
     * For example, inserts a string value into an integer parameter.
     *
     * @param tc Test case to mutate. NOTE: If the mutation is applied, the original
     *           {@code tc} object will not be preserved, it should be cloned before
     *           calling this method.
     * @param specOperation OpenAPI operation related to the test case. Necessary
     *                      to extract all required parameters.
     * @return Description of the mutation applied, "" if none applied.
     */
    public static String mutate(TestCase tc, Operation specOperation) {
        List<OpenAPIParameter> candidateParameters = getParametersFeaturesSubjectToInvalidValueChange(specOperation) // Parameters that can be mutated to create a faulty test case
                .stream()
                .filter(openAPIParameter -> openAPIParameter.getFormat() != null)
                .collect(toList());

        if (candidateParameters.isEmpty())
        	return "";

        OpenAPIParameter selectedParam = candidateParameters.get(ThreadLocalRandom.current().nextInt(0, candidateParameters.size())); // Select one randomly

        switch (selectedParam.getFormat()) {
            case FLOAT_FORMAT:
                return FloatToInvalid.mutate(tc, selectedParam);
            case DOUBLE_FORMAT:
                return DoubleToInvalid.mutate(tc, selectedParam);
            case INT32_FORMAT:
                return Int32ToInvalid.mutate(tc, selectedParam);
            case INT64_FORMAT:
                return Int64ToInvalid.mutate(tc, selectedParam);
            case DATE_FORMAT:
                return DateToInvalid.mutate(tc, selectedParam);
            case DATETIME_FORMAT:
                return DatetimeToInvalid.mutate(tc, selectedParam);
        }

        return "";
        
    }
}

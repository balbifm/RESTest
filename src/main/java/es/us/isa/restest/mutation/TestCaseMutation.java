package es.us.isa.restest.mutation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.us.isa.restest.mutation.operators.RemoveRequiredParameter;
import es.us.isa.restest.mutation.operators.invalidvalue.InvalidFormatValue;
import es.us.isa.restest.mutation.operators.invalidvalue.InvalidParameterValue;
import es.us.isa.restest.testcases.TestCase;
import io.swagger.v3.oas.models.Operation;
import org.apache.commons.lang3.StringUtils;

/**
 * This class implements method for mutating a test case based on a set of mutation operators.
 *
 * @author Alberto Martin-Lopez
 */
public class TestCaseMutation {

    public static final String INVALID_TYPE = "INVALID_PARAMETER_VALUE";
    public static final String INVALID_FORMAT = "INVALID_PARAMETER_FORMAT";
    public static final String REMOVE_REQUIRED_PARAMETER = "REMOVE_REQUIRED_PARAMETER";
    public static final String[] mutationOperators = {INVALID_TYPE, INVALID_FORMAT, REMOVE_REQUIRED_PARAMETER};


    /**
     * Given a valid (nominal) test case, if possible, mutate it and convert it into an invalid (faulty) test case.
     * Set of possible mutations:
     * <ol>
     *     <li>Remove required parameter.</li>
     *     <li>Change type of parameter value (e.g. use a string for an integer parameter).</li>
     *     <li>Violate a constraint of a parameter (e.g. use an integer value higher than the maximum.</li>
     * </ol>
     *
     * @param testCase      Original valid test case. NOTE: If the mutation is applied, the original
     *                      {@code nominalTestCase} object will not be preserved, it should be cloned
     *                      before calling this method.
     * @param specOperation OpenAPI operation related to the test case.
     * @return a string indicating the mutation operator applied, empty if none.
     */
    public static String mutate(TestCase testCase, Operation specOperation) {
        String mutationApplied = "";

        // Shuffle list of operators
        List<String> operators = Arrays.asList(mutationOperators);
        Collections.shuffle(operators);

        int index = 0;
        while (index < operators.size() && mutationApplied.equals("")) {
            mutationApplied = mutate(testCase, specOperation, operators.get(index)).orElse("");
            index++;
        }

        return mutationApplied;
    }

    public static Optional<String> mutate(TestCase testCase, Operation specOperation, String mutation) {
        String mutationApplied;

        switch (mutation) {
            case INVALID_TYPE:
                mutationApplied = InvalidParameterValue.mutate(testCase, specOperation);
                break;
            case INVALID_FORMAT:
                mutationApplied = InvalidFormatValue.mutate(testCase, specOperation);
                break;
            case REMOVE_REQUIRED_PARAMETER:
                mutationApplied = RemoveRequiredParameter.mutate(testCase, specOperation);
                break;
            default:
                throw new RuntimeException("Mutation Operator " + mutation + " does not exist");
        }

        return StringUtils.isBlank(mutationApplied) ? Optional.empty() : Optional.of(mutationApplied);

    }

}

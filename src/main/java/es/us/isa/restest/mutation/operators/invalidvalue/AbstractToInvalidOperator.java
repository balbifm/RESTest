package es.us.isa.restest.mutation.operators.invalidvalue;

import es.us.isa.restest.mutation.operators.AbstractMutationOperator;
import es.us.isa.restest.specification.OpenAPIParameter;
import es.us.isa.restest.testcases.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static es.us.isa.restest.mutation.operators.invalidvalue.InvalidParameterValue.INTEGER_TYPE;
import static es.us.isa.restest.mutation.operators.invalidvalue.InvalidParameterValue.NUMBER_TYPE;

/**
 * This class contains all possible mutations that can be performed to assign an
 * invalid value to a parameter. Specific mutation operators invoke this class' mutate()
 * method to mutate a test case.
 *
 * @author Alberto Martin-Lopez
 */
public abstract class AbstractToInvalidOperator extends AbstractMutationOperator {

    protected static final String REPLACE_WITH_INT = "REPLACE_WITH_INT";
    protected static final String REPLACE_WITH_BOOL = "REPLACE_WITH_BOOL";
    protected static final String REPLACE_WITH_STRING = "REPLACE_WITH_STRING";
    protected static final String REPLACE_WITH_NUMBER = "REPLACE_WITH_NUMBER";
    protected static final String VIOLATE_MAX_CONSTRAINT = "VIOLATE_MAX_CONSTRAINT";
    protected static final String VIOLATE_MIN_CONSTRAINT = "VIOLATE_MIN_CONSTRAINT";
    protected static final String VIOLATE_FORMAT_CONSTRAINT = "VIOLATE_FORMAT_CONSTRAINT";
    protected static final String VIOLATE_MAX_LENGTH_CONSTRAINT = "VIOLATE_MAX_LENGTH_CONSTRAINT";
    protected static final String VIOLATE_MIN_LENGTH_CONSTRAINT = "VIOLATE_MIN_LENGTH_CONSTRAINT";
    protected static final String REPLACE_WITH_DATETIME = "REPLACE_WITH_DATETIME";
    protected static final String REPLACE_WITH_DATE = "REPLACE_WITH_DATE";
    protected static final String REPLACE_WITH_INT64 = "REPLACE_WITH_INT64";
    protected static final String REPLACE_WITH_GT_INT64 = "REPLACE_WITH_GT_INT64";
    protected static final String REPLACE_WITH_INVALID_FLOAT = "REPLACE_WITH_INVALID_FLOAT";
    protected static final String REPLACE_WITH_INVALID_DOUBLE = "REPLACE_WITH_INVALID_DOUBLE";

    public static String mutate(TestCase tc, OpenAPIParameter param, String[] mutations) {
        String mutationApplied = "";

        // Shuffle list of mutations
        List<String> mutationsList = Arrays.asList(mutations);
        Collections.shuffle(mutationsList);

        int index = 0;
        while (index<mutationsList.size() && mutationApplied.equals("")) {
            switch (mutationsList.get(index)) {
                case REPLACE_WITH_INT:
                    String randomInt = Integer.toString(ThreadLocalRandom.current().nextInt(1000, 10001));
                    if (param.getEnumValues() == null || !param.getEnumValues().contains(randomInt)) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, randomInt);
                        tc.addParameter(param, randomInt);
                    }
                    break;
                case REPLACE_WITH_BOOL:
                    String randomBoolean = Boolean.toString(ThreadLocalRandom.current().nextBoolean());
                    if (param.getEnumValues() == null || !param.getEnumValues().contains(randomBoolean)) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, randomBoolean);
                        tc.addParameter(param, randomBoolean);
                    }
                    break;
                case REPLACE_WITH_STRING:
                    String randomString = RandomStringUtils.randomAlphabetic(10, 20);
                    if (param.getEnumValues() == null || !param.getEnumValues().contains(randomString)) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, randomString);
                        tc.addParameter(param, randomString);
                    }
                    break;
                case REPLACE_WITH_NUMBER:
                    String randomNumber = Double.toString(ThreadLocalRandom.current().nextDouble(1000, 10001));
                    if (param.getEnumValues() == null || !param.getEnumValues().contains(randomNumber)) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, randomNumber);
                        tc.addParameter(param, randomNumber);
                    }
                    break;
                case VIOLATE_MAX_CONSTRAINT:
                    if (param.getMax() != null) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                        if (param.getType().equals(NUMBER_TYPE))
                            tc.addParameter(param, Double.toString(param.getMax().doubleValue() + ThreadLocalRandom.current().nextDouble(1, 10)));
                        else if (param.getType().equals(INTEGER_TYPE))
                            tc.addParameter(param, Integer.toString(param.getMax().intValue() + ThreadLocalRandom.current().nextInt(1, 10)));
                    }
                    break;
                case VIOLATE_MIN_CONSTRAINT:
                    if (param.getMin() != null) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                        if (param.getType().equals(NUMBER_TYPE))
                            tc.addParameter(param, Double.toString(param.getMin().doubleValue() - ThreadLocalRandom.current().nextDouble(1, 10)));
                        else if (param.getType().equals(INTEGER_TYPE))
                            tc.addParameter(param, Integer.toString(param.getMin().intValue() - ThreadLocalRandom.current().nextInt(1, 10)));
                    }
                    break;
                case VIOLATE_FORMAT_CONSTRAINT:
                    if (param.getFormat() != null || param.getPattern() != null) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                        tc.addParameter(param, RandomStringUtils.randomAlphabetic(10, 20));
                    }
                    break;
                case VIOLATE_MAX_LENGTH_CONSTRAINT:
                    if (param.getMaxLength() != null) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                        tc.addParameter(param, RandomStringUtils.randomAlphabetic(param.getMaxLength() + ThreadLocalRandom.current().nextInt(1, 11)));
                    }
                    break;
                case VIOLATE_MIN_LENGTH_CONSTRAINT:
                    if (param.getMinLength() != null && param.getMinLength() > 1) {
                        mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                        tc.addParameter(param, RandomStringUtils.randomAlphabetic(param.getMinLength() - 1));
                    }
                    break;
                case REPLACE_WITH_DATETIME:
                    mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                    tc.addParameter(param, "2017-07-21T17:32:28Z");
                    break;
                case REPLACE_WITH_DATE:
                    mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                    tc.addParameter(param, "2017-07-21");
                    break;
                case REPLACE_WITH_GT_INT64:
                    mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                    tc.addParameter(param, "9223372036854775808");
//                    tc.addParameter(param, "92233720368547758080");
                    break;
                case REPLACE_WITH_INVALID_FLOAT:
//                    mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
//                    tc.addParameter(param, "9223372036854775808");
                    break;
                case REPLACE_WITH_INT64:
                    mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                    tc.addParameter(param, "1844674407370955");
                    break;
                case REPLACE_WITH_INVALID_DOUBLE:
//                    mutationApplied = getMutationMessage(mutationsList.get(index), param, tc, null);
                    break;
                default:
                    throw new IllegalArgumentException("Mutation not supported: " + mutationsList.get(index));
            }
            index++;
        }

        return mutationApplied;
    }

    private static String getMutationMessage(String mutation, OpenAPIParameter param, TestCase tc, String newValue) {
        switch (mutation) {
            case REPLACE_WITH_INT:
                return "Changed value of " + param.getType() + (param.getEnumValues() != null ? " (enum) " : " ") + "parameter " + param.getName() + " from '" + tc.getParameterValue(param) + "' to integer '" + newValue + "'";
            case REPLACE_WITH_BOOL:
                return "Changed value of " + param.getType() + (param.getEnumValues() != null ? " (enum) " : " ") + "parameter " + param.getName() + " from '" + tc.getParameterValue(param) + "' to boolean '" + newValue + "'";
            case REPLACE_WITH_STRING:
                return "Changed value of " + param.getType() + (param.getEnumValues() != null ? " (enum) " : " ") + "parameter " + param.getName() + " from '" + tc.getParameterValue(param) + "' to string '" + newValue + "'";
            case REPLACE_WITH_NUMBER:
                return "Changed value of " + param.getType() + (param.getEnumValues() != null ? " (enum) " : " ") + "parameter " + param.getName() + " from '" + tc.getParameterValue(param) + "' to number '" + newValue + "'";
            case VIOLATE_MAX_CONSTRAINT:
                return "Violated 'max' constraint of " + param.getType() + " parameter " + param.getName();
            case VIOLATE_MIN_CONSTRAINT:
                return "Violated 'min' constraint of " + param.getType() + " parameter " + param.getName();
            case VIOLATE_FORMAT_CONSTRAINT:
                return "Violated 'format/pattern' constraint of string parameter " + param.getName();
            case VIOLATE_MAX_LENGTH_CONSTRAINT:
                return "Violated 'max_length' constraint of string parameter " + param.getName();
            case VIOLATE_MIN_LENGTH_CONSTRAINT:
                return "Violated 'min_length' constraint of string parameter " + param.getName();
            case REPLACE_WITH_DATETIME:
                return "Changed value to datetime for " + param.getName();
            case REPLACE_WITH_DATE:
                return "Changed value to date for " + param.getName();
            case REPLACE_WITH_GT_INT64:
                return "Changed value to greater than int64 for " + param.getName();
            case REPLACE_WITH_INVALID_FLOAT:
                return "Changed value to an invalid float for " + param.getName();
            case REPLACE_WITH_INT64:
                return "Changed value to int64 for " + param.getName();
            case REPLACE_WITH_INVALID_DOUBLE:
                return "Changed value to invalid double for " + param.getName();
            default:
                throw new IllegalArgumentException("Mutation not supported: " + mutation);
        }
    }
}

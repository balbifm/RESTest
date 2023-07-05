package es.us.isa.restest.mutation.operators.invalidvalue;

import es.us.isa.restest.specification.OpenAPIParameter;
import es.us.isa.restest.testcases.TestCase;

/**
 * Mutate an integer parameter by assigning it a string, a boolean, a double, or
 * violating either a max or min constraint.
 *
 * @author Alberto Martin-Lopez
 */
public class DateToInvalid extends AbstractToInvalidOperator {


    private static final String[] mutations = {
            REPLACE_WITH_DATETIME
    };

    public static String mutate(TestCase tc, OpenAPIParameter param) {
        return mutate(tc, param, mutations);
    }
}

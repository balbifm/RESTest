package es.us.isa.restest.generators;

import static es.us.isa.restest.mutation.SchemaMutation.MutationPipeline.CHANGE_BODY_PROPERTY_FORMAT;
import static es.us.isa.restest.mutation.SchemaMutation.MutationPipeline.CHANGE_BODY_PROPERTY_TYPE;
import static es.us.isa.restest.mutation.SchemaMutation.MutationPipeline.REMOVE_REQUIRED_FROM_BODY;
import static es.us.isa.restest.mutation.TestCaseMutation.INVALID_FORMAT;
import static es.us.isa.restest.mutation.TestCaseMutation.INVALID_TYPE;
import static es.us.isa.restest.mutation.TestCaseMutation.REMOVE_REQUIRED_PARAMETER;
import static es.us.isa.restest.specification.OpenAPISpecificationVisitor.hasDependencies;
import static es.us.isa.restest.util.IDLAdapter.idl2restestTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import es.us.isa.idlreasonerchoco.analyzer.Analyzer;
import es.us.isa.idlreasonerchoco.analyzer.OASAnalyzer;
import es.us.isa.idlreasonerchoco.configuration.IDLException;
import es.us.isa.restest.configuration.pojos.Operation;
import es.us.isa.restest.configuration.pojos.TestConfigurationObject;
import es.us.isa.restest.configuration.pojos.TestParameter;
import es.us.isa.restest.inputs.ITestDataGenerator;
import es.us.isa.restest.inputs.random.RandomBooleanGenerator;
import es.us.isa.restest.inputs.random.RandomInputValueIterator;
import es.us.isa.restest.inputs.stateful.BodyGenerator;
import es.us.isa.restest.mutation.SchemaMutation.MutationPipeline;
import es.us.isa.restest.mutation.TestCaseMutation;
import es.us.isa.restest.specification.OpenAPISpecification;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.OASAPIValidator;
import es.us.isa.restest.util.RESTestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

/**
 * This class implements a constraint-based test case generator using IDLReasoner, a CSP-based tool for the automated analysis of inter-parameter dependencies
 *
 * @author Alberto Martin-Lopez
 */
public class FBTestCaseGenerator extends AbstractTestCaseGenerator {

    public static final String INTER_PARAMETER_DEPENDENCY = "inter_parameter_dependency";

    private Float faultyDependencyRatio = 0.5f;                                                // Ratio of faulty test cases due to inter-parameter deps. Defaults to 0.5
    private Integer reloadInputDataEvery = 100;                                            // Number of requests using the same randomly generated input data
    private Integer inputDataMaxValues = 1000;                                            // Number of values used for each parameter when reloading input data
    private Analyzer idlReasoner;                                                            // IDLReasoner to check if requests are valid or not

    // Indexes
    private int maxFaultyTestDueToDependencyViolations;                                            // Maximum number of faulty test cases due to dependency violations to be generated
    private int maxFaultyTestsDueToIndividualConstraints;                                        // Maximum number of faulty test cases due to individual constraints to be generated
    int nFaultyTestDueToDependencyViolations;                                                    // Current number of faulty test cases due to dependency violations to be generated
    int nFaultyTestsDueToIndividualConstraint;                                                    // Current number of faulty test cases due to individual constraints to be generated

    private static Logger logger = LogManager.getLogger(FBTestCaseGenerator.class.getName());

    public FBTestCaseGenerator(OpenAPISpecification spec, TestConfigurationObject conf, int nTests) {
        super(spec, conf, nTests);

    }

    /**
     * Set IDLReasoner for the generation of dependency-aware valid/invalid test cases
     *
     * @param testOperation API operation under test
     */
    public void setUpIDLReasoner(Operation testOperation) {
        idlReasoner = null;
        if (hasDependencies(testOperation.getOpenApiOperation())) // If the operation contains dependencies, create new IDLReasoner for that operation
        {
            try {
                idlReasoner = new OASAnalyzer(spec.getPath(), testOperation.getTestPath(), testOperation.getMethod());
            } catch (IDLException e) {
                logger.warn("There was an error processing the dependencies of the operation {} {}: {}", testOperation.getMethod(), testOperation.getTestPath(), e.getMessage());
            }
        }
    }

    /**
     * Refresh the test data used for the generation of test cases. Test data must be fed to IDLReasoner, which in turn uses it for the generation of test cases using a CSP solver.
     *
     * @param testOperation API operation under test
     */
    public void checkIDLReasonerData(Operation testOperation) {
        if (idlReasoner != null && nTests % reloadInputDataEvery == 0) {
            Map<String, List<String>> inputData = generateInputData(testOperation.getTestParameters()); // Update input data
            try {
                idlReasoner.updateData(inputData);
            } catch (IDLException e) {
                logger.warn("There was an error updating the data of IDLReasoner: {}", e.getMessage());
            }
        }
    }

    /*
     * Generate the collection of test cases
     */
    protected Collection<TestCase> generateOperationTestCases(Operation testOperation) throws RESTestException {


        List<TestCase> testCases = new ArrayList<>();

        setUpIDLReasoner(testOperation);

        // Reset counters for the current operation
        resetOperation();

        // Calculate number and type of faulty tests to be generated
        if (idlReasoner == null) // The operation has no inter-parameter dependencies
            maxFaultyTestDueToDependencyViolations = 0;
        else
            maxFaultyTestDueToDependencyViolations = (int) ((numberOfTests * faultyRatio * faultyDependencyRatio));

        maxFaultyTestsDueToIndividualConstraints = (int) (((numberOfTests * faultyRatio) - maxFaultyTestDueToDependencyViolations));

        nFaultyTestDueToDependencyViolations = 0;
        nFaultyTestsDueToIndividualConstraint = 0;


//        while (hasNext()) {
//            checkIDLReasonerData(testOperation);
//
//            //Timer.startCounting(TEST_CASE_GENERATION);
//            TestCase test = generateNextTestCase(testOperation);
//            //Timer.stopCounting(TEST_CASE_GENERATION);
//
//            // Set authentication data
//            authenticateTestCase(test);
//
//            testCases.add(test);
//
//            // Update indexes
//            updateIndexes(test);
//
//        }

        logger.info("Generating tests for {}:{}", testOperation.getMethod(), testOperation.getTestPath());

        // Valid Test with Valid Fuzzing values
        testCases.add(generateValidTestCaseNew(testOperation));
        // Valid Test with Valid Fuzzing values
        testCases.add(generateValidTestCaseNew(testOperation));
        // Valid Test with Valid Fuzzing values
        testCases.add(generateValidTestCaseNew(testOperation));
        // Valid Test removing optional properties
//        if (testOperation.getOpenApiOperation().getRequestBody() != null) {
//            testCases.add(generateValidTestWithMissingOptionalProperties(testOperation));
//        }

        /*
         * If it has Body
         */
        // Invalid Test duplicating property ??
        // Invalid Test missing content-type ??
        getBodyGenerator(testOperation)
                .ifPresent(bodyGenerator -> {
                    try {
                        // Invalid Test removing required property
                        generateTestWithInvalidBody(testOperation, bodyGenerator, REMOVE_REQUIRED_FROM_BODY).ifPresent(testCases::add);
                        // Invalid Test sending an invalid type property
                        generateTestWithInvalidBody(testOperation, bodyGenerator, CHANGE_BODY_PROPERTY_TYPE).ifPresent(testCases::add);
                        // Invalid Test sending an invalid format property
                        generateTestWithInvalidBody(testOperation, bodyGenerator, CHANGE_BODY_PROPERTY_FORMAT).ifPresent(testCases::add);
                    } catch (RESTestException e) {
                        throw new RuntimeException(e);
                    }
                });

        // TODO: Nullable

        /*
         * If it has parameters
         */
        // Invalid Test removing required property
        generateInvalidTestCaseMutatingProperty(testOperation, REMOVE_REQUIRED_PARAMETER).ifPresent(testCases::add);
        // Invalid Test sending an invalid type property
        generateInvalidTestCaseMutatingProperty(testOperation, INVALID_TYPE).ifPresent(testCases::add);
        // Invalid Test sending an invalid format property
        generateInvalidTestCaseMutatingProperty(testOperation, INVALID_FORMAT).ifPresent(testCases::add);

        return testCases;
    }

    private Optional<TestCase> generateTestWithInvalidBody(Operation testOperation, ITestDataGenerator bodyGenerator,
                                                           MutationPipeline mutation) throws RESTestException {
        TestCase test = generateValidTestCase(testOperation);
        TestCase originalTest = new TestCase(test);

        TestParameter bodyParam = testOperation.getTestParameters()
                .stream()
                .filter(x -> x.getName().equals("body"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Something weird"));

        test.addParameter(bodyParam, ((BodyGenerator) bodyGenerator).nextValueAsString(mutation));
        List<String> errors = test.getValidationErrors(OASAPIValidator.getValidator(spec));

        if (!errors.isEmpty()) {
            test.setFaulty(true);
            test.setId(test.getId() + "_" + mutation);
            String errorsMsg = String.join(" --- ", errors);
            test.setFaultyReason(INDIVIDUAL_PARAMETER_CONSTRAINT + ':' + " invalid request body: " + errorsMsg);
            return Optional.of(test);
        }

        return Optional.empty();
    }

    private Optional<TestCase> generateInvalidTestCaseMutatingProperty(Operation testOperation, String mutation) throws RESTestException {
        TestCase test = generateValidTestCase(testOperation);
        Optional<String> mutated = TestCaseMutation.mutate(test, testOperation.getOpenApiOperation(), mutation);
        mutated.ifPresent(s -> {
            test.setId(test.getId() + "_" + mutation);
            test.setFaulty(true);
            test.setFaultyReason(INDIVIDUAL_PARAMETER_CONSTRAINT + ": " + mutation + s);
        });
        return mutated.isPresent() ? Optional.of(test) : Optional.empty();
    }

    private TestCase generateValidTestCaseNew(Operation testOperation) throws RESTestException {
        TestCase test = generateValidTestCase(testOperation);
        checkTestCaseValidity(test);
        authenticateTestCase(test);
        updateIndexes(test);
        return test;
    }

//    private TestCase generateValidTestWithMissingOptionalProperties(Operation testOperation) throws RESTestException {
//        TestCase test = generateValidTestCase(testOperation);
//
//        makeTestCaseFaultyDueToIndividualConstraints()
//        makeTestCaseFaultyDueToInvalidRequestBody()
//        checkTestCaseValidity(test);
//        authenticateTestCase(test);
//        updateIndexes(test);
//    }

    // Generate the next test case and update the generation index
    public TestCase generateNextTestCase(Operation testOperation) throws RESTestException {

        TestCase test;

        if (nFaultyTestDueToDependencyViolations < maxFaultyTestDueToDependencyViolations)        // Try generating a faulty test case violating one or more inter-parameter dependency
            test = generateNextTestCase(testOperation, INTER_PARAMETER_DEPENDENCY);

        else if (nFaultyTestsDueToIndividualConstraint < maxFaultyTestsDueToIndividualConstraints)        // Try generating a faulty test case violating an individual constraint
            test = generateNextTestCase(testOperation, INDIVIDUAL_PARAMETER_CONSTRAINT);

        else
            test = generateNextTestCase(testOperation, "none");

        checkTestCaseValidity(test);

        return test;
    }


    /**
     * Returns a valid or invalid test cases based on the faultyReason provided.
     *
     * @param testOperation API operation under test
     * @param faultyReason  Faulty reason. Possible values: "none", "individual_parameter_constraint", and "inter_parameter_dependency"
     * @return a valid or invalid test case
     * @throws RESTestException if the test case generated does not conform to the specification
     */
    public TestCase generateNextTestCase(Operation testOperation, String faultyReason) throws RESTestException {

        TestCase test;

        switch (faultyReason) {
            case "none":
                test = generateValidTestCase(testOperation);
                break;

            case INTER_PARAMETER_DEPENDENCY:
                test = generateFaultyTestCaseDueToViolatedDependencies(testOperation);
                if (test.getFaulty())
                    nFaultyTestDueToDependencyViolations++;
                break;

            case INDIVIDUAL_PARAMETER_CONSTRAINT:
                test = generateValidTestCase(testOperation);
                if (makeTestCaseFaultyDueToIndividualConstraints(test, testOperation))
                    nFaultyTestsDueToIndividualConstraint++;
                break;
            default:
                throw new IllegalArgumentException("The faulty reason '" + faultyReason + "' is not supported.");
        }


        return test;
    }


    /* Returns a valid test case satisfying all the individual constraints and inter-parameter dependencies described in the API specification */
    TestCase generateValidTestCase(Operation testOperation) throws RESTestException {
        TestCase test;

        if (idlReasoner != null) {        // The operation has inter-parameter dependencies
            test = createTestCaseTemplate(testOperation);
            try {
                idl2restestTestCase(test, idlReasoner.getRandomValidRequest(), testOperation); // Generate valid test case with IDLReasoner
            } catch (IDLException e) {
                logger.warn("There was an error generating a valid request with IDLReasoner: {}", e.getMessage());
                throw new RESTestException(e);
            }
        } else                            // The operation has no inter-parameter dependences: generate a random test case
            test = generateRandomValidTestCase(testOperation); // Generate valid test case normally (no need to manage deps.)

        test.setFaulty(false);
        test.setFaultyReason("none");
        test.setFulfillsDependencies(true);

        return test;
    }


    /* Returns a faulty test case violating one ore more inter-parameter dependency constraints */
    TestCase generateFaultyTestCaseDueToViolatedDependencies(Operation testOperation) throws RESTestException {

        TestCase test = null;

        if (idlReasoner != null) {        // The operation has inter-parameter dependencies
            test = createTestCaseTemplate(testOperation);
            try {
                idl2restestTestCase(test, idlReasoner.getRandomInvalidRequest(), testOperation); // Generate invalid test case with IDLReasoner
            } catch (IDLException e) {
                logger.warn("There was an error generating an invalid request with IDLReasoner: {}", e.getMessage());
                throw new RESTestException(e);
            }
            test.setFaulty(true);
            test.setFaultyReason(INTER_PARAMETER_DEPENDENCY);
        } else {                        // The operation has no inter-parameter dependencies
            test = generateRandomValidTestCase(testOperation); // Impossible (no deps.), generate valid request
            test.setFaulty(false);
            test.setFaultyReason("none");
            test.setFulfillsDependencies(true);
        }

        return test;
    }


    private Map<String, List<String>> generateInputData(List<TestParameter> testParameters) {
        Map<String, List<String>> inputData = new HashMap<>();
        List<String> paramValues;
        ITestDataGenerator generator;
        for (TestParameter parameter : testParameters) {
            if (parameter.getWeight() == null || parameter.getWeight() > 0) {
                paramValues = new ArrayList<>();
                generator = getRandomGenerator(nominalGenerators.get(Pair.with(parameter.getName(), parameter.getIn())));
                if (generator instanceof RandomInputValueIterator && ((RandomInputValueIterator) generator).getMaxValues() == 1) {
                    paramValues = ((RandomInputValueIterator) generator).getValues();
                } else if (generator instanceof RandomBooleanGenerator) {
                    paramValues = Arrays.asList("true", "false");
                } else {
                    while (paramValues.size() < inputDataMaxValues) {
                        paramValues.add(generator.nextValueAsString());
                    }
                }
                inputData.put(parameter.getName(), paramValues.stream().distinct().collect(Collectors.toList()));
            }
        }

        return inputData;
    }

    // Returns true if there are more test cases to be generated
    protected boolean hasNext() {
        return nTests < numberOfTests;
    }

    public Float getFaultyDependencyRatio() {
        return faultyDependencyRatio;
    }

    public void setFaultyDependencyRatio(Float faultyDependencyRatio) {
        this.faultyDependencyRatio = faultyDependencyRatio;
    }

    public Integer getReloadInputDataEvery() {
        return reloadInputDataEvery;
    }

    public void setReloadInputDataEvery(Integer reloadInputDataEvery) {
        this.reloadInputDataEvery = reloadInputDataEvery;
    }

    public Integer getInputDataMaxValues() {
        return inputDataMaxValues;
    }

    public void setInputDataMaxValues(Integer inputDataMaxValues) {
        this.inputDataMaxValues = inputDataMaxValues;
    }

    public Analyzer getIdlReasoner() {
        return idlReasoner;
    }

    public void setIdlReasoner(Analyzer idlReasoner) {
        this.idlReasoner = idlReasoner;
    }

    public int getnFaultyTestDueToDependencyViolations() {
        return nFaultyTestDueToDependencyViolations;
    }

    public int getnFaultyTestsDueToIndividualConstraint() {
        return nFaultyTestsDueToIndividualConstraint;
    }
}

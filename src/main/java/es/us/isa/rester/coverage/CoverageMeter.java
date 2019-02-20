package es.us.isa.rester.coverage;

import java.util.List;

import es.us.isa.rester.coverage.CriterionType;
import es.us.isa.rester.testcases.TestCase;

/**
 * Class for the measurement of test coverage
 * 
 * @author Alberto Martin-Lopez
 */
public class CoverageMeter {

    CoverageGatherer coverageGatherer;  // coverage gatherer already containing all criteria to be covered
    List<TestCase> testSuite;           // full set of abstract test cases addressing the API
    // List<TestResult> testResults;       // test outputs generated after running the test suite against the API


    public CoverageMeter(CoverageGatherer coverageGatherer) {
        this.coverageGatherer = coverageGatherer;
        this.testSuite = null;
    }

    public CoverageMeter(CoverageGatherer coverageGatherer, List<TestCase> testSuite) {
        this.coverageGatherer = coverageGatherer;
        this.testSuite = testSuite;
    }

    public CoverageGatherer getCoverageGatherer() {
        return this.coverageGatherer;
    }

    public void setCoverageGatherer(CoverageGatherer coverageGatherer) {
        this.coverageGatherer = coverageGatherer;
    }

    public List<TestCase> getTestSuite() {
        return this.testSuite;
    }

    public void setTestSuite(List<TestCase> testSuite) {
        this.testSuite = testSuite;
    }

    public int getAllTotalElements() {
        return getAllElements(null);
    }

    public int getAllInputElements() {
        return getAllElements("input");
    }

    public int getAllOutputElements() {
        return getAllElements("output");
    }

    public int getCoveredTotalElements() {
        return getCoveredElements(null);
    }

    public int getCoveredInputElements() {
        return getCoveredElements("input");
    }

    public int getCoveredOutputElements() {
        return getCoveredElements("output");
    }

    /**
     * Get all elements to cover from all coverage criteria in the API
     * 
     * @param criterionType Type of criteria to consider: "input", "output" or null for all
     * @return Number of elements collected among all coverage criteria
     */
    private int getAllElements(String criterionType) {
        return coverageGatherer.getCoverageCriteria().stream()
                .filter(c -> CriterionType.getTypes(criterionType).contains(c.getType()))
                .mapToInt(c -> c.getAllElements().size())
                .sum();
    }

    /**
     * Get covered elements from all coverage criteria in the API
     * 
     * @param criterionType Type of criteria to consider: "input", "output" or null for all
     * @return Number of covered elements collected among all coverage criteria
     */
    private int getCoveredElements(String criterionType) {
        return coverageGatherer.getCoverageCriteria().stream()
                .filter(c -> CriterionType.getTypes(criterionType).contains(c.getType()))
                .mapToInt(c -> c.getCoveredElements().size())
                .sum();
    }

    /**
     * Get total coverage (input and output) considering all criteria
     * 
     * @return coverage percentage
     */
    public float getTotalCoverage() {
        if (getAllTotalElements() == 0) {
            return 100;
        }
        return 100 * (float) getCoveredTotalElements() / (float) getAllTotalElements();
    }

    /**
     * Get input coverage considering all input criteria
     * 
     * @return coverage percentage
     */
    public float getInputCoverage() {
        if (getAllInputElements() == 0) {
            return 100;
        }
        return 100 * (float) getCoveredInputElements() / (float) getAllInputElements();
    }

    /**
     * Get output coverage considering all output criteria
     * 
     * @return coverage percentage
     */
    public float getOutputCoverage() {
        if (getAllOutputElements() == 0) {
            return 100;
        }
        return 100 * (float) getCoveredOutputElements() / (float) getAllOutputElements();
    }

    /**
     * Get coverage of all criteria of a given type
     * 
     * @param type Type of criterion to check coverage (e.g. PATH, STATUS_CODE, etc.)
     * @return Coverage percentage
     */
    public float getCriterionTypeCoverage(CriterionType type) {
        int allElements = coverageGatherer.getCoverageCriteria().stream()
                .filter(c -> c.getType() == type)
                .mapToInt(c -> c.getAllElements().size())
                .sum();

        if (allElements == 0) {
            return 100;
        }

        int coveredElements = coverageGatherer.getCoverageCriteria().stream()
                .filter(c -> c.getType() == type)
                .mapToInt(c -> c.getCoveredElements().size())
                .sum();
        
        return 100 * (float) coveredElements / (float) allElements;
    }

    /**
     * Get coverage of a single criterion, identified by its type and rootPath
     * 
     * @param type Type of criterion (e.g. PATH, STATUS_CODE, etc.)
     * @param rootPath path that uniquely identifies the criterion (e.g. "/pet->getPetById->id")
     * @return Coverage percentage
     */
    public float getCriterionCoverage(CriterionType type, String rootPath) {
        int allElements = coverageGatherer.getCoverageCriteria().stream()
                .filter(c -> c.getType() == type && c.getRootPath().equals(rootPath))
                .findFirst()
                .orElse(new CoverageCriterion(type)) // if no matching criterion is found, return an empty made-up one (100% coverage)
                .getAllElements().size();

        if (allElements == 0) {
            return 100;
        }

        int coveredElements = coverageGatherer.getCoverageCriteria().stream()
                .filter(c -> c.getType() == type && c.getRootPath().equals(rootPath))
                .findFirst()
                .get()
                .getCoveredElements().size();
        
        return 100 * (float) coveredElements / (float) allElements;
    }

    /**
     * Set 'coveredElements' field of every input CoverageCriterion. Only works
     * once testSuite has been initialized.
     * 
     * @return true if coveredElements were set, false otherwise
     */
    public boolean setCoveredInputElements() {
        if(testSuite == null) {
            return false;
        }

        

        return true;
    }
}

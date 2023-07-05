package es.us.isa.restest.mutation.rules;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.models.media.Schema;

public class RequiredRule extends PathRule {

    private static RequiredRule instance;

    private RequiredRule() {
        super();
    }

    public static RequiredRule getInstance() {
        if (instance == null) {
            instance = new RequiredRule();
        }
        return instance;
    }

    protected void applyNodeFuzzingRule(Schema<?> schema, String objectChild) {
        List<String> propertyNames = schema.getRequired();
//        schema.getProperties().entrySet()
//                .stream()
//                .filter(schema1 -> schema1.getValue().getRequired())
        if (propertyNames != null) {
            propertyNames.remove(objectChild);

            if (!propertyNames.isEmpty()) {
                String dropProperty = propertyNames.get(random.nextInt(propertyNames.size()));
                schema.getProperties().remove(dropProperty);
            }
        }
    }

}

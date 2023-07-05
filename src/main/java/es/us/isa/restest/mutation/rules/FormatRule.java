package es.us.isa.restest.mutation.rules;

import com.google.common.collect.Lists;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public class FormatRule extends SingleRule {

    private static FormatRule instance;

    private FormatRule() {
        super();
    }

    public static FormatRule getInstance() {
        if (instance == null) {
            instance = new FormatRule();
        }
        return instance;
    }

    @Override
    protected void applyNodeFuzzingRule(Schema<?> schema, OpenAPI spec) {
        String type = schema.getType();
        String format = schema.getFormat();

        if (type.equals("integer") && "int32".equals(format)) {
            schema.setFormat("int64");
            schema.example(null);
        } else if (type.equals("integer") && "int64".equals(format)) {
            schema.setFormat(null);
            schema.example(null);
        } else if (type.equals("string") && "date".equals(format)) {
            schema.setFormat("date-time");
            schema.example(null);
        } else if (type.equals("string") && "date-time".equals(format)) {
            schema.setFormat("date");
            schema.example(null);
        }
    }
}

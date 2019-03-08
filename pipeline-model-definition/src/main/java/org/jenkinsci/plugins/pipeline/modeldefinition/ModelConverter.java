package org.jenkinsci.plugins.pipeline.modeldefinition;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.tree.SimpleJsonTree;
import com.github.fge.jsonschema.util.JsonLoader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.parser.Converter;
import org.jenkinsci.plugins.pipeline.modeldefinition.parser.JSONParser;
import org.jenkinsci.plugins.pipeline.modeldefinition.validator.ErrorCollector;
import org.apache.commons.lang3.StringUtils;

public class ModelConverter {

    public JSONObject doToJenkinsfile(String jsonAsString) {

        JSONObject result = new JSONObject();

        if (!StringUtils.isEmpty(jsonAsString)) {
            try {
                JsonNode json = JsonLoader.fromString(jsonAsString);

                JSONParser parser = new JSONParser(new SimpleJsonTree(json));

                ModelASTPipelineDef pipelineDef = parser.parse();

                if (pipelineDef != null && !collectErrors(result, parser.getErrorCollector())) {
                    try {
                        Converter.scriptToPipelineDef(pipelineDef.toPrettyGroovy());
                        result.accumulate("result", "success");
                        result.accumulate("jenkinsfile", pipelineDef.toPrettyGroovy());
                    } catch (Exception e) {
                        JSONObject jfErrors = new JSONObject();
                        // reportFailure(jfErrors, e);
                        JSONArray errors = new JSONArray();
                        errors.add(new JSONObject().accumulate("jenkinsfileErrors", jfErrors));
                        // reportFailure(result, errors);
                    }
                }
            } catch (Exception je) {
                // reportFailure(result, je);
            }
        } else {
            // reportFailure(result, "No content found for 'json' parameter");
        }

        return result;
    }

    @SuppressWarnings("unused")
    public JSONObject doToJson(String groovyAsString) {
        JSONObject result = new JSONObject();

        if (!StringUtils.isEmpty(groovyAsString)) {
            try {
                ModelASTPipelineDef pipelineDef = Converter.scriptToPipelineDef(groovyAsString);
                if (pipelineDef != null) {
                    result.accumulate("result", "success");
                    result.accumulate("json", pipelineDef.toJSON());
                } else {
                    // reportFailure(result, "Jenkinsfile content '" + groovyAsString + "' did not contain the 'pipeline' step");
                }
            } catch (Exception e) {
                // reportFailure(result, e);
            }
        } else {
            // reportFailure(result, "No content found for 'jenkinsfile' parameter");
        }

        return result;
    }

    /**
     * Checks the error collector for errors, and if there are any set the result as failure
     * @param result the result to mutate if so
     * @param errorCollector the collector of errors
     * @return {@code true} if any errors where collected.
     */
    private boolean collectErrors(JSONObject result, ErrorCollector errorCollector) {
        if (errorCollector.getErrorCount() > 0) {
            JSONArray errors = errorCollector.asJson();
            // reportFailure(result, errors);
            return true;
        }
        return false;
    }
}

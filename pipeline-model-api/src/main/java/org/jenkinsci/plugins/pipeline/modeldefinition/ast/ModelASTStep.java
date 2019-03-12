package org.jenkinsci.plugins.pipeline.modeldefinition.ast;

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.pipeline.modeldefinition.validator.ModelValidator;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents an individual step within any of the various blocks that can contain steps.
 *
 * @author Kohsuke Kawaguchi
 * @author Andrew Bayer
 */
@SuppressFBWarnings(value = "SE_NO_SERIALVERSIONID")
public class ModelASTStep extends ModelASTElement {
    /**
     * @deprecated since 1.2-beta-4
     */
    @Deprecated
    public static Map<String, String> blockedStepsBase() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        // map.put("stage", Messages.ModelASTStep_BlockedSteps_Stage());
        // map.put("properties", Messages.ModelASTStep_BlockedSteps_Properties());
        // map.put("parallel", Messages.ModelASTStep_BlockedSteps_Parallel());
        return map;
    }

    /**
     * Use {@code org.jenkinsci.plugins.pipeline.modeldefinition.validator.BlockedStepsAndMethodCalls.blockedInSteps()} instead.
     *
     * @deprecated since 1.2-beta-4
     */
    @Deprecated
    public static Map<String, String> getBlockedSteps() {
        return blockedStepsBase();
    }

    private String name;
    private ModelASTArgumentList args;

    public ModelASTStep(Object sourceLocation) {
        super(sourceLocation);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.accumulate("name", name);
        if (args != null) {
            o.accumulate("arguments", args.toJSON());
        }
        return o;
    }

    @Override
    public void validate(@Nonnull ModelValidator validator) {
        validator.validateElement(this);
        if (args != null) {
            args.validate(validator);
        }
    }

    @Override
    public String toGroovy() {
        return "";
    }

    private String withOrWithoutParens(ModelASTArgumentList argList) {
        if (argList == null) {
            return name + "()";
        } else {
            String argGroovy = argList.toGroovy();
            if (!(this instanceof ModelASTTreeStep) &&
                    argList instanceof ModelASTSingleArgument &&
                    // Special-casing for list/map args since they still need parentheses.
                    !argGroovy.startsWith("[")) {
                return name + " " + argGroovy;
            } else {
                return name + "(" + argGroovy + ")";
            }
        }
    }

    @Override
    public void removeSourceLocation() {
        super.removeSourceLocation();
        if (args != null) {
            args.removeSourceLocation();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelASTArgumentList getArgs() {
        return args;
    }

    public void setArgs(ModelASTArgumentList args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "ModelASTStep{" +
                "name='" + name + '\'' +
                ", args=" + args +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ModelASTStep that = (ModelASTStep) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }
        return getArgs() != null ? getArgs().equals(that.getArgs()) : that.getArgs() == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getArgs() != null ? getArgs().hashCode() : 0);
        return result;
    }
}

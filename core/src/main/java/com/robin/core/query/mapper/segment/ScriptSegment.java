package com.robin.core.query.mapper.segment;

import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.script.ScriptExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.script.Bindings;
import javax.script.CompiledScript;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class ScriptSegment extends AbstractSegment {
    private String scriptType;
    private ScriptExecutor executor;
    private CompiledScript script;

    public ScriptSegment(String nameSpace,String id, String value) {
        super(nameSpace,id, value);
        this.executor= ScriptExecutor.getInstance();

    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
        try {
            script = executor.returnScriptNoCache(scriptType, value);
        }catch (Exception ex){
            log.error("",ex);
        }
    }

    @Override
    public String getSqlPart(Map<String, Object> params, Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap) {
        try {
            Bindings bindings = executor.createBindings(scriptType);
            Iterator<Map.Entry<String, Object>> iter = params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Object> entry = iter.next();
                bindings.put(entry.getKey(), entry.getValue());
            }
            return script.eval(bindings).toString();
        }catch (Exception ex){
            log.error("",ex);
        }
        return null;
    }
}

package com.robin.core.script.spel;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class SpringExpressionScriptEngine implements ScriptEngine, Compilable {
    public static final String ROOT = "_root";
    private final ExpressionParser expressionParser=new SpelExpressionParser();
    private ScriptContext scriptContext=new SimpleScriptContext();
    @Override
    public CompiledScript compile(String script) throws ScriptException {
        try{
            Expression expression=expressionParser.parseExpression(script);
            return new SpringExpressionCompiledScript(this,expression);
        }catch (Exception ex){
            throw new ScriptException(ex);
        }
    }

    @Override
    public CompiledScript compile(Reader reader) throws ScriptException {
        return compile(readScript(reader));
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return eval(script, context.getBindings(ScriptContext.ENGINE_SCOPE));
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return eval(readScript(reader), context);
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return eval(script, scriptContext);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return eval(readScript(reader));
    }



    @Override
    public Object eval(Reader reader, Bindings n) throws ScriptException {
        return eval(readScript(reader), scriptContext.getBindings(ScriptContext.ENGINE_SCOPE));
    }
    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        CompiledScript compile = compile(script);
        return compile.eval(bindings);
    }


    @Override
    public void put(String key, Object value) {
        getBindings(ScriptContext.ENGINE_SCOPE).put(key, value);
    }

    @Override
    public Object get(String key) {
        return getBindings(ScriptContext.ENGINE_SCOPE).get(key);
    }

    @Override
    public Bindings getBindings(int scope) {
        return scriptContext.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        scriptContext.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptContext getContext() {
        return scriptContext;
    }

    @Override
    public void setContext(@NonNull ScriptContext context) {
        Assert.notNull(context,"");
        this.scriptContext=context;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new SpringExpressionScriptEngineFactory();
    }
    private String readScript(Reader reader) throws ScriptException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)){
            StringBuilder s = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                s.append(line);
                s.append("\n");
            }
            return s.toString();
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }
}

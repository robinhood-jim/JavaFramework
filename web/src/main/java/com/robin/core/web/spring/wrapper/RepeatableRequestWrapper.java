package com.robin.core.web.spring.wrapper;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RepeatableRequestWrapper extends HttpServletRequestWrapper {
    private final ByteArrayOutputStream outputStream;

    public RepeatableRequestWrapper(HttpServletRequest request, ServletResponse response) throws IOException {
        super(request);
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        outputStream=wrapByte(request);
    }
    private ByteArrayOutputStream wrapByte(HttpServletRequest request) throws IOException{
        try(InputStream inputStream=request.getInputStream();
                ByteArrayOutputStream outputStream=new ByteArrayOutputStream(inputStream.available())){
            IOUtils.copy(inputStream,outputStream);
            return outputStream;
        }catch (IOException ex){
            throw ex;
        }
    }
    public ServletInputStream getInputStream(){
        final ByteArrayInputStream bais=new ByteArrayInputStream(outputStream.toByteArray());
        return new ServletInputStream()
        {
            @Override
            public int read() throws IOException
            {
                return bais.read();
            }

            @Override
            public int available() throws IOException
            {
                return outputStream.size();
            }
            @Override
            public boolean isFinished()
            {
                return false;
            }

            @Override
            public boolean isReady()
            {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener)
            {

            }
        };
    }
}

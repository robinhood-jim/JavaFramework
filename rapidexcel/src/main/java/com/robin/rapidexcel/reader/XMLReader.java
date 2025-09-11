package com.robin.rapidexcel.reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class XMLReader implements Closeable {
    private XMLInputFactory factory;
    private final InputStream inputStream;
    private final XMLStreamReader reader;
    public XMLReader(XMLInputFactory factory,InputStream inputStream) throws XMLStreamException {
        this.factory=factory;
        this.inputStream=inputStream;
        this.reader=factory.createXMLStreamReader(inputStream);
    }
    public boolean goTo(BooleanSupplier predicate) throws XMLStreamException{
        while (reader.hasNext()){
            reader.next();
            if(predicate.getAsBoolean()){
                return true;
            }
        }
        return false;
    }
    public boolean isStartElement(String elementName) {
        return reader.isStartElement() && elementName.equals(reader.getLocalName());
    }

    public boolean isEndElement(String elementName) {
        return reader.isEndElement() && elementName.equals(reader.getLocalName());
    }

    public boolean goTo(String elementName) throws XMLStreamException {
        return goTo(() -> isStartElement(elementName));
    }
    public boolean hasNext() throws XMLStreamException{
        return reader.hasNext();
    }

    public String getAttribute(String name) {
        return reader.getAttributeValue(null, name);
    }

    public String getAttributeRequired(String name) throws XMLStreamException {
        String value = getAttribute(name);
        if(value == null) {
            throw new XMLStreamException("missing required attribute "+name);
        }
        return value;
    }

    public String getAttribute(String namespace, String name) {
        return reader.getAttributeValue(namespace, name);
    }

    public Optional<String> getOptionalAttribute(String name) {
        return Optional.ofNullable(reader.getAttributeValue(null, name));
    }
    public String getValueUntilEndElement(String elementName) throws XMLStreamException {
        return getValueUntilEndElement(elementName, "");
    }

    public String getValueUntilEndElement(String elementName, String skipping) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        int childElement = 1;
        while (reader.hasNext()) {
            int type = reader.next();
            if (type == XMLStreamReader.CDATA || type == XMLStreamReader.CHARACTERS || type == XMLStreamReader.SPACE) {
                sb.append(reader.getText());
            } else if (type == XMLStreamReader.START_ELEMENT) {
                if(skipping.equals(reader.getLocalName())) {
                    getValueUntilEndElement(reader.getLocalName());
                }else {
                    childElement++;
                }
            } else if (type == XMLStreamReader.END_ELEMENT) {
                childElement--;
                if (elementName.equals(reader.getLocalName()) && childElement == 0) {
                    break;
                }
            }
        }
        return sb.toString();
    }
    public String getLocalName() {
        return reader.getLocalName();
    }
    public void forEach(String startChildElement, String untilEndElement, Consumer<XMLReader> consumer) throws XMLStreamException {
        while (goTo(() -> isStartElement(startChildElement) || isEndElement(untilEndElement))) {
            if (untilEndElement.equals(getLocalName())) {
                break;
            }
            consumer.accept(this);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (reader != null) {
                reader.close();
            }
        }catch (XMLStreamException ex1){
            throw new IOException(ex1);
        }
        if(inputStream!=null){
            inputStream.close();
        }
    }
}

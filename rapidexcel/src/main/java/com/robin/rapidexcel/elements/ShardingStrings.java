package com.robin.rapidexcel.elements;

import com.robin.rapidexcel.reader.XMLReader;
import com.robin.rapidexcel.utils.XMLFactoryUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ShardingStrings {
    private List<String> values=new ArrayList<>();
    private InputStream inputStream;
    private ShardingStrings(InputStream inputStream){
        this.inputStream=inputStream;
    }
    public static ShardingStrings formInputStream(InputStream inputStream) throws XMLStreamException,IOException {
        ShardingStrings shardingStrings=new ShardingStrings(inputStream);
        shardingStrings.construct();
        return shardingStrings;
    }
    private void construct() throws XMLStreamException, IOException{
        try(XMLReader reader=new XMLReader(XMLFactoryUtils.getDefaultInputFactory(),inputStream)){
            while (reader.hasNext()) {
                reader.goTo("si");
                StringBuilder sb=new StringBuilder();
                while (reader.goTo(() -> reader.isStartElement("t")
                        || reader.isStartElement("rPh")
                        || reader.isEndElement("si"))) {
                    if (reader.isStartElement("t")) {
                        sb.append(reader.getValueUntilEndElement("t"));
                    } else if (reader.isEndElement("si")) {
                        break;
                    } else if (reader.isStartElement("rPh")) {
                        reader.goTo(() -> reader.isEndElement("rPh"));
                    }
                }
                values.add(sb.toString());
            }
        }
    }

    public List<String> getValues() {
        return values;
    }
}

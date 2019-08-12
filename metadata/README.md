MetaData Controller 

Main function:
    
     I、Provide Avro Schema By specify DataSource(DB/HDFS/VFS/Redis/Mongodb/)
     2.File Base DataSource support (CSV/JSON/XML/AVRO/PARQUET/PROTOBUF)
     3.Data Explorer,Can detected Data Type Automatically
     
Run Example
    
    1.IDEA run as SpringBoot Application or under target folder run java -jar metadata-1.0-SNAPSHOT.jar
    2.browser http://localhost:8088/schema/resource?sourceId=2&sourceParam=/tmp
    
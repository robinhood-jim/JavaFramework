package com.robin.rapidexcel.utils;

import com.robin.core.fileaccess.util.ByteBufferInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZipStreamEntry implements Closeable {
    private Map<String, InputStream> zipEntrys=new HashMap<>();
    private List<MemorySegment> segmentList=new ArrayList<>();

    private ZipArchiveInputStream stream;
    private static final long DEFAULTINMEMORYSIZE=50*1024*1024L;

    public ZipStreamEntry(ZipArchiveInputStream inputStream) throws IOException{
        this(inputStream,DEFAULTINMEMORYSIZE);
    }

    public ZipStreamEntry(ZipArchiveInputStream inputStream,long maxInMemorySize) throws IOException{
        Assert.notNull(inputStream,"");
        ArchiveEntry entry;
        try {
            while ((entry = inputStream.getNextEntry())!= null) {
                long entrySize=entry.getSize();
                if(entry.getSize()>0L && entrySize < 2147483647L){
                    if(entry.getSize()>maxInMemorySize){
                        MemorySegment segment= MemorySegmentFactory.allocateOffHeapUnsafeMemory(Long.valueOf(entrySize).intValue(),this,new Thread(){});
                        segmentList.add(segment);
                        ByteBufferInputStream inp=new ByteBufferInputStream(segment.getOffHeapBuffer(), IOUtils.toByteArray(inputStream));
                        zipEntrys.put(entry.getName(),inp);
                    }else{
                        InputStream inp=new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
                        zipEntrys.put(entry.getName(),inp);
                    }
                }else{
                    throw new IOException("file too large");
                }
            }
        }catch (IOException ex){

        }
    }
    public InputStream getInputStream(String name){
        String tname=name;
        if(tname.startsWith("/")){
            tname=name.substring(1);
        }
        if(zipEntrys.containsKey(tname)){
            return zipEntrys.get(tname);
        }else if(zipEntrys.containsKey(tname.toLowerCase())){
            return zipEntrys.get(tname.toLowerCase());
        }else if(zipEntrys.containsKey(tname.toUpperCase())){
            return zipEntrys.get(tname.toUpperCase());
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if(!CollectionUtils.isEmpty(zipEntrys)){
            for(InputStream inp: zipEntrys.values()){
                try{
                    inp.close();
                }catch (IOException ex){

                }
            }
        }
        if(!CollectionUtils.isEmpty(segmentList)){
            segmentList.forEach(seg->{
                seg.free();
            });
        }

    }
}

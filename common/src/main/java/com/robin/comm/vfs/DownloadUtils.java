package com.robin.comm.vfs;


import com.google.common.util.concurrent.*;
import com.robin.core.fileaccess.fs.ApacheVfsFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Data;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
@SuppressWarnings("UnstableApiUsage")
public class DownloadUtils {
    private static final FileSystemManager manager = new StandardFileSystemManager();
    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);
    private DownloadUtils(){

    }

    /**
     * using Apache VFS to  multi thread asynchronized download to Local path
     * @param collectionMeta  remote configuration
     * @param resPath         remote path
     * @param targetPath      target Local Path
     * @param threadNum       download Thread
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void asyncDownloadLocal(DataCollectionMeta collectionMeta,String resPath, String targetPath, int threadNum) throws IOException,ExecutionException,InterruptedException {
        FileObject sourceObject = ApacheVfsFileSystemAccessor.getFileObject(manager, collectionMeta, resPath);
        ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(threadNum));
        //目标为本地文件
        FileObject targetObject = manager.resolveFile("file:///" + targetPath, new FileSystemOptions());
        List<ListenableFuture<Long>> futures = new ArrayList<>();
        try {
            long totalSize = sourceObject.getContent().getSize();
            long downloadPart = totalSize / threadNum;
            if (downloadPart % threadNum > 0) {
                downloadPart++;
            }
            for (int i = 0; i < threadNum; i++) {
                DownThread thread = new DownThread(sourceObject, targetObject, totalSize, i * downloadPart, downloadPart, i);
                ListenableFuture<Long> future = pool.submit(thread);

                Futures.addCallback(future, new FutureCallback<Long>() {
                    @Override
                    public void onSuccess(@Nullable Long size) {
                        logger.info(" thread {} read from {} finish! size {}", thread.getDownloadPart(), thread.getStartPos(), size);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                },pool);
                futures.add(future);
            }
            for(ListenableFuture<Long> future:futures){
                future.get();
            }
        }
        catch (IOException |ExecutionException | InterruptedException ex1) {
            throw ex1;
        }
    }

    @Data
    public static class DownThread implements Callable<Long> {
        private FileObject source;
        private FileObject target;
        private Long totalSize;
        private Long startPos;
        private Long readNums;
        private Integer downloadPart;

        public DownThread(FileObject source, FileObject target, Long totalSize, Long startPos, Long readNums, int downloadPart) {
            this.source = source;
            this.target = target;
            this.totalSize = totalSize;
            this.startPos = startPos;
            this.readNums = readNums;
            if(startPos+readNums>totalSize){
                this.readNums=totalSize-startPos;
            }
            this.downloadPart = downloadPart;
        }

        @Override
        public Long call() throws Exception {
            RandomAccessContent content=null;
            RandomAccessContent targetContent=null;
            try {
                //获取服务器端得读写内容
                content = source.getContent().getRandomAccessContent(RandomAccessMode.READ);
                content.seek(startPos);
                //写入到目标系统
                targetContent = target.getContent().getRandomAccessContent(RandomAccessMode.READWRITE);
                targetContent.seek(startPos);
                byte[] buffer = new byte[8190];
                long curpos = 0L;
                while (curpos <  readNums) {
                    int writeNum = 8190;
                    if ( readNums - curpos > 8190) {
                        content.readFully(buffer, 0, 8190);
                    } else {
                        writeNum = Long.valueOf(startPos + readNums - curpos).intValue();
                        content.readFully(buffer, 0, writeNum);
                    }
                    targetContent.write(buffer, 0, writeNum);
                    curpos += writeNum;
                }

                return curpos - startPos;
            } catch (Exception ex1) {
                throw ex1;
            }finally {
                if(!ObjectUtils.isEmpty(content)){
                    content.close();
                }
                if(!ObjectUtils.isEmpty(targetContent)){
                    targetContent.close();
                }
            }
        }

    }

}

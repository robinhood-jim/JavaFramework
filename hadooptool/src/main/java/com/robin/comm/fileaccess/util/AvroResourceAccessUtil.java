package com.robin.comm.fileaccess.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.avro.Schema;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;

public class AvroResourceAccessUtil extends AbstractResourceAccessUtil{
	private Schema schema;
	
	@Override
	public BufferedReader getInResourceByReader(DataCollectionMeta meta)
			throws Exception {
		return null;
	}

	@Override
	public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta)
			throws Exception {
		return null;
	}

	@Override
	public OutputStream getOutResourceByStream(DataCollectionMeta meta)
			throws Exception {
		return null;
	}

	@Override
	public InputStream getInResourceByStream(DataCollectionMeta meta)
			throws Exception {
		return null;
	}

}

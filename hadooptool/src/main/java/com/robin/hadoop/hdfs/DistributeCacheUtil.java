package com.robin.hadoop.hdfs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;

import org.apache.hadoop.fs.Path;

public class DistributeCacheUtil {
	public static List<Map<String, String>> getConfigByCache(Configuration conf, String columnArr, String fileNamePrefix,String sep) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			Path[] paths = DistributedCache.getLocalCacheFiles(conf);
			String[] columnNames = columnArr.split(",");
			if (paths != null && paths.length > 0) {
				for (int i = 0; i < paths.length; i++) {
					String filename = paths[i].getName();
					if (filename.startsWith(fileNamePrefix)) {
						BufferedReader br = new BufferedReader(new FileReader(paths[i].toString()));
						String line;
						String[] tokens;
						try {
							while ((line = br.readLine()) != null) {
								tokens = line.split(sep);
								if (tokens.length < columnNames.length)
									continue;
								Map<String, String> tmpMap = new HashMap<String, String>();
								for (int j = 0; j < columnNames.length; j++) {
									tmpMap.put(columnNames[j], tokens[j]);
								}
								list.add(tmpMap);
							}
						} finally {
							br.close();
						}
					}
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

}

/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.base.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

@Slf4j
public class CsvProcessor {

    public static int ReadFile(InputStream inputStream, CsvConfig config, List<Map<String, String>> columnResultList) {

        int pos = 0;
        try {
            ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), CsvPreference.STANDARD_PREFERENCE);
            pos = readHeadContentByConfig(reader, config, columnResultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    public static int readFile(InputStream inputStream, CsvConfig config, char separator, List<Map<String, String>> columnResultList) {

        int pos = 0;
        try {
            ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), new CsvPreference.Builder('"', separator, "n").build());
            pos = readHeadContentByConfig(reader, config, columnResultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    public static int readFile(InputStream inputStream, List<Map<String, String>> columnResultList) {

        int pos = 0;
        try {
            ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), CsvPreference.STANDARD_PREFERENCE);
            pos = readHeadAndContent(reader, columnResultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    public static int readFile(InputStream inputStream, char seperator, List<Map<String, String>> columnResultList) {

        int pos = 0;
        try {
            ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), new CsvPreference.Builder('"', seperator, "n").build());
            pos = readHeadAndContent(reader, columnResultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    public static int readFile(Reader ireader, List<Map<String, String>> columnResultList) {

        int pos = 0;
        try {
            ICsvListReader reader = new CsvListReader(ireader, CsvPreference.STANDARD_PREFERENCE);
            pos = readHeadAndContent(reader, columnResultList);
        } catch (Exception e) {
            log.error("", e);
        }
        return pos;
    }

    public static int readFile(Reader ireader, CsvConfig config, List<Map<String, String>> columnResultList) {

        int pos = 0;
        try {
            ICsvListReader reader = new CsvListReader(ireader, CsvPreference.STANDARD_PREFERENCE);
            pos = readHeadContentByConfig(reader, config, columnResultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    public static int readFile(Reader ireader, char seperator, List<Map<String, String>> columnResultList) {

        int pos = 0;
        try {
            ICsvListReader reader = new CsvListReader(ireader, new CsvPreference.Builder('"', seperator, "n").build());
            pos = readHeadAndContent(reader, columnResultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    public static int readFile(Reader ireader, char seperator, CsvConfig config, List<Map<String, String>> columnResultList) {
        int pos = 0;
        try {
            ICsvListReader reader = new CsvListReader(ireader, new CsvPreference.Builder('"', seperator, "n").build());
            pos = readHeadContentByConfig(reader, config, columnResultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pos;
    }

    public static void writeFile(PrintWriter pwriter, String[] header, List<String[]> resultList, String splitchar) {
        try {
            CsvPreference preference = new CsvPreference.Builder('"', splitchar.charAt(0), "n").build();
            ICsvListWriter writer = new CsvListWriter(pwriter, preference);
            writer.writeHeader(header);
            for (String[] strArr : resultList) {
                writer.write(strArr);
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int readHeadContentByConfig(ICsvListReader reader, CsvConfig csvConfig, List<Map<String, String>> columnResultList) throws IOException {
        int pos = 0;
        List<CsvColumnConfig> columnList = csvConfig.getConfigList();
        List<String> resultlist;
        while ((resultlist = reader.read()) != null) {
            pos++;
            Map<String, String> resultMap = new HashMap<String, String>();
            for (int j = 0; j < columnList.size(); j++) {
                CsvColumnConfig colconfig = columnList.get(j);
                resultMap.put(colconfig.getColumnCode(), resultlist.get(j));
            }
            columnResultList.add(resultMap);
        }
        return pos;
    }

    private static int readHeadAndContent(ICsvListReader reader, List<Map<String, String>> columnResultList) throws Exception {
        int pos = 0;
        String[] header = reader.getHeader(true);
        if (header == null || header.length == 0) {
            throw new Exception("no file");
        }

        List<String> resultlist;
        while ((resultlist = reader.read()) != null) {
            pos++;
            Map<String, String> resultMap = new HashMap<String, String>();
            for (int j = 0; j < header.length; j++) {
                resultMap.put(header[j], resultlist.get(j));
            }
            columnResultList.add(resultMap);
        }
        return pos;
    }

}
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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.CharSequenceUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.util.Assert;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    public static final int ASCII_VISIBLE_START = 48;
    public static final int ASCII_VISIBLE_END = 122;
    public static final int ASCII_UPPER_START = 64;
    public static final int ASCII_LOWER_START = 96;
    private static final Random random = new Random();

    /**
     * custom String split
     *
     * @param str
     * @param delimer
     * @param excludeArr char must exclude for example like \","{:}"
     * @return
     */
    public static String[] split(String str, char delimer, String[] excludeArr) {
        char[] chars = str.toCharArray();
        List<String> list = new ArrayList<>();
        String[] arrs = (excludeArr != null && excludeArr.length > 0) ? excludeArr : new String[]{"\"", "'"};
        List<Character> includeList = new ArrayList<>();
        List<Character> includeSuffixList = new ArrayList<>();
        for (String arr : arrs) {
            if (!arr.contains(":")) {
                includeList.add(arr.charAt(0));
                includeSuffixList.add(arr.charAt(0));
            } else {
                String[] sepArr = arr.split(":");
                includeList.add(sepArr[0].charAt(0));
                includeSuffixList.add(sepArr[1].charAt(0));
            }
        }
        int start = 0;
        int length = str.length();
        int i = 0;
        Character curstr ;
        int selpos ;
        //start pos
        boolean startpos = true;
        while (i < length) {
            curstr = chars[i];
            if (startpos && includeList.contains(curstr)) {
                if (start == i) {
                    start = ++i;
                    selpos = includeList.indexOf(curstr);
                    while (i < length && !Character.valueOf(chars[i]).equals(includeSuffixList.get(selpos))) {
                        i++;
                    }
                    list.add(str.substring(start, i));
                    if (i + 2 < chars.length) {
                        i += 2;
                        start = i;
                    } else {
                        start = length;
                        i = length;
                    }
                    startpos = true;
                }
            } else if (chars[i] != delimer) {
                i++;
                startpos = false;
            } else {
                if (start == i) {
                    list.add("");
                    i++;
                    start = i;
                } else {
                    list.add(str.substring(start, i));
                    if (i < length) {
                        i++;
                        while (i < length && chars[i] == delimer) {
                            list.add("");
                            i++;
                        }
                    }
                    startpos = true;
                }
                start = i;
            }
        }
        if (start < length) {
            int pos = length;
            for (Character character : includeSuffixList) {
                if (str.endsWith(character.toString())) {
                    pos--;
                    break;
                }
            }

            list.add(str.substring(start, pos));
        }
        if (str.endsWith(String.valueOf(delimer))) {
            list.add("");
        }
        return list.toArray(new String[1]);
    }

    public static String[] split(String str, char delimer) {
        return split(str, delimer, null);
    }

    public static String getStackTrace(Exception ex) {
        StringWriter writer = new StringWriter();
        PrintWriter wr = new PrintWriter(writer);
        String errMsg = null;
        try {
            ex.printStackTrace(wr);
            errMsg = writer.toString();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            wr.close();
        }
        return errMsg;
    }

    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(Object[] array, String separator, int startIndex,
                              int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }

        int bufSize = endIndex - startIndex;
        if (bufSize <= 0) {
            return "";
        }

        bufSize *= (((array[startIndex] == null) ? 16 : array[startIndex]
                .toString().length()) + separator.length());

        StrBuilder buf = new StrBuilder(bufSize);

        for (int i = startIndex; i < endIndex; ++i) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    public static int getSplitCharInt(String split) {
        int retchar ;
        if ("\\t".equals(split)) {
            retchar = 10;
        } else if ("0x1F".equalsIgnoreCase(split)) {
            retchar = 31;
        } else {
            retchar = split.charAt(0);
        }
        return retchar;
    }

    public static String getSplitChar(String split) {
        String retchar;
        if ("\\t".equals(split)) {
            retchar = "\t";
        } else if ("0x1F".equalsIgnoreCase(split)) {
            retchar = String.valueOf('\u001F');
        } else {
            retchar = split;
        }
        return retchar;
    }

    public static String initialCharToUpperCase(String input) {
        if (input.length() > 2) {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        } else {
            return null;
        }
    }

    public static String initialCharToLowCase(String input) {
        if (input.length() > 2) {
            return input.substring(0, 1).toLowerCase() + input.substring(1);
        } else {
            return null;
        }
    }

    public static String generateRandomChar(int length) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            builder.append((char) (ASCII_VISIBLE_START + getRandomChar(random)));
        }
        return builder.toString();
    }

    private static int getRandomUpperChar(Random random) {
        return ASCII_UPPER_START + random.nextInt(26) + 1;
    }

    private static int getRandomLowerChar(Random random) {
        return ASCII_LOWER_START + random.nextInt(26) + 1;
    }

    public static String genarateRandomUpperLowerChar(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (random.nextFloat() < 0.5) {
                builder.append((char) getRandomUpperChar(random));
            } else {
                builder.append((char) getRandomLowerChar(random));
            }
        }
        return builder.toString();
    }

    private static int getRandomChar(Random random) {
        return random.nextInt(ASCII_VISIBLE_END - ASCII_VISIBLE_START + 1);
    }

    public static String getMd5Encry(String inputStr) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(inputStr.getBytes());
        return Hex.encodeHexString(md.digest()).toUpperCase();
    }

    public static String returnCamelCaseByFieldName(String fieldName) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < fieldName.length(); i++) {
            if (fieldName.charAt(i) == '_') {
                i++;
                builder.append(Character.toUpperCase(fieldName.charAt(i)));
            } else {
                builder.append(Character.toLowerCase(fieldName.charAt(i)));
            }

        }
		
        return builder.toString();
    }

    public static String getFieldNameByCamelCase(String fieldName) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(fieldName)) {
            return fieldName;
        }
        StringBuilder builder = new StringBuilder();
        //if String contains UpperCase
        if (!fieldName.equals(fieldName.toLowerCase())) {
            for (int i = 0; i < fieldName.length(); i++) {
                if (Character.isUpperCase(fieldName.charAt(i))) {
                    builder.append("_");
                    builder.append(Character.toLowerCase(fieldName.charAt(i)));
                } else {
                    builder.append(fieldName.charAt(i));
                }
            }
        } else {
            builder.append(fieldName);
        }
        return builder.toString();
    }

    public static boolean isEmpty(Object object) {
        return object == null || object.toString().trim().isEmpty();
    }

    /**
     * get trim string by specify char end  (430000  trim zero  result 43)
     * @param input
     * @param selChar  end char
     * @return
     */
    public static String trimCharRight(String input,char selChar){
        int pos=input.length();
        for(int i=input.length()-1;i>0;i--){
            if(input.charAt(i)==selChar){
                pos=i;
            }else{
                break;
            }
        }
        return input.substring(0,pos);
    }
    public static String fillCharHeader(String input,int length,char fillChar){
        StringBuilder builder=new StringBuilder(input);
        for(int i=0;i<length-input.length();i++){
            builder.append(fillChar);
        }
        return builder.toString();
    }
    public static String fillCharTail(String input,int length,char fillChar){
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<length-input.length();i++){
            builder.append(fillChar);
        }
        builder.append(input);
        return builder.toString();
    }
    public static String stringToUnicode(String input,String header){
        Assert.isTrue(!StringUtils.isEmpty(input),"");
        String headerStr=StringUtils.isEmpty(header)?"\\u":header;
        char[] chars=input.toCharArray();
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<chars.length;i++){
            builder.append(headerStr+Integer.toHexString(chars[i]).toUpperCase());
        }
        return builder.toString();
    }
    public static boolean isValidUtf8(byte[] b,int aMaxCount){
        int lLen=b.length,lCharCount=0;
        for(int i=0;i<lLen && lCharCount<aMaxCount;++lCharCount){
            byte lByte=b[i++];//to fast operation, ++ now, ready for the following for(;;)
            if(lByte>=0) continue;//>=0 is normal ascii
            if(lByte<(byte)0xc0 || lByte>(byte)0xfd) return false;
            int lCount=lByte>(byte)0xfc?5:lByte>(byte)0xf8?4
                    :lByte>(byte)0xf0?3:lByte>(byte)0xe0?2:1;
            if(i+lCount>lLen) return false;
            for(int j=0;j<lCount;++j,++i) if(b[i]>=(byte)0xc0) return false;
        }
        return true;
    }


    public static void main(String[] args) {
        //System.out.println(getFieldNameByCamelCase("asdsadTTsdadDDasda"));
        //System.out.println(returnCamelCaseByFieldName("index_cd"));
        System.out.println(stringToUnicode("更新时间：2023年09月05日10时","%u"));
    }

}

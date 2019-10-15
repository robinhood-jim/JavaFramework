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

import org.apache.commons.lang3.text.StrBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



import javax.xml.bind.DatatypeConverter;

public class StringUtils {
	public static final int ASCII_VISABLE_START=48;
	public static final int ASCII_VISABLE_END=122;
	public static final int ASCII_UPPER_START=64;
	public static final int ASCII_LOWER_START=96;

	/**
	 * custom String split 
	 * @param str   
	 * @param delimer   
	 * @param excludeArr  char must exclude for example like \","{:}"
	 * @return
	 */
	public static String[] split(String str,char delimer,String[] excludeArr){
		char[] chars=str.toCharArray();
		List<String> list=new ArrayList<String>();
		String[] arrs=(excludeArr!=null && excludeArr.length>0)?excludeArr:new String[]{"\"","'"};
		List<Character> includeList=new ArrayList<Character>();//Arrays.asList(arrs);
		List<Character> includeSuffixList=new ArrayList<Character>();
		for (int i = 0; i < arrs.length; i++) {
			if(!arrs[i].contains(":")){
				includeList.add(Character.valueOf(arrs[i].charAt(0)));
				includeSuffixList.add(Character.valueOf(arrs[i].charAt(0)));
			}else{
				String[] sepArr=arrs[i].split(":");
				includeList.add(Character.valueOf(sepArr[0].charAt(0)));
				includeSuffixList.add(Character.valueOf(sepArr[1].charAt(0)));
			}
		}
		int start=0;
		int length=str.length();
		int i=0;
		Character curstr=null;
		int selpos=0;
		//start pos
		boolean startpos=true;
		while (i<length) {
			curstr=Character.valueOf(chars[i]);
			if(startpos && includeList.contains(curstr)){
				if(start==i){
					start=++i;
					selpos=includeList.indexOf(curstr);
					while(i<length && !Character.valueOf(chars[i]).equals(includeSuffixList.get(selpos))){
						i++;
					}
					list.add(str.substring(start,i));
					if(i+2<chars.length){
						i+=2;
						start=i;
					}
					else{
						start=length;
						i=length;
					}
					startpos=true;
				}
			}else if(chars[i]!=delimer){
				i++;
				startpos=false;
			}else{
				if(start==i){
					list.add("");
					i++;
					start=i;
				}else{
					list.add(str.substring(start,i));
					if(i<length){
						i++;
						while(i<length && chars[i]==delimer){
							list.add("");
							i++;
						}
					}
					startpos=true;
				}
				start=i;
			}
		}
		if(start<length){
			int pos=length;
			for (int j = 0; j < includeSuffixList.size(); j++) {
				if(str.endsWith(includeSuffixList.get(j).toString())){
					pos--;
					break;
				}
			}
			
			list.add(str.substring(start,pos));
		}
		if(str.endsWith(new String(new char[]{delimer}))){
			list.add("");
		}
		String[] retArr=list.toArray(new String[1]);
		return retArr;
	}
	public static String[] split(String str,char delimer){
		return split(str, delimer, null);
	}
	public static String getStackTrace(Exception ex){
		StringWriter writer=new StringWriter();
		PrintWriter wr=new PrintWriter(writer);
		String errMsg=null;
		try  
	    {  
	        ex.printStackTrace(wr);  
	        errMsg=writer.toString();  
	    } catch(Exception e1){
	    	e1.printStackTrace();	
	    }
	    finally  
	    {  
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
	public static int getSplitCharInt(String split){
		int retchar=0;
		if("\\t".equals(split)){
			retchar=10;
		}else if("0x1F".equalsIgnoreCase(split)){
			retchar=31;
		}else {
			retchar=split.charAt(0);
		}
		return retchar;
	}
	public static String getSplitChar(String split){
		String retchar="";
		if("\\t".equals(split)){
			retchar="\t";
		}else if("0x1F".equalsIgnoreCase(split)){
			retchar=String.valueOf('\u001F');
		}else {
			retchar=split;
		}
		return retchar;
	}
	public static String initailCharToUpperCase(String input){
		if(input.length()>2) {
            return input.substring(0,1).toUpperCase()+input.substring(1);
        } else {
            return null;
        }
	}
	public static String initailCharToLowCase(String input){
		if(input.length()>2) {
            return input.substring(0,1).toLowerCase()+input.substring(1);
        } else {
            return null;
        }
	}
	public static String generateRandomChar(int length){
		StringBuilder builder=new StringBuilder();
		Random random=new Random();
		for(int i=0;i<length;i++){
			builder.append((char)(ASCII_VISABLE_START+getRandomChar(random)));
		}
		return builder.toString();
	}
	private static int getRandomUpperChar(Random random){
		return ASCII_UPPER_START+random.nextInt(26)+1;
	}
	private static int getRandomLowerChar(Random random){
		return ASCII_LOWER_START+random.nextInt(26)+1;
	}
	public static String genarateRandomUpperLowerChar(int length){
		StringBuilder builder=new StringBuilder();
		Random random=new Random();
		for(int i=0;i<length;i++){
			if(random.nextFloat()<0.5) {
                builder.append((char)getRandomUpperChar(random));
            } else{
				builder.append((char)getRandomLowerChar(random));
			}
		}
		return builder.toString();
	}
	private static int getRandomChar(Random random){
		return random.nextInt(ASCII_VISABLE_END-ASCII_VISABLE_START+1);
	}
	public static String getMd5Encry(String inputStr) throws NoSuchAlgorithmException {
		MessageDigest md=MessageDigest.getInstance("MD5");
		md.update(inputStr.getBytes());
		return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
	}
	public static boolean isEmpty(Object object){
		return object!=null && !object.toString().isEmpty();
	}
	public static void main(String[] args){
		System.out.println(genarateRandomUpperLowerChar(8));
	}

}

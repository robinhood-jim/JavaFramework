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
package com.robin.core.collection.util;

import org.springframework.util.CollectionUtils;

import java.util.*;

public class CollectionsUtil {
	private CollectionsUtil(){

	}

	public static List<Map.Entry<String,Long>> getSortedMapByLongValue(Map<String,Long> h, final boolean asc) {
		List<Map.Entry<String, Long>> l = new ArrayList<>(h.entrySet());
		Collections.sort(l, (o1, o2) -> getCompareVal(o1, o2,asc));
		return l;
	}
	private static int getCompareVal(Map.Entry<String, ?> o1, Map.Entry<String, ?> o2,boolean asc) {
		int retval = 0;
		if(o1.getValue() instanceof Long) {
			if ((Long) o2.getValue() > (Long) o1.getValue()) {
				if (asc) {
                    retval = -1;
                } else {
                    retval = 1;
                }
			} else if ((Long)o2.getValue() < (Long)o1.getValue()) {
				if (asc) {
                    retval = 1;
                } else {
                    retval = -1;
                }
			}
		}else if(o1.getValue() instanceof Integer){
			if ((Integer) o2.getValue() > (Integer) o1.getValue()) {
				if (asc) {
                    retval = -1;
                } else {
                    retval = 1;
                }
			} else if ((Integer)o2.getValue() < (Integer) o1.getValue()) {
				if (asc) {
                    retval = 1;
                } else {
                    retval = -1;
                }
			}
		}else if(o1.getValue() instanceof Double){
			if ((Double) o2.getValue() > (Double) o1.getValue()) {
				if (asc) {
                    retval = -1;
                } else {
                    retval = 1;
                }
			} else if ((Double)o2.getValue() < (Double) o1.getValue()) {
				if (asc) {
                    retval = 1;
                } else {
                    retval = -1;
                }
			}
		}
		return retval;
	}
	public static List<Map.Entry<String,Integer>> getSortedMapByIntValue(Map<String,Integer> h,final boolean asc) {
		List<Map.Entry<String, Integer>> l = new ArrayList<>(h.entrySet());
		Collections.sort(l, (o1, o2) -> getCompareVal(o1,o2,asc));
		return l;
	}
	public static List<Map.Entry<String,Double>> getSortedMapByDoubleValue(Map<String,Double> h,final boolean asc) {
		List<Map.Entry<String, Double>> l = new ArrayList<>(h.entrySet());
		Collections.sort(l, (o1, o2) -> getCompareVal(o1,o2,asc));
		return l;
	}
	public static boolean isEmpty(Collection<?> collection){
		return CollectionUtils.isEmpty(collection);
	}
	public static boolean isEmpty(Map<?,?> map){
		return CollectionUtils.isEmpty(map);
	}
	public static boolean isEmpty(Object[] objects){
		return objects==null || objects.length==0;
	}

}

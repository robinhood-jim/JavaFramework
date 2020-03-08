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

import java.util.*;

public class CollectionsUtil {

	public static ArrayList<Map.Entry<String,Long>> getSortedMapByLongValue(Map<String,Long> h,final boolean asc) {
		ArrayList<Map.Entry<String, Long>> l = new ArrayList<Map.Entry<String, Long>>(h.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Long>>() {
			@Override
            public int compare(Map.Entry<String, Long> o1,
                               Map.Entry<String, Long> o2) {
				return getCompareVal(o1, o2,asc);
			}
		});
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
	public static ArrayList<Map.Entry<String,Integer>> getSortedMapByIntValue(Map<String,Integer> h,final boolean asc) {
		ArrayList<Map.Entry<String, Integer>> l = new ArrayList<Map.Entry<String, Integer>>(h.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Integer>>() {
			@Override
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
				return getCompareVal(o1,o2,asc);
			}
		});
		return l;
	}
	public static ArrayList<Map.Entry<String,Double>> getSortedMapByDoubleValue(Map<String,Double> h,final boolean asc) {
		ArrayList<Map.Entry<String, Double>> l = new ArrayList<Map.Entry<String, Double>>(h.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Double>>() {
			@Override
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
				return getCompareVal(o1,o2,asc);
			}
		});
		return l;
	}
	public static boolean isEmpty(Collection collection){
		return collection==null || collection.isEmpty();
	}
	public static boolean isEmpty(Map map){
		return map==null || map.isEmpty();
	}
	public static boolean isEmpty(Object[] objects){
		return objects==null || objects.length==0;
	}

}

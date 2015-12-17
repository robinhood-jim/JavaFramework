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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class CollectionsUtil {

	public static ArrayList<Map.Entry<String,Long>> getSortedMapByLongValue(Map<String,Long> h,final boolean asc) {
		ArrayList<Map.Entry<String, Long>> l = new ArrayList<Map.Entry<String, Long>>(h.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Long>>() {
			public int compare(Map.Entry<String, Long> o1,
					Map.Entry<String, Long> o2) {
				int retval = 0;
				if (o2.getValue() > o1.getValue()){
					if(asc)
						retval=-1;
					else
						retval = 1;
				}
				else if (o2.getValue() < o1.getValue()){
					if(asc)
						retval=1;
					else
						retval = -1;
				}
				return retval;
			}
		});
		return l;
	}
	public static ArrayList<Map.Entry<String,Integer>> getSortedMapByIntValue(Map<String,Integer> h,final boolean asc) {
		ArrayList<Map.Entry<String, Integer>> l = new ArrayList<Map.Entry<String, Integer>>(h.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				int retval = 0;
				if (o2.getValue() > o1.getValue()){
					if(asc)
						retval=-1;
					else
						retval = 1;
				}
				else if (o2.getValue() < o1.getValue()){
					if(asc)
						retval=1;
					else
						retval = -1;
				}
				return retval;
			}
		});
		return l;
	}

}

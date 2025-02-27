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
package com.robin.basis.controller.main;

import com.robin.core.base.util.Const;
import com.robin.core.web.util.Session;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/menu")
public class MenuController {
	@Resource
	private RedisTemplate<String,Object> redisTemplate;


	@PostMapping("/list")
	@ResponseBody
	public Map<String,Object> getMenu(HttpServletRequest request,HttpServletResponse response){
		String id=request.getParameter("id");
		Session session=(Session) request.getSession().getAttribute(Const.SESSION);
		Map<Long,List<Map<String,Object>>> privMap= (Map<Long, List<Map<String, Object>>>) redisTemplate.opsForValue().get("SESSION:priv:"+session.getUserId());
		List<Map<String,Object>> privList=privMap.get(Long.valueOf(id));
		Map<String,Object> map=new HashMap<>();
		map.put("id", id);
		List<Map<String,Object>> itemlist=new ArrayList<>();
		if(privList!=null){
			for (Map<String, Object> tmap:privList) {
				Map<String,Object> insertMap=new HashMap<>();
				insertMap.put("id", tmap.get("id"));
				insertMap.put("text", tmap.get("name"));
				Map<String,String> userMap=new HashMap<>();
				userMap.put("name", "url");
				if(tmap.get("url")!=null) {
                    userMap.put("content", tmap.get("url").toString());
                } else{
					userMap.put("content", "");
				}
				List<Map<String,String>> list1=new ArrayList<>();
				list1.add(userMap);
				insertMap.put("userdata", list1);
				if(tmap.get("leafTag").toString().equals(Const.VALID)){
					insertMap.put("open", "1");
				}else {
					insertMap.put("child", "1");
				}
				itemlist.add(insertMap);
			}
			map.put("item", itemlist);
		}else{
			map.put("item", new ArrayList<String>());
		}
		return map;
	}
}

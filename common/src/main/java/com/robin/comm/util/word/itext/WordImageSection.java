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
package com.robin.comm.util.word.itext;

import com.lowagie.text.Image;

public class WordImageSection {
	private String imgUrl;
	private int imageAlign=Image.ALIGN_CENTER;
	private int height;
	private int weight;
	private int percent=100;
	private int heightPercent=100;
	private int weightPercent=100;
	private int rotation;
	private float absoluteX=0;
	private float absoluteY=0;
	
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public int getImageAlign() {
		return imageAlign;
	}
	public void setImageAlign(int imageAlign) {
		this.imageAlign = imageAlign;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}
	public int getHeightPercent() {
		return heightPercent;
	}
	public void setHeightPercent(int heightPercent) {
		this.heightPercent = heightPercent;
	}
	public int getWeightPercent() {
		return weightPercent;
	}
	public void setWeightPercent(int weightPercent) {
		this.weightPercent = weightPercent;
	}
	public int getRotation() {
		return rotation;
	}
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	public void setAbsolute(float x,float y){
		this.absoluteX=x;
		this.absoluteY=y;
	}
	public float getAbsoluteX() {
		return absoluteX;
	}
	public float getAbsoluteY() {
		return absoluteY;
	}
	
}

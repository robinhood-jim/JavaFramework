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
package com.robin.meta.comm;

public class MetaDataConst {
	public enum ResourceType{
		TYPE_LOCALFILE("0"),  //local
		TYPE_HDFSFILE("1"),   //hdfs
		TYPE_FTPFILE("2"),     //ftp
		TYPE_SFTP("3"),   //
		TYPE_SFTPFILE("4"), //sftp
		TYPE_DB("5"),  //db
		TYPE_REDIS("6"), //redis
		TYPE_MONGODB("7"),
		TYPE_SVN("8"),
		TYPE_GIT("9"); //mongodb
	 	private String value;

	    private ResourceType(String value) {
	      this.value = value;
	    }

	    @Override
	    public String toString() {
	      return String.valueOf(this.value);
	    }
	}
	public enum FileFormat{
		TYPE_CSV("1"),       //csv
		TYPE_JSON("2"),   //json
		TYPE_XLSX("3"),     //xlsx
		TYPE_XML("4"),     //xml
		TYPE_PARQUET("5"),  //parquet
		TYPE_AVRO("6");   //avro
		private String value;
		private FileFormat(String value){this.value=value;}
		@Override
        public String toString() {
			return String.valueOf(this.value);
		}
	}

}


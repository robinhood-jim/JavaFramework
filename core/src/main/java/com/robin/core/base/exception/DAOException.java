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
package com.robin.core.base.exception;

@SuppressWarnings("serial")
public class DAOException extends AbstractCodeException
{

    public DAOException()
    {
        super(500);
    }

    public DAOException(String s)
    {
        super(500,s);
    }
    public DAOException(int retCode,String message)
    {
        super(retCode,message);
    }
    public DAOException(Exception e){
    	super(e);
    }
    public DAOException(Throwable ex1){
        super(500,ex1.getMessage());
    }
}
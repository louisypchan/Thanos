package com.thanos.test;

import com.louis.ice.client.IceClient2;
import com.louis.ice.client.config.IceClientProperties;
import com.thanos.service.entry.EntryServicePrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.util.HashMap;
import java.util.Map;

/****************************************************************************
 Copyright (c) 2017 Louis Y P Chen.
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
public class Test {

    public static void main(String... args){

        IceClientProperties iceClientProperties = new IceClientProperties();
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("com-thanos-service-entry-EntryServicePrx", "tcp -p 20003");
        iceClientProperties.setEndpoints(endpoints);

        Communicator communicator =  Util.initialize(args);

        IceClient2 iceClient = new IceClient2(communicator, iceClientProperties);

        EntryServicePrx prx = iceClient.getService(EntryServicePrx.class);

        if(prx == null){
            throw new Error("Invalid proxy");
        }


        System.out.println(prx.login(""));

//        Communicator communicator = Util.initialize(args);
//
//        ObjectPrx base = communicator.stringToProxy("entry:tcp -p 10002");
//        EntryServicePrx prx = EntryServicePrx.checkedCast(base);
//
//        System.out.println(prx.login(null));
    }
}

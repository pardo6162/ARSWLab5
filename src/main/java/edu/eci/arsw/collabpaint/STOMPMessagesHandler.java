/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabpaint;

import edu.eci.arsw.collabpaint.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.concurrent.atomic.AtomicInteger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import util.JedisUtil;

/**
 *
 * @author 2115237
 */
@Controller
public class STOMPMessagesHandler {

    @Autowired
    SimpMessagingTemplate msgt;
    

    @MessageMapping("/newpoint.{numdibujo}")
    public void handlePointEvent(Point pt, @DestinationVariable String numdibujo) throws Exception {

        Jedis jedis;
        jedis = JedisUtil.getPool().getResource();
        String luaScript = "local xval,yval; \n"
                + "if (redis.call('LLEN','x')==4) then \n"
                + "	xval=redis.call('LRANGE','x',0,-1); 			\n"
                + "	yval=redis.call('LRANGE','y',0,-1);\n"
                + "	redis.call('DEL','x'); \n"
                + "	redis.call('DEL','y'); 		\n"
                + "	return {xval,yval}; \n"
                + "else \n"
                + "	return {}; \n"
                + "end";
        List<Object> res = new ArrayList<>();
        Response<Object> luares;
        jedis.watch("x", "y");
        Transaction t = jedis.multi();
        t.rpush("x", String.valueOf(pt.getX()));
        t.rpush("y", String.valueOf(pt.getY()));
        luares = t.eval(luaScript.getBytes(), 0, "0".getBytes());
        res = t.exec();
        ArrayList<Point> polygonPoints=new ArrayList<>();
        if (((ArrayList) luares.get()).size() == 2) {

            for(int i=0; i<4 ;i++){
                int x = Integer.parseInt( new String((byte[]) ((ArrayList) (((ArrayList) luares.get()).get(0))).get(i)));
                int y = Integer.parseInt(new String((byte[]) ((ArrayList) (((ArrayList) luares.get()).get(1))).get(i)));
                polygonPoints.add(new Point(x,y));
            }
            System.out.println(new String((byte[]) ((ArrayList) (((ArrayList) luares.get()).get(0))).get(0)));
        }

        System.out.println("point success");
        System.out.println("Nuevo punto recibido en el servidor!:" + pt);
        msgt.convertAndSend("/topic/newpoint." + numdibujo, pt);
        System.out.printf("longitud %d %n",polygonPoints.size());
        if (polygonPoints.size() == 4) {
            msgt.convertAndSend("/topic/newpolygon." + numdibujo,polygonPoints);
            System.out.println("Nuevo poligono recibido en el servidor: " + polygonPoints);
        }
        jedis.close();
    }
}

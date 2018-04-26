/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabpaint;

import edu.eci.arsw.collabpaint.model.Point;
import edu.eci.arsw.collabpaint.persistence.PersistenceModel;
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

    @Autowired
    PersistenceModel pm;

    @MessageMapping("/newpoint.{numdibujo}")
    public void handlePointEvent(Point pt, @DestinationVariable String numdibujo) throws Exception {
        pm.addPoint(pt, numdibujo);
        System.out.println("point success");
        System.out.println("Nuevo punto recibido en el servidor!:" + pt);
        msgt.convertAndSend("/topic/newpoint." + numdibujo, pt);
        ArrayList<Point> polygonPoints = pm.getPoligonPoints(numdibujo);
        if (polygonPoints.size() == 4) {
            msgt.convertAndSend("/topic/newpolygon." + numdibujo, polygonPoints);
            System.out.println("Nuevo poligono recibido en el servidor: " + polygonPoints);
        }
    }
}

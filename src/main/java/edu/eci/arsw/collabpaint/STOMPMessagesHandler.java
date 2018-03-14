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

/**
 *
 * @author 2115237
 */
@Controller
public class STOMPMessagesHandler {
    
	@Autowired
	SimpMessagingTemplate msgt;
        ConcurrentMap<String,List> drawPoints =  new ConcurrentHashMap<>();
	@MessageMapping("/newpoint.{numdibujo}")    
	public void handlePointEvent(Point pt,@DestinationVariable String numdibujo) throws Exception {
                if(!drawPoints.keySet().contains(numdibujo)){
                    List<Point> points=Collections.synchronizedList(new ArrayList<Point>());
                    drawPoints.put(numdibujo,points);
                }
                   
		System.out.println("Nuevo punto recibido en el servidor!:"+pt);
		msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
		drawPoints.get(numdibujo).add(pt);
		if(drawPoints.get(numdibujo).size()>=3){
			msgt.convertAndSend("/topic/newpolygon."+numdibujo,drawPoints.get(numdibujo));
                        System.out.println("Nuevo poligono recibido en el servidor: "+drawPoints.get(numdibujo));
		}
	}
}

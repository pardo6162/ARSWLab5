/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabpaint.persistence;

import edu.eci.arsw.collabpaint.model.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

/**
 *
 * @author 2115237
 */
@Service
public class PersistenceModelMemory implements PersistenceModel{
    
    ConcurrentMap<String,List> drawPoints =  new ConcurrentHashMap<>();
    
    @Override
    public void addPoint(Point pt,String numdibujo) {
        if(!drawPoints.keySet().contains(numdibujo)){
                    List<Point> points=Collections.synchronizedList(new ArrayList<Point>());
                    drawPoints.put(numdibujo,points);
        }
        drawPoints.get(numdibujo).add(pt);
    }

    @Override
    public ArrayList<Point> getPoligonPoints(String numdibujo) {
        drawPoints.get(numdibujo).clear();
        return (ArrayList) drawPoints.get(numdibujo);
       
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabpaint.persistence;

import edu.eci.arsw.collabpaint.model.Point;
import java.util.ArrayList;

/**
 *
 * @author 2115237
 */
public interface PersistenceModel {
    void addPoint(Point pt);    
    ArrayList<Point> getPoligonPoints();
}

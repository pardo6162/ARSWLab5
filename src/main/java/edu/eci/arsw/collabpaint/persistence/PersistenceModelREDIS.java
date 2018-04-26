/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabpaint.persistence;

import edu.eci.arsw.collabpaint.model.Point;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import util.JedisUtil;

/**
 *
 * @author 2115237
 */
@Service
public class PersistenceModelREDIS implements PersistenceModel {

    private List<Object> res;
    private Response<Object> luares;

    @Override
    public void addPoint(Point pt) {
        
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
        res = new ArrayList<>();
        jedis.watch("x", "y");
        Transaction t = jedis.multi();
        t.rpush("x", String.valueOf(pt.getX()));
        t.rpush("y", String.valueOf(pt.getY()));
        luares = t.eval(luaScript.getBytes(), 0, "0".getBytes());
        res = t.exec();
    }

    @Override
    public ArrayList<Point> getPoligonPoints() {
        ArrayList<Point> polygonPoints=new ArrayList<>();
        if (((ArrayList) luares.get()).size() == 2) {

            for(int i=0; i<4 ;i++){
                int x = Integer.parseInt( new String((byte[]) ((ArrayList) (((ArrayList) luares.get()).get(0))).get(i)));
                int y = Integer.parseInt(new String((byte[]) ((ArrayList) (((ArrayList) luares.get()).get(1))).get(i)));
                polygonPoints.add(new Point(x,y));
            }
            System.out.println(new String((byte[]) ((ArrayList) (((ArrayList) luares.get()).get(0))).get(0)));
        }
        return polygonPoints;
    }

}

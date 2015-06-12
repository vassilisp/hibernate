package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.util.PerformanceUtil;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

public class Graphs {
    
    public static void Start(){
        
        PerformanceUtil performance = new PerformanceUtil("ms");
        String userId = "tom";
        /*
        AuditLogDao dao = new AuditLogDao();
        
        performance.Tick();
        List<String> userIds = dao.getAllUsers();
        for (String user : userIds){
            System.out.println(user);
        }
        performance.Tock();


        //Create a keyboard scanner and verify userId existance
        Boolean foundFlag = false;
        Scanner keyboard = new Scanner(System.in);
        while(!foundFlag){
            System.out.print("perform database request for userId: ");
            userId = keyboard.next();

            for (String user : userIds){
                if (user.equals(userId)){
                    foundFlag=true;
                    break;
                }
            }
            if (foundFlag==true){
                System.out.println("Performing Search for userId: " + userId);
                break;
            }else{System.out.println("UserId not Found in list of available users");}
        }
        keyboard.close();
        */
        
        AccessLogDao accessLogDao = new AccessLogDao();
        performance.Tick();
        List<AccessLog> acl2 = accessLogDao.getAccessLogs_fromNavajoLog_fromAuditLog(userId);
        performance.Tock("retrieving AccessLogs for a specific userId by first finding the clientIds from the AuditLog and then the"
                + " transactionIds performed by those clientIds from NavajoLog");System.out.println(acl2.size());
        
        
                System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Graph graph = new MultiGraph("tut", false, true);
        
        graph.setAutoCreate(true);
        
        graph.display();
        
        String edge = "";
        String leftNode = "";
        String rightNode = "";
        
        String method="";
        String label = "";
        
        double color = 0;
        Integer order = 0;
        
        for (AccessLog acl: acl2){
            color += 0.01;
            order +=1;
            
            leftNode = acl.getReferer();
            if (leftNode == null || leftNode.isEmpty() || leftNode.equalsIgnoreCase("null")) leftNode = "NULL";

            leftNode = leftNode.replace("https://aww-int.adnovum.ch", "").replace("https://aww.adnovum.ch", "");
           
            rightNode = acl.getRequestedResource().toString().replace(" HTTP/1.1" , "").replace(" ", "");
            
            if (rightNode.startsWith("GET")){
                method = "GET";
            }else if (rightNode.startsWith("POST")){
                method = "POST";
            }

            
            label = order.toString() + " )  " + method + " <<AT>> " + toDate(acl.getRequestDate().longValue());
            
            rightNode = rightNode.replace("POST", "").replace("GET", "");
            
            edge = leftNode + "  <<TO>> " + rightNode;
            
            
            Iterator<Edge> ee = null;
            try{
                Node aa = graph.getNode(leftNode);
                ee = aa.getEachEdge().iterator();
            }catch (Throwable e){
                System.out.println("NODE not found || " + e);
            }

            if (ee != null){
            while (ee.hasNext()){
                Edge edg = ee.next();
                String oldLabel = edg.getAttribute("ui.label");
                
                Node aa1 = edg.getNode1();
                Node aa0 = edg.getNode0();
                
                boolean rr11 = aa1.equals(graph.getNode(leftNode));
                boolean rr10 = aa1.equals(graph.getNode(rightNode));
                
                boolean rr01 = aa0.equals(graph.getNode(rightNode));
                boolean rr00 = aa0.equals(graph.getNode(leftNode));
                
                if ((rr10 && rr00) || (rr11 && rr01)){
                    label = oldLabel + " /n " + label;
                    edg.changeAttribute("ui.label", "");
                }
            }
            }
            
            
            
            graph.addEdge(edge, leftNode, rightNode, true);
            
            String aaaa=graph.getEdge(edge).getAttribute("ui.label");
            graph.getEdge(edge).changeAttribute("ui.label", aaaa + label);
            graph.getEdge(edge).addAttribute("ui.label", label);
            graph.getEdge(edge).addAttribute("ui.color", color);
            graph.getNode(leftNode).addAttribute("ui.label", leftNode);
            graph.getNode(rightNode).addAttribute("ui.label", rightNode);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
          
    }
    private static String toDate(long timestamp){
        Date realDate = new Date();
        realDate.setTime(timestamp);
        return realDate.toString();
    }
    
}

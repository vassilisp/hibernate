package gq.panop.hibernate;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

import edu.uci.ics.jung.visualization.VisualizationViewer;

import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.util.MiscUtil;
import gq.panop.util.PerformanceUtil;


public class JungGraph extends javax.swing.JApplet{

    static int edgeCount = 0;
    
    MyNode targetN;

    MyNode sourceN;
    DirectedSparseMultigraph<String, String> svg;
    
    public JungGraph(){}
    
    public void Start(){
         svg = new DirectedSparseMultigraph<String, String>();
         
        //svg.addVertex((Integer)1);
        //svg.addVertex((Integer)2);
        //svg.addVertex((Integer)3);
        
        // Add some edges. From above we defined these to be of type String
        // Note that the default is for undirected edges.
        
        //svg.addEdge("Edge-A", 1, 2); // Note that Java 1.5 auto-boxes primitives
        //svg.addEdge("Edge-B", 2, 3);
        
        // Let's see what we have. Note the nice output from the
        // SparseMultigraph<V,E> toString() method
        //System.out.println("The graph g = " + svg.toString());

        // The Layout<V, E> is parameterized by the vertex and edge types
        //Layout<String, String> layout = new FRLayout(svg);
         
        //Layout<String, String> layout = new edu.uci.ics.jung.algorithms.layout.KKLayout<String, String>(svg);
         
         Layout<String, String> layout = new edu.uci.ics.jung.algorithms.layout.DAGLayout<String, String>(svg);

        layout.setSize(new Dimension(800,800)); // sets the initial size of the space
        VisualizationViewer<String, String> vv =
                new VisualizationViewer<String, String>(layout);
        vv.setPreferredSize(new Dimension(900,900));
        // Show vertex and edge labels
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        vv.addKeyListener(gm.getModeKeyListener());

        JFrame frame = new JFrame("Interactive Graph View 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);

        
        /*
        svg.addVertex("bill");
        svg.addVertex("pan");
        svg.addVertex("ante");
        svg.addEdge("Edge-D", "bill","pan");
        svg.addEdge("pou", "pan", "ante", EdgeType.DIRECTED);
        //svg.addEdge("Edge-E", 2,3, EdgeType.DIRECTED);
        //svg.addEdge("Edge-F", 3, 2, EdgeType.DIRECTED);
        //svg.addEdge("Edge-P", 2,3); // A parallel edge
        //System.out.println("The graph g2 = " + svg.toString());        
        
        /*
        svg.addVertex((Integer)1);
        svg.addVertex((Integer)2);
        svg.addVertex((Integer)3);
        svg.addEdge("Edge-A", 1,3);
        svg.addEdge("Edge-B", 2,3, EdgeType.DIRECTED);
        svg.addEdge("Edge-C", 3, 2, EdgeType.DIRECTED);
        svg.addEdge("Edge-P", 2,3); // A parallel edge
        System.out.println("The graph g2 = " + svg.toString()); 
        */
        
        PerformanceUtil performance = new PerformanceUtil("ms");
        String userId = "tom";
        

        
        AccessLogDao accessLogDao = new AccessLogDao();
        performance.Tick();
        List<AccessLog> acl2 = accessLogDao.getAccessLogs_fromNavajoLog_fromAuditLog(userId);
        performance.Tock("retrieving AccessLogs for a specific userId by first finding the clientIds from the AuditLog and then the"
                + " transactionIds performed by those clientIds from NavajoLog");System.out.println(acl2.size());       
                
        String edge = "";
        String leftNode = "";
        String rightNode = "";
        
        String method="";
        String label = "";
        
        double color = 0;
        Integer order = 0;
        
        
        Integer startingDay = 0;
        boolean setup = false;
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
            rightNode = rightNode.replace("POST", "").replace("GET", "");
            
            
            label = order.toString() + " )  " + method + " <<AT>> " + MiscUtil.toDate(acl.getRequestDate().longValue());
            
            if (MiscUtil.toDate(acl.getRequestDate().longValue()).getDay()!=startingDay){
                if (setup == false){
                    setup = true;
                    startingDay = MiscUtil.toDate(acl.getRequestDate().longValue()).getDay();
                }else
                    break;
            }
            
            
            edge = order.toString() + ") " + method + "  " + MiscUtil.toDate(acl.getRequestDate().longValue());
            
            sourceN = new MyNode(leftNode);
            targetN = new MyNode(rightNode);
            svg.addVertex(leftNode);
            svg.addVertex(rightNode);
            
            MyEdge link = new MyEdge(1,1);
            
            svg.addEdge(edge, leftNode, rightNode);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            layout.reset();
            vv.repaint();
        }
        System.out.println("The graph g2 = " + svg.toString());
        
    }
    
    class MyNode {
        String id;
        public MyNode(String id) {
            this.id = id;
        }
        /**
         * @return the id
         */
        public String getId() {
            return id;
        }
        public String toString() {
            return "V"+id;
        }        
    }
    
    class MyEdge {
        double capacity;
        double weight;
        int id;
        
        public MyEdge(double weight, double capacity) {
            this.id = edgeCount++;
            this.weight = weight;
            this.capacity = capacity;
        } 

        /**
         * @return the capacity
         */
        public double getCapacity() {
            return capacity;
        }

        /**
         * @param capacity the capacity to set
         */
        public void setCapacity(double capacity) {
            this.capacity = capacity;
        }

        /**
         * @return the weight
         */
        public double getWeight() {
            return weight;
        }

        /**
         * @param weight the weight to set
         */
        public void setWeight(double weight) {
            this.weight = weight;
        }

        public String toString() {
            return "E"+id;
        }
        
    }
   

}
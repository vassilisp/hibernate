package testingGround;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;
import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.util.MiscUtil;
import gq.panop.util.PerformanceUtil;


public class JungGraph extends javax.swing.JApplet{

    static int edgeCount = 0;
    
    private Graph<String,MyEdge> svg = null;
    private AbstractLayout<String,MyEdge> layout = null;
    private VisualizationViewer<String,MyEdge> vv = null;
    
    public JungGraph(){}
    
    //Algo setup parameters
    public Boolean keepParameters = false;
    public Integer numberOfDays = 2;
    public Boolean showLinkLabels = false;
    
    public void Start(){
        init();
        Create();
    }
    
    public void init(){
        
        Graph<String,MyEdge> ig = Graphs.<String,MyEdge>synchronizedDirectedGraph(new DirectedSparseMultigraph<String,MyEdge>());

        ObservableGraph<String,MyEdge> og = new ObservableGraph<String,MyEdge>(ig);
        og.addGraphEventListener(new GraphEventListener<String,MyEdge>() {

            public void handleGraphEvent(GraphEvent<String, MyEdge> evt) {
                System.err.println("got "+evt);
            }});
        this.svg = og;
   
        // The Layout<V, E> is parameterized by the vertex and edge types
        //Layout<String, String> layout = new FRLayout(svg);
         
        //Layout<String, String> layout = new edu.uci.ics.jung.algorithms.layout.KKLayout<String, String>(svg);
         
        //layout = new edu.uci.ics.jung.algorithms.layout.DAGLayout<String, String>(svg);

        layout = new FRLayout<String, MyEdge>(svg);
        //layout = new SpringLayout<String, String>(svg);
        layout.setSize(new Dimension(800,800)); // sets the initial size of the space
        vv = new VisualizationViewer<String, MyEdge>(layout);
        //vv.setPreferredSize(new Dimension(900,900));
        // Show vertex and edge labels
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        vv.addKeyListener(gm.getModeKeyListener());

        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller() {
            @Override
            public String transform(Object v) {
                String result = (String) v;
                try{
                    if(((String)v).indexOf("?")>0){
                        result = ((String)v).substring(0, ((String)v).indexOf("?"));
                    }
                }catch (Throwable e){
                    System.out.println(e);
                }
                return result;
            }});
        
        vv.addComponentListener(new ComponentAdapter() {

            /**
             * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
             */
            @Override
            public void componentResized(ComponentEvent arg0) {
                super.componentResized(arg0);
                System.err.println("resized");
                layout.setSize(arg0.getComponent().getSize());
                vv.setSize(layout.getSize());
                Refresh();
            }});
        
        
        vv.addKeyListener(new KeyListener(){

            @Override
            public void keyPressed(KeyEvent arg0) {
                // TODO Auto-generated method stub
                System.err.println(arg0.getKeyCode() + arg0.getKeyChar() + " pressed" );
                
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub
                System.err.println(arg0.getKeyCode() + arg0.getKeyChar() + " released" );
                
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub
                System.err.println(arg0.getKeyCode() + " /" + String.valueOf(arg0.getKeyChar()) + " Typed" );
                if (String.valueOf(arg0.getKeyChar()).equals("a")){
                    Edger();
                    Refresh();
                }
                
            }
   
        });
        
//-------------
        Transformer<String,Paint> vertexColor = new Transformer<String,Paint>() {
            public Paint transform(String i) {
                if(i == "1") return Color.GREEN;
                return Color.GREEN;
            }
        };
        Transformer<String,Shape> vertexSize = new Transformer<String,Shape>(){
            public Shape transform(String i){
                Ellipse2D circle = new Ellipse2D.Double(-10, -10, 20, 20);
                i="3";
                // in this case, the vertex is twice as large
                if(i == "2") return AffineTransform.getScaleInstance(2, 2).createTransformedShape(circle);
                if(i == "3") return AffineTransform.getScaleInstance(0.5, 0.5).createTransformedShape(circle);
                else return circle;
            }
        };
        vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
        vv.getRenderContext().setVertexShapeTransformer(vertexSize);
//-------------
        
        
        JFrame frame = new JFrame("Interactive Graph View 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);

        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
    }
    
    
    public void Create(){
        
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
            
            String result = MiscUtil.URLRefererCleaner(leftNode);
            
            if (leftNode == null || leftNode.isEmpty() || leftNode.equalsIgnoreCase("null")) leftNode = "NULL";

            leftNode = leftNode.replace("https://aww-int.adnovum.ch", "").replace("https://aww.adnovum.ch", "");
           
            rightNode = acl.getRequestedResource().toString().replace(" HTTP/1.1" , "").replace(" ", "");
            
            if (rightNode.startsWith("GET")){
                method = "GET";
            }else if (rightNode.startsWith("POST")){
                method = "POST";
            }
            rightNode = rightNode.replace("POST", "").replace("GET", "");
            
            result = MiscUtil.URLTargetCleaner(rightNode);
            
            label = order.toString() + " )  " + method + " <<AT>> " + MiscUtil.toDate(acl.getRequestDate().longValue());
            
            if (setup == false){
                setup = true;
                startingDay = MiscUtil.toDate(acl.getRequestDate().longValue()).getDay();
            }
            if (MiscUtil.toDate(acl.getRequestDate().longValue()).getDay()>startingDay+ numberOfDays - 1){
                if (setup==true){
                    break;
                }
            }
            
            if (!(keepParameters)){
                if(leftNode.indexOf("?")>0){
                    leftNode = leftNode.substring(0, leftNode.indexOf("?"));
                }
                if(rightNode.indexOf("?")>0){
                    rightNode = rightNode.substring(0, rightNode.indexOf("?"));
                }
            }
            
            
            if (showLinkLabels){
                edge = order.toString() + ") " + method + "  " + MiscUtil.toDate(acl.getRequestDate().longValue());
            }else{
                edge = order.toString();
            }
            
            svg.addVertex(leftNode);
            svg.addVertex(rightNode);
            
            
            Long max = 0L;
            for (MyEdge inEdge:svg.getInEdges(leftNode)){
                if (inEdge.getTimestamp()>max){
                    max = inEdge.getTimestamp();
                }
            }
            if (max == 0L){
                for (MyEdge outEdge:svg.getOutEdges(leftNode)){
                    if (outEdge.getTimestamp()>max){
                        max = outEdge.getTimestamp();
                    }
                }
            }

            
            svg.addEdge(new MyEdge(edge, acl.getNavajoLog().getTimestamp().longValueExact(), acl.getNavajoLog().getTimestamp().longValueExact()- max), leftNode, rightNode);
            
            try {
                System.out.println("hi");
                //Thread.sleep(700);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            layout.reset();
            vv.repaint();
        }
        System.out.println("The graph g2 = " + svg.toString());
        
        Refresh();
        
    }
    
    public void Refresh(){
        layout.initialize();

        Relaxer relaxer = new VisRunner((IterativeContext)layout);
        relaxer.stop();
        relaxer.prerelax();
        StaticLayout<String, MyEdge> staticLayout =
            new StaticLayout<String, MyEdge>(svg, layout);
        LayoutTransition<String, MyEdge> lt =
            new LayoutTransition<String, MyEdge>(vv, vv.getGraphLayout(),
                    staticLayout);
        Animator animator = new Animator(lt);
        animator.start();
//      vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
        vv.repaint();
    }
    
    
    public void Edger(){
        for (String vert:svg.getVertices()){
            for (String neighbor :svg.getNeighbors(vert)){
                
                //hypothesis to test if findEdgeSet is directional
                //Answer: it is not directional
                /*
                Collection<String> set1 = svg.findEdgeSet(vert, neighbor);
                Collection<String> set2 = svg.findEdgeSet(neighbor, vert);
                if (set1.containsAll(set2) && !(set2.isEmpty())){
                    System.out.println("find edge set is not directional");
                }
                
                if (set1.size()==set2.size()){
                    System.out.println("the are the same size");
                }
                */
                Integer numberOfEdges = svg.findEdgeSet(vert, neighbor).size();
                String removedEdges = "";
                for (MyEdge edge : svg.findEdgeSet(vert, neighbor)){
                    svg.removeEdge(edge);
                    removedEdges += "/" + edge.getId();
                }
                if (numberOfEdges!=0){
                    String label = (removedEdges +"???" + numberOfEdges.toString());
                    svg.addEdge( new MyEdge(label,0L, 0L ), vert, neighbor);
                }
            }
        }
    }
    
    
    public class MyNode {
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
    
    public class MyEdge {
        Long interval;
        Long timestamp;
        String label;
        int id;
        
        public MyEdge(String label, Long timestamp, Long interval) {
            this.id = edgeCount++;
            this.timestamp = timestamp;
            this.interval = interval;
            this.label = label;
        } 

     

        public int getId() {
            return id;
        }



        public void setId(int id) {
            this.id = id;
        }



        public Long getInterval() {
            return interval;
        }



        public void setInterval(Long interval) {
            this.interval = interval;
        }



        public Long getTimestamp() {
            return timestamp;
        }



        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }



        public String toString() {
            return "Edge: " + id + " / " + label + " / " + interval;
        }
        
    }

}

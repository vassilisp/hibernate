package gq.panop.hibernate;

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
import edu.uci.ics.jung.graph.event.GraphEvent.Vertex;
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
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;
import gq.panop.util.PerformanceUtil;


public class JungGraphCreator extends javax.swing.JApplet{

    private Integer edgeCount = 0;
    
    private Boolean keepVertexLabelParameters = true;
    private Graph<MyNode,MyEdge> svg = null;
    private AbstractLayout<MyNode,MyEdge> layout = null;
    private VisualizationViewer<MyNode,MyEdge> vv = null;
    
    public JungGraphCreator(Boolean keepParameters, Boolean showLinkLabels){
        this.keepParameters = keepParameters;
        this.showLinkLabels = showLinkLabels;
        
        init();
        
    }
    
    //Algo setup parameters
    private Boolean keepParameters = false;
    private Boolean showLinkLabels = false;
    
    
    public void init(){
        
        Graph<MyNode,MyEdge> ig = Graphs.<MyNode,MyEdge>synchronizedDirectedGraph(new DirectedSparseMultigraph<MyNode,MyEdge>());

        ObservableGraph<MyNode,MyEdge> og = new ObservableGraph<MyNode,MyEdge>(ig);
        og.addGraphEventListener(new GraphEventListener<MyNode,MyEdge>() {

            public void handleGraphEvent(GraphEvent<MyNode, MyEdge> evt) {
                System.err.println("got "+evt);
            }});
        this.svg = og;
   
        // The Layout<V, E> is parameterized by the vertex and edge types
        //Layout<MyNode, String> layout = new FRLayout(svg);
         
        //Layout<MyNode, String> layout = new edu.uci.ics.jung.algorithms.layout.KKLayout<MyNode, String>(svg);
         
        //layout = new edu.uci.ics.jung.algorithms.layout.DAGLayout<MyNode, String>(svg);

        layout = new FRLayout<MyNode, MyEdge>(svg);
        //layout = new SpringLayout<MyNode, String>(svg);
        layout.setSize(new Dimension(800,800)); // sets the initial size of the space
        vv = new VisualizationViewer<MyNode, MyEdge>(layout);
        //vv.setPreferredSize(new Dimension(900,900));
        // Show vertex and edge labels
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        vv.addKeyListener(gm.getModeKeyListener());

        /*
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller() {
            @Override
            public String transform(Object v) {
                String result = "";
                if (!keepVertexLabelParameters){
                    try{
                        if(((MyNode)v).toString().indexOf("?")>0){
                            result = ((MyNode)v).toString().substring(0, ((MyNode)v).toString().indexOf("?"));
                        }
                    }catch (Throwable e){
                        System.out.println(e);
                        result = "";
                    }
                }else{
                    result = ((MyNode)v).toString();
                }
                return result;
            }});
        */
        
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
                //System.err.println(arg0.getKeyCode() + arg0.getKeyChar() + " pressed" );
                
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub
                //System.err.println(arg0.getKeyCode() + arg0.getKeyChar() + " released" );
                
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
        Transformer<MyNode,Paint> vertexColor = new Transformer<MyNode,Paint>() {
            public Paint transform(MyNode in) {
                
                String i = in.getSessionId();
                if(i.endsWith("1")){
                    return Color.PINK;
                }else if (i.endsWith("2")){
                    return Color.RED;
                }else if (i.endsWith("3")){
                    return Color.BLUE;
                }else if (i.endsWith("4")){
                    return Color.YELLOW;
                }else if (i.endsWith("5")){
                    return Color.CYAN;
                }else if (i.endsWith("6")){
                    return Color.MAGENTA;
                }else if (i.endsWith("7")){
                    return Color.WHITE;
                }else if (i.endsWith("8")){
                    return Color.BLACK;
                }else{
                    return Color.ORANGE;
                }
                //return Color.WHITE;
            }
        };
        
        Transformer<MyNode,Shape> vertexSize = new Transformer<MyNode,Shape>(){
            public Shape transform(MyNode in){
                Integer inE = svg.getInEdges(in).size();
                Integer outE = svg.getOutEdges(in).size();
                Ellipse2D circle = new Ellipse2D.Double(-10, -10, inE*5+5, outE*5+5);
                //Ellipse2D circle = new Ellipse2D.Double(-10, -10, 20, 20);
                String i="4";
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
    
    
    public void AddTransition(Transition transition){

       
        String edge = "";
        String leftNode = "";
        String rightNode = "";
        
        String method="";

        leftNode = transition.getReferer();
        leftNode = MiscUtil.URLRefererCleaner(leftNode);

        rightNode = transition.getTarget();

        if (rightNode.startsWith("GET")){
            method = "GET";
        }else if (rightNode.startsWith("POST")){
            method = "POST";
        }
        rightNode = MiscUtil.URLTargetCleaner(rightNode);

        if (!(keepParameters)){
            if(leftNode.indexOf("?")>0){
                leftNode = leftNode.substring(0, leftNode.indexOf("?"));
            }
            if(rightNode.indexOf("?")>0){
                rightNode = rightNode.substring(0, rightNode.indexOf("?"));
            }
        }


        if (showLinkLabels){
            edge = method + "  " + MiscUtil.toDate(transition.getTimestamp());
        }else{
            edge = transition.getSubSessionId();
        }

        MyNode leftNodeNode = new MyNode(leftNode);
        leftNodeNode.setSessionId(transition.getSubSessionId());
        MyNode rightNodeNode = new MyNode(rightNode);
        rightNodeNode.setSessionId(transition.getSubSessionId());
        if(svg.containsVertex(leftNodeNode) || svg.containsVertex(rightNodeNode)){
            for(MyNode myNode : svg.getVertices()){
                if (myNode.equals(leftNodeNode)){
                    myNode.setSessionId(leftNodeNode.getSessionId());
                }else if(myNode.equals(rightNodeNode)){
                    myNode.setSessionId(rightNodeNode.getSessionId());
                }
            }
        }

        svg.addVertex(leftNodeNode);
        svg.addVertex(rightNodeNode);

        svg.addEdge(new MyEdge(edge), leftNodeNode, rightNodeNode);

        try {
            //Thread.sleep(700);
        } catch (Throwable e) {
            e.printStackTrace();
       }
       //layout.reset();
       //vv.repaint();

    }
    
    
    /*
    public Long findLastTimeOccurance(MyNode node){
        Long max = 0L;
        for (MyEdge inEdge:svg.getInEdges(node)){
            if (inEdge.getTimestamp()>max){
                max = inEdge.getTimestamp();
            }
        }
        if (max == 0L){
            for (MyEdge outEdge:svg.getOutEdges(node)){
                if (outEdge.getTimestamp()>max){
                    max = outEdge.getTimestamp();
                }
            }
        }
    }
    
    */
    public void Refresh(){
        layout.initialize();

        Relaxer relaxer = new VisRunner((IterativeContext)layout);
        relaxer.stop();
        relaxer.prerelax();
        StaticLayout<MyNode, MyEdge> staticLayout =
            new StaticLayout<MyNode, MyEdge>(svg, layout);
        LayoutTransition<MyNode, MyEdge> lt =
            new LayoutTransition<MyNode, MyEdge>(vv, vv.getGraphLayout(),
                    staticLayout);
        Animator animator = new Animator(lt);
        animator.start();
//      vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
        vv.repaint();
    }
    
    
    public void Edger(){
        for (MyNode vert:svg.getVertices()){
            for (MyNode neighbor :svg.getNeighbors(vert)){
                
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
    
    
    class MyNode {
        String id;
        
        private String sessionId;
        public String getSessionId() {
            return sessionId;
        }
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        
 
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
            return id;
        }
        
        
       public int hashCode(){
           return this.id.hashCode(); 
       }
        
       public boolean equals(Object o){
           if (o!=null && o instanceof MyNode){
               return ((MyNode)o).getId().equals(this.id);
           }else{
               return false;
           }
       }
        
        
    }
    
    class MyEdge {
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
    

        public MyEdge(String label) {
            this.id = edgeCount++;
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
            return "E:" + id + " / " + label; //+ " / " + interval;
        }
        
    }

}

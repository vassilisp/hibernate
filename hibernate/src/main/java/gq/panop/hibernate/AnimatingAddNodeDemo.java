package gq.panop.hibernate;


import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;
import gq.panop.hibernate.JungGraph.MyEdge;
import gq.panop.hibernate.JungGraph.MyNode;
import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.util.MiscUtil;
import gq.panop.util.PerformanceUtil;

import org.apache.commons.collections15.functors.ConstantTransformer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;

/**
* A variation of AddNodeDemo that animates transitions between graph states.
*
* @author Tom Nelson
*/
public class AnimatingAddNodeDemo extends javax.swing.JApplet {

   /**
    *
    */
   private static final long serialVersionUID = -5345319851341875800L;
   
   private static Iterator<AccessLog> acl2Iter;
   private static Integer orderId = 0;
   private static Integer startingDay = 0;
   private static Boolean setup = false;

   private AccessLog item = null;
   private Graph<String,String> g = null;

   private VisualizationViewer<String,String> vv = null;

   private AbstractLayout<String,String> layout = null;

   Timer timer;

   boolean done;

   protected JButton switchLayout;

   public static final int EDGE_LENGTH = 100;

   @Override
   public void init() {

       //create a graph
       Graph<String,String> ig = Graphs.<String,String>synchronizedDirectedGraph(new DirectedSparseMultigraph<String,String>());

       ObservableGraph<String,String> og = new ObservableGraph<String,String>(ig);
       og.addGraphEventListener(new GraphEventListener<String,String>() {

           public void handleGraphEvent(GraphEvent<String, String> evt) {
               System.err.println("got "+evt);

           }});
       this.g = og;
       //create a graphdraw
       layout = new FRLayout<String,String>(g);
       layout.setSize(new Dimension(600,600));
       Relaxer relaxer = new VisRunner((IterativeContext)layout);
       relaxer.stop();
       relaxer.prerelax();

       Layout<String,String> staticLayout =
           new StaticLayout<String,String>(g, layout);

       vv = new VisualizationViewer<String,String>(staticLayout, new Dimension(600,600));

       JRootPane rp = this.getRootPane();
       rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

       getContentPane().setLayout(new BorderLayout());
       getContentPane().setBackground(java.awt.Color.lightGray);
       getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));

       //vv.setGraphMouse(new DefaultModalGraphMouse<String,String>());

       DefaultModalGraphMouse<String,String> gm = new DefaultModalGraphMouse<String,String>();
       gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
       vv.setGraphMouse(gm);
       vv.addKeyListener(gm.getModeKeyListener());
       
       
       vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
       //vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
       
       
       
       
       
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
       
       
       vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<String>());
       vv.setForeground(Color.black);

       vv.addComponentListener(new ComponentAdapter() {

           /**
            * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
            */
           @Override
           public void componentResized(ComponentEvent arg0) {
               super.componentResized(arg0);
               System.err.println("resized");
               layout.setSize(arg0.getComponent().getSize());
           }});

       getContentPane().add(vv);
       switchLayout = new JButton("Switch to SpringLayout");
       switchLayout.addActionListener(new ActionListener() {

           @SuppressWarnings("unchecked")
           public void actionPerformed(ActionEvent ae) {
               Dimension d = vv.getSize();//new Dimension(600,600);
               if (switchLayout.getText().indexOf("Spring") > 0) {
                   switchLayout.setText("Switch to FRLayout");
                   layout =
                       new SpringLayout<String,String>(g, new ConstantTransformer(EDGE_LENGTH));
                   layout.setSize(d);
                   Relaxer relaxer = new VisRunner((IterativeContext)layout);
                   relaxer.stop();
                   relaxer.prerelax();
                   StaticLayout<String,String> staticLayout =
                       new StaticLayout<String,String>(g, layout);
                   LayoutTransition<String,String> lt =
                       new LayoutTransition<String,String>(vv, vv.getGraphLayout(),
                               staticLayout);
                   Animator animator = new Animator(lt);
                   animator.start();
               //  vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
                   vv.repaint();

               } else {
                   switchLayout.setText("Switch to SpringLayout");
                   layout = new FRLayout<String,String>(g, d);
                   layout.setSize(d);
                   Relaxer relaxer = new VisRunner((IterativeContext)layout);
                   relaxer.stop();
                   relaxer.prerelax();
                   StaticLayout<String,String> staticLayout =
                       new StaticLayout<String,String>(g, layout);
                   LayoutTransition<String,String> lt =
                       new LayoutTransition<String,String>(vv, vv.getGraphLayout(),
                               staticLayout);
                   Animator animator = new Animator(lt);
                   animator.start();
               //  vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
                   vv.repaint();

               }
           }
       });

       getContentPane().add(switchLayout, BorderLayout.SOUTH);

       timer = new Timer();
   }

   @Override
   public void start() {
       validate();
       //set timer so applet will change
       timer.schedule(new RemindTask(), 1000, 1000); //subsequent rate
       vv.repaint();
   }

   Integer v_prev = null;

   public void process() {

       orderId++;
       String leftNode = null;
       String rightNode = null;
       String method = null;
       
       
       vv.getRenderContext().getPickedVertexState().clear();
       vv.getRenderContext().getPickedEdgeState().clear();
       try {

           
           if (acl2Iter.hasNext()){
               item = acl2Iter.next();
               
               leftNode = item.getReferer();
               if (leftNode == null || leftNode.isEmpty() || leftNode.equalsIgnoreCase("null")) leftNode = "NULL";

               leftNode = leftNode.replace("https://aww-int.adnovum.ch", "").replace("https://aww.adnovum.ch", "");
              
               rightNode = item.getRequestedResource().toString().replace(" HTTP/1.1" , "").replace(" ", "");
               
               if (rightNode.startsWith("GET")){
                   method = "GET";
               }else if (rightNode.startsWith("POST")){
                   method = "POST";
               }
               rightNode = rightNode.replace("POST", "").replace("GET", "");
               
               if (MiscUtil.toDate(item.getRequestDate().longValue()).getDay()!=startingDay){
                   if (setup == false){
                       setup = true;
                       startingDay = MiscUtil.toDate(item.getRequestDate().longValue()).getDay();
                   }else
                       done = true;
               } 
           
               String oi = null;
               oi = orderId.toString();
               
               String date =  MiscUtil.toDate(item.getRequestDate().longValue()).toString();
           
           String edge = oi + ") " + method + "  " + date ;
           
           g.addVertex(leftNode);
           g.addVertex(rightNode);
           
           
           
           
           
           g.addEdge(edge, leftNode, rightNode);
           
           //vv.getRenderContext().getVertexLabelRenderer();
           //vv.getRenderContext().getVertexLabelTransformer();
           vv.getRenderContext().getPickedVertexState().pick(rightNode, true);
           vv.getRenderContext().getPickedVertexState().pick(leftNode, true);
           vv.getRenderContext().getPickedEdgeState().pick(edge, true);
           
           layout.initialize();
           
           
           /*
           if (g.getVertexCount() < 100) {
               //add a vertex
               Integer v1 = new Integer(g.getVertexCount());

               g.addVertex(v1);
               vv.getRenderContext().getPickedVertexState().pick(v1, true);

               // wire it to some edges
               if (v_prev != null) {
                   Integer edge = g.getEdgeCount();
                   vv.getRenderContext().getPickedEdgeState().pick(edge, true);
                   g.addEdge(edge, v_prev, v1);
                   // let's connect to a random vertex, too!
                   int rand = (int) (Math.random() * g.getVertexCount());
                   edge = g.getEdgeCount();
                   vv.getRenderContext().getPickedEdgeState().pick(edge, true);
                  g.addEdge(edge, v1, rand);
               }

               v_prev = v1;

               layout.initialize();
               */

               Relaxer relaxer = new VisRunner((IterativeContext)layout);
               relaxer.stop();
               relaxer.prerelax();
               StaticLayout<String,String> staticLayout =
                   new StaticLayout<String,String>(g, layout);
               LayoutTransition<String,String> lt =
                   new LayoutTransition<String,String>(vv, vv.getGraphLayout(),
                           staticLayout);
               Animator animator = new Animator(lt);
               animator.start();
//             vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
               vv.repaint();

           } else {
               done = true;
           }

       } catch (Exception e) {
           System.out.println(e);

       }

   }

   class RemindTask extends TimerTask {

       @Override
       public void run() {
           process();
           if(done) cancel();

       }
   }

   public static void play() {
       AnimatingAddNodeDemo and = new AnimatingAddNodeDemo();
       JFrame frame = new JFrame();
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.getContentPane().add(and);

       and.init();
       and.start();
       frame.pack();
       frame.setVisible(true);
       
       PerformanceUtil performance = new PerformanceUtil("ms");
       String userId = "tom";
       

       
       AccessLogDao accessLogDao = new AccessLogDao();
       performance.Tick();
       List<AccessLog> acl2 = accessLogDao.getAccessLogs_fromNavajoLog_fromAuditLog(userId);
       performance.Tock("retrieving AccessLogs for a specific userId by first finding the clientIds from the AuditLog and then the"
               + " transactionIds performed by those clientIds from NavajoLog");System.out.println(acl2.size()); 
       
       acl2Iter = acl2.iterator();
   }
}
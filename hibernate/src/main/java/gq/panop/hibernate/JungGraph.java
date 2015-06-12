package gq.panop.hibernate;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
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
import gq.panop.util.PerformanceUtil;


public class JungGraph {

    
    
    public static void Start(){
        Graph<Integer, String> svg = new DirectedSparseMultigraph<Integer, String>();
        
        svg.addVertex((Integer)1);
        svg.addVertex((Integer)2);
        svg.addVertex((Integer)3);
        // Add some edges. From above we defined these to be of type String
        // Note that the default is for undirected edges.
        svg.addEdge("Edge-A", 1, 2); // Note that Java 1.5 auto-boxes primitives
        svg.addEdge("Edge-B", 2, 3);
        // Let's see what we have. Note the nice output from the
        // SparseMultigraph<V,E> toString() method
        System.out.println("The graph g = " + svg.toString());

        // The Layout<V, E> is parameterized by the vertex and edge types
        Layout<Integer, String> layout = new CircleLayout(svg);
        layout.setSize(new Dimension(300,300)); // sets the initial size of the space
        
        VisualizationViewer<Integer,String> vv =
                new VisualizationViewer<Integer,String>(layout);
        vv.setPreferredSize(new Dimension(350,350));
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

        svg.addVertex((Integer)1);
        svg.addVertex((Integer)2);
        svg.addVertex((Integer)3);
        svg.addEdge("Edge-D", 1,3);
        svg.addEdge("Edge-E", 2,3, EdgeType.DIRECTED);
        svg.addEdge("Edge-F", 3, 2, EdgeType.DIRECTED);
        svg.addEdge("Edge-P", 2,3); // A parallel edge
        System.out.println("The graph g2 = " + svg.toString());        
        
        /*
        PerformanceUtil performance = new PerformanceUtil("ms");
        String userId = "tom";
        

        AccessLogDao accessLogDao = new AccessLogDao();
        performance.Tick();
        List<AccessLog> acl2 = accessLogDao.getAccessLogs_fromNavajoLog_fromAuditLog(userId);
        performance.Tock("retrieving AccessLogs for a specific userId by first finding the clientIds from the AuditLog and then the"
                + " transactionIds performed by those clientIds from NavajoLog");System.out.println(acl2.size());

        */
    }
    
}

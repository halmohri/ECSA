/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Graph;

import java.util.List;

/**
 *
 * @author almohri
 */
public class Graph {
    public List arcs;
    public List vertices;
    public int size;
    public Graph(List arcs, List vertices) {
        this.arcs = arcs;
        this.vertices = vertices;
        this.size = vertices.size();
    }
    public int totalFactNodes() {
        if(this.vertices==null)
            return 0;
        int factsize=0;
        for(int i=0; i<this.vertices.size();i++) {
            Vertex v = (Vertex) vertices.get(i);
            if(Vertex.fact==v.type) 
                factsize++;
        }
        return factsize;
    }
}

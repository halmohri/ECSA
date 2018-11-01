/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Success;

import Graph.Arc;
import Graph.Vertex;
import Utilities.FastIO;
import java.util.ArrayList;
import java.util.List;
import Utilities.FileAccess;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author almohri
 */
public class Solver { 
    protected String variablesCached;

    public Solver() {
        this.variablesCached = null;
    }
    
    public Boolean runSolverInterior(String path)
    {
        FileAccess fa = new FileAccess();
        List <String> cmd = new ArrayList<>();
        cmd.add("/usr/local/bin/glpsol");
        cmd.add("--interior");
        cmd.add("-m");
        cmd.add(path);
        cmd.add("-o");
        cmd.add(path+".sol");
        
        if(!fa.processCommand(cmd)) {
            return false;
        }
//        Runtime rt = Runtime.getRuntime();
//        try {
//            Process pr = rt.exec("/usr/local/bin/glpsol --interior -m "+path+" -o"+path+".sol");
//        } catch (IOException ex) {
//            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
//        }

        return true;
    }
    
    public Boolean runSolverSimplex(String path)
    {
        FileAccess fa = new FileAccess();
        List <String> cmd = new ArrayList<>();
        cmd.add("/usr/local/bin/glpsol");
        cmd.add("-m");
        cmd.add(path);
        cmd.add("-o");
        cmd.add(path+".sol");
        
        if(!fa.processCommand(cmd)) {
            return false;
        }
        return true;
    }
    
    public Boolean runSolver(String path, String infile, String outfile)
    {
        FileAccess fa = new FileAccess();
        List <String> cmd = new ArrayList<>();
        cmd.add("/usr/local/bin/glpsol");
        cmd.add("-m");
        cmd.add(path+infile);
        cmd.add("-o");
        cmd.add(path+outfile);
        
        if(!fa.processCommand(cmd)) {
            return false;
        }
        return true;
    }
    
    protected void addVariables(FileAccess fa, BufferedWriter bf, List vertices)
    {
        if(this.variablesCached==null) {
            this.variablesCached= "";
            for(int i=0; i<vertices.size(); i++)
            {
                Vertex v = (Vertex) vertices.get(i);
                if(!v.isFact())
                {
                    this.variablesCached+= "var x" + (i + 1) + "<=1; \n";
                }
            }
        }
        if(!fa.writeToFile(bf, this.variablesCached)) {
            System.out.println("I/O error");
        }
    }
    
    protected void addVariables(String file, List vertices)
    {
        if(this.variablesCached==null) {
            this.variablesCached= "";
            for(int i=0; i<vertices.size(); i++)
            {
                Vertex v = (Vertex) vertices.get(i);
                if(!v.isFact())
                {
                    this.variablesCached+= "var x" + (i + 1) + "<=1; \n";
                }
            }
        }
        FastIO fast = new FastIO();
        fast.write(file, this.variablesCached);
    }
    
    protected void writeObjectiveFunctionSimple
            (String file)
    {
        FastIO fast = new FastIO();
        fast.writeAppend(file, "\nmaximize z: x1;\n");
    }
    
    protected List findNeighbors(int index, List arcs, List vertices)
    {
        List neighbors = new ArrayList();

        for(int i=0; i<arcs.size(); i++)
        {
            Arc a = (Arc) arcs.get(i);
            if(a.v==index)
            {
                Vertex u = (Vertex) vertices.get(a.u-1);
                neighbors.add(u);
            }
        }

        return neighbors;
    }
    
    protected boolean areAllFacts(List neighbors)
    {
        for(int i=0; i<neighbors.size(); i++)
            if(!((Vertex)neighbors.get(i)).isFact())
                return false;
        return true;
    }
    
    protected double [] generateUniformGoalSelectors(int count)
    {
        double [] value = new double[count];
        
        if(count==1) {
            value[0] = 1f;
            return value;
        }
        
        else  {
            double uniformWeight = 1.0f/((double)count);
            for(int i=0; i<count; i++)
                value[i]=uniformWeight;
        }
        return value;
    }
    
}

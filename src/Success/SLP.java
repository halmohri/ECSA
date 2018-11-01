/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Success;
import Graph.*;
import Utilities.FastIO;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Random;

/**
 *
 * @author almohri
 */
public class SLP extends Solver{
    
    private double epsilon;
    private double epsilonChangeFactor;
    private double differenceFactor;
    
    public SLP() {
        /*
         * Moving limit (epsilon) is used to prevent
         * large changes in the solution.
         * The moving limit will be applied
         * to all variables such that
         * |Z_i - (Z_0)_i| <= epsilon
         */
        this.epsilon = 0.2;
        /*
         * We will slowly decrease the
         * epsilon by epsilonChangeFactor
         * in each iteration.
         */
        this.epsilonChangeFactor = 0.95;
        
        this.differenceFactor = 0.0001;
    }
    
    public Graph solve(String path, Graph g)
    {
        /* 
         * Test if the graph has an initial
         * solution. If not abort. SLP
         * cannot start without a warm up.
         */
        if(((Vertex)g.vertices.get(0)).computedProbability <=0) {
           return g; 
        }
        
        /*
         * Y selectors need some value.
         * This method will assign them and 
         * store them in the graph for each goal node.
         */
        this.setYSelectors(g);
        
        double difference = 1f;
        double sol = ((Vertex)g.vertices.get(0)).computedProbability;
        int i;
        
        for(i=0; difference>this.differenceFactor; i++) {
            /*
             * Create a GLPK file that is ready to be solved.
             */

            this.createLinearizedModelFileSLP(path, g);

            /*
             * Solve using GLPK
             */
            this.runSolverSimplex(path+"slp.m");

            /* 
             * Read the solution file.
             */
            SolutionReader reader = new SolutionReader();
            reader.readSolFileUtility(path+"slp.m.sol",g, false);
            
            difference = ((Vertex)g.vertices.get(0)).computedProbability-sol;
            difference = (difference > 0)? difference : -difference;
            sol = ((Vertex)g.vertices.get(0)).computedProbability; 
            
            System.out.println("Current solution: x1="+sol
                    +" with difference: "+difference);
            
            /*
             * In the following I increase the difference factor 
             * for deciding whether the loop must end. This
             * will prevent iterating forever when solutions
             * repeatedly keep in coming up and the difference
             * never reaches the initially assigned factor. 
             * I did this according to the problems I tested.
             * It might not be always true. 
             */
            this.differenceFactor = (this.differenceFactor*0.5)
                    +this.differenceFactor;
            System.out.println("Move limit changed to "+this.differenceFactor);
        }
        
        System.out.println("Iterations: "+i);
        
        return g;
    }
    
    private void createLinearizedModelFileSLP(String path, Graph g)
    {
        String file = path+"slp.m";
        this.addVariables(file, g.vertices);
        this.writeObjectiveFunctionSimple(file);
        this.writeConstraintsNonLinear(file,g);
    }
    
    private void  writeConstraintsNonLinear(String file,Graph g)
    {
        FastIO fast= new FastIO();
        FileChannel channel = fast.openChannel(file);
        for(int i=0; i<g.vertices.size(); i++)
        {
            Vertex n = (Vertex) g.vertices.get(i);
            if(n.isFact()) {
                continue;
            }
            else
            {
                if(n.predecessors!=null)
                {
                    String constraint = this.neighborsSLP(n.predecessors,n,i);
                    fast.writeAppend(channel, constraint);
                }
            }
        }
        
        fast.writeAppend(channel, "\n");
        
    }
    
    private String neighborsSLP(List neighbors,Vertex n,int index)
    {
        TaylorExpansion texp = new TaylorExpansion();
        
        java.text.DecimalFormat df2 = new java.text.DecimalFormat("#.#######");
        String constraint = "";

        /* Goal nodes (\phi(g))*w */
        /* If one neighbor, w=1*/
        if(n.isGoal() && neighbors.size()==1)
        {
            constraint = "s.t. c"+n.node+": x"
                    +n.node+" = x"+((Vertex)neighbors.get(0)).node
                    +";\n";
        }
        /* If more than one neighbor, w=1/neighbors.size() */
        else if(n.isGoal() && neighbors.size()>1)
        {
            constraint ="";
            /* add selector variables */
            for(int i=0; i<neighbors.size()-1; i++)
            {
                constraint += "var "+yVariable(n.node,i)+"<=1;\n";
                constraint += "s.t. c"+yVariable(n.node,i)+": "+yVariable(n.node,i)+">=0;\n";
            }
            
            //Cannot have all selectors zero.
//            constraint +="s.t. cys"+n.node+": ";
//            for(int i=0; i<neighbors.size()-1; i++)
//            {
//                if(i==neighbors.size()-2)
//                    constraint += yVariable(n.node,i)+">=0.001";
//                else constraint +=  yVariable(n.node, i) + "+";
//            }
//            constraint += ";\n"; 

            constraint += "s.t. c"+n.node+": x"+n.node+" = "
            + texp.taylorExpansion(n);
            constraint +=";\n";

            /* Find the max point from dependencies */
            double max = ((Vertex)neighbors.get(0)).computedProbability;
            for(int c=0;c<neighbors.size(); c++){
                if(max < ((Vertex)neighbors.get(c)).computedProbability) {
                    max = ((Vertex)neighbors.get(c)).computedProbability;
                }
            }

            /* write a max constraint for the goal node. */
            //constraint += "s.t. ll"+n.node+": x"+n.node+"<="+max+";\n";
            //constraint += "s.t. ll"+n.node+": x"+n.node+"<=1;\n";
        }

        /* Rule nodes  */
        else if(n.isRule())
        {
            /* If rule nodes have all fact nodes in \phi(r)*/
            if(this.areAllFacts(neighbors)){
                //calculate the scalar
                double a = 1.0;
                for(int i=0; i<neighbors.size(); i++)
                {
                    a *= ((Vertex)neighbors.get(i)).initialProbability;
                }

                constraint = "s.t. c"+n.node+": x"+n.node
                        +"="+df2.format(a)+";\n";
            }
            /* If rule node has a fact and a goal */
            else
            {
                double a = 1.0;
                String nonfact = "x";
                for(int i=0; i<neighbors.size(); i++) {
                    if(((Vertex)neighbors.get(i)).isFact())
                    {
                        a *= ((Vertex)neighbors.get(i)).initialProbability;
                    }
                    else
                    {
                        nonfact += ((Vertex)neighbors.get(i)).node;
                    }
                }
                constraint = "s.t. c"+n.node+": x"+n.node
                        +"="+df2.format(a)+"*"+nonfact+";\n";
            }
        }
        constraint += "s.t. l"+n.node+": x"+n.node+">="//0;\n";
                       +"0.0001;\n";
        return constraint;
    }
    
    /* 
    * Prepare the vertex for SLP.
    * If it is a goal node, it will need
    * some random weights for its Y
    * selectors. These are carefully chosen
    * in the called method.
    */
    public void setYSelectors(Graph g)
    {
        for(int i=0; i<g.vertices.size(); i++) {
            Vertex v = (Vertex) g.vertices.get(i);
            if(v.isRule()){
                v.predecessors = this.findNeighbors(v.node, g.arcs, g.vertices);
            }
            if(v.isGoal()) {
                v.predecessors = this.findNeighbors(v.node, g.arcs, g.vertices);
                v.randWeights = 
                        this.ySelectorWeights(v.predecessors.size()-1);
                g.vertices.set(i, v);
            }
            else {
                continue;
            }
        }
    }
   
    
    private double [] ySelectorWeights(int numberOfSelectors)
    {
        double weightsSum = 0f;
        double [] weights = new double[numberOfSelectors];
        Random generator = new Random(); 
        double rangeMin = 5f;
        double rangeMax = 10f;
        for(int i=0; i<numberOfSelectors; i++) {
            weights[i] = rangeMin + (rangeMax - rangeMin) * generator.nextDouble();
            weightsSum += weights[i];
        }
        for(int i=0; i<numberOfSelectors; i++) { 
            weights[i] = weights[i]/weightsSum;
        }
        
        return weights;
    }
    
    public String yVariable(int xi, int yi)
    {
        return "y"+yi+"x"+xi;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Graph;

import java.util.List;

/**
 *
 * @author Hussain Almohri, Kansas State University
 */


public class Vertex implements Comparable
{
    public int     node;
    public int     xindex;  //For y variables only.
    public int     ninode;
    public int     constraintCount; //used for negative impact rules only
    public String  body;
    public String  bodyPlain;
    public int     type;
    public double  rank;
    public double  computedProbability;
    public double  mulvaln;
    public boolean attackNode;
    public double  prior;
    public double  initialProbability;
    public int     steps; //only applicable to rule nodes
    public double [] randWeights;
    public String constraint;
    public List     predecessors;

    public static final int rule = 0;
    public static final int goal = 1;
    public static final int fact = 2;
    public static final int nfact = 3;
    public static final int optnode = 4;
    public static final int ynode = 5;

    public boolean isNegativeFact()
    {
        return (this.type==this.nfact);
    }

    public boolean isYNode()
    {
        return (this.type==this.ynode);
    }

    public boolean isOption()
    {
        return (this.type==this.optnode);
    }

    public boolean isFact()
    {
        return (this.type==this.fact);
    }

    public boolean isGoal()
    {
        return (this.type==this.goal);
    }

    public boolean isRule()
    {
        return (this.type==this.rule);
    }

    public boolean isNegativeImpactRule()
    {
        if(!this.isRule())
            return false;
        return (this.bodyPlain.contains("Negative"));
    }

    public boolean isNetAccess()
    {
        if(!isGoal())
            return false;
        return (bodyPlain.contains("netAccess"));
    }

    public boolean isAvailability()
    {
        if(!isFact())
            return false;
        return (bodyPlain.contains("deviceOnline"));
    }

    public boolean isStaticAvailability()
    {
        if(!isFact())
            return false;
        return (bodyPlain.contains("deviceOnline") && !bodyPlain.contains("mobile"));
    }

    public boolean isMalInteraction()
    {
        if(!isFact())
            return false;
        return (bodyPlain.contains("maliciousInteraction"));
    }
        
    public boolean isExploitRule()
    {
        if(!isRule())
            return false;
        return (bodyPlain.contains("exploit"));
    }

    public boolean isAttacker()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("attackerInRegion"))
            return true;
        return false;
    }

    public boolean isSoftware()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("softwarePerm"))
            return true;
        return false;
    }

    public boolean isIPTable()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("iptables"))
            return true;
        return false;
    }

    public boolean containsStandardPort()
    {
        return (bodyPlain.contains("22") ||
           bodyPlain.contains("3306") ||
           bodyPlain.contains("80") ||
           bodyPlain.contains("443") ||
           bodyPlain.contains("8080") ||
           bodyPlain.contains("8403"));
    }

    public boolean isIPTableNonStandard()
    {
        if(!this.isFact())
            return false;
        if(this.isIPTable() && !this.containsStandardPort())
            return true;
        return false;
    }

    public boolean isMobileAvailability()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("mobile") && bodyPlain.contains("deviceOnline"))
            return true;
        return false;
    }
    
    public boolean isVulnerability()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("vulExists"))
            return true;
        return false;
    }

    public boolean isNetworkService()
    {
        if(!this.isFact()) {
            return false;
        }
        if(bodyPlain.contains("networkService")) {
            return true;
        }
        return false;
    }
    
    public boolean isNetworkServiceNonStandard()
    {
        if(!this.isFact()) {
            return false;
        }
        if(bodyPlain.contains("networkService") 
                && !this.containsStandardPort()) {
            return true;
        }
        return false;
    }    

    public boolean isHostAccess()
    {
        if(!this.isFact()) {
            return false;
        }
        if(bodyPlain.contains("hacl")) {
            return true;
        }
        return false;
    }

    public boolean isWirelessNetwork()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("wireless") && this.isAvailability())
            return true;
        return false;
    }

    public boolean isWirelessHACL()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("wireless") && this.isHostAccess())
            return true;
        return false;
    }

    public boolean isWireless()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("wireless"))
            return true;
        return false;
    }

    public boolean isVulnerablePlatform()
    {
        if(!this.isFact())
            return false;
        if(bodyPlain.contains("android") ||
           bodyPlain.contains("win"))
            return true;
        return false;
    }

    public String accessFrom()
    {
        String from = "";
        if(!this.isHostAccess())
            return "";

        int bracket = bodyPlain.indexOf('(');
        int comma   = bodyPlain.indexOf(',');
        from = bodyPlain.substring(bracket+1, comma);

        return from;
    }

    public String accessTo()
    {
        //String to = "";
        if(!this.isHostAccess())
            return "";

        int start = bodyPlain.indexOf(',')+1;
        String subs = bodyPlain.substring(start);
        int comma2  = subs.indexOf(",");

        subs = subs.substring(0,comma2);
        
        return subs;
    }

    public int compareTo(Object o1) {
        if (this.rank == ((Vertex) o1).rank)
            return 0;
        else if ((this.rank) < ((Vertex) o1).rank)
            return 1;
        else
            return -1;
    }
}

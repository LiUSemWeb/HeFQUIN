package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

import java.util.List;

public class PhysicalPlanWithStatistics {
    public final PhysicalPlan plan;
    protected final int cardinality;
    protected final int numOfAccess;
    protected final List<FederationMember> fms;
    protected final List<VocabularyMapping> vms;

    public PhysicalPlanWithStatistics( final PhysicalPlan plan, final List<FederationMember> fms, final List<VocabularyMapping> vms, final int cardinality, final int numOfAccess ){
        this.plan = plan;
        this.fms = fms;
        this.vms = vms;
        this.cardinality = cardinality;
        this.numOfAccess = numOfAccess;
    }

    public int getCardinality( ){
        return cardinality;
    }

    public int getNumOfAccess( ){
        return numOfAccess;
    }

    public List<FederationMember> getFederationMembers( ){
        return fms;
    }

    public List<VocabularyMapping> getVocabularyMappings( ){
         return vms;
    }
}
package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

/**
 * This class provides methods to convert logical operators into
 * physical operators by using the respective default type of
 * physical operator for each type of logical operator.
 */
public class LogicalToPhysicalOpConverter
{
	public static boolean handleVocabMappingsExplicitly = true;

	public static PhysicalOperator convert( final LogicalOperator lop ) {
		if (      lop instanceof NullaryLogicalOp ) return convert( (NullaryLogicalOp) lop );
		else if ( lop instanceof UnaryLogicalOp )   return convert( (UnaryLogicalOp) lop );
		else if ( lop instanceof BinaryLogicalOp )  return convert( (BinaryLogicalOp) lop );
		else if ( lop instanceof NaryLogicalOp )    return convert( (NaryLogicalOp) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	// --------- nullary operators -----------

	public static NullaryPhysicalOp convert( final NullaryLogicalOp lop ) {
		if ( lop instanceof LogicalOpRequest ) return convert( (LogicalOpRequest<?,?>) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static NullaryPhysicalOp convert( final LogicalOpRequest<?,?> lop ) {
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			return new PhysicalOpRequestWithTranslation<>(lop);
		else
			return new PhysicalOpRequest<>(lop);
	}

	// --------- unary operators -----------

	public static UnaryPhysicalOp convert( final UnaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpTPAdd )     return convert( (LogicalOpTPAdd) lop );
		else if ( lop instanceof LogicalOpTPOptAdd )  return convert( (LogicalOpTPOptAdd) lop );
		else if ( lop instanceof LogicalOpBGPAdd )    return convert( (LogicalOpBGPAdd) lop );
		else if ( lop instanceof LogicalOpBGPOptAdd ) return convert( (LogicalOpBGPOptAdd) lop );
		else if ( lop instanceof LogicalOpFilter )    return convert( (LogicalOpFilter) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpTPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		// first, consider the possibility that vocabulary mappings should be
		// handled implicitly (i.e., within the physical operators), which is
		// not the default behavior of the engine

		if ( ! handleVocabMappingsExplicitly && fm.getVocabularyMapping() != null ) {
			if ( fm instanceof SPARQLEndpoint )  return new PhysicalOpBindJoinWithFILTERandTranslation(lop);
			else throw new UnsupportedOperationException("No suitable operator for the given type of federation member: " + fm.getClass().getName() + ".");
		}

		// now, consider the default behavior in which vocabulary mappings
		// are handled explicitly during query planning

		if (      fm instanceof SPARQLEndpoint ) return new PhysicalOpBindJoinWithFILTER(lop);

		else if ( fm instanceof TPFServer )      return new PhysicalOpIndexNestedLoopsJoin(lop);

		else if ( fm instanceof BRTPFServer )    return new PhysicalOpBindJoin(lop);

		else throw new UnsupportedOperationException("Unsupported type of federation member: " + fm.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpTPOptAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		// first, consider the possibility that vocabulary mappings should be
		// handled implicitly (i.e., within the physical operators), which is
		// not the default behavior of the engine

		if ( ! handleVocabMappingsExplicitly && fm.getVocabularyMapping() != null ) {
			throw new UnsupportedOperationException("No suitable operator for the given type of federation member: " + fm.getClass().getName() + ".");
		}

		// now, consider the default behavior in which vocabulary mappings
		// are handled explicitly during query planning

		if (      fm instanceof SPARQLEndpoint ) return new PhysicalOpBindJoinWithFILTER(lop);

		else if ( fm instanceof TPFServer )      return new PhysicalOpIndexNestedLoopsJoin(lop);

		else if ( fm instanceof BRTPFServer )    return new PhysicalOpBindJoin(lop);

		else throw new UnsupportedOperationException("Unsupported type of federation member: " + fm.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpBGPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		// first, consider the possibility that vocabulary mappings should be
		// handled implicitly (i.e., within the physical operators), which is
		// not the default behavior of the engine

		if ( ! handleVocabMappingsExplicitly && fm.getVocabularyMapping() != null ) {
			if ( fm instanceof SPARQLEndpoint )  return new PhysicalOpBindJoinWithFILTERandTranslation(lop);
			else throw new UnsupportedOperationException("No suitable operator for the given type of federation member: " + fm.getClass().getName() + ".");
		}

		// now, consider the default behavior in which vocabulary mappings
		// are handled explicitly during query planning

		if (      fm instanceof SPARQLEndpoint ) return new PhysicalOpBindJoinWithFILTER(lop);

		else if ( fm instanceof TPFServer )      throw new IllegalArgumentException();

		else if ( fm instanceof BRTPFServer )    throw new IllegalArgumentException();

		else throw new UnsupportedOperationException("Unsupported type of federation member: " + fm.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpBGPOptAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		// first, consider the possibility that vocabulary mappings should be
		// handled implicitly (i.e., within the physical operators), which is
		// not the default behavior of the engine

		if ( ! handleVocabMappingsExplicitly && fm.getVocabularyMapping() != null ) {
			throw new UnsupportedOperationException("No suitable operator for the given type of federation member: " + fm.getClass().getName() + ".");
		}

		// now, consider the default behavior in which vocabulary mappings
		// are handled explicitly during query planning

		if (      fm instanceof SPARQLEndpoint ) return new PhysicalOpBindJoinWithFILTER(lop);

		else if ( fm instanceof TPFServer )      throw new IllegalArgumentException();

		else if ( fm instanceof BRTPFServer )    throw new IllegalArgumentException();

		else throw new UnsupportedOperationException("Unsupported type of federation member: " + fm.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpFilter lop ) {
		return new PhysicalOpFilter(lop);
	}

	// --------- binary operators -----------

	public static BinaryPhysicalOp convert( final BinaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpJoin )     return convert( (LogicalOpJoin) lop );
		else if ( lop instanceof LogicalOpUnion )    return convert( (LogicalOpUnion) lop );
		else if ( lop instanceof LogicalOpRightJoin ) return convert( (LogicalOpRightJoin) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static BinaryPhysicalOp convert( final LogicalOpJoin lop ) {
		return new PhysicalOpSymmetricHashJoin(lop);
	}

	public static BinaryPhysicalOp convert( final LogicalOpUnion lop ) {
		return new PhysicalOpBinaryUnion(lop);
	}

	public static BinaryPhysicalOp convert( final LogicalOpRightJoin lop ) {
		return new PhysicalOpHashRJoin(lop);
	}

	// --------- n-ary operators -----------

	public static NaryPhysicalOp convert( final NaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpMultiwayJoin )  return convert( (LogicalOpMultiwayJoin) lop );
		else if ( lop instanceof LogicalOpMultiwayLeftJoin ) return convert( (LogicalOpMultiwayLeftJoin) lop );
		else if ( lop instanceof LogicalOpMultiwayUnion ) return convert( (LogicalOpMultiwayUnion) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static NaryPhysicalOp convert( final LogicalOpMultiwayJoin lop ) {
		throw new UnsupportedOperationException();
	}

	public static NaryPhysicalOp convert( final LogicalOpMultiwayLeftJoin lop ) {
		throw new UnsupportedOperationException();
	}

	public static NaryPhysicalOp convert( final LogicalOpMultiwayUnion lop ) {
		throw new UnsupportedOperationException();
	}

}

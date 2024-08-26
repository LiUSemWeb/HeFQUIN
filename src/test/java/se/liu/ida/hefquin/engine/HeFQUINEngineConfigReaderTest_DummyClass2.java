package se.liu.ida.hefquin.engine;

import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class HeFQUINEngineConfigReaderTest_DummyClass2 implements HeFQUINEngineConfigReaderTest_DummyInterface
{
	public final int i;
	public final HeFQUINEngineConfigReaderTest_DummyInterface sub;
	public final QueryProcContext ctx;

	public HeFQUINEngineConfigReaderTest_DummyClass2() {
		i = 42;
		sub = null;
		ctx = null;
	};

	public HeFQUINEngineConfigReaderTest_DummyClass2( final int i,
	                                                  final HeFQUINEngineConfigReaderTest_DummyInterface sub ) {
		this.i = i;
		this.sub = sub;
		ctx = null;
	};

	public HeFQUINEngineConfigReaderTest_DummyClass2( final QueryProcContext ctx ) {
		i = 4321;
		sub = null;
		this.ctx = ctx;
	};

	@Override
	public int getInt() { return i; }
}

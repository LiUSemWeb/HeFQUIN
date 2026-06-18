package se.liu.ida.hefquin.engine;

import java.util.concurrent.ExecutorService;

public class HeFQUINEngineConfigReaderTest_DummyClass2 implements HeFQUINEngineConfigReaderTest_DummyInterface
{
	public final int i;
	public final HeFQUINEngineConfigReaderTest_DummyInterface sub;
	public final ExecutorService execService;

	public HeFQUINEngineConfigReaderTest_DummyClass2() {
		i = 42;
		sub = null;
		execService = null;
	};

	public HeFQUINEngineConfigReaderTest_DummyClass2( final int i,
	                                                  final HeFQUINEngineConfigReaderTest_DummyInterface sub ) {
		this.i = i;
		this.sub = sub;
		execService = null;
	};

	public HeFQUINEngineConfigReaderTest_DummyClass2( final ExecutorService execService ) {
		i = 4321;
		sub = null;
		this.execService = execService;
	};

	@Override
	public int getInt() { return i; }
}

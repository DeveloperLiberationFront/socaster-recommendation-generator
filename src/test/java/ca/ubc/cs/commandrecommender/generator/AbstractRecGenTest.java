package ca.ubc.cs.commandrecommender.generator;

import ca.ubc.cs.commandrecommender.model.ToolUse;
import ca.ubc.cs.commandrecommender.model.ToolUseCollection;
import org.junit.Before;

import java.sql.Timestamp;

public abstract class AbstractRecGenTest {

	protected AbstractRecGen rec;

    @Before
	public void setUp() throws Exception {
		rec = getRec();
	}

	public ToolUseCollection person(int... ns) {
		return person(false, false, ns);
	}

    public ToolUseCollection person(boolean largeInterval, boolean hotkey, boolean alternate, int... ns) {
        ToolUseCollection person = new ToolUseCollection(ns[0]);
        for(int i = 1; i<ns.length; i++){
            boolean shortcut = alternate ? ((i % 2) == 0) : hotkey;
            if (largeInterval) {
                for (int j = 0; j < 10 ; j ++) {
                    person.add(new ToolUse(new Timestamp(i * 1000000000L + j * 100000000L), ns[i], shortcut));
                }
            } else {
                person.add(new ToolUse(now(), ns[i], shortcut));
            }
        }
        return person;
    }

    public ToolUseCollection person(boolean hotkey, boolean alternate, int... ns) {
        return person(false, hotkey, alternate, ns);
    }

    public ToolUseCollection person(boolean largeInterval, int... ns) {
        return person(largeInterval, false, false, ns);
    }

	protected ToolUseCollection addDummyPerson() {
		ToolUseCollection p = new ToolUseCollection(10000);
		p.add(new ToolUse(now(), 10000, true));
		return p;
	}

	protected Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	protected abstract AbstractRecGen getRec();

}
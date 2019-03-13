package co.lotc.core.command;

public class VoidArg extends CmdArg<Void> { //If this actually makes it through debugging please punch me in the face
	
	public VoidArg(String name, String errorMessage, String description) {
		super(name, errorMessage, "PLACEHOLDER_IF_YOU_SEE_THIS_SOMETHING_WENT_WRONG", description);
		this.setMapper(s->null);
	}

}

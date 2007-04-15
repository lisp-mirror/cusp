package jasko.tim.lisp.editors.actions;

import jasko.tim.lisp.editors.LispEditor;
import jasko.tim.lisp.swank.*;

import java.util.*;

import org.eclipse.jface.dialogs.Dialog;

public class EditDefinitionAction extends LispAction {
	
	public EditDefinitionAction() {
	}
	
	public EditDefinitionAction(LispEditor editor) {
		super(editor);
	}

	
	public void run() {
		String symbol = getSymbol();
		
		if (!symbol.equals("")) {
			getSwank().sendFindDefinitions(symbol, editor.getPackage(), new OpenDefinitionRunnable(symbol));
		}
		
	}
	
	private class OpenDefinitionRunnable extends SwankRunnable {
		private String symbol;
		
		public OpenDefinitionRunnable(String symbol) {
			this.symbol = symbol;
		}
		public void run() {
			LispNode guts = result.getf(":return").getf(":ok");
			ArrayList<String> names = new ArrayList<String>(guts.params.size());
			ArrayList<LispNode> data = new ArrayList<LispNode>(guts.params.size());
			ArrayList<String> tips = new ArrayList<String>(guts.params.size());
			for (LispNode possibility: guts.params) {
				String name = possibility.get(0).value;
				names.add(name);
				data.add(possibility);
				if (possibility.get(1).get(0).value.equals(":error")) {
					tips.add(possibility.get(1).get(1).value);
				} else {
					tips.add(possibility.get(1).getf(":file").value);
				}
			}
			
			LispNode chosen;
			
			if (names.size() <= 0) {
				editor.showPopupInfo("Unable to find definitions");
				return;
			} else if (names.size() == 1) {
				chosen = data.get(0);
			} else {
				ListDialog<LispNode> win = new ListDialog<LispNode>(editor.getSite().getShell(),
						names, data, tips);
				win.create();
				win.setTitle("Definitions");
				
				if (names.get(0).startsWith("(DEFGENERIC")) {
					win.setSelection(1);
				} else {
					win.setSelection(0);
				}
				
				if (win.open() == Dialog.OK) {
					chosen = win.getData();
				} else {
					return;
				}
			}
			
			LispNode location = chosen.get(1);
			System.out.println(location);
			if (location.get(0).value.equals(":error")) {
				editor.showPopupInfo(location.getf(":error").value);
				return;
			}
			String path = location.getf(":file").value;
			if (path.equals("")) {
				path = location.getf(":buffer").value;
			}
			int position = location.getf(":position").asInt();
			String snippet = location.getf(":snippet").value;
			
			LispEditor.jumpToDefinition(path, position, snippet, symbol);
			
		}
	}
	


}

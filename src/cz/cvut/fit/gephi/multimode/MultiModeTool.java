package cz.cvut.fit.gephi.multimode;

import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Kuchar
 */
@ServiceProvider(service = Tool.class)
public class MultiModeTool implements Tool {

    private final MultiModeUI ui = new MultiModeUI();

    @Override
    public void select() {
    }

    @Override
    public void unselect() {
    }

    @Override
    public ToolEventListener[] getListeners() {
        // no tool event lisener
        return new ToolEventListener[0];
    }

    @Override
    public ToolUI getUI() {
        return ui;
    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.NONE;
    }

    
}

package net.wakamesoba98.knitcap.window;


import net.wakamesoba98.knitcap.config.Config;
import net.wakamesoba98.knitcap.view.Controller;
import org.newdawn.slick.*;

public class MainWindow extends BasicGame {

    public static final int TOOLBAR_HEIGHT = 36;
    private Controller controller;
    private String device;

    MainWindow(String title) {
        super(title);
    }

    void start(Config config) {
        this.device = config.getDevice();
        int w = Integer.parseInt(config.getScreenSize().split("x")[0]);
        int h = Integer.parseInt(config.getScreenSize().split("x")[1]);
        try {
            AppGameContainer app = new AppGameContainer(this);
            app.setDisplayMode(w, h, false);
            app.setVSync(true);
            app.setShowFPS(true);
            app.setAlwaysRender(true);
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(GameContainer container, Graphics graphics) throws SlickException {
        graphics.setColor(Color.lightGray);
        graphics.fillRect(0, 0, container.getWidth(), TOOLBAR_HEIGHT);
        graphics.setBackground(Color.white);
        graphics.setColor(Color.black);
        controller.draw(graphics);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        controller = new Controller(container.getWidth(), container.getHeight());
        controller.start(device);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {

    }

    @Override
    public boolean closeRequested() {
        controller.destroy();
        return super.closeRequested();
    }
}

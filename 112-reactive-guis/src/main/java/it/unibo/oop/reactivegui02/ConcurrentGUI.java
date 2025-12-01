package it.unibo.oop.reactivegui02;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

/**
 * Second example of reactive GUI.
 */
public final class ConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentGUI.class);
    private final JLabel display = new JLabel();

    /**
     * Setup and start GUI.
     */
    public ConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        final JButton upButton = new JButton("Up");
        panel.add(upButton);
        final JButton downButton = new JButton("Down");
        panel.add(downButton);
        final JButton stopButton = new JButton("Stop");
        panel.add(stopButton);
        this.getContentPane().add(panel);
        this.setLocationByPlatform(true);
        this.setVisible(true);
        /*
         * Counter Agent
         */
        final Agent agent = new Agent();
        new Thread(agent).start();
        /*
         * Button listeners 
         */
        upButton.addActionListener(e -> agent.setIncreasing(true));
        downButton.addActionListener(e -> agent.setIncreasing(false));
        stopButton.addActionListener(e -> {
            agent.stopCounting();
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            stopButton.setEnabled(false);
        });
    }

    private final class Agent implements Runnable {
        private volatile boolean stop;
        private volatile boolean increasing = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
                    this.counter += increasing ? 1 : -1;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        public void stopCounting() {
            this.stop = true;
        }

        public void setIncreasing(final boolean value) {
            this.increasing = value;
        }
    }
}

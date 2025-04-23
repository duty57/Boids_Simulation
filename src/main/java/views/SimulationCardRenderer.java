package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

import models.SimulationCard;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class SimulationCardRenderer extends JPanel implements ListCellRenderer<SimulationCard> {
    private final JLabel titleLabel;
    private final JLabel visionLabel;
    private final JLabel temperatureLabel;
    private final JLabel maxSpeedLabel;

    public SimulationCardRenderer() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        setPreferredSize(new Dimension(155, getPreferredSize().height));
        setMaximumSize(new Dimension(155, getPreferredSize().height));
        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        visionLabel = new JLabel();
        visionLabel.setFont(visionLabel.getFont().deriveFont(Font.BOLD));
        temperatureLabel = new JLabel();
        temperatureLabel.setFont(temperatureLabel.getFont().deriveFont(Font.BOLD));
        maxSpeedLabel = new JLabel();
        maxSpeedLabel.setFont(maxSpeedLabel.getFont().deriveFont(Font.BOLD));
        textPanel.add(titleLabel);
        textPanel.add(visionLabel);
        textPanel.add(temperatureLabel);
        textPanel.add(maxSpeedLabel);

        add(textPanel, BorderLayout.CENTER);
        setBackground(Color.WHITE);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SimulationCard> list,
                                                  SimulationCard card, int index, boolean isSelected, boolean cellHasFocus) {
        titleLabel.setText(StringUtils.defaultString(card.getTitle(), "Untitled"));
        visionLabel.setText(String.format("Vision: %s m",
                ObjectUtils.defaultIfNull(card.getVision(), 0.0f)));
        temperatureLabel.setText(String.format("Temperature: %s°C",
                ObjectUtils.defaultIfNull(card.getTemperature(), 0.0f)));
        maxSpeedLabel.setText(String.format("Speed: %s m/s",
                ObjectUtils.defaultIfNull(card.getMaxSpeed(), 0.0f)));

        if (isSelected) {
            setBackground(ObjectUtils.defaultIfNull(list.getSelectionBackground(), Color.LIGHT_GRAY));
            setForeground(ObjectUtils.defaultIfNull(list.getSelectionForeground(), Color.BLACK));
        } else {
            setBackground(ObjectUtils.defaultIfNull(list.getBackground(), Color.WHITE));
            setForeground(ObjectUtils.defaultIfNull(list.getForeground(), Color.BLACK));
        }

        return this;
    }

    public static SimulationCard onMouseClicked(MouseEvent e, DefaultListModel<SimulationCard> historyListModel, JList<SimulationCard> historyList, SimulationCard selectedCard) {
        if (ObjectUtils.anyNull(e, historyListModel, historyList)) {
            return selectedCard;
        }

        int index = historyList.locationToIndex(e.getPoint());
        if (index < 0) return selectedCard;

        selectedCard = historyListModel.getElementAt(index);
        System.out.println("Selected card: " + selectedCard);
        historyList.repaint();
        return selectedCard;
    }

}

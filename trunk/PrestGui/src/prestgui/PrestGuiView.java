/*
 * PrestGuiView.java
 */
package prestgui;

import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DebugGraphics;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;

import com.sd.dev.lib.ISDContext;

import parser.enumeration.Language;
import wizards.dynamiccategorizer.DynamicCategorizerWizard;
import categorizer.core.Categorizer;
import categorizer.core.CategorizerExecutor;
import categorizer.core.DataHeader;
import categorizer.core.DataSet;
import categorizer.core.MetricOperator;
import categorizer.core.Operand;
import categorizer.core.Option;
import categorizer.core.PerformanceMetric;
import categorizer.core.Threshold;
import categorizer.core.ThresholdOperator;
import categorizer.core.VirtualMetric;
import categorizer.core.filter.LogFilter;
import categorizer.core.util.Arff2DataSet;
import categorizer.staticCategorizer.StaticCategorizer;

import common.DataContext;
import common.ExtensionFileFilter;
import common.MetricGroup;
import common.gui.actions.ThresholdTableMouseListener;
import common.gui.models.ThresholdContent;
import common.gui.models.ThresholdTableModel;
import common.gui.packageexplorer.PackageExplorer;
import common.gui.statics.Components;
import common.gui.util.ComponentState;

import predictor.WekaRunner;

import definitions.application.ApplicationProperties;
import executor.ParserExecutor;

/**
 * The application's main frame.
 */

// SciDesktop Modification TA_R001	--- class implements WindowListener
public class PrestGuiView extends FrameView implements WindowListener {
	
	// SciDesktop Modification TA_R001	--- additional variables are needed for the created instance
	private ISDContext sdContext;
	
	// SciDesktop Modification TA_R001	--- constructor is modified to allow initialization and handling of context
	public PrestGuiView(SingleFrameApplication app, ISDContext ctx) {
		super(app);
		
		// SciDesktop Modification TA_R001	--- context initialization and modification of JFrame instance
		sdContext = ctx;
		
		JFrame frame = getFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);
		
		if (sdContext != null && sdContext.getMode() == ISDContext.MODE_NATIVE && sdContext.getApplicationIcon() != null)
			frame.setIconImage(sdContext.getApplicationIcon().getImage());
		
		initComponents();

		// status bar initialization - message timeout, idle icon and busy
		// animation, etc
		ResourceMap resourceMap = getResourceMap();
		int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
		messageTimer = new Timer(messageTimeout, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				statusMessageLabel.setText("");
			}
		});
		messageTimer.setRepeats(false);
		int busyAnimationRate = resourceMap
				.getInteger("StatusBar.busyAnimationRate");
		for (int i = 0; i < busyIcons.length; i++) {
			busyIcons[i] = resourceMap
					.getIcon("StatusBar.busyIcons[" + i + "]");
		}
		busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
				statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
			}
		});
		idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
		statusAnimationLabel.setIcon(idleIcon);
		progressBar.setVisible(false);

		// connecting action tasks to status bar via TaskMonitor
		TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
		taskMonitor
				.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

					public void propertyChange(
							java.beans.PropertyChangeEvent evt) {
						String propertyName = evt.getPropertyName();
						if ("started".equals(propertyName)) {
							if (!busyIconTimer.isRunning()) {
								statusAnimationLabel.setIcon(busyIcons[0]);
								busyIconIndex = 0;
								busyIconTimer.start();
							}
							progressBar.setVisible(true);
							progressBar.setIndeterminate(true);
						} else if ("done".equals(propertyName)) {
							busyIconTimer.stop();
							statusAnimationLabel.setIcon(idleIcon);
							progressBar.setVisible(false);
							progressBar.setValue(0);
						} else if ("message".equals(propertyName)) {
							String text = (String) (evt.getNewValue());
							statusMessageLabel.setText((text == null) ? ""
									: text);
							messageTimer.restart();
						} else if ("progress".equals(propertyName)) {
							int value = (Integer) (evt.getNewValue());
							progressBar.setVisible(true);
							progressBar.setIndeterminate(false);
							progressBar.setValue(value);
						}
					}
				});

		setStaticComponents();

		packageExplorer = new PackageExplorer();
		packageExplorer.generateRepositoryTree(mainContentSplitPanel);
		packageExplorer.traverseRepository();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        mainContentSplitPanel = new javax.swing.JSplitPane();
        analysisResultsPanel = new javax.swing.JPanel();
        tabPaneAnalysisResults = new javax.swing.JTabbedPane();
        pnlDataTab = new javax.swing.JPanel();
        pnlCategorizeButtons = new javax.swing.JPanel();
        btnLoadCategorizer = new javax.swing.JButton();
        btnCategorize = new javax.swing.JButton();
        btnStoreCategorizer = new javax.swing.JButton();
        pnlDataCardLayout = new javax.swing.JPanel();
        pnlParseResultsAnalyze = new javax.swing.JPanel();
        cmbMetricGroups = new javax.swing.JComboBox();
        metricsCardLayoutPanel = new javax.swing.JPanel();
        packageMetricsTabbedPane = new javax.swing.JTabbedPane();
        packageMetricsDataSetPanel = new javax.swing.JPanel();
        packageMetricsThresholdPanel = new javax.swing.JPanel();
        fileMetricsTabbedPane = new javax.swing.JTabbedPane();
        fileMetricsDataSetPanel = new javax.swing.JPanel();
        fileMetricsThresholdPanel = new javax.swing.JPanel();
        classMetricsTabbedPane = new javax.swing.JTabbedPane();
        classMetricsDataSetPanel = new javax.swing.JPanel();
        classMetricsThresholdPanel = new javax.swing.JPanel();
        methodMetricsTabbedPane = new javax.swing.JTabbedPane();
        methodMetricsDataSetPanel = new javax.swing.JPanel();
        methodMetricsThresholdPanel = new javax.swing.JPanel();
        pnlLanguageRadioPanel = new javax.swing.JPanel();
        pnlDataFileAnalyze = new javax.swing.JPanel();
        tabPaneDataFileAnalyze = new javax.swing.JTabbedPane();
        pnlDataFile = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnLoadTraining = new javax.swing.JButton();
        btnLoadTest = new javax.swing.JButton();
        btnStartWeka = new javax.swing.JButton();
        jComboBoxChooseAlgorithm = new javax.swing.JComboBox();
        chkboxCrossValidate = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxNormalize = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        addProjectMenuItem = new javax.swing.JMenuItem();
        switchWorkspaceMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        analyzeMenu = new javax.swing.JMenu();
        thresholdMenuItem = new javax.swing.JMenuItem();
        virtualMetricMenuItem = new javax.swing.JMenuItem();
        filterMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        testOptionsButtonGroup = new javax.swing.ButtonGroup();
        virtualMetricWizardDialog = new javax.swing.JDialog();
        pnlVmHeader = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        pnlVmCardLayout = new javax.swing.JPanel();
        pnlVmSettings = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtVmMetricName = new javax.swing.JTextField();
        cmbVmMetricOperator = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        rbtnVmOperand1Txt = new javax.swing.JRadioButton();
        txtVmOperand1 = new javax.swing.JTextField();
        rbtnVmOperand1Cmb = new javax.swing.JRadioButton();
        cmbVmOperand1 = new javax.swing.JComboBox();
        lblVmOperand2 = new javax.swing.JLabel();
        rbtnVmOperand2Txt = new javax.swing.JRadioButton();
        txtVmOperand2 = new javax.swing.JTextField();
        rbtnVmOperand2Cmb = new javax.swing.JRadioButton();
        cmbVmOperand2 = new javax.swing.JComboBox();
        pnlVmApproval = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        lblVirtualMetricName = new javax.swing.JLabel();
        lblVirtualMetricEquation = new javax.swing.JLabel();
        pnlVmButtons = new javax.swing.JPanel();
        btnVmBack = new javax.swing.JButton();
        cancelVirtualMetricButton = new javax.swing.JButton();
        btnVmNext = new javax.swing.JButton();
        thresholdWizardDialog = new javax.swing.JDialog();
        pnlthresholdWizardMain = new javax.swing.JPanel();
        pnlThresholdSettingsCardLayout = new javax.swing.JPanel();
        pnlThresholdSettings = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        cmbThresholdMetricList = new javax.swing.JComboBox();
        cmbThresholdOperator1 = new javax.swing.JComboBox();
        cmbThresholdOperator = new javax.swing.JComboBox();
        rbtnThresholdOperatorOneTxt = new javax.swing.JRadioButton();
        rbtnThresholdOperator1Cmb = new javax.swing.JRadioButton();
        txtThresholdOperator1 = new javax.swing.JTextField();
        cmbRiskLevels = new javax.swing.JComboBox();
        lblThresholdOperand2 = new javax.swing.JLabel();
        rbtnThresholdOperator2Txt = new javax.swing.JRadioButton();
        txtThresholdOperator2 = new javax.swing.JTextField();
        rbtnThresholdOperator2Cmb = new javax.swing.JRadioButton();
        cmbThresholdOperator2 = new javax.swing.JComboBox();
        pnlThresholdApprove = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        lblThresholdExpression = new javax.swing.JLabel();
        lblThresholdRiskLevel = new javax.swing.JLabel();
        pnlThresholdWizardHeader = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        pnlThresholdWizardButtons = new javax.swing.JPanel();
        btnThresholdWizardBack = new javax.swing.JButton();
        btnThresholdWizardNext = new javax.swing.JButton();
        btnThresholdWizardCancel = new javax.swing.JButton();
        btngRiskLevel = new javax.swing.ButtonGroup();
        btngFirstOperand = new javax.swing.ButtonGroup();
        btngSecondOperand = new javax.swing.ButtonGroup();
        btngVmOperand1 = new javax.swing.ButtonGroup();
        btngVmOperand2 = new javax.swing.ButtonGroup();
        btngLanguageGroup = new javax.swing.ButtonGroup();
        filterWizardDialog = new javax.swing.JDialog();
        pnlFilterHeader = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        pnlFilterButtons = new javax.swing.JPanel();
        btnApplyFilter = new javax.swing.JButton();
        btnCancelFilter = new javax.swing.JButton();
        pnlFilterMain = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listAllMetrics = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        listFilterMetrics = new javax.swing.JList();
        jLabel22 = new javax.swing.JLabel();
        btnTransferToFilter = new javax.swing.JButton();
        btnTransferLeft = new javax.swing.JButton();
        btnTransferAllToFilter = new javax.swing.JButton();
        btnTransferAllToLeft = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N
        mainContentSplitPanel.setDividerLocation(150);
        mainContentSplitPanel.setDebugGraphicsOptions(DebugGraphics.LOG_OPTION );
        mainContentSplitPanel.setMinimumSize(new java.awt.Dimension(150, 80));
        mainContentSplitPanel.setName("mainContentSplitPanel"); // NOI18N
        mainContentSplitPanel.setPreferredSize(new java.awt.Dimension(150, 80));

        analysisResultsPanel.setName("analysisResultsPanel"); // NOI18N

        tabPaneAnalysisResults.setName("tabPaneAnalysisResults"); // NOI18N

        pnlDataTab.setName("pnlDataTab"); // NOI18N

        pnlCategorizeButtons.setName("pnlCategorizeButtons"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(prestgui.PrestGuiApp.class).getContext().getActionMap(PrestGuiView.class, this);
        btnLoadCategorizer.setAction(actionMap.get("loadCategorizer")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(prestgui.PrestGuiApp.class).getContext().getResourceMap(PrestGuiView.class);
        btnLoadCategorizer.setText(resourceMap.getString("btnLoadCategorizer.text")); // NOI18N
        btnLoadCategorizer.setName("btnLoadCategorizer"); // NOI18N
        btnLoadCategorizer.setVisible(false);

        btnCategorize.setAction(actionMap.get("categorize")); // NOI18N
        btnCategorize.setText(resourceMap.getString("btnCategorize.text")); // NOI18N
        btnCategorize.setName("btnCategorize"); // NOI18N
        btnCategorize.setVisible(false);

        btnStoreCategorizer.setAction(actionMap.get("storeCategorizer")); // NOI18N
        btnStoreCategorizer.setText(resourceMap.getString("btnStoreCategorizer.text")); // NOI18N
        btnStoreCategorizer.setName("btnStoreCategorizer"); // NOI18N
        btnStoreCategorizer.setVisible(false);

        javax.swing.GroupLayout pnlCategorizeButtonsLayout = new javax.swing.GroupLayout(pnlCategorizeButtons);
        pnlCategorizeButtons.setLayout(pnlCategorizeButtonsLayout);
        pnlCategorizeButtonsLayout.setHorizontalGroup(
            pnlCategorizeButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCategorizeButtonsLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(btnLoadCategorizer)
                .addGap(28, 28, 28)
                .addComponent(btnCategorize)
                .addGap(29, 29, 29)
                .addComponent(btnStoreCategorizer)
                .addContainerGap(300, Short.MAX_VALUE))
        );
        pnlCategorizeButtonsLayout.setVerticalGroup(
            pnlCategorizeButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCategorizeButtonsLayout.createSequentialGroup()
                .addGroup(pnlCategorizeButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLoadCategorizer)
                    .addComponent(btnCategorize)
                    .addComponent(btnStoreCategorizer))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlDataCardLayout.setName("pnlDataCardLayout"); // NOI18N
        pnlDataCardLayout.setLayout(new java.awt.CardLayout());

        pnlParseResultsAnalyze.setName("pnlParseResultsAnalyze"); // NOI18N

        cmbMetricGroups.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Package Metrics", "File Metrics", "Class Metrics", "Method Metrics" }));
        cmbMetricGroups.setName("cmbMetricGroups"); // NOI18N
        cmbMetricGroups.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbMetricGroupsItemStateChanged(evt);
            }
        });

        metricsCardLayoutPanel.setName("metricsCardLayoutPanel"); // NOI18N
        metricsCardLayoutPanel.setLayout(new java.awt.CardLayout());
        CardLayout cards = (CardLayout)metricsCardLayoutPanel.getLayout();
        cards.show(metricsCardLayoutPanel, "card3");

        packageMetricsTabbedPane.setName("packageMetricsTabbedPane"); // NOI18N

        packageMetricsDataSetPanel.setName("packageMetricsDataSetPanel"); // NOI18N

        javax.swing.GroupLayout packageMetricsDataSetPanelLayout = new javax.swing.GroupLayout(packageMetricsDataSetPanel);
        packageMetricsDataSetPanel.setLayout(packageMetricsDataSetPanelLayout);
        packageMetricsDataSetPanelLayout.setHorizontalGroup(
            packageMetricsDataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 774, Short.MAX_VALUE)
        );
        packageMetricsDataSetPanelLayout.setVerticalGroup(
            packageMetricsDataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        packageMetricsTabbedPane.addTab(resourceMap.getString("packageMetricsDataSetPanel.TabConstraints.tabTitle"), packageMetricsDataSetPanel); // NOI18N

        packageMetricsThresholdPanel.setName("packageMetricsThresholdPanel"); // NOI18N

        javax.swing.GroupLayout packageMetricsThresholdPanelLayout = new javax.swing.GroupLayout(packageMetricsThresholdPanel);
        packageMetricsThresholdPanel.setLayout(packageMetricsThresholdPanelLayout);
        packageMetricsThresholdPanelLayout.setHorizontalGroup(
            packageMetricsThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 774, Short.MAX_VALUE)
        );
        packageMetricsThresholdPanelLayout.setVerticalGroup(
            packageMetricsThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        packageMetricsTabbedPane.addTab(resourceMap.getString("packageMetricsThresholdPanel.TabConstraints.tabTitle"), packageMetricsThresholdPanel); // NOI18N

        packageMetricsTabbedPane.setSelectedIndex(0);

        metricsCardLayoutPanel.add(packageMetricsTabbedPane, "card3");

        fileMetricsTabbedPane.setName("fileMetricsTabbedPane"); // NOI18N

        fileMetricsDataSetPanel.setName("fileMetricsDataSetPanel"); // NOI18N

        javax.swing.GroupLayout fileMetricsDataSetPanelLayout = new javax.swing.GroupLayout(fileMetricsDataSetPanel);
        fileMetricsDataSetPanel.setLayout(fileMetricsDataSetPanelLayout);
        fileMetricsDataSetPanelLayout.setHorizontalGroup(
            fileMetricsDataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 774, Short.MAX_VALUE)
        );
        fileMetricsDataSetPanelLayout.setVerticalGroup(
            fileMetricsDataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        fileMetricsTabbedPane.addTab(resourceMap.getString("fileMetricsDataSetPanel.TabConstraints.tabTitle"), fileMetricsDataSetPanel); // NOI18N

        fileMetricsThresholdPanel.setName("fileMetricsThresholdPanel"); // NOI18N

        javax.swing.GroupLayout fileMetricsThresholdPanelLayout = new javax.swing.GroupLayout(fileMetricsThresholdPanel);
        fileMetricsThresholdPanel.setLayout(fileMetricsThresholdPanelLayout);
        fileMetricsThresholdPanelLayout.setHorizontalGroup(
            fileMetricsThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 774, Short.MAX_VALUE)
        );
        fileMetricsThresholdPanelLayout.setVerticalGroup(
            fileMetricsThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        fileMetricsTabbedPane.addTab(resourceMap.getString("fileMetricsThresholdPanel.TabConstraints.tabTitle"), fileMetricsThresholdPanel); // NOI18N

        fileMetricsTabbedPane.setSelectedIndex(0);

        metricsCardLayoutPanel.add(fileMetricsTabbedPane, "card2");

        classMetricsTabbedPane.setName("classMetricsTabbedPane"); // NOI18N

        classMetricsDataSetPanel.setName("classMetricsDataSetPanel"); // NOI18N

        javax.swing.GroupLayout classMetricsDataSetPanelLayout = new javax.swing.GroupLayout(classMetricsDataSetPanel);
        classMetricsDataSetPanel.setLayout(classMetricsDataSetPanelLayout);
        classMetricsDataSetPanelLayout.setHorizontalGroup(
            classMetricsDataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 774, Short.MAX_VALUE)
        );
        classMetricsDataSetPanelLayout.setVerticalGroup(
            classMetricsDataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        classMetricsTabbedPane.addTab(resourceMap.getString("classMetricsDataSetPanel.TabConstraints.tabTitle"), classMetricsDataSetPanel); // NOI18N

        classMetricsThresholdPanel.setName("classMetricsThresholdPanel"); // NOI18N

        javax.swing.GroupLayout classMetricsThresholdPanelLayout = new javax.swing.GroupLayout(classMetricsThresholdPanel);
        classMetricsThresholdPanel.setLayout(classMetricsThresholdPanelLayout);
        classMetricsThresholdPanelLayout.setHorizontalGroup(
            classMetricsThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 774, Short.MAX_VALUE)
        );
        classMetricsThresholdPanelLayout.setVerticalGroup(
            classMetricsThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        classMetricsTabbedPane.addTab(resourceMap.getString("classMetricsThresholdPanel.TabConstraints.tabTitle"), classMetricsThresholdPanel); // NOI18N

        classMetricsTabbedPane.setSelectedIndex(0);

        metricsCardLayoutPanel.add(classMetricsTabbedPane, "card5");

        methodMetricsTabbedPane.setName("methodMetricsTabbedPane"); // NOI18N

        methodMetricsDataSetPanel.setName("methodMetricsDataSetPanel"); // NOI18N

        javax.swing.GroupLayout methodMetricsDataSetPanelLayout = new javax.swing.GroupLayout(methodMetricsDataSetPanel);
        methodMetricsDataSetPanel.setLayout(methodMetricsDataSetPanelLayout);
        methodMetricsDataSetPanelLayout.setHorizontalGroup(
            methodMetricsDataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 774, Short.MAX_VALUE)
        );
        methodMetricsDataSetPanelLayout.setVerticalGroup(
            methodMetricsDataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        methodMetricsTabbedPane.addTab(resourceMap.getString("methodMetricsDataSetPanel.TabConstraints.tabTitle"), methodMetricsDataSetPanel); // NOI18N

        methodMetricsThresholdPanel.setName("methodMetricsThresholdPanel"); // NOI18N

        javax.swing.GroupLayout methodMetricsThresholdPanelLayout = new javax.swing.GroupLayout(methodMetricsThresholdPanel);
        methodMetricsThresholdPanel.setLayout(methodMetricsThresholdPanelLayout);
        methodMetricsThresholdPanelLayout.setHorizontalGroup(
            methodMetricsThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 774, Short.MAX_VALUE)
        );
        methodMetricsThresholdPanelLayout.setVerticalGroup(
            methodMetricsThresholdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        methodMetricsTabbedPane.addTab(resourceMap.getString("methodMetricsThresholdPanel.TabConstraints.tabTitle"), methodMetricsThresholdPanel); // NOI18N

        methodMetricsTabbedPane.setSelectedIndex(0);

        metricsCardLayoutPanel.add(methodMetricsTabbedPane, "card4");

        pnlLanguageRadioPanel.setAlignmentY(0.0F);
        pnlLanguageRadioPanel.setName("pnlLanguageRadioPanel"); // NOI18N

        javax.swing.GroupLayout pnlLanguageRadioPanelLayout = new javax.swing.GroupLayout(pnlLanguageRadioPanel);
        pnlLanguageRadioPanel.setLayout(pnlLanguageRadioPanelLayout);
        pnlLanguageRadioPanelLayout.setHorizontalGroup(
            pnlLanguageRadioPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 671, Short.MAX_VALUE)
        );
        pnlLanguageRadioPanelLayout.setVerticalGroup(
            pnlLanguageRadioPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlParseResultsAnalyzeLayout = new javax.swing.GroupLayout(pnlParseResultsAnalyze);
        pnlParseResultsAnalyze.setLayout(pnlParseResultsAnalyzeLayout);
        pnlParseResultsAnalyzeLayout.setHorizontalGroup(
            pnlParseResultsAnalyzeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParseResultsAnalyzeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlParseResultsAnalyzeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(metricsCardLayoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE)
                    .addGroup(pnlParseResultsAnalyzeLayout.createSequentialGroup()
                        .addComponent(cmbMetricGroups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlLanguageRadioPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlParseResultsAnalyzeLayout.setVerticalGroup(
            pnlParseResultsAnalyzeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParseResultsAnalyzeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlParseResultsAnalyzeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlLanguageRadioPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbMetricGroups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(metricsCardLayoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
        );

        pnlDataCardLayout.add(pnlParseResultsAnalyze, "card4");

        pnlDataFileAnalyze.setName("pnlDataFileAnalyze"); // NOI18N

        tabPaneDataFileAnalyze.setName("tabPaneDataFileAnalyze"); // NOI18N

        pnlDataFile.setName("pnlDataFile"); // NOI18N

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        javax.swing.GroupLayout pnlDataFileLayout = new javax.swing.GroupLayout(pnlDataFile);
        pnlDataFile.setLayout(pnlDataFileLayout);
        pnlDataFileLayout.setHorizontalGroup(
            pnlDataFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDataFileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel23)
                .addContainerGap(665, Short.MAX_VALUE))
        );
        pnlDataFileLayout.setVerticalGroup(
            pnlDataFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDataFileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel23)
                .addContainerGap(286, Short.MAX_VALUE))
        );

        tabPaneDataFileAnalyze.addTab(resourceMap.getString("pnlDataFile.TabConstraints.tabTitle"), pnlDataFile); // NOI18N

        javax.swing.GroupLayout pnlDataFileAnalyzeLayout = new javax.swing.GroupLayout(pnlDataFileAnalyze);
        pnlDataFileAnalyze.setLayout(pnlDataFileAnalyzeLayout);
        pnlDataFileAnalyzeLayout.setHorizontalGroup(
            pnlDataFileAnalyzeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPaneDataFileAnalyze, javax.swing.GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE)
        );
        pnlDataFileAnalyzeLayout.setVerticalGroup(
            pnlDataFileAnalyzeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPaneDataFileAnalyze, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
        );

        pnlDataCardLayout.add(pnlDataFileAnalyze, "card3");

        javax.swing.GroupLayout pnlDataTabLayout = new javax.swing.GroupLayout(pnlDataTab);
        pnlDataTab.setLayout(pnlDataTabLayout);
        pnlDataTabLayout.setHorizontalGroup(
            pnlDataTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDataTabLayout.createSequentialGroup()
                .addGroup(pnlDataTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlDataTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlDataCardLayout, javax.swing.GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlDataTabLayout.createSequentialGroup()
                        .addGap(118, 118, 118)
                        .addComponent(pnlCategorizeButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlDataTabLayout.setVerticalGroup(
            pnlDataTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDataTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlCategorizeButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(pnlDataCardLayout, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPaneAnalysisResults.addTab(resourceMap.getString("pnlDataTab.TabConstraints.tabTitle"), pnlDataTab); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        btnLoadTraining.setAction(actionMap.get("openTrainingFile")); // NOI18N
        btnLoadTraining.setText(resourceMap.getString("btnLoadTraining.text")); // NOI18N
        btnLoadTraining.setName("btnLoadTraining"); // NOI18N

        btnLoadTest.setAction(actionMap.get("openTestFile")); // NOI18N
        btnLoadTest.setName("btnLoadTest"); // NOI18N

        btnStartWeka.setAction(actionMap.get("startWeka")); // NOI18N
        btnStartWeka.setText(resourceMap.getString("btnStartWeka.text")); // NOI18N
        btnStartWeka.setName("btnStartWeka"); // NOI18N

        jComboBoxChooseAlgorithm.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Naive Bayes", "J48" }));
        jComboBoxChooseAlgorithm.setName("jComboBoxChooseAlgorithm"); // NOI18N
        jComboBoxChooseAlgorithm.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxChooseAlgorithmItemStateChanged(evt);
            }
        });

        chkboxCrossValidate.setAction(actionMap.get("crossValidateAction")); // NOI18N
        chkboxCrossValidate.setName("chkboxCrossValidate"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jCheckBoxNormalize.setText(resourceMap.getString("jCheckBoxNormalize.text")); // NOI18N
        jCheckBoxNormalize.setName("jCheckBoxNormalize"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(chkboxCrossValidate))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(btnLoadTest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(btnLoadTraining, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnStartWeka, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jCheckBoxNormalize, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jComboBoxChooseAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(8, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnLoadTraining)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLoadTest)
                .addGap(14, 14, 14)
                .addComponent(chkboxCrossValidate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxNormalize)
                .addGap(75, 75, 75)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxChooseAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                .addComponent(btnStartWeka)
                .addContainerGap())
        );

        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(resourceMap.getFont("jTextArea1.font")); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setText(resourceMap.getString("jTextArea1.text")); // NOI18N
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane4.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 657, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabPaneAnalysisResults.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout analysisResultsPanelLayout = new javax.swing.GroupLayout(analysisResultsPanel);
        analysisResultsPanel.setLayout(analysisResultsPanelLayout);
        analysisResultsPanelLayout.setHorizontalGroup(
            analysisResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisResultsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPaneAnalysisResults, javax.swing.GroupLayout.DEFAULT_SIZE, 824, Short.MAX_VALUE)
                .addGap(4, 4, 4))
        );
        analysisResultsPanelLayout.setVerticalGroup(
            analysisResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisResultsPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(tabPaneAnalysisResults, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE))
        );

        mainContentSplitPanel.setRightComponent(analysisResultsPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainContentSplitPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 750, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainContentSplitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        addProjectMenuItem.setText(resourceMap.getString("addProjectMenuItem.text")); // NOI18N
        addProjectMenuItem.setName("addProjectMenuItem"); // NOI18N
        addProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(addProjectMenuItem);

        switchWorkspaceMenuItem.setAction(actionMap.get("switchWorkspace")); // NOI18N
        switchWorkspaceMenuItem.setText(resourceMap.getString("switchWorkspaceMenuItem.text")); // NOI18N
        switchWorkspaceMenuItem.setName("switchWorkspaceMenuItem"); // NOI18N
        fileMenu.add(switchWorkspaceMenuItem);

		// SciDesktop Modification TA_R001	--- exit item is modified to redirect the action to application's terminate method
		// note that PrestGuiView.properties file is also modified for redefinition of this menu item
        exitMenuItem.setAction(actionMap.get("terminate")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        analyzeMenu.setAction(actionMap.get("defineThreshold")); // NOI18N
        analyzeMenu.setText(resourceMap.getString("analyzeMenu.text")); // NOI18N
        analyzeMenu.setName("analyzeMenu"); // NOI18N

        thresholdMenuItem.setAction(actionMap.get("defineThreshold")); // NOI18N
        thresholdMenuItem.setText(resourceMap.getString("thresholdMenuItem.text")); // NOI18N
        thresholdMenuItem.setName("thresholdMenuItem"); // NOI18N
        analyzeMenu.add(thresholdMenuItem);

        virtualMetricMenuItem.setAction(actionMap.get("defineVirtualMetric")); // NOI18N
        virtualMetricMenuItem.setText(resourceMap.getString("virtualMetricMenuItem.text")); // NOI18N
        virtualMetricMenuItem.setName("virtualMetricMenuItem"); // NOI18N
        analyzeMenu.add(virtualMetricMenuItem);

        filterMenuItem.setAction(actionMap.get("defineNewFilter")); // NOI18N
        filterMenuItem.setText(resourceMap.getString("filterMenuItem.text")); // NOI18N
        filterMenuItem.setName("filterMenuItem"); // NOI18N
        analyzeMenu.add(filterMenuItem);

        menuBar.add(analyzeMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 750, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        virtualMetricWizardDialog.setTitle(resourceMap.getString("virtualMetricWizardDialog.title")); // NOI18N
        virtualMetricWizardDialog.setAlwaysOnTop(true);
        virtualMetricWizardDialog.setName("virtualMetricWizardDialog"); // NOI18N
        virtualMetricWizardDialog.setResizable(false);

        pnlVmHeader.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlVmHeader.setName("pnlVmHeader"); // NOI18N

        jLabel19.setFont(resourceMap.getFont("jLabel19.font")); // NOI18N
        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        javax.swing.GroupLayout pnlVmHeaderLayout = new javax.swing.GroupLayout(pnlVmHeader);
        pnlVmHeader.setLayout(pnlVmHeaderLayout);
        pnlVmHeaderLayout.setHorizontalGroup(
            pnlVmHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVmHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addContainerGap(267, Short.MAX_VALUE))
        );
        pnlVmHeaderLayout.setVerticalGroup(
            pnlVmHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlVmHeaderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel19)
                .addContainerGap())
        );

        pnlVmCardLayout.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlVmCardLayout.setName("pnlVmCardLayout"); // NOI18N
        pnlVmCardLayout.setLayout(new java.awt.CardLayout());

        pnlVmSettings.setName("pnlVmSettings"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        txtVmMetricName.setText(resourceMap.getString("txtVmMetricName.text")); // NOI18N
        txtVmMetricName.setName("txtVmMetricName"); // NOI18N

        cmbVmMetricOperator.setName("cmbVmMetricOperator"); // NOI18N
        cmbVmMetricOperator.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbVmMetricOperatorItemStateChanged(evt);
            }
        });

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        btngVmOperand1.add(rbtnVmOperand1Txt);
        rbtnVmOperand1Txt.setSelected(true);
        rbtnVmOperand1Txt.setText(resourceMap.getString("rbtnVmOperand1Txt.text")); // NOI18N
        rbtnVmOperand1Txt.setName("rbtnVmOperand1Txt"); // NOI18N
        rbtnVmOperand1Txt.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbtnVmOperand1TxtStateChanged(evt);
            }
        });

        txtVmOperand1.setText(resourceMap.getString("txtVmOperand1.text")); // NOI18N
        txtVmOperand1.setName("txtVmOperand1"); // NOI18N

        btngVmOperand1.add(rbtnVmOperand1Cmb);
        rbtnVmOperand1Cmb.setText(resourceMap.getString("rbtnVmOperand1Cmb.text")); // NOI18N
        rbtnVmOperand1Cmb.setName("rbtnVmOperand1Cmb"); // NOI18N
        rbtnVmOperand1Cmb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbtnVmOperand1CmbItemStateChanged(evt);
            }
        });

        cmbVmOperand1.setEnabled(false);
        cmbVmOperand1.setName("cmbVmOperand1"); // NOI18N

        lblVmOperand2.setText(resourceMap.getString("lblVmOperand2.text")); // NOI18N
        lblVmOperand2.setName("lblVmOperand2"); // NOI18N

        btngVmOperand2.add(rbtnVmOperand2Txt);
        rbtnVmOperand2Txt.setSelected(true);
        rbtnVmOperand2Txt.setText(resourceMap.getString("rbtnVmOperand2Txt.text")); // NOI18N
        rbtnVmOperand2Txt.setName("rbtnVmOperand2Txt"); // NOI18N
        rbtnVmOperand2Txt.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbtnVmOperand2TxtStateChanged(evt);
            }
        });

        txtVmOperand2.setText(resourceMap.getString("txtVmOperand2.text")); // NOI18N
        txtVmOperand2.setName("txtVmOperand2"); // NOI18N

        btngVmOperand2.add(rbtnVmOperand2Cmb);
        rbtnVmOperand2Cmb.setText(resourceMap.getString("rbtnVmOperand2Cmb.text")); // NOI18N
        rbtnVmOperand2Cmb.setName("rbtnVmOperand2Cmb"); // NOI18N
        rbtnVmOperand2Cmb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbtnVmOperand2CmbItemStateChanged(evt);
            }
        });

        cmbVmOperand2.setEnabled(false);
        cmbVmOperand2.setName("cmbVmOperand2"); // NOI18N

        javax.swing.GroupLayout pnlVmSettingsLayout = new javax.swing.GroupLayout(pnlVmSettings);
        pnlVmSettings.setLayout(pnlVmSettingsLayout);
        pnlVmSettingsLayout.setHorizontalGroup(
            pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVmSettingsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(lblVmOperand2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlVmSettingsLayout.createSequentialGroup()
                        .addComponent(rbtnVmOperand2Txt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtVmOperand2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtnVmOperand2Cmb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbVmOperand2, 0, 167, Short.MAX_VALUE))
                    .addComponent(cmbVmMetricOperator, 0, 286, Short.MAX_VALUE)
                    .addComponent(txtVmMetricName, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                    .addGroup(pnlVmSettingsLayout.createSequentialGroup()
                        .addComponent(rbtnVmOperand1Txt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtVmOperand1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtnVmOperand1Cmb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbVmOperand1, 0, 167, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlVmSettingsLayout.setVerticalGroup(
            pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVmSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtVmMetricName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(cmbVmMetricOperator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel9)
                        .addComponent(rbtnVmOperand1Txt)
                        .addGroup(pnlVmSettingsLayout.createSequentialGroup()
                            .addGap(1, 1, 1)
                            .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbVmOperand1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(rbtnVmOperand1Cmb))))
                    .addComponent(txtVmOperand1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rbtnVmOperand2Txt)
                    .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtVmOperand2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(pnlVmSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cmbVmOperand2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rbtnVmOperand2Cmb)))
                    .addComponent(lblVmOperand2))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlVmCardLayout.add(pnlVmSettings, "card3");

        pnlVmApproval.setName("pnlVmApproval"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        lblVirtualMetricName.setFont(resourceMap.getFont("lblVirtualMetricName.font")); // NOI18N
        lblVirtualMetricName.setText(resourceMap.getString("lblVirtualMetricName.text")); // NOI18N
        lblVirtualMetricName.setName("lblVirtualMetricName"); // NOI18N

        lblVirtualMetricEquation.setFont(resourceMap.getFont("lblVirtualMetricEquation.font")); // NOI18N
        lblVirtualMetricEquation.setText(resourceMap.getString("lblVirtualMetricEquation.text")); // NOI18N
        lblVirtualMetricEquation.setName("lblVirtualMetricEquation"); // NOI18N

        javax.swing.GroupLayout pnlVmApprovalLayout = new javax.swing.GroupLayout(pnlVmApproval);
        pnlVmApproval.setLayout(pnlVmApprovalLayout);
        pnlVmApprovalLayout.setHorizontalGroup(
            pnlVmApprovalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVmApprovalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlVmApprovalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addGroup(pnlVmApprovalLayout.createSequentialGroup()
                        .addGroup(pnlVmApprovalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlVmApprovalLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel20))
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlVmApprovalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblVirtualMetricName, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                            .addComponent(lblVirtualMetricEquation, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlVmApprovalLayout.setVerticalGroup(
            pnlVmApprovalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVmApprovalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlVmApprovalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(lblVirtualMetricName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(pnlVmApprovalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(lblVirtualMetricEquation, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pnlVmCardLayout.add(pnlVmApproval, "card2");

        pnlVmButtons.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlVmButtons.setName("pnlVmButtons"); // NOI18N

        btnVmBack.setAction(actionMap.get("vitualMetricBack")); // NOI18N
        btnVmBack.setText(resourceMap.getString("btnVmBack.text")); // NOI18N
        btnVmBack.setName("btnVmBack"); // NOI18N

        cancelVirtualMetricButton.setAction(actionMap.get("closeVirtualMetricDialog")); // NOI18N
        cancelVirtualMetricButton.setText(resourceMap.getString("cancelVirtualMetricButton.text")); // NOI18N
        cancelVirtualMetricButton.setName("cancelVirtualMetricButton"); // NOI18N

        btnVmNext.setAction(actionMap.get("virtualMetricWizardNext")); // NOI18N
        btnVmNext.setText(resourceMap.getString("btnVmNext.text")); // NOI18N
        btnVmNext.setName("btnVmNext"); // NOI18N

        javax.swing.GroupLayout pnlVmButtonsLayout = new javax.swing.GroupLayout(pnlVmButtons);
        pnlVmButtons.setLayout(pnlVmButtonsLayout);
        pnlVmButtonsLayout.setHorizontalGroup(
            pnlVmButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlVmButtonsLayout.createSequentialGroup()
                .addContainerGap(181, Short.MAX_VALUE)
                .addComponent(btnVmBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVmNext)
                .addGap(26, 26, 26)
                .addComponent(cancelVirtualMetricButton)
                .addContainerGap())
        );
        pnlVmButtonsLayout.setVerticalGroup(
            pnlVmButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVmButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(cancelVirtualMetricButton)
                .addComponent(btnVmNext)
                .addComponent(btnVmBack, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout virtualMetricWizardDialogLayout = new javax.swing.GroupLayout(virtualMetricWizardDialog.getContentPane());
        virtualMetricWizardDialog.getContentPane().setLayout(virtualMetricWizardDialogLayout);
        virtualMetricWizardDialogLayout.setHorizontalGroup(
            virtualMetricWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(virtualMetricWizardDialogLayout.createSequentialGroup()
                .addGroup(virtualMetricWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(virtualMetricWizardDialogLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(virtualMetricWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlVmCardLayout, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                            .addComponent(pnlVmHeader, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(virtualMetricWizardDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlVmButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        virtualMetricWizardDialogLayout.setVerticalGroup(
            virtualMetricWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(virtualMetricWizardDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlVmHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlVmCardLayout, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlVmButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        thresholdWizardDialog.setTitle(resourceMap.getString("thresholdWizardDialog.title")); // NOI18N
        thresholdWizardDialog.setAlwaysOnTop(true);
        thresholdWizardDialog.setName("thresholdWizardDialog"); // NOI18N
        thresholdWizardDialog.setResizable(false);

        pnlthresholdWizardMain.setName("pnlthresholdWizardMain"); // NOI18N

        pnlThresholdSettingsCardLayout.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlThresholdSettingsCardLayout.setName("pnlThresholdSettingsCardLayout"); // NOI18N
        pnlThresholdSettingsCardLayout.setLayout(new java.awt.CardLayout());

        pnlThresholdSettings.setName("pnlThresholdSettings"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        cmbThresholdMetricList.setToolTipText(resourceMap.getString("cmbThresholdMetricList.toolTipText")); // NOI18N
        cmbThresholdMetricList.setName("cmbThresholdMetricList"); // NOI18N

        cmbThresholdOperator1.setEnabled(false);
        cmbThresholdOperator1.setName("cmbThresholdOperator1"); // NOI18N

        cmbThresholdOperator.setName("cmbThresholdOperator"); // NOI18N
        cmbThresholdOperator.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbThresholdOperatorItemStateChanged(evt);
            }
        });

        btngFirstOperand.add(rbtnThresholdOperatorOneTxt);
        rbtnThresholdOperatorOneTxt.setSelected(true);
        rbtnThresholdOperatorOneTxt.setText(resourceMap.getString("rbtnThresholdOperatorOneTxt.text")); // NOI18N
        rbtnThresholdOperatorOneTxt.setName("rbtnThresholdOperatorOneTxt"); // NOI18N
        rbtnThresholdOperatorOneTxt.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbtnThresholdOperatorOneTxtStateChanged(evt);
            }
        });

        btngFirstOperand.add(rbtnThresholdOperator1Cmb);
        rbtnThresholdOperator1Cmb.setText(resourceMap.getString("rbtnThresholdOperator1Cmb.text")); // NOI18N
        rbtnThresholdOperator1Cmb.setName("rbtnThresholdOperator1Cmb"); // NOI18N

        txtThresholdOperator1.setText(resourceMap.getString("txtThresholdOperator1.text")); // NOI18N
        txtThresholdOperator1.setName("txtThresholdOperator1"); // NOI18N

        cmbRiskLevels.setName("cmbRiskLevels"); // NOI18N

        lblThresholdOperand2.setText(resourceMap.getString("lblThresholdOperand2.text")); // NOI18N
        lblThresholdOperand2.setName("lblThresholdOperand2"); // NOI18N

        btngSecondOperand.add(rbtnThresholdOperator2Txt);
        rbtnThresholdOperator2Txt.setSelected(true);
        rbtnThresholdOperator2Txt.setName("rbtnThresholdOperator2Txt"); // NOI18N
        rbtnThresholdOperator2Txt.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbtnThresholdOperator2TxtStateChanged(evt);
            }
        });

        txtThresholdOperator2.setName("txtThresholdOperator2"); // NOI18N

        btngSecondOperand.add(rbtnThresholdOperator2Cmb);
        rbtnThresholdOperator2Cmb.setName("rbtnThresholdOperator2Cmb"); // NOI18N

        cmbThresholdOperator2.setEnabled(false);
        cmbThresholdOperator2.setName("cmbThresholdOperator2"); // NOI18N

        javax.swing.GroupLayout pnlThresholdSettingsLayout = new javax.swing.GroupLayout(pnlThresholdSettings);
        pnlThresholdSettings.setLayout(pnlThresholdSettingsLayout);
        pnlThresholdSettingsLayout.setHorizontalGroup(
            pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13)
                    .addComponent(jLabel15)
                    .addComponent(lblThresholdOperand2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbThresholdOperator, 0, 263, Short.MAX_VALUE)
                    .addComponent(cmbThresholdMetricList, 0, 263, Short.MAX_VALUE)
                    .addComponent(cmbRiskLevels, 0, 263, Short.MAX_VALUE)
                    .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbtnThresholdOperatorOneTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtThresholdOperator1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rbtnThresholdOperator1Cmb))
                            .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                                .addComponent(rbtnThresholdOperator2Txt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtThresholdOperator2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rbtnThresholdOperator2Cmb)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cmbThresholdOperator1, 0, 148, Short.MAX_VALUE)
                            .addComponent(cmbThresholdOperator2, 0, 148, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlThresholdSettingsLayout.setVerticalGroup(
            pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(cmbRiskLevels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(cmbThresholdMetricList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(cmbThresholdOperator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(rbtnThresholdOperatorOneTxt)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(rbtnThresholdOperator1Cmb)
                            .addComponent(txtThresholdOperator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(cmbThresholdOperator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbtnThresholdOperator2Txt)
                            .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtThresholdOperator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addGroup(pnlThresholdSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(cmbThresholdOperator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(rbtnThresholdOperator2Cmb)))))))
                    .addGroup(pnlThresholdSettingsLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(lblThresholdOperand2)))
                .addGap(11, 11, 11))
        );

        pnlThresholdSettingsCardLayout.add(pnlThresholdSettings, "card2");

        pnlThresholdApprove.setName("pnlThresholdApprove"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        lblThresholdExpression.setFont(resourceMap.getFont("lblThresholdExpression.font")); // NOI18N
        lblThresholdExpression.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblThresholdExpression.setText(resourceMap.getString("lblThresholdExpression.text")); // NOI18N
        lblThresholdExpression.setName("lblThresholdExpression"); // NOI18N

        lblThresholdRiskLevel.setFont(resourceMap.getFont("lblThresholdRiskLevel.font")); // NOI18N
        lblThresholdRiskLevel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblThresholdRiskLevel.setText(resourceMap.getString("lblThresholdRiskLevel.text")); // NOI18N
        lblThresholdRiskLevel.setName("lblThresholdRiskLevel"); // NOI18N

        javax.swing.GroupLayout pnlThresholdApproveLayout = new javax.swing.GroupLayout(pnlThresholdApprove);
        pnlThresholdApprove.setLayout(pnlThresholdApproveLayout);
        pnlThresholdApproveLayout.setHorizontalGroup(
            pnlThresholdApproveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlThresholdApproveLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlThresholdApproveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblThresholdExpression, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                    .addComponent(lblThresholdRiskLevel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        pnlThresholdApproveLayout.setVerticalGroup(
            pnlThresholdApproveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThresholdApproveLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblThresholdRiskLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblThresholdExpression, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        pnlThresholdSettingsCardLayout.add(pnlThresholdApprove, "card3");

        pnlThresholdWizardHeader.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlThresholdWizardHeader.setName("pnlThresholdWizardHeader"); // NOI18N

        jLabel17.setFont(resourceMap.getFont("jLabel17.font")); // NOI18N
        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        javax.swing.GroupLayout pnlThresholdWizardHeaderLayout = new javax.swing.GroupLayout(pnlThresholdWizardHeader);
        pnlThresholdWizardHeader.setLayout(pnlThresholdWizardHeaderLayout);
        pnlThresholdWizardHeaderLayout.setHorizontalGroup(
            pnlThresholdWizardHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThresholdWizardHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addContainerGap(215, Short.MAX_VALUE))
        );
        pnlThresholdWizardHeaderLayout.setVerticalGroup(
            pnlThresholdWizardHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlThresholdWizardHeaderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel17)
                .addContainerGap())
        );

        pnlThresholdWizardButtons.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlThresholdWizardButtons.setName("pnlThresholdWizardButtons"); // NOI18N

        btnThresholdWizardBack.setAction(actionMap.get("thresholdWizardBackButton")); // NOI18N
        btnThresholdWizardBack.setText(resourceMap.getString("btnThresholdWizardBack.text")); // NOI18N
        btnThresholdWizardBack.setName("btnThresholdWizardBack"); // NOI18N

        btnThresholdWizardNext.setAction(actionMap.get("thresholdWizardNextButton")); // NOI18N
        btnThresholdWizardNext.setText(resourceMap.getString("btnThresholdWizardNext.text")); // NOI18N
        btnThresholdWizardNext.setName("btnThresholdWizardNext"); // NOI18N

        btnThresholdWizardCancel.setAction(actionMap.get("closeThresholdWizardDialog")); // NOI18N
        btnThresholdWizardCancel.setText(resourceMap.getString("btnThresholdWizardCancel.text")); // NOI18N
        btnThresholdWizardCancel.setName("btnThresholdWizardCancel"); // NOI18N

        javax.swing.GroupLayout pnlThresholdWizardButtonsLayout = new javax.swing.GroupLayout(pnlThresholdWizardButtons);
        pnlThresholdWizardButtons.setLayout(pnlThresholdWizardButtonsLayout);
        pnlThresholdWizardButtonsLayout.setHorizontalGroup(
            pnlThresholdWizardButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlThresholdWizardButtonsLayout.createSequentialGroup()
                .addContainerGap(177, Short.MAX_VALUE)
                .addComponent(btnThresholdWizardBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnThresholdWizardNext)
                .addGap(18, 18, 18)
                .addComponent(btnThresholdWizardCancel)
                .addContainerGap())
        );
        pnlThresholdWizardButtonsLayout.setVerticalGroup(
            pnlThresholdWizardButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThresholdWizardButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnThresholdWizardCancel)
                .addComponent(btnThresholdWizardNext)
                .addComponent(btnThresholdWizardBack))
        );

        javax.swing.GroupLayout pnlthresholdWizardMainLayout = new javax.swing.GroupLayout(pnlthresholdWizardMain);
        pnlthresholdWizardMain.setLayout(pnlthresholdWizardMainLayout);
        pnlthresholdWizardMainLayout.setHorizontalGroup(
            pnlthresholdWizardMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlthresholdWizardMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlthresholdWizardMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlThresholdSettingsCardLayout, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlThresholdWizardHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlThresholdWizardButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlthresholdWizardMainLayout.setVerticalGroup(
            pnlthresholdWizardMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlthresholdWizardMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlThresholdWizardHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(pnlThresholdSettingsCardLayout, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlThresholdWizardButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout thresholdWizardDialogLayout = new javax.swing.GroupLayout(thresholdWizardDialog.getContentPane());
        thresholdWizardDialog.getContentPane().setLayout(thresholdWizardDialogLayout);
        thresholdWizardDialogLayout.setHorizontalGroup(
            thresholdWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlthresholdWizardMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        thresholdWizardDialogLayout.setVerticalGroup(
            thresholdWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlthresholdWizardMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        filterWizardDialog.setTitle(resourceMap.getString("filterWizardDialog.title")); // NOI18N
        filterWizardDialog.setModal(true);
        filterWizardDialog.setName("filterWizardDialog"); // NOI18N
        filterWizardDialog.setResizable(false);

        pnlFilterHeader.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlFilterHeader.setName("pnlFilterHeader"); // NOI18N

        jLabel21.setFont(resourceMap.getFont("jLabel21.font")); // NOI18N
        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        javax.swing.GroupLayout pnlFilterHeaderLayout = new javax.swing.GroupLayout(pnlFilterHeader);
        pnlFilterHeader.setLayout(pnlFilterHeaderLayout);
        pnlFilterHeaderLayout.setHorizontalGroup(
            pnlFilterHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(258, Short.MAX_VALUE))
        );
        pnlFilterHeaderLayout.setVerticalGroup(
            pnlFilterHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFilterHeaderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel21)
                .addContainerGap())
        );

        pnlFilterButtons.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlFilterButtons.setName("pnlFilterButtons"); // NOI18N

        btnApplyFilter.setAction(actionMap.get("filterWizardApplyButton")); // NOI18N
        btnApplyFilter.setText(resourceMap.getString("btnApplyFilter.text")); // NOI18N
        btnApplyFilter.setName("btnApplyFilter"); // NOI18N

        btnCancelFilter.setAction(actionMap.get("filterWizardCancelButton")); // NOI18N
        btnCancelFilter.setText(resourceMap.getString("btnCancelFilter.text")); // NOI18N
        btnCancelFilter.setName("btnCancelFilter"); // NOI18N

        javax.swing.GroupLayout pnlFilterButtonsLayout = new javax.swing.GroupLayout(pnlFilterButtons);
        pnlFilterButtons.setLayout(pnlFilterButtonsLayout);
        pnlFilterButtonsLayout.setHorizontalGroup(
            pnlFilterButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFilterButtonsLayout.createSequentialGroup()
                .addContainerGap(235, Short.MAX_VALUE)
                .addComponent(btnApplyFilter)
                .addGap(18, 18, 18)
                .addComponent(btnCancelFilter)
                .addGap(11, 11, 11))
        );
        pnlFilterButtonsLayout.setVerticalGroup(
            pnlFilterButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnCancelFilter)
                .addComponent(btnApplyFilter))
        );

        pnlFilterMain.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlFilterMain.setName("pnlFilterMain"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        listAllMetrics.setName("listAllMetrics"); // NOI18N
        listAllMetrics.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listAllMetricsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(listAllMetrics);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        listFilterMetrics.setName("listFilterMetrics"); // NOI18N
        listFilterMetrics.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listFilterMetricsValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(listFilterMetrics);

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        btnTransferToFilter.setAction(actionMap.get("transferHeadersToFilter")); // NOI18N
        btnTransferToFilter.setText(resourceMap.getString("btnTransferToFilter.text")); // NOI18N
        btnTransferToFilter.setName("btnTransferToFilter"); // NOI18N

        btnTransferLeft.setAction(actionMap.get("transferHeadersToLeft")); // NOI18N
        btnTransferLeft.setText(resourceMap.getString("btnTransferLeft.text")); // NOI18N
        btnTransferLeft.setName("btnTransferLeft"); // NOI18N

        btnTransferAllToFilter.setAction(actionMap.get("transferAllHeadersToFilter")); // NOI18N
        btnTransferAllToFilter.setText(resourceMap.getString("btnTransferAllToFilter.text")); // NOI18N
        btnTransferAllToFilter.setName("btnTransferAllToFilter"); // NOI18N

        btnTransferAllToLeft.setAction(actionMap.get("transferAllHeadersToLeft")); // NOI18N
        btnTransferAllToLeft.setText(resourceMap.getString("btnTransferAllToLeft.text")); // NOI18N
        btnTransferAllToLeft.setName("btnTransferAllToLeft"); // NOI18N

        javax.swing.GroupLayout pnlFilterMainLayout = new javax.swing.GroupLayout(pnlFilterMain);
        pnlFilterMain.setLayout(pnlFilterMainLayout);
        pnlFilterMainLayout.setHorizontalGroup(
            pnlFilterMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFilterMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFilterMainLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                        .addGroup(pnlFilterMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFilterMainLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(btnTransferToFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                            .addGroup(pnlFilterMainLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlFilterMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnTransferAllToFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnTransferLeft, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                                    .addComponent(btnTransferAllToLeft, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlFilterMainLayout.setVerticalGroup(
            pnlFilterMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFilterMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                    .addGroup(pnlFilterMainLayout.createSequentialGroup()
                        .addComponent(btnTransferToFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTransferLeft)
                        .addGap(26, 26, 26)
                        .addComponent(btnTransferAllToFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTransferAllToLeft)))
                .addContainerGap())
        );

        javax.swing.GroupLayout filterWizardDialogLayout = new javax.swing.GroupLayout(filterWizardDialog.getContentPane());
        filterWizardDialog.getContentPane().setLayout(filterWizardDialogLayout);
        filterWizardDialogLayout.setHorizontalGroup(
            filterWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterWizardDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filterWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFilterButtons, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlFilterMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlFilterHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        filterWizardDialogLayout.setVerticalGroup(
            filterWizardDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterWizardDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFilterHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFilterMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFilterButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxChooseAlgorithmItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxChooseAlgorithmItemStateChanged
        wekaAlgorithmType = jComboBoxChooseAlgorithm.getItemAt(jComboBoxChooseAlgorithm.getSelectedIndex()).toString();
    }//GEN-LAST:event_jComboBoxChooseAlgorithmItemStateChanged

	// <editor-fold defaultstate="collapsed" desc="Actions">

	// SciDesktop Modification TA_R001	--- terminate action (for the frame menu) is introduced here 
	@Action
	public void terminate() {
		PrestGuiApp app = org.jdesktop.application.Application.getInstance(prestgui.PrestGuiApp.class);
		app.terminate();
	}
	
	@Action
	public void switchWorkspace() {
		File repositoryFile = PrestGuiApp.getProjectDirectoryFromUser();
		if (repositoryFile != null) {
			ApplicationProperties.set("repositorylocation", repositoryFile
					.getAbsolutePath());
			ApplicationProperties.reCreatePropertiesFile();
			JOptionPane
					.showMessageDialog(
							analysisResultsPanel,
							"Changes will not be effective until you restart the tool!",
							"Switch Workspace", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void addProjectMenuItemActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_addProjectMenuItemActionPerformed
		// TODO add your handling code here:
		packageExplorer.addNewProject();
	}// GEN-LAST:event_addProjectMenuItemActionPerformed

	private void rbtnSuppliedTestSetActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_rbtnSuppliedTestSetActionPerformed
		disableAllUnnecessaryForCategorizer();
		// btnTestSet.setEnabled(true);
	}// GEN-LAST:event_rbtnSuppliedTestSetActionPerformed


	private void updateCategorizerList() {

		//listCategorizers.removeAll();

		Vector<Categorizer> catList = categorizerExecutor.getCategorizers();
		List<String> itemList = new ArrayList<String>();
		if (catList != null) {

			int size = catList.size();

			for (int i = 0; i < size; i++) {
				itemList.add("Categorizer_" + (i + 1));
			}

			//listCategorizers.setListData(itemList.toArray());
		}
	}

	private void cmbMetricGroupsItemStateChanged(java.awt.event.ItemEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_cmbMetricGroupsItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			CardLayout cards = (CardLayout) (metricsCardLayoutPanel.getLayout());

			if (cmbMetricGroups.getSelectedItem().equals("File Metrics"))
				cards.show(metricsCardLayoutPanel, "card2");
			else if (cmbMetricGroups.getSelectedItem()
					.equals("Package Metrics"))
				cards.show(metricsCardLayoutPanel, "card3");
			else if (cmbMetricGroups.getSelectedItem().equals("Method Metrics"))
				cards.show(metricsCardLayoutPanel, "card4");
			else
				cards.show(metricsCardLayoutPanel, "card5");
		}

	}// GEN-LAST:event_cmbMetricGroupsItemStateChanged

	private void rbtnVmOperand1CmbItemStateChanged(java.awt.event.ItemEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_rbtnVmOperand1CmbItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			if (rbtnVmOperand1Cmb.isSelected())
				cmbVmOperand1.setEnabled(true);
			else
				txtVmOperand1.setEnabled(true);
		}
	}// GEN-LAST:event_rbtnVmOperand1CmbItemStateChanged

	private void rbtnVmOperand2CmbItemStateChanged(java.awt.event.ItemEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_rbtnVmOperand2CmbItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			if (rbtnVmOperand2Cmb.isSelected())
				cmbVmOperand2.setEnabled(true);
			else
				txtVmOperand2.setEnabled(true);
		}
	}// GEN-LAST:event_rbtnVmOperand2CmbItemStateChanged

	private void cmbVmMetricOperatorItemStateChanged(
			java.awt.event.ItemEvent evt) {// GEN-FIRST:
		// event_cmbVmMetricOperatorItemStateChanged
		virtualMetricOperand2Visibility();

			}// GEN-LAST:event_cmbVmMetricOperatorItemStateChanged

	private void rbtnThresholdOperatorOneTxtStateChanged(
			javax.swing.event.ChangeEvent evt) {// GEN-FIRST:
		// event_rbtnThresholdOperatorOneTxtStateChanged
		if (((JRadioButton) evt.getSource()).isSelected()) {
			txtThresholdOperator1.setEnabled(true);
			cmbThresholdOperator1.setEnabled(false);
		} else {
			txtThresholdOperator1.setEnabled(false);
			cmbThresholdOperator1.setEnabled(true);
		}
	}// GEN-LAST:event_rbtnThresholdOperatorOneTxtStateChanged

	private void rbtnThresholdOperator2TxtStateChanged(
			javax.swing.event.ChangeEvent evt) {// GEN-FIRST:
		// event_rbtnThresholdOperator2TxtStateChanged
		if (((JRadioButton) evt.getSource()).isSelected()) {
			txtThresholdOperator2.setEnabled(true);
			cmbThresholdOperator2.setEnabled(false);
		} else {
			txtThresholdOperator2.setEnabled(false);
			cmbThresholdOperator2.setEnabled(true);
		}
	}// GEN-LAST:event_rbtnThresholdOperator2TxtStateChanged

	private void rbtnVmOperand1TxtStateChanged(javax.swing.event.ChangeEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_rbtnVmOperand1TxtStateChanged
		if (((JRadioButton) evt.getSource()).isSelected()) {
			txtVmOperand1.setEnabled(true);
			cmbVmOperand1.setEnabled(false);

		} else {
			txtVmOperand1.setEnabled(false);
			cmbVmOperand1.setEnabled(true);
		}
	}// GEN-LAST:event_rbtnVmOperand1TxtStateChanged

	private void rbtnVmOperand2TxtStateChanged(javax.swing.event.ChangeEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_rbtnVmOperand2TxtStateChanged
		if (((JRadioButton) evt.getSource()).isSelected()) {
			txtVmOperand2.setEnabled(true);
			cmbVmOperand2.setEnabled(false);
		} else {
			txtVmOperand2.setEnabled(false);
			cmbVmOperand2.setEnabled(true);
		}
	}// GEN-LAST:event_rbtnVmOperand2TxtStateChanged

	private void cmbThresholdOperatorItemStateChanged(
			java.awt.event.ItemEvent evt) {// GEN-FIRST:
		// event_cmbThresholdOperatorItemStateChanged
		thresholdOperand2Visibility();
	}// GEN-LAST:event_cmbThresholdOperatorItemStateChanged

	private void listAllMetricsValueChanged(
			javax.swing.event.ListSelectionEvent evt) {// GEN-FIRST:
		// event_listAllMetricsValueChanged
		if (evt.getValueIsAdjusting() == false) {
			if (listAllMetrics.getSelectedIndex() == -1) {
				btnTransferToFilter.setEnabled(false);

			} else {
				btnTransferToFilter.setEnabled(true);
			}

			if (listAllMetrics.getComponentCount() == 0)
				btnTransferAllToFilter.setEnabled(false);
			else
				btnTransferAllToFilter.setEnabled(true);
		}
	}// GEN-LAST:event_listAllMetricsValueChanged

	private void listFilterMetricsValueChanged(
			javax.swing.event.ListSelectionEvent evt) {// GEN-FIRST:
		// event_listFilterMetricsValueChanged
		if (evt.getValueIsAdjusting() == false) {
			if (listFilterMetrics.getSelectedIndex() == -1) {
				btnTransferLeft.setEnabled(false);
			} else {
				btnTransferLeft.setEnabled(true);
			}

			if (listFilterMetrics.getComponentCount() == 0)
				btnTransferAllToLeft.setEnabled(false);
			else
				btnTransferAllToLeft.setEnabled(true);
		}
	}// GEN-LAST:event_listFilterMetricsValueChanged

	@Action
	public void showAboutBox() {
		if (aboutBox == null) {
			JFrame mainFrame = PrestGuiApp.getApplication().getMainFrame();
			aboutBox = new PrestGuiAboutBox(mainFrame);
			aboutBox.setLocationRelativeTo(mainFrame);
		}
		PrestGuiApp.getApplication().show(aboutBox);
	}

	@Action
	public void openDataFile() {
		JFileChooser fileChooser = new JFileChooser();
		File currentDirectory;
		try {
			currentDirectory = new File(ApplicationProperties
					.get("repositorylocation"));

			if (currentDirectory != null) {
				fileChooser.setCurrentDirectory(currentDirectory);
				fileChooser.setDialogTitle("Select arff file");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				ExtensionFileFilter fileFilter = new ExtensionFileFilter(
						"arff files only", "ARFF");
				fileChooser.setFileFilter(fileFilter);
				int returnVal = fileChooser.showOpenDialog(null);

				File dir = null;

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					dir = fileChooser.getSelectedFile();
				}
				try {
					Arff2DataSet arff2DataSet = new Arff2DataSet(dir
							.getAbsolutePath());
					arffDataSet = arff2DataSet.reader();
					packageExplorer.displayMetrics(pnlDataFile, arffDataSet);
					CardLayout cards = (CardLayout) (pnlDataCardLayout
							.getLayout());
					cards.show(pnlDataCardLayout, "card3");
					Components.dataFileActive = true;

				} catch (Exception ex) {
					Logger.getLogger(PrestGuiView.class.getName()).log(
							Level.SEVERE, null, ex);
				} 
			} 
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null,
					"Please select a project first!", "Select Project",
					JOptionPane.WARNING_MESSAGE);
		}

	}

	public DataSet openDataSetFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select arff file");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		ExtensionFileFilter fileFilter = new ExtensionFileFilter(
				"arff files only", "ARFF");
		fileChooser.setFileFilter(fileFilter);
		int returnVal = fileChooser.showOpenDialog(null);
		DataSet newDataSet = null;
		File dir = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			dir = fileChooser.getSelectedFile();
		}
		try {
			Arff2DataSet arff2DataSet = new Arff2DataSet(dir.getAbsolutePath());
			newDataSet = arff2DataSet.reader();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(PrestGuiView.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (Exception e) {
			System.out.println(" **** ex " + e.getMessage());
			e.printStackTrace();
		}
		return newDataSet;
	}

	@Action
	public void defineThreshold() {
		if (packageExplorer.isResultsTransferred()) {
			thresholdWizardFillComboBoxes();
			thresholdWizardDialog.pack();
			thresholdWizardDialog.setVisible(true);
		} else
			JOptionPane.showMessageDialog(null,
					"Please select a project to add threshold",
					"Select Project", JOptionPane.ERROR_MESSAGE);
	}

	@Action
	public void defineVirtualMetric() {
		if (packageExplorer.isResultsTransferred()) {
			virtualMetricWizardFillComboBoxes();
			virtualMetricWizardDialog.pack();
			virtualMetricWizardDialog.setVisible(true);
		} else
			JOptionPane.showMessageDialog(null,
					"Please select a project to add threshold",
					"Select Project", JOptionPane.ERROR_MESSAGE);
	}

	public class ThresholdActionAdapter implements ActionListener {

		private int column;

		public ThresholdActionAdapter(int column) {
			super();
			this.column = column;
		}

		public void actionPerformed(ActionEvent e) {
			int selectedMetricGroup = Components.getSelectedMetricGroupIndex();
			DataSet ds = null;
			ds = ParserExecutor.getDataSetByMetricGroup(selectedMetricGroup,
					ParserExecutor.getCurrentLanguage(),
					Components.dataFileActive);
			DataHeader dataHeader = ds.getDataHeaders()[column - 1];
			// defineThreshold(dataHeader.getLabel());
		}
	}

	// </editor-fold>

	/**
	 * Disables all unnecessary categorizer build fields
	 */
	private void disableAllUnnecessaryForCategorizer() {
		List<JComponent> componentList = new ArrayList<JComponent>();


		ComponentState.setEnabledDisabled(componentList, false);
	}

	public void setStaticComponents() {
		ParserExecutor.setMainPanel(this.mainPanel);
		Components.packageMetricsDataSetPanel = this.packageMetricsDataSetPanel;
		Components.fileMetricsDataSetPanel = this.fileMetricsDataSetPanel;
		Components.classMetricsDataSetPanel = this.classMetricsDataSetPanel;
		Components.methodMetricsDataSetPanel = this.methodMetricsDataSetPanel;
		Components.cmbMetricGroups = this.cmbMetricGroups;
		Components.languageRadioButtonGroupPanel = this.pnlLanguageRadioPanel;
		Components.languageButtonGroup = this.btngLanguageGroup;
		Components.classMetricsThresholdPanel = this.classMetricsThresholdPanel;
		Components.fileMetricsThresholdPanel = this.fileMetricsThresholdPanel;
		Components.methodMetricsThresholdPanel = this.methodMetricsThresholdPanel;
		Components.packageMetricsThresholdPanel = this.methodMetricsThresholdPanel;
		Components.pnlDataCardLayout = this.pnlDataCardLayout;
		Components.pnlCategorizeButtons = this.pnlCategorizeButtons;
		Components.btnCategorize = this.btnCategorize;
		Components.btnLoadCategorizer = this.btnLoadCategorizer;
		Components.btnStoreCategorizer = this.btnStoreCategorizer;
	}

	private DataSet activeDataSet() {
		int selectedMetricType = cmbMetricGroups.getSelectedIndex();
		Language lang = Language.JAVA;// radioButtondan alinacak
		return ParserExecutor.getDataSetByMetricGroup(selectedMetricType, lang,
				Components.dataFileActive);
	}

	private JPanel activeDataSetPanel() {
		switch (cmbMetricGroups.getSelectedIndex()) {
		case 0:
			return packageMetricsDataSetPanel;
		case 1:
			return fileMetricsDataSetPanel;
		case 2:
			return classMetricsDataSetPanel;
		case 3:
			return methodMetricsDataSetPanel;
		default:
			return null;
		}
	}

	private JPanel activeThresholdPanel() {
		switch (cmbMetricGroups.getSelectedIndex()) {
		case 0:
			return packageMetricsThresholdPanel;
		case 1:
			return fileMetricsThresholdPanel;
		case 2:
			return classMetricsThresholdPanel;
		case 3:
			return methodMetricsThresholdPanel;
		default:
			return null;
		}
	}

	private JTabbedPane activeTabbedPane() {
		switch (cmbMetricGroups.getSelectedIndex()) {
		case 0:
			return packageMetricsTabbedPane;
		case 1:
			return fileMetricsTabbedPane;
		case 2:
			return classMetricsTabbedPane;
		case 3:
			return methodMetricsTabbedPane;
		default:
			return null;
		}
	}

	// @Action
	// public void staticCategorizeButton() {
	// try {
	// StaticCategorizer staticCategorizer = new StaticCategorizer();
	// //DataContext dataContext = staticCategorizer.store();
	// //dataContext.
	// staticCategorizer.loadDataSet(activeDataSet());
	// staticCategorizer.buildCategorizer();
	// packageExplorer.displayMetrics(activeDataSetPanel(), activeDataSet());
	// } catch (Exception ex) {
	// Logger.getLogger(PrestGuiView.class.getName()).log(Level.SEVERE, null,
	// ex);
	// }
	// }

	public File getCategorizerDirectoryFromUser() {
		File currentDirectory = new File(ApplicationProperties
				.get("repositorylocation")
				+ "\\" + packageExplorer.getProjectDirectory().getName());
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		fileChooser.setDialogTitle("Select folder for repository");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fileChooser.showOpenDialog(null);

		File dir = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			dir = fileChooser.getSelectedFile();
		}
		return dir;
	}

	@Action
	public void loadCategorizer() {
		try {
			File file = getCategorizerDirectoryFromUser();
			DataContext storedContext = DataContext.readFromFile(file
					.getAbsolutePath());
			if (storedContext != null) {
				for (int i = 0; i < 4; i++) {
					String selectedMetric = (String) cmbMetricGroups
							.getItemAt(i);
					selectedMetric = selectedMetric.replace(" ", "");
					DataContext relatedPart = storedContext
							.getNode(selectedMetric);
					if (relatedPart != null) {
						staticCategorizer = new StaticCategorizer();
						staticCategorizer.load(relatedPart);
						staticCategorizer.getDataSet().setDataItems(
								activeDataSet().getDataItems());
						ParserExecutor.setDataSetByMetricGroup(i,
								ParserExecutor.getCurrentLanguage(),
								staticCategorizer.getDataSet());
						staticCategorizer.buildCategorizer();
						cmbMetricGroups.setSelectedIndex(i);
						JOptionPane.showMessageDialog(mainContentSplitPanel,
								"Categorizer loaded successfully!",
								"Load Result", JOptionPane.INFORMATION_MESSAGE);
						packageExplorer.displayMetrics(activeDataSetPanel(),
								activeDataSet());
						JTabbedPane jTabbedPane = (JTabbedPane) activeDataSetPanel()
								.getParent();
						jTabbedPane.setSelectedIndex(0);
						displayThresholds(activeThresholdPanel(),
								activeDataSet());
						btnCategorize.setVisible(true);
						break;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Action
	public void categorize() {
		try {

			staticCategorizer = new StaticCategorizer();
			staticCategorizer.loadDataSet(activeDataSet());
			staticCategorizer.buildCategorizer();
			packageExplorer.displayMetrics(activeDataSetPanel(),
					activeDataSet());
			JTabbedPane jTabbedPane = (JTabbedPane) activeDataSetPanel()
					.getParent();
			jTabbedPane.setSelectedIndex(0);

		} catch (Exception ex) {
			Logger.getLogger(PrestGuiView.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		btnStoreCategorizer.setVisible(true);
	}

	@Action
	public void storeCategorizer() {
		try {
			DataContext context = new DataContext();
			String selectedMetric = (String) cmbMetricGroups.getSelectedItem();
			selectedMetric = selectedMetric.replace(" ", "");
			staticCategorizer.setTitle(selectedMetric);
			context.add(selectedMetric, staticCategorizer.store());
			context.writeToFile(ApplicationProperties.get("repositorylocation")
					+ "\\" + packageExplorer.getProjectDirectory().getName()
					+ "\\categorizer"
					+ Calendar.getInstance().getTimeInMillis() + ".xml");
			JOptionPane.showMessageDialog(null,
					"Categorizer stored successfully!", "Store Result",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			Logger.getLogger(PrestGuiView.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	// <editor-fold defaultstate="collapsed" desc="Threshold & Virtual Metric
	// Wizard methods">

	private void thresholdWizardFillComboBoxes() {
		riskLevels();
		attributesComboBoxValues(activeDataSet(), cmbThresholdMetricList);
		attributesComboBoxValues(activeDataSet(), cmbThresholdOperator1);
		attributesComboBoxValues(activeDataSet(), cmbThresholdOperator2);
		thresholdOperators();
		CardLayout cards = (CardLayout) (pnlThresholdSettingsCardLayout
				.getLayout());
		cards.show(pnlThresholdSettingsCardLayout, "card2");
		btnThresholdWizardNext.setText("Next");
	}

	private void thresholdWizardFillComboBoxesWithSelected(String label) {
		riskLevels();
		attributesComboBoxValuesWithSelected(activeDataSet(),
				cmbThresholdMetricList, label);
		attributesComboBoxValues(activeDataSet(), cmbThresholdOperator1);
		attributesComboBoxValues(activeDataSet(), cmbThresholdOperator2);
		thresholdOperators();
		CardLayout cards = (CardLayout) (pnlThresholdSettingsCardLayout
				.getLayout());
		cards.show(pnlThresholdSettingsCardLayout, "card2");
		btnThresholdWizardNext.setText("Next");
	}

	private void riskLevels() {
		List<String> riskLevelsArray = new ArrayList<String>();
		riskLevelsArray = getRiskLevels(activeDataSet());
		riskLevelsArray.add(0, "Please select risk level");
		cmbRiskLevels.setModel(new DefaultComboBoxModel(riskLevelsArray
				.toArray()));
	}

	private List<String> getRiskLevels(DataSet dataSet) {
		int classIndex = dataSet.getClassIndex();

		if (classIndex == -1) {
			for (int i = dataSet.getDataHeaders().length - 1; i >= 0; i--) {
				if (dataSet.getDataHeaders()[i].isNominal()) {
					classIndex = i;
					break;
				}
			}
		}

		String[] riskArray = dataSet.getDataHeaders()[classIndex]
				.getAvailableValue();
		List<String> returnList = new ArrayList<String>();

		for (String s : riskArray) {
			returnList.add(s);
		}

		return returnList;
	}

	private void thresholdOperators() {
		cmbThresholdOperator.addItem("Please select an operator");
		cmbThresholdOperator
				.addItem((String) (categorizer.core.ThresholdOperator.LT
						.operator())
						+ "         LESS THAN");
		cmbThresholdOperator
				.addItem((String) (categorizer.core.ThresholdOperator.GT
						.operator())
						+ "         GREATER THAN");
		cmbThresholdOperator
				.addItem((String) (categorizer.core.ThresholdOperator.LTE
						.operator())
						+ "       LESS THAN OR EQUAL");
		cmbThresholdOperator
				.addItem((String) (categorizer.core.ThresholdOperator.GTE
						.operator())
						+ "       GREATER THAN OR EQUAL");
		cmbThresholdOperator
				.addItem((String) (categorizer.core.ThresholdOperator.EQU
						.operator())
						+ "          EQUAL");
		cmbThresholdOperator
				.addItem((String) (categorizer.core.ThresholdOperator.BTW
						.operator())
						+ "       BETWEEN");
		cmbThresholdOperator.setSelectedIndex(0);
	}

	private void virtualMetricWizardFillComboBoxes() {
		txtVmMetricName.setText("");
		txtVmOperand1.setText("");
		txtVmOperand2.setText("");
		virtualMetricOperators();
		attributesComboBoxValues(activeDataSet(), cmbVmOperand1);
		attributesComboBoxValues(activeDataSet(), cmbVmOperand2);
		CardLayout cards = (CardLayout) (pnlVmCardLayout.getLayout());
		cards.show(pnlVmCardLayout, "card3");
		btnVmNext.setText("Next");
	}

	private void virtualMetricOperators() {
		cmbVmMetricOperator.addItem("Please select an operator");
		cmbVmMetricOperator
				.addItem((String) (categorizer.core.MetricOperator.ADD
						.operator())
						+ "    ADD");
		cmbVmMetricOperator
				.addItem((String) (categorizer.core.MetricOperator.SUB
						.operator())
						+ "    SUBTRACT");
		cmbVmMetricOperator
				.addItem((String) (categorizer.core.MetricOperator.MUL
						.operator())
						+ "    MULTIPLICATE");
		cmbVmMetricOperator
				.addItem((String) (categorizer.core.MetricOperator.DIV
						.operator())
						+ "    DIVIDE");
		cmbVmMetricOperator
				.addItem((String) (categorizer.core.MetricOperator.POW
						.operator())
						+ "    POWER OF");
		cmbVmMetricOperator
				.addItem((String) (categorizer.core.MetricOperator.EXP
						.operator())
						+ "    EXPONENT OF");
		cmbVmMetricOperator
				.addItem((String) (categorizer.core.MetricOperator.LOG
						.operator())
						+ "    LOGARITHM OF");
		cmbVmMetricOperator
				.addItem((String) (categorizer.core.MetricOperator.NOT
						.operator())
						+ "    NOT");
		cmbVmMetricOperator.setSelectedIndex(0);
	}

	private void attributesComboBoxValues(DataSet dataSet, JComboBox comboBox) {
		List<String> dataHeadersArray = new ArrayList<String>();
		dataHeadersArray.add("Please select a metric");
		for (DataHeader dh : dataSet.getDataHeaders()) {
			dataHeadersArray.add(dh.getLabel());
		}
		comboBox.setModel(new DefaultComboBoxModel(dataHeadersArray.toArray()));
		comboBox.setSelectedIndex(0);
	}

	private void attributesComboBoxValuesWithSelected(DataSet dataSet,
			JComboBox comboBox, String label) {
		List<String> dataHeadersArray = new ArrayList<String>();
		dataHeadersArray.add("Please select a metric");
		int index = 0;
		int selectedIndex = 0;
		for (DataHeader dh : dataSet.getDataHeaders()) {
			dataHeadersArray.add(dh.getLabel());
			if (dh.getLabel().equals(label)) {
				selectedIndex = index;
			}
			index++;
		}
		comboBox.setModel(new DefaultComboBoxModel(dataHeadersArray.toArray()));
		comboBox.setSelectedIndex(selectedIndex);
	}

	private void thresholdOperand2Visibility() {
		if (cmbThresholdOperator.getSelectedIndex() == 6) {
			lblThresholdOperand2.setEnabled(true);
			rbtnThresholdOperator2Txt.setEnabled(true);
			txtThresholdOperator2.setEnabled(true);
			rbtnThresholdOperator2Cmb.setEnabled(true);
			cmbThresholdOperator2.setEnabled(false);
		} else {
			lblThresholdOperand2.setEnabled(false);
			rbtnThresholdOperator2Txt.setEnabled(false);
			txtThresholdOperator2.setEnabled(false);
			rbtnThresholdOperator2Cmb.setEnabled(false);
			cmbThresholdOperator2.setEnabled(false);
		}

	}

	private void virtualMetricOperand2Visibility() {
		if (cmbVmMetricOperator.getSelectedIndex() == 0
				|| cmbVmMetricOperator.getSelectedIndex() == 6
				|| cmbVmMetricOperator.getSelectedIndex() == 7
				|| cmbVmMetricOperator.getSelectedIndex() == 8) {
			lblVmOperand2.setEnabled(false);
			rbtnVmOperand2Txt.setEnabled(false);
			txtVmOperand2.setEnabled(false);
			rbtnVmOperand2Cmb.setEnabled(false);
			cmbVmOperand2.setEnabled(false);
		} else {
			lblVmOperand2.setEnabled(true);
			rbtnVmOperand2Txt.setEnabled(true);
			txtVmOperand2.setEnabled(true);
			rbtnVmOperand2Cmb.setEnabled(true);
			cmbVmOperand2.setEnabled(false);
		}
	}

	private void createNewThreshold(JPanel thresholdPanel, DataSet dataSet) {
		DataHeader attributeHeader = dataSet
				.getDataHeader(cmbThresholdMetricList.getSelectedItem()
						.toString());

		Threshold threshold = new Threshold();
		threshold.setDataHeader(attributeHeader);
		threshold.setOperands(thresholdOperands);
		threshold.setOperator(thresholdOperator);
		threshold.setClassValue(cmbRiskLevels.getSelectedItem().toString());

		attributeHeader.addThreshold(threshold);

		displayThresholds(thresholdPanel, dataSet);
	}

	private void displayThresholds(JPanel panel, DataSet dataSet) {
		panel.removeAll();

		List<ThresholdContent> thresholdList = new ArrayList<ThresholdContent>();

		HashMap hashMap = dataSet.getThresholds();
		DataHeader[] dataHeaders = dataSet.getDataHeaders();
		for (DataHeader dataHeader : dataHeaders) {
			if (hashMap.get(dataHeader.getLabel()) != null) {
				Threshold[] thresholds = (Threshold[]) hashMap.get(dataHeader
						.getLabel());
				for (Threshold threshold : thresholds) {
					ThresholdContent thresholdContent = new ThresholdContent();
					thresholdContent.setThresholdDataHeader(dataHeader
							.getLabel());
					thresholdContent.setThresholdOperator(threshold
							.getOperator().operator());
					if (threshold.getOperands()[0].isDataHeader())
						thresholdContent
								.setThresholdFirstOperand(((DataHeader) (threshold
										.getOperands()[0].getOperandValue()))
										.getLabel());
					else
						thresholdContent.setThresholdFirstOperand(threshold
								.getOperands()[0].getOperandValue().toString());
					if (thresholdContent.getThresholdOperator().equals("<>")) {
						if (threshold.getOperands()[1].isDataHeader())
							thresholdContent
									.setThresholdSecondOperand(((DataHeader) (threshold
											.getOperands()[1].getOperandValue()))
											.getLabel());
						else
							thresholdContent
									.setThresholdSecondOperand(threshold
											.getOperands()[1].getOperandValue()
											.toString());
					}
					thresholdContent.setThresholdRiskLevel(threshold
							.getClassValue());

					thresholdList.add(thresholdContent);
				}
			}
		}

		ThresholdTableModel thresholdTableModel = new ThresholdTableModel(
				thresholdList);
		JTable table = new JTable(thresholdTableModel);

		ThresholdTableMouseListener mouseListener = new ThresholdTableMouseListener(
				table);
		table.addMouseListener(mouseListener);

    	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoCreateRowSorter(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(table);
		scrollPane.setVisible(true);
		javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
		panel.setLayout(null);
		panelLayout.setHorizontalGroup(panelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				panelLayout.createSequentialGroup().addContainerGap()
						.addComponent(scrollPane,
								javax.swing.GroupLayout.DEFAULT_SIZE, 500,
								Short.MAX_VALUE)));
		panelLayout.setVerticalGroup(panelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				panelLayout.createSequentialGroup().addContainerGap()
						.addComponent(scrollPane,
								javax.swing.GroupLayout.DEFAULT_SIZE, 500,
								Short.MAX_VALUE)));
		panel.setLayout(panelLayout);
		thresholdTableModel.fireTableDataChanged();
		panel.repaint();
		activeTabbedPane().setSelectedIndex(1);
	}

	@Action
	public void thresholdWizardNextButton() {
		doubleOperands = false;

		if (btnThresholdWizardNext.getText().equals("Finish")) {
			createNewThreshold(activeThresholdPanel(), activeDataSet());
			btnCategorize.setVisible(true);
			thresholdWizardDialog.dispose();
		} else {
			thresholdOperands = new Operand[2];

			String expression = "";
			String operator = "";
			thresholdSelectedMetric = cmbThresholdMetricList.getSelectedItem()
					.toString();

			switch (cmbThresholdOperator.getSelectedIndex()) {
			case 1:
				thresholdOperator = ThresholdOperator.LT;
				operator = " < ";
				break;
			case 2:
				thresholdOperator = ThresholdOperator.GT;
				operator = " > ";
				break;
			case 3:
				thresholdOperator = ThresholdOperator.LTE;
				operator = " <= ";
				break;
			case 4:
				thresholdOperator = ThresholdOperator.GTE;
				operator = " >= ";
				break;
			case 5:
				thresholdOperator = ThresholdOperator.EQU;
				operator = "=";
				break;
			case 6:
				thresholdOperator = ThresholdOperator.BTW;
				// expression = thresholdOperand1 + " <= " +
				// thresholdSelectedMetric + " <= " + thresholdOperand2;
				doubleOperands = true;
				break;
			}

			if (rbtnThresholdOperatorOneTxt.isSelected()) {
				thresholdOperand1 = txtThresholdOperator1.getText();
				thresholdOperands[0] = new Operand(false, Double
						.parseDouble(thresholdOperand1));
			} else {
				thresholdOperand1 = cmbThresholdOperator1.getSelectedItem()
						.toString();
				thresholdOperands[0] = new Operand(true, activeDataSet()
						.getDataHeader(thresholdOperand1));
			}

			if (doubleOperands) {
				if (rbtnThresholdOperator2Txt.isSelected()) {
					thresholdOperand2 = txtThresholdOperator2.getText();
					thresholdOperands[1] = new Operand(false, Double
							.parseDouble(thresholdOperand2));
				} else {
					thresholdOperand2 = cmbThresholdOperator2.getSelectedItem()
							.toString();
					thresholdOperands[1] = new Operand(true, activeDataSet()
							.getDataHeader(thresholdOperand2));
				}

				expression = thresholdOperand1 + " <= "
						+ thresholdSelectedMetric + " <= " + thresholdOperand2;
			} else
				expression = thresholdSelectedMetric + operator
						+ thresholdOperand1;

			lblThresholdExpression.setText(expression);
			lblThresholdRiskLevel.setText("Risk Level: "
					+ cmbRiskLevels.getSelectedItem().toString());
			CardLayout cards = (CardLayout) (pnlThresholdSettingsCardLayout
					.getLayout());
			cards.show(pnlThresholdSettingsCardLayout, "card3");
			btnThresholdWizardBack.setEnabled(true);
			btnThresholdWizardNext.setText("Finish");
		}
	}

	@Action
	public void thresholdWizardBackButton() {

		CardLayout cards = (CardLayout) (pnlThresholdSettingsCardLayout
				.getLayout());
		cards.show(pnlThresholdSettingsCardLayout, "card2");
		btnThresholdWizardBack.setEnabled(false);
		btnThresholdWizardNext.setText("Next");

	}

	private void addNewMetricToTable(JPanel panel, DataSet dataSet) {
		VirtualMetric virtualMetric = new VirtualMetric(virtualMetricOperator,
				virtualMetricName, virtualMetricOperands);
		dataSet = virtualMetric.modifySet(dataSet);
		packageExplorer.displayMetrics(panel, dataSet);
	}

	@Action
	public void virtualMetricWizardNext() {
		if (btnVmNext.getText().equals("Finish")) {
			addNewMetricToTable(activeDataSetPanel(), activeDataSet());
			virtualMetricWizardDialog.dispose();
		} else {
			virtualMetricOperands = new Operand[2];
			virtualMetricName = txtVmMetricName.getText();
			lblVirtualMetricName.setText(virtualMetricName);

			String expression = "";

			if (rbtnVmOperand1Txt.isSelected()) {
				virtualMetricOperand1 = txtVmOperand1.getText();
				virtualMetricOperands[0] = new Operand(false, Double
						.parseDouble(virtualMetricOperand1));
			} else {
				virtualMetricOperand1 = cmbVmOperand1.getSelectedItem()
						.toString();
				virtualMetricOperands[0] = new Operand(true,
						virtualMetricOperand1);
				// virtualMetricOperands[0] = new
				// Operand(true,activeDataSet().getDataHeader(
				// virtualMetricOperand1));
			}

			if (rbtnVmOperand2Txt.isSelected()) {
				virtualMetricOperand2 = txtVmOperand2.getText();
				virtualMetricOperands[1] = new Operand(false, Double
						.parseDouble(virtualMetricOperand2));
			} else {
				virtualMetricOperand2 = cmbVmOperand2.getSelectedItem()
						.toString();
				virtualMetricOperands[1] = new Operand(true,
						virtualMetricOperand2);
				// virtualMetricOperands[1] = new
				// Operand(true,activeDataSet().getDataHeader(
				// virtualMetricOperand2));
			}

			switch (cmbVmMetricOperator.getSelectedIndex()) {
			case 1:
				virtualMetricOperator = MetricOperator.ADD;
				expression = virtualMetricOperand1 + " + "
						+ virtualMetricOperand2;
				break;
			case 2:
				virtualMetricOperator = MetricOperator.SUB;
				expression = virtualMetricOperand1 + " - "
						+ virtualMetricOperand2;
				break;
			case 3:
				virtualMetricOperator = MetricOperator.MUL;
				expression = virtualMetricOperand1 + " * "
						+ virtualMetricOperand2;
				break;
			case 4:
				virtualMetricOperator = MetricOperator.DIV;
				expression = virtualMetricOperand1 + " / "
						+ virtualMetricOperand2;
				break;
			case 5:
				virtualMetricOperator = MetricOperator.POW;
				expression = virtualMetricOperand1 + " ^ "
						+ virtualMetricOperand2;
				break;
			case 6:
				virtualMetricOperator = MetricOperator.EXP;
				expression = "exp(" + virtualMetricOperand1 + ")";
				break;
			case 7:
				virtualMetricOperator = MetricOperator.LOG;
				expression = "log(" + virtualMetricOperand1 + ")";
				break;
			case 8:
				virtualMetricOperator = MetricOperator.NOT;
				expression = "not(" + virtualMetricOperand1 + ")";
				break;
			}

			lblVirtualMetricEquation.setText(expression);

			CardLayout cards = (CardLayout) (pnlVmCardLayout.getLayout());
			cards.show(pnlVmCardLayout, "card2");
			btnVmBack.setEnabled(true);
			btnVmNext.setText("Finish");
		}
	}

	@Action
	public void vitualMetricBack() {
		CardLayout cards = (CardLayout) (pnlVmCardLayout.getLayout());
		cards.show(pnlVmCardLayout, "card3");
		btnVmBack.setEnabled(false);
		btnVmNext.setText("Next");
	}

	@Action
	public void closeVirtualMetricDialog() {
		if (virtualMetricWizardDialog != null) {
			virtualMetricWizardDialog.dispose();
		}
	}

	@Action
	public void closeThresholdWizardDialog() {
		if (thresholdWizardDialog != null) {
			thresholdWizardDialog.dispose();
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Filter Operations">
	@Action
	public void transferHeadersToFilter() {
		int[] indices = listAllMetrics.getSelectedIndices();
		for (int i = indices.length - 1; i >= 0; i--) {
			String header = (String) leftListModel.get(indices[i]);
			leftListModel.remove(indices[i]);
			rightListModel.addElement(header);
		}
	}

	@Action
	public void transferHeadersToLeft() {
		int[] indices = listFilterMetrics.getSelectedIndices();
		for (int i = indices.length - 1; i >= 0; i--) {
			String header = (String) rightListModel.get(indices[i]);
			rightListModel.remove(indices[i]);
			leftListModel.addElement(header);
		}

	}

	@Action
	public void transferAllHeadersToFilter() {
		for (int i = leftListModel.getSize() - 1; i >= 0; i--) {
			rightListModel.addElement(leftListModel.remove(i));
		}
	}

	@Action
	public void transferAllHeadersToLeft() {
		for (int i = rightListModel.getSize() - 1; i >= 0; i--) {
			leftListModel.addElement(rightListModel.remove(i));
		}
	}

	@Action
	public void defineNewFilter() {
		if (packageExplorer.isResultsTransferred()) {
			filterWizardFillHeaderList();
			filterWizardDialog.pack();
			filterWizardDialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null,
					"Please select a project to add threshold",
					"Select Project", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void filterWizardFillHeaderList() {
		leftListModel = new DefaultListModel();
		listAllMetrics.setModel(leftListModel);

		rightListModel = new DefaultListModel();
		listFilterMetrics.setModel(rightListModel);

		DataHeader[] dataHeaders = activeDataSet().getDataHeaders();
		for (DataHeader dataHeader : dataHeaders) {
			leftListModel.addElement(dataHeader.getLabel());
		}
	}

	@Action
	public void filterWizardCancelButton() {
		if (filterWizardDialog != null) {
			filterWizardDialog.dispose();
		}
	}

	@Action
	public void filterWizardApplyButton() {
		DataSet dataSet = activeDataSet();
		LogFilter logFilter;

		for (int i = rightListModel.getSize() - 1; i >= 0; i--) {
			try {
				Option[] options = new Option[1];
				options[0] = new Option();
				options[0].setValue(String
						.valueOf(dataSet.getDataHeaderIndex(rightListModel
								.remove(i).toString())));

				logFilter = new LogFilter(options);
				dataSet = logFilter.filter(dataSet);
			} catch (Exception ex) {
				Logger.getLogger(PrestGuiView.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}

		filterWizardDialog.dispose();
		packageExplorer.displayMetrics(activeDataSetPanel(), activeDataSet());
	}

	@Action
	public void createClassifierWizard() {
		JDialog frame = new JDialog(null, "Categorizer Definition Wizard",
				Dialog.ModalityType.APPLICATION_MODAL);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		DynamicCategorizerWizard catWizard = new DynamicCategorizerWizard(frame);
		frame.getContentPane().add(catWizard.getSplitPane());
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	@Action
	public void openTrainingFile() {
		JFileChooser fileChooser = new JFileChooser();
		File currentDirectory;
		try {
			currentDirectory = new File(ApplicationProperties
					.get("repositorylocation"));

			if (currentDirectory != null) {
				fileChooser.setCurrentDirectory(currentDirectory);
				fileChooser.setDialogTitle("Select arff file");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				ExtensionFileFilter fileFilter = new ExtensionFileFilter(
						"arff files only", "ARFF");
				fileChooser.setFileFilter(fileFilter);
				int returnVal = fileChooser.showOpenDialog(null);

				File dir = null;

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					dir = fileChooser.getSelectedFile();
				}
				try {
					trainingSetPath = dir.getAbsolutePath();
					jTextArea1.append("\nTraining File Loaded : " +dir.getAbsolutePath());
				} catch (Exception ex) {
					Logger.getLogger(PrestGuiView.class.getName()).log(
							Level.SEVERE, null, ex);
				} 
			} 
		} catch (Exception e1) {
			//do something
		}
	}
	
	@Action
	public void openTestFile() {
		JFileChooser fileChooser = new JFileChooser();
		File currentDirectory;
		try {
			currentDirectory = new File(ApplicationProperties
					.get("repositorylocation"));

			if (currentDirectory != null) {
				fileChooser.setCurrentDirectory(currentDirectory);
				fileChooser.setDialogTitle("Select arff file");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				ExtensionFileFilter fileFilter = new ExtensionFileFilter(
						"arff files only", "ARFF");
				fileChooser.setFileFilter(fileFilter);
				int returnVal = fileChooser.showOpenDialog(null);

				File dir = null;

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					dir = fileChooser.getSelectedFile();
				}
				try {
					testSetPath = dir.getAbsolutePath();
					jTextArea1.append("\nTest File Loaded : " +dir.getAbsolutePath());
				} catch (Exception ex) {
					Logger.getLogger(PrestGuiView.class.getName()).log(
							Level.SEVERE, null, ex);
				} 
			} 
		} catch (Exception e1) {
			//do something
		}
	}
	
	@Action
	public void startWeka() {
		String output  = "";
		if(((testSetPath == null || trainingSetPath == null) &&  wekaCrossValidate == "false") ||
				(testSetPath == null && trainingSetPath == null &&  wekaCrossValidate == "true")){
			JOptionPane.showMessageDialog(null,
					"Load training and test sets first.", "Store Result",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			output = WekaRunner.runWeka(trainingSetPath,testSetPath,wekaAlgorithmType,wekaPreProcess,wekaCrossValidate);
			jTextArea1.append("\n" +output);
		}
	}

	// </editor-fold>

	public class CategorizerListMouseListener extends MouseAdapter {

		public CategorizerListMouseListener() {
			super();
		}

		public void mouseReleased(MouseEvent e) {
			JList source = (JList) e.getSource();
			int selectedIndex = source.getSelectedIndex();
			String displayText = "";
			if (selectedIndex != -1) {
				Categorizer selectedCategorizer = (Categorizer) ParserExecutor
						.getCategorizerExecutor().getCategorizers().get(
								selectedIndex);
				if (selectedCategorizer != null) {
					displayText = selectedCategorizer.getConfusionMatrix()
							.toString();
					PerformanceMetric[] performanceMetrics = selectedCategorizer
							.getPerformanceMetrics();
					if (performanceMetrics != null) {
						for (int i = 0; i < performanceMetrics.length; i++) {
							try {
								displayText += "\n"
										+ performanceMetrics[i].toString();
							} catch (Exception ex) {

							}
						}
					}
					Components.confuseMatrixDisplayArea.setText(displayText);

					if (e.isPopupTrigger()) {
						JPopupMenu popup = new JPopupMenu();
						JMenuItem storeCategorizer = new JMenuItem("Store");
						storeCategorizer
								.addActionListener(new StoreCategorizerAdapter(
										selectedCategorizer));
						popup.add(storeCategorizer);
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		}
	}

	public class StoreCategorizerAdapter implements ActionListener {

		private Categorizer categorizer;

		public StoreCategorizerAdapter(Categorizer categorizer) {
			this.categorizer = categorizer;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				DataContext context = new DataContext();
				categorizer.setTitle("categorizerai");
				context.add("categorizerai", categorizer.store());
				context.writeToFile(ApplicationProperties
						.get("repositorylocation")
						+ "\\"
						+ packageExplorer.getProjectDirectory().getName()
						+ "\\categorizer_"
						+ categorizer.getClassName()
						+ ".xml");
				JOptionPane.showMessageDialog(null,
						"Categorizer stored successfully!", "Store Result",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				Logger.getLogger(PrestGuiView.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}

		public Categorizer getCategorizer() {
			return categorizer;
		}

		public void setCategorizer(Categorizer categorizer) {
			this.categorizer = categorizer;
		}

	}

    @Action
    public void crossValidateAction() {
    	if(chkboxCrossValidate.isSelected())
    		wekaCrossValidate = "true";
    	else
    		wekaCrossValidate = "false";
    }
    
    @Action
    public void normalizeDataAction() {
    	if(jCheckBoxNormalize.isSelected())
    		wekaPreProcess = "normalize";
    	else
    		wekaPreProcess = "false";
    }

	// <editor-fold defaultstate="collapsed" desc="Generated Variable
	// Declaration">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addProjectMenuItem;
    private javax.swing.JPanel analysisResultsPanel;
    private javax.swing.JMenu analyzeMenu;
    private javax.swing.JButton btnApplyFilter;
    private javax.swing.JButton btnCancelFilter;
    private javax.swing.JButton btnCategorize;
    private javax.swing.JButton btnLoadCategorizer;
    private javax.swing.JButton btnLoadTest;
    private javax.swing.JButton btnLoadTraining;
    private javax.swing.JButton btnStartWeka;
    private javax.swing.JButton btnStoreCategorizer;
    private javax.swing.JButton btnThresholdWizardBack;
    private javax.swing.JButton btnThresholdWizardCancel;
    private javax.swing.JButton btnThresholdWizardNext;
    private javax.swing.JButton btnTransferAllToFilter;
    private javax.swing.JButton btnTransferAllToLeft;
    private javax.swing.JButton btnTransferLeft;
    private javax.swing.JButton btnTransferToFilter;
    private javax.swing.JButton btnVmBack;
    private javax.swing.JButton btnVmNext;
    private javax.swing.ButtonGroup btngFirstOperand;
    private javax.swing.ButtonGroup btngLanguageGroup;
    private javax.swing.ButtonGroup btngRiskLevel;
    private javax.swing.ButtonGroup btngSecondOperand;
    private javax.swing.ButtonGroup btngVmOperand1;
    private javax.swing.ButtonGroup btngVmOperand2;
    private javax.swing.JButton cancelVirtualMetricButton;
    private javax.swing.JCheckBox chkboxCrossValidate;
    private javax.swing.JPanel classMetricsDataSetPanel;
    private javax.swing.JTabbedPane classMetricsTabbedPane;
    private javax.swing.JPanel classMetricsThresholdPanel;
    private javax.swing.JComboBox cmbMetricGroups;
    private javax.swing.JComboBox cmbRiskLevels;
    private javax.swing.JComboBox cmbThresholdMetricList;
    private javax.swing.JComboBox cmbThresholdOperator;
    private javax.swing.JComboBox cmbThresholdOperator1;
    private javax.swing.JComboBox cmbThresholdOperator2;
    private javax.swing.JComboBox cmbVmMetricOperator;
    private javax.swing.JComboBox cmbVmOperand1;
    private javax.swing.JComboBox cmbVmOperand2;
    private javax.swing.JPanel fileMetricsDataSetPanel;
    private javax.swing.JTabbedPane fileMetricsTabbedPane;
    private javax.swing.JPanel fileMetricsThresholdPanel;
    private javax.swing.JMenuItem filterMenuItem;
    private javax.swing.JDialog filterWizardDialog;
    private javax.swing.JCheckBox jCheckBoxNormalize;
    private javax.swing.JComboBox jComboBoxChooseAlgorithm;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lblThresholdExpression;
    private javax.swing.JLabel lblThresholdOperand2;
    private javax.swing.JLabel lblThresholdRiskLevel;
    private javax.swing.JLabel lblVirtualMetricEquation;
    private javax.swing.JLabel lblVirtualMetricName;
    private javax.swing.JLabel lblVmOperand2;
    private javax.swing.JList listAllMetrics;
    private javax.swing.JList listFilterMetrics;
    private javax.swing.JSplitPane mainContentSplitPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel methodMetricsDataSetPanel;
    private javax.swing.JTabbedPane methodMetricsTabbedPane;
    private javax.swing.JPanel methodMetricsThresholdPanel;
    private javax.swing.JPanel metricsCardLayoutPanel;
    private javax.swing.JPanel packageMetricsDataSetPanel;
    private javax.swing.JTabbedPane packageMetricsTabbedPane;
    private javax.swing.JPanel packageMetricsThresholdPanel;
    private javax.swing.JPanel pnlCategorizeButtons;
    private javax.swing.JPanel pnlDataCardLayout;
    private javax.swing.JPanel pnlDataFile;
    private javax.swing.JPanel pnlDataFileAnalyze;
    private javax.swing.JPanel pnlDataTab;
    private javax.swing.JPanel pnlFilterButtons;
    private javax.swing.JPanel pnlFilterHeader;
    private javax.swing.JPanel pnlFilterMain;
    private javax.swing.JPanel pnlLanguageRadioPanel;
    private javax.swing.JPanel pnlParseResultsAnalyze;
    private javax.swing.JPanel pnlThresholdApprove;
    private javax.swing.JPanel pnlThresholdSettings;
    private javax.swing.JPanel pnlThresholdSettingsCardLayout;
    private javax.swing.JPanel pnlThresholdWizardButtons;
    private javax.swing.JPanel pnlThresholdWizardHeader;
    private javax.swing.JPanel pnlVmApproval;
    private javax.swing.JPanel pnlVmButtons;
    private javax.swing.JPanel pnlVmCardLayout;
    private javax.swing.JPanel pnlVmHeader;
    private javax.swing.JPanel pnlVmSettings;
    private javax.swing.JPanel pnlthresholdWizardMain;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JRadioButton rbtnThresholdOperator1Cmb;
    private javax.swing.JRadioButton rbtnThresholdOperator2Cmb;
    private javax.swing.JRadioButton rbtnThresholdOperator2Txt;
    private javax.swing.JRadioButton rbtnThresholdOperatorOneTxt;
    private javax.swing.JRadioButton rbtnVmOperand1Cmb;
    private javax.swing.JRadioButton rbtnVmOperand1Txt;
    private javax.swing.JRadioButton rbtnVmOperand2Cmb;
    private javax.swing.JRadioButton rbtnVmOperand2Txt;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem switchWorkspaceMenuItem;
    private javax.swing.JTabbedPane tabPaneAnalysisResults;
    private javax.swing.JTabbedPane tabPaneDataFileAnalyze;
    private javax.swing.ButtonGroup testOptionsButtonGroup;
    private javax.swing.JMenuItem thresholdMenuItem;
    private javax.swing.JDialog thresholdWizardDialog;
    private javax.swing.JTextField txtThresholdOperator1;
    private javax.swing.JTextField txtThresholdOperator2;
    private javax.swing.JTextField txtVmMetricName;
    private javax.swing.JTextField txtVmOperand1;
    private javax.swing.JTextField txtVmOperand2;
    private javax.swing.JMenuItem virtualMetricMenuItem;
    private javax.swing.JDialog virtualMetricWizardDialog;
    // End of variables declaration//GEN-END:variables
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Added variable declarations">
	private StaticCategorizer staticCategorizer;
	private DefaultListModel leftListModel;
	private DefaultListModel rightListModel;
	private boolean doubleOperands;
	private MetricOperator virtualMetricOperator;
	private String virtualMetricName;
	private Operand[] virtualMetricOperands;
	private String virtualMetricOperand1;
	private String virtualMetricOperand2;
	private ThresholdOperator thresholdOperator;
	private String thresholdSelectedMetric;
	private String thresholdOperand1;
	private String thresholdOperand2;
	private Operand[] thresholdOperands;
	private PackageExplorer packageExplorer;
	private DataHeader thresholdDataHeader;
	private Operand[] operands;
	private MetricOperator metricOperator;
	private List<MetricGroup> packageMetrics;
	private List<MetricGroup> fileMetrics;
	private List<MetricGroup> classMetrics;
	private List<MetricGroup> methodMetrics;
	private DataSet arffDataSet;
	private CategorizerExecutor categorizerExecutor;
	private String trainingSetPath;
	private String testSetPath;
	private String wekaAlgorithmType = "Naive Bayes";
	private String wekaCrossValidate = "false";
	private String wekaPreProcess = "false";
	private final Timer messageTimer;
	private final Timer busyIconTimer;
	private final Icon idleIcon;
	private final Icon[] busyIcons = new Icon[15];
	private int busyIconIndex = 0;
	private JDialog aboutBox;
	// </editor-fold>

	// SciDesktop Modification TA_R001	--- window listener methods start here 
	public void windowActivated(WindowEvent arg0)
	{
	}

	public void windowClosed(WindowEvent arg0)
	{
	}

	public void windowClosing(WindowEvent arg0)
	{
		terminate();
	}

	public void windowDeactivated(WindowEvent arg0)
	{
	}

	public void windowDeiconified(WindowEvent arg0)
	{
	}

	public void windowIconified(WindowEvent arg0)
	{
	}

	public void windowOpened(WindowEvent arg0)
	{
	}
}

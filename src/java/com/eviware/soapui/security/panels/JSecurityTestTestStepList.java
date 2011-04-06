/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXTree;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.actions.testsuite.AddNewTestCaseAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.actions.CloneParametersAction;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.AbstractSecurityCheckWithProperties;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.support.ProgressBarSecurityTestStepAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.AutoscrollSupport;
import com.eviware.soapui.support.swing.TreePathUtils;
import com.eviware.x.form.XFormDialog;

/**
 * A panel showing a scrollable list of TestSteps in a SecurityTest.
 * 
 * @author dragica.soldo
 */

@SuppressWarnings( "serial" )
public class JSecurityTestTestStepList extends JPanel implements TreeSelectionListener, MouseListener
{
	private Map<TestStep, TestStepListEntryPanel> panels = new HashMap<TestStep, TestStepListEntryPanel>();
	private SecurityTest securityTest;
	private final TestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private TestStepListEntryPanel selectedTestStep;
	private JList securityChecksList;
	private JSplitPane splitPane;
	private JComponent secCheckPanel;
	private JTree securityTestTree;
	private AddSecurityCheckAction addSecurityCheckAction;
	private ConfigureSecurityCheckAction configureSecurityCheckAction;
	private RemoveSecurityCheckAction removeSecurityCheckAction;
	private CloneParametersAction cloneParametersAction;

	private JSecurityTestRunLog securityTestLog;
	private JPopupMenu securityCheckPopUp;

	private JPopupMenu securityCheckWithPropertiesPopUp;
	private JPopupMenu testStepPopUp;
	private SecurityTreeCellRender cellRender;

	public JSecurityTestTestStepList( SecurityTest securityTest, JSecurityTestRunLog securityTestLog )
	{
		this.securityTest = securityTest;
		setLayout( new BorderLayout() );

		JXToolBar toolbar = initToolbar();

		securityCheckPopUp = new JPopupMenu();
		securityCheckPopUp.add( addSecurityCheckAction );
		securityCheckPopUp.add( configureSecurityCheckAction );
		securityCheckPopUp.addSeparator();
		securityCheckPopUp.add( removeSecurityCheckAction );
		securityCheckPopUp.add( new ShowOnlineHelpAction( HelpUrls.RESPONSE_ASSERTIONS_HELP_URL ) );

		securityCheckWithPropertiesPopUp = new JPopupMenu();
		securityCheckWithPropertiesPopUp.add( addSecurityCheckAction );
		securityCheckWithPropertiesPopUp.add( configureSecurityCheckAction );
		securityCheckWithPropertiesPopUp.add( cloneParametersAction );
		securityCheckWithPropertiesPopUp.addSeparator();
		securityCheckWithPropertiesPopUp.add( removeSecurityCheckAction );
		securityCheckWithPropertiesPopUp.add( new ShowOnlineHelpAction( HelpUrls.RESPONSE_ASSERTIONS_HELP_URL ) );

		testStepPopUp = new JPopupMenu();
		initTestStepPopUpActions();
		testStepPopUp.addSeparator();
		testStepPopUp.add( new ShowOnlineHelpAction( HelpUrls.RESPONSE_ASSERTIONS_HELP_URL ) );

		securityTestTree = new JXTree( new SecurityCheckTree( securityTest ) );
		securityTestTree.putClientProperty( "JTree.lineStyle", "None" );
		securityTestTree.setUI( new CustomTreeUI() );
		securityTestTree.setRootVisible( false );
		securityTestTree.setLargeModel( true );
		cellRender = new SecurityTreeCellRender();
		securityTestTree.setCellRenderer( cellRender );
		securityTestTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
		securityTestTree.addTreeSelectionListener( this );
		securityTestTree.addMouseListener( this );
		securityTestTree.setRowHeight( 30 );
		securityTestTree.setToggleClickCount( 0 );
		securityTestTree.setBackground( new Color( 240, 240, 240 ) );
		add( toolbar, BorderLayout.NORTH );
		JScrollPane scollPane = new JScrollPane( securityTestTree );
		add( scollPane, BorderLayout.CENTER );
		securityTest.getTestCase().getTestSuite().addTestSuiteListener( testSuiteListener );

		for( int row = 0; row < securityTestTree.getRowCount(); row++ )
		{
			securityTestTree.expandRow( row );
		}
		this.securityTestLog = securityTestLog;

	}

	protected SecurityTest getSecurityTest()
	{
		return securityTest;
	}

	protected void setSecurityTest( SecurityTest securityTest )
	{
		this.securityTest = securityTest;
	}

	protected JPopupMenu getTestStepPopUp()
	{
		return testStepPopUp;
	}

	protected void initTestStepPopUpActions()
	{
		testStepPopUp.add( addSecurityCheckAction );
	}

	protected JPopupMenu getSecurityCheckPopUp()
	{
		return securityCheckPopUp;
	}

	private JXToolBar initToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		initToolbarLeft( toolbar );

		JButton expandActionBtn = UISupport.createToolbarButton( new ExpandTreeAction() );
		expandActionBtn.setText( "Expanded" );
		expandActionBtn.setPreferredSize( new Dimension( 80, 21 ) );
		JButton collapsActionBtn = UISupport.createToolbarButton( new CollapsTreeAction() );
		collapsActionBtn.setText( "Collapsed" );
		collapsActionBtn.setPreferredSize( new Dimension( 80, 21 ) );
		toolbar.addGlue();
		toolbar.add( expandActionBtn );
		toolbar.add( collapsActionBtn );

		return toolbar;
	}

	protected void initToolbarLeft( JXToolBar toolbar )
	{
		addSecurityCheckAction = new AddSecurityCheckAction();
		configureSecurityCheckAction = new ConfigureSecurityCheckAction();
		removeSecurityCheckAction = new RemoveSecurityCheckAction();
		cloneParametersAction = new CloneParametersAction();

		toolbar.addFixed( UISupport.createToolbarButton( addSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( configureSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( removeSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( cloneParametersAction ) );
	}

	public SecurityCheck getCurrentSecurityCheck()
	{
		int ix = securityChecksList.getSelectedIndex();
		return ix == -1 ? null : securityTest.getTestStepSecurityCheckAt( selectedTestStep.getTestStep().getId(), ix );
	}

	protected JPanel buildSecurityChecksPanel()
	{
		if( selectedTestStep != null && AbstractSecurityCheck.isSecurable( selectedTestStep.getTestStep() ) )
		{
			return new SecurityChecksPanel( selectedTestStep.getTestStep(), securityTest, securityTestLog );
		}
		else
		{
			return new JPanel();
		}
	}

	protected JComponent buildSecurityChecksInspector()
	{
		JPanel p = new JPanel( new BorderLayout() );
		return p;
	}

	public void reset()
	{
		for( TestStepListEntryPanel testStepPanel : panels.values() )
		{
			if( AbstractSecurityCheck.isSecurable( testStepPanel.getTestStep() ) )
			{
				testStepPanel.reset();
			}
		}
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		securityTest.getTestCase().getTestSuite().addTestSuiteListener( testSuiteListener );
	}

	@Override
	public void removeNotify()
	{
		super.removeNotify();
		securityTest.getTestCase().getTestSuite().removeTestSuiteListener( testSuiteListener );
	}

	private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		@Override
		public void testStepAdded( TestStep testStep, int index )
		{
			( ( SecurityCheckTree )securityTestTree.getModel() ).insertNodeInto( testStep );
		}

		@Override
		public void testStepRemoved( TestStep testStep, int index )
		{
			TestStepNode node = ( ( SecurityCheckTree )securityTestTree.getModel() ).getTestStepNode( testStep );
			cellRender.remove( node );
			( ( SecurityCheckTree )securityTestTree.getModel() ).removeNodeFromParent( node );
		}

		@Override
		public void testStepMoved( TestStep testStep, int index, int offset )
		{
			// TestStepListEntryPanel testStepListEntry = panels.get( testStep );
			// if( testStepListEntry != null )
			// {
			// boolean hadFocus = testStepListEntry.hasFocus();
			//
			// testStepListPanel.remove( testStepListEntry );
			// testStepListPanel.add( testStepListEntry, index + offset );
			// splitPane.remove( splitPane.getTopComponent() );
			// splitPane.setTopComponent( new JScrollPane( testStepListPanel ) );
			//
			// revalidate();
			// repaint();
			//
			// if( hadFocus )
			// testStepListEntry.requestFocus();
			// }
		}
	}

	public final class TestStepListEntryPanel extends JPanel implements Autoscroll, PropertyChangeListener
	{
		private final WsdlTestStep testStep;
		private JProgressBar progressBar;
		private JLabel label;
		private ProgressBarSecurityTestStepAdapter progressBarAdapter;
		private TestCasePropertyChangeListener testCasePropertyChangeListener;
		private AutoscrollSupport autoscrollSupport;

		public TestStepListEntryPanel( WsdlTestStep testStep )
		{
			super( new BorderLayout() );

			setFocusable( true );

			this.testStep = testStep;
			autoscrollSupport = new AutoscrollSupport( this );
			label = new JLabel( testStep.getLabel(), SwingConstants.LEFT );
			label.setIcon( testStep.getIcon() );
			label.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
			label.setInheritsPopupMenu( true );
			label.setEnabled( !testStep.isDisabled() );
			testStep.addPropertyChangeListener( TestStep.ICON_PROPERTY, this );

			add( label, BorderLayout.LINE_START );

			if( AbstractSecurityCheck.isSecurable( testStep ) )
			{
				progressBar = new JProgressBar()
				{
					protected void processMouseEvent( MouseEvent e )
					{
						if( e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED )
						{
							TestStepListEntryPanel.this.processMouseEvent( translateMouseEvent( e ) );
						}
					}

					protected void processMouseMotionEvent( MouseEvent e )
					{
						TestStepListEntryPanel.this.processMouseMotionEvent( translateMouseEvent( e ) );
					}

					/**
					 * Translates the given mouse event to the enclosing map panel's
					 * coordinate space.
					 */
					private MouseEvent translateMouseEvent( MouseEvent e )
					{
						return new MouseEvent( TestStepListEntryPanel.this, e.getID(), e.getWhen(), e.getModifiers(),
								e.getX() + getX(), e.getY() + getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton() );
					}
				};

				JPanel progressPanel = UISupport.createProgressBarPanel( progressBar, 5, false );

				progressBar.setMinimumSize( new Dimension( 0, 200 ) );
				progressBar.setBackground( Color.WHITE );
				progressBar.setInheritsPopupMenu( true );

				add( progressPanel, BorderLayout.LINE_END );
			}

			testCasePropertyChangeListener = new TestCasePropertyChangeListener();

			// initPopup( testStep );

			addMouseListener( new MouseAdapter()
			{

				public void mouseClicked( MouseEvent e )
				{
					if( e.getClickCount() < 2 )
					{
						if( selectedTestStep != null )
							selectedTestStep.setSelected( false );

						selectedTestStep = TestStepListEntryPanel.this;
						requestFocus();
						splitPane.remove( secCheckPanel );
						secCheckPanel = buildSecurityChecksPanel();
						secCheckPanel.revalidate();
						splitPane.setBottomComponent( secCheckPanel );
						splitPane.revalidate();
						if( AbstractSecurityCheck.isSecurable( selectedTestStep.getTestStep() ) )
						{
							setSelected( true );
						}
					}
					else
					{
						UISupport.selectAndShow( TestStepListEntryPanel.this.testStep );
					}
				}
			} );

			addKeyListener( new TestCaseListPanelKeyHandler() );

			// init border
			setSelected( false );
		}

		public void reset()
		{
			progressBar.setValue( 0 );
			progressBar.setString( "" );
		}

		private void initPopup( WsdlTestStep testStep )
		{
			ActionList actions = ActionListBuilder.buildActions( testStep );
			actions
					.insertAction( SwingActionDelegate.createDelegate( AddNewTestCaseAction.SOAPUI_ACTION_ID, securityTest,
							null, null ), 0 );
			actions.insertAction( ActionSupport.SEPARATOR_ACTION, 1 );

			setComponentPopupMenu( ActionSupport.buildPopup( actions ) );
		}

		public void addNotify()
		{
			super.addNotify();
			testStep.addPropertyChangeListener( testCasePropertyChangeListener );
			if( progressBar != null )
			{
				// progressBarAdapter = new ProgressBarSecurityTestStepAdapter(
				// progressBar, securityTest, testStep );
			}
		}

		public void removeNotify()
		{
			super.removeNotify();
			if( progressBarAdapter != null )
			{
				testStep.removePropertyChangeListener( testCasePropertyChangeListener );
				progressBarAdapter.release();

				progressBarAdapter = null;
			}
		}

		public Dimension getMaximumSize()
		{
			Dimension size = super.getMaximumSize();
			size.height = 50;
			return size;
		}

		public void setSelected( boolean selected )
		{
			if( selected )
			{
				setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
			}
			else
			{
				setBorder( BorderFactory.createLineBorder( Color.WHITE ) );
			}
		}

		public boolean isSelected()
		{
			return selectedTestStep != null && selectedTestStep.getTestStep() == testStep;
		}

		private final class TestCasePropertyChangeListener implements PropertyChangeListener
		{
			public void propertyChange( PropertyChangeEvent evt )
			{
				if( evt.getPropertyName().equals( TestCase.LABEL_PROPERTY ) )
				{
					label.setEnabled( !testStep.isDisabled() );
					label.setText( testStep.getLabel() );
				}
				else if( evt.getPropertyName().equals( TestCase.DISABLED_PROPERTY ) )
				{
					initPopup( testStep );
				}
			}
		}

		protected TestStep getTestStep()
		{
			return testStep;
		}

		public ModelItem getModelItem()
		{
			return testStep;
		}

		public void autoscroll( Point pt )
		{
			int ix = getIndexOf( this );
			if( pt.getY() < 12 && ix > 0 )
			{
				Rectangle bounds = JSecurityTestTestStepList.this.getComponent( ix - 1 ).getBounds();
				JSecurityTestTestStepList.this.scrollRectToVisible( bounds );
			}
			else if( pt.getY() > getHeight() - 12 && ix < securityTest.getTestCase().getTestStepCount() - 1 )
			{
				Rectangle bounds = JSecurityTestTestStepList.this.getComponent( ix + 1 ).getBounds();
				JSecurityTestTestStepList.this.scrollRectToVisible( bounds );
			}
		}

		public Insets getAutoscrollInsets()
		{
			return autoscrollSupport.getAutoscrollInsets();
		}

		private final class TestCaseListPanelKeyHandler extends KeyAdapter
		{
			public void keyPressed( KeyEvent e )
			{
				if( e.getKeyChar() == KeyEvent.VK_ENTER )
				{
					UISupport.selectAndShow( testStep );
					e.consume();
				}
				else
				{
					ActionList actions = ActionListBuilder.buildActions( testStep );
					if( actions != null )
						actions.dispatchKeyEvent( e );
				}
			}
		}

		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			label.setIcon( testStep.getIcon() );
		}

		public void release()
		{
			testStep.removePropertyChangeListener( this );
		}
	}

	protected int getIndexOf( TestStepListEntryPanel panel )
	{
		return Arrays.asList( getComponents() ).indexOf( panel );
	}

	protected TestStepListEntryPanel createTestStepListPanel( TestStep testStep )
	{
		TestStepListEntryPanel testStepListPanel = new TestStepListEntryPanel( ( WsdlTestStep )testStep );

		return testStepListPanel;
	}

	public void release()
	{
		for( TestStepListEntryPanel testStepPanel : panels.values() )
		{
			testStepPanel.release();
		}
	}

	// toolbar actions
	public class AddSecurityCheckAction extends AbstractAction
	{
		public AddSecurityCheckAction()
		{
			super( "Add SecurityCheck" );

			putValue( Action.SHORT_DESCRIPTION, "Adds a security check to this item" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/addSecurityCheck.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			TestStepNode node = ( TestStepNode )securityTestTree.getLastSelectedPathComponent();
			if( !node.getAllowsChildren() )
				return;
			TestStep testStep = node.getTestStep();
			String[] availableChecksNames = SoapUI.getSoapUICore().getSecurityCheckRegistry()
					.getAvailableSecurityChecksNames( testStep );
			String type = UISupport.prompt( "Specify type of security check", "Add SecurityCheck", availableChecksNames );
			if( type == null || type.trim().length() == 0 )
				return;
			String name = UISupport.prompt( "Specify name for security check", "Add SecurityCheck",
					securityTest.findTestStepCheckUniqueName( testStep.getId(), type ) );
			if( name == null || name.trim().length() == 0 )
				return;

			while( securityTest.getTestStepSecurityCheckByName( testStep.getId(), name ) != null )
			{
				name = UISupport.prompt( "Specify unique name for check", "Add SecurityCheck",
						name + " " + ( securityTest.getTestStepSecurityChecks( testStep.getId() ).size() ) );
				if( name == null )
				{
					return;
				}
			}

			if( availableChecksNames == null || availableChecksNames.length == 0 )
			{
				UISupport.showErrorMessage( "No security checks available for this message" );
				return;
			}

			if( !securityTest.canAddSecurityCheck( testStep, type ) )
			{
				UISupport.showErrorMessage( "Security check type already exists" );
				return;
			}

			SecurityCheck securityCheck = securityTest.addSecurityCheck( testStep, type, name );

			if( securityCheck == null )
			{
				UISupport.showErrorMessage( "Failed to add security check" );
				return;
			}

			SecurityCheckNode newnode = new SecurityCheckNode( securityCheck );
			node.add( newnode );
			( ( SecurityCheckTree )securityTestTree.getModel() ).nodeStructureChanged( node );
			securityTestTree.setSelectionInterval( securityTestTree.getModel().getIndexOfChild( node, newnode ) + 1,
					securityTestTree.getModel().getIndexOfChild( node, newnode ) + 1 );

			XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
					.buildSecurityCheckConfigurationDialog( ( AbstractSecurityCheck )securityCheck );

			dialog.show();

		}

	}

	public class ConfigureSecurityCheckAction extends AbstractAction
	{
		ConfigureSecurityCheckAction()
		{
			super( "Configure" );
			putValue( Action.SHORT_DESCRIPTION, "Configures selected security check" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/options.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			SecurityCheckNode node = ( SecurityCheckNode )securityTestTree.getLastSelectedPathComponent();
			SecurityCheck securityCheck = node.getSecurityCheck();

			if( securityCheck.isConfigurable() )
			{
				XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
						.buildSecurityCheckConfigurationDialog( ( AbstractSecurityCheck )securityCheck );

				dialog.show();
			}
		}
	}

	public class RemoveSecurityCheckAction extends AbstractAction
	{
		public RemoveSecurityCheckAction()
		{
			super( "Remove SecurityCheck" );
			putValue( Action.SHORT_DESCRIPTION, "Removes the selected security check" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_securityCheck.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			SecurityCheckNode node = ( SecurityCheckNode )securityTestTree.getLastSelectedPathComponent();
			SecurityCheck securityCheck = node.getSecurityCheck();

			TestStep testStep = ( ( TestStepNode )node.getParent() ).getTestStep();
			if( UISupport.confirm( "Remove security check [" + securityCheck.getName() + "]", "Remove SecurityCheck" ) )
			{

				securityTest.removeSecurityCheck( testStep, ( AbstractSecurityCheck )securityCheck );
				cellRender.remove( node );
				( ( SecurityCheckTree )securityTestTree.getModel() ).removeNodeFromParent( node );
			}
		}
	}

	public class ExpandTreeAction extends AbstractAction
	{
		public ExpandTreeAction()
		{
			super( "Expand Tree" );
			putValue( Action.SHORT_DESCRIPTION, "Expand Tree" );
		}

		public void actionPerformed( ActionEvent e )
		{

			for( int row = 0; row < securityTestTree.getRowCount(); row++ )
			{
				securityTestTree.expandRow( row );
			}
		}
	}

	public class CollapsTreeAction extends AbstractAction
	{
		public CollapsTreeAction()
		{
			super( "Collaps Tree" );
			putValue( Action.SHORT_DESCRIPTION, "Collaps Tree" );
		}

		public void actionPerformed( ActionEvent e )
		{

			for( int row = securityTestTree.getRowCount() - 1; row >= 0; row-- )
			{
				securityTestTree.collapseRow( row );
			}
		}
	}

	// tree selection
	@Override
	public void valueChanged( TreeSelectionEvent e )
	{
		DefaultMutableTreeNode node = ( DefaultMutableTreeNode )securityTestTree.getLastSelectedPathComponent();

		/* if nothing is selected */
		if( node == null )
			return;

		if( node instanceof TestStepNode )
		{
			enableTestStepActions( node );
		}
		else if( node instanceof SecurityCheckNode )
		{
			enableSecurityCheckActions();
		}

	}

	protected void enableSecurityCheckActions()
	{
		securityTestLog.locateSecurityCheck( ( ( SecurityCheckNode )securityTestTree.getLastSelectedPathComponent() )
				.getSecurityCheck() );
		addSecurityCheckAction.setEnabled( false );
		configureSecurityCheckAction.setEnabled( true );
		removeSecurityCheckAction.setEnabled( true );
		if( ( ( SecurityCheckNode )securityTestTree.getLastSelectedPathComponent() ).getSecurityCheck() instanceof AbstractSecurityCheckWithProperties )
		{
			cloneParametersAction.setEnabled( true );
			cloneParametersAction
					.setSecurityCheck( ( AbstractSecurityCheckWithProperties )( ( SecurityCheckNode )securityTestTree
							.getLastSelectedPathComponent() ).getSecurityCheck() );
		}
	}

	protected void enableTestStepActions( DefaultMutableTreeNode node )
	{
		if( node.getAllowsChildren() )
			addSecurityCheckAction.setEnabled( true );
		else
			addSecurityCheckAction.setEnabled( false );
		configureSecurityCheckAction.setEnabled( false );
		removeSecurityCheckAction.setEnabled( false );
		cloneParametersAction.setEnabled( false );
	}

	@Override
	public void mouseClicked( MouseEvent e )
	{
		DefaultMutableTreeNode node = ( DefaultMutableTreeNode )securityTestTree.getLastSelectedPathComponent();
		if( node == null )
			return;

		if( ( e.getModifiers() & InputEvent.BUTTON3_MASK ) == InputEvent.BUTTON3_MASK )
			return;
		/* if nothing is selected */
		if( e.getClickCount() == 1 )
		{
			if( securityTestTree.isExpanded( TreePathUtils.getPath( node ) ) && node instanceof TestStepNode
					&& cellRender.isOn( ( TestStepNode )node, e.getX(), e.getY() ) )
			{
				securityTestTree.collapseRow( securityTestTree.getRowForLocation( e.getX(), e.getY() ) );
			}
			else
			{
				securityTestTree.expandRow( securityTestTree.getRowForLocation( e.getX(), e.getY() ) );
			}
			e.consume();
			return;
		}

		if( node instanceof SecurityCheckNode )
		{
			SecurityCheck securityCheck = ( ( SecurityCheckNode )securityTestTree.getLastSelectedPathComponent() )
					.getSecurityCheck();

			if( securityCheck.isConfigurable() )
			{
				XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
						.buildSecurityCheckConfigurationDialog( ( AbstractSecurityCheck )securityCheck );

				dialog.show();
			}
		}
		else
		{
			if( securityTestTree.isExpanded( TreePathUtils.getPath( node ) ) )
			{
				UISupport.selectAndShow( ( ( TestStepNode )node ).getTestStep() );
				e.consume();
			}
		}

	}

	@Override
	public void mouseEntered( MouseEvent e )
	{
		// TODO
	}

	@Override
	public void mouseExited( MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed( MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased( MouseEvent e )
	{
		TreePath path = securityTestTree.getPathForLocation( e.getX(), e.getY() );
		securityTestTree.setSelectionPath( path );

		Object node = securityTestTree.getLastSelectedPathComponent();

		if( node == null )
			return;
		if( ( e.getModifiers() & InputEvent.BUTTON3_MASK ) == InputEvent.BUTTON3_MASK )
		{
			if( node instanceof SecurityCheckNode )
			{
				if( ( ( SecurityCheckNode )node ).getSecurityCheck() instanceof AbstractSecurityCheckWithProperties )
					securityCheckWithPropertiesPopUp.show( securityTestTree, e.getX(), e.getY() );
				else
					securityCheckPopUp.show( securityTestTree, e.getX(), e.getY() );
			}
			else if( ( ( TestStepNode )node ).getTestStep() instanceof Securable )
				testStepPopUp.show( securityTestTree, e.getX(), e.getY() );
		}
	}

	public class CustomTreeUI extends BasicTreeUI
	{

		public CustomTreeUI()
		{
			super();
			leftChildIndent = 0;
			rightChildIndent = 0;
			totalChildIndent = 0;

		}

		@Override
		public int getLeftChildIndent()
		{
			return 0;

		}

		@Override
		protected void installListeners()
		{
			super.installListeners();

			tree.addComponentListener( componentListener );
		}

		@Override
		protected void uninstallListeners()
		{
			tree.removeComponentListener( componentListener );

			super.uninstallListeners();
		}

		@Override
		protected AbstractLayoutCache.NodeDimensions createNodeDimensions()
		{
			return new NodeDimensionsHandler()
			{
				@Override
				public Rectangle getNodeDimensions( Object value, int row, int depth, boolean expanded, Rectangle size )
				{
					Rectangle dimensions = super.getNodeDimensions( value, row, depth, expanded, size );
					Insets insets = tree.getInsets();
					dimensions.width = tree.getWidth() - getRowX( row, depth ) - insets.right;
					return dimensions;
				}
			};
		}

		private final ComponentListener componentListener = new ComponentAdapter()
		{
			@Override
			public void componentResized( ComponentEvent e )
			{
				treeState.invalidateSizes();
				tree.repaint();
			};
		};

		protected void paintRow( Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path,
				int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf )
		{
			super.paintRow( g, clipBounds, insets, new Rectangle( 0, bounds.y, bounds.width + bounds.x, bounds.height ),
					path, row, isExpanded, hasBeenExpanded, isLeaf );
		};

		@Override
		protected void paintHorizontalPartOfLeg( Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds,
				TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf )
		{
		};

		@Override
		protected void paintVerticalPartOfLeg( Graphics g, Rectangle clipBounds, Insets insets, TreePath path )
		{
			// TODO Auto-generated method stub
			// super.paintVerticalPartOfLeg( g, clipBounds, insets, path );
		}
	}
}

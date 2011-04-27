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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXTree;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestListener;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.actions.CloneParametersAction;
import com.eviware.soapui.security.check.AbstractSecurityCheckWithProperties;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.support.SecurityTestRunListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.TreePathUtils;
import com.eviware.x.form.XFormDialog;

/**
 * A panel showing a scrollable list of TestSteps in a SecurityTest.
 * 
 * @author dragica.soldo
 */

@SuppressWarnings( "serial" )
public class JSecurityTestTestStepList extends JPanel implements TreeSelectionListener, MouseListener,
		SecurityTestListener
{
	private SecurityTest securityTest;
	private final TestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private JXTree securityTestTree;
	private AddSecurityCheckAction addSecurityCheckAction;
	private ConfigureSecurityCheckAction configureSecurityCheckAction;
	private RemoveSecurityCheckAction removeSecurityCheckAction;
	private CloneParametersAction cloneParametersAction;

	private JSecurityTestRunLog securityTestLog;
	private JPopupMenu securityCheckPopUp;

	private JPopupMenu securityCheckWithPropertiesPopUp;
	private JPopupMenu testStepPopUp;
	private SecurityTreeCellRender cellRender;
	private SecurityCheckTree treeModel;
	private InternalSecurityTestRunListener testRunListener;

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

		treeModel = new SecurityCheckTree( securityTest );
		securityTestTree = new JXTree( treeModel );
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
		JScrollPane scrollPane = new JScrollPane( securityTestTree );
		add( scrollPane, BorderLayout.CENTER );
		securityTest.getTestCase().getTestSuite().addTestSuiteListener( testSuiteListener );
		securityTest.addSecurityTestListener( this );
		testRunListener = new InternalSecurityTestRunListener();
		securityTest.addSecurityTestRunListener( testRunListener );
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

	protected JComponent buildSecurityChecksInspector()
	{
		JPanel p = new JPanel( new BorderLayout() );
		return p;
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
			for( int cnt = 0; cnt < node.getChildCount(); cnt++ )
			{
				SecurityCheckNode nodeCld = ( SecurityCheckNode )node.getChildAt( cnt );
				cellRender.remove( nodeCld );
				treeModel.removeNodeFromParent( nodeCld );
			}
			cellRender.remove( node );
			treeModel.removeNodeFromParent( node );
		}

		@Override
		public void testStepMoved( TestStep testStep, int index, int offset )
		{
			TreePath path = treeModel.moveTestStepNode( testStep, index, offset );

			securityTestTree.expandPath( path );
			securityTestTree.setSelectionPath( path );
		}
	}

	// toolbar actions
	public class AddSecurityCheckAction extends AbstractAction
	{
		public AddSecurityCheckAction()
		{
			super( "Add SecurityScan" );

			putValue( Action.SHORT_DESCRIPTION, "Adds a security scan to this item" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/addSecurityCheck.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			TestStepNode node = ( TestStepNode )securityTestTree.getLastSelectedPathComponent();
			if( !node.getAllowsChildren() )
			{
				return;
			}

			TestStep testStep = node.getTestStep();

			String[] availableChecksNames = SoapUI.getSoapUICore().getSecurityCheckRegistry()
					.getAvailableSecurityChecksNames( testStep );
			availableChecksNames = securityTest.getAvailableSecurityCheckNames( testStep, availableChecksNames );

			if( availableChecksNames == null || availableChecksNames.length == 0 )
			{
				UISupport.showErrorMessage( "No security scans available for this test step" );
				return;
			}

			String name = UISupport.prompt( "Specify type of security scan", "Add SecurityScan", availableChecksNames );
			if( name == null || name.trim().length() == 0 )
				return;

			String type = SoapUI.getSoapUICore().getSecurityCheckRegistry().getSecurityCheckTypeForName( name );
			if( type == null || type.trim().length() == 0 )
				return;

			SecurityCheck securityCheck = securityTest.addNewSecurityCheck( testStep, name );

			if( securityCheck == null )
			{
				UISupport.showErrorMessage( "Failed to add security scan" );
				return;
			}

			XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
					.buildSecurityCheckConfigurationDialog( ( SecurityCheck )securityCheck );

			dialog.show();

			if( dialog.getReturnValue() == XFormDialog.CANCEL_OPTION )
			{
				SecurityCheckNode securityCheckNode = ( SecurityCheckNode )node.getLastChild();

				securityTest.removeSecurityCheck( testStep, ( SecurityCheck )securityCheck );
				cellRender.remove( securityCheckNode );
			}
		}

	}

	public class ConfigureSecurityCheckAction extends AbstractAction
	{
		ConfigureSecurityCheckAction()
		{
			super( "Configure" );
			putValue( Action.SHORT_DESCRIPTION, "Configures selected security scan" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/options.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			SecurityCheckNode node = ( SecurityCheckNode )securityTestTree.getLastSelectedPathComponent();
			SecurityCheck securityCheck = node.getSecurityCheck();

			if( securityCheck.isConfigurable() )
			{
				SecurityCheckConfig backupCheckConfig = ( SecurityCheckConfig )securityCheck.getConfig().copy();

				XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
						.buildSecurityCheckConfigurationDialog( ( SecurityCheck )securityCheck );

				dialog.show();

				if( dialog.getReturnValue() == XFormDialog.CANCEL_OPTION )
				{
					securityCheck.copyConfig( backupCheckConfig );
				}
			}
		}
	}

	public class RemoveSecurityCheckAction extends AbstractAction
	{
		public RemoveSecurityCheckAction()
		{
			super( "Remove SecurityScan" );
			putValue( Action.SHORT_DESCRIPTION, "Removes the selected security scan" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_securityCheck.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			SecurityCheckNode node = ( SecurityCheckNode )securityTestTree.getLastSelectedPathComponent();
			SecurityCheck securityCheck = node.getSecurityCheck();

			TestStep testStep = ( ( TestStepNode )node.getParent() ).getTestStep();
			if( UISupport.confirm( "Remove security scan [" + securityCheck.getName() + "]", "Remove SecurityScan" ) )
			{

				securityTest.removeSecurityCheck( testStep, ( SecurityCheck )securityCheck );
				cellRender.remove( node );
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

	public class InternalSecurityTestRunListener extends SecurityTestRunListenerAdapter
	{

		@Override
		public void beforeSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityCheck securityCheck )
		{
			securityTestTree.setSelectionRow( securityTestTree.getRowForPath( new TreePath( treeModel
					.getSecurityCheckNode( securityCheck ).getPath() ) ) );
		}

		@Override
		public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
		{
			disableAllActions();
		}

		@Override
		public void afterRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
		{
			enableActionsAfterRun();
		}
	}

	// tree selection
	@Override
	public void valueChanged( TreeSelectionEvent e )
	{
		enableActionsAfterRun();
	}

	/**
	 * 
	 */
	protected void enableActionsAfterRun()
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
		if( securityTest.isRunning() )
			return;
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
		if( securityTest.isRunning() )
			return;
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
			if( securityTest.isRunning() )
				return;
			SecurityCheck securityCheck = ( ( SecurityCheckNode )securityTestTree.getLastSelectedPathComponent() )
					.getSecurityCheck();

			if( securityCheck.isConfigurable() )
			{
				SecurityCheckConfig backupCheckConfig = ( SecurityCheckConfig )securityCheck.getConfig().copy();

				XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
						.buildSecurityCheckConfigurationDialog( ( SecurityCheck )securityCheck );

				dialog.show();

				if( dialog.getReturnValue() == XFormDialog.CANCEL_OPTION )
				{
					securityCheck.copyConfig( backupCheckConfig );
				}
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
		if( securityTest.isRunning() )
			return;
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

	public void release()
	{
		cellRender.release();
		securityTest.getTestCase().getTestSuite().removeTestSuiteListener( testSuiteListener );
		securityTest.removeSecurityTestRunListener( testRunListener );

	}

	@Override
	public void securityCheckAdded( SecurityCheck securityCheck )
	{
		treeModel.addSecurityCheckNode( securityTestTree, securityCheck );

	}

	@Override
	public void securityCheckRemoved( SecurityCheck securityCheck )
	{
		treeModel.removeSecurityCheckNode( securityCheck );
	}

	/**
	 * 
	 */
	protected void disableAllActions()
	{
		addSecurityCheckAction.setEnabled( false );
		configureSecurityCheckAction.setEnabled( false );
		removeSecurityCheckAction.setEnabled( false );
		cloneParametersAction.setEnabled( false );
	}

}

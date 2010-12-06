/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.impl.wsdl.actions.testsuite.AddNewTestCaseAction;
import com.eviware.soapui.impl.wsdl.panels.support.ProgressBarTestCaseAdapter;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.swing.AutoscrollSupport;

/**
 * A panel showing a scrollable list of TestSteps in a SecurityTest.
 * 
 * @author dragica.soldo
 */

public class JSecurityTestTestStepList extends JPanel
{
	private Map<TestStep, TestStepListPanel> panels = new HashMap<TestStep, TestStepListPanel>();
	private final SecurityTest securityTest;
	private final InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private TestStepListPanel selectedTestStep;
	private JInspectorPanel inspectorPanel;
	private DefaultListModel listModel;
	private JList securityChecksList;
	JSplitPane splitPane;
	private JPanel securityCheckConfigPanel;
	JComponentInspector<JComponent> securityChecksInspector;
	private JComponent secCheckPanel;

	public JSecurityTestTestStepList( SecurityTest securityTest )
	{
		JPanel p = new JPanel( new BorderLayout() );
		this.securityTest = securityTest;
		p.setLayout( new BoxLayout( p, BoxLayout.Y_AXIS ) );

		for( int c = 0; c < securityTest.getTestCase().getTestStepCount(); c++ )
		{
			TestStepListPanel testCaseListPanel = createTestStepListPanel( securityTest.getTestCase().getTestStepAt( c ) );
			panels.put( securityTest.getTestCase().getTestStepAt( c ), testCaseListPanel );
			p.add( testCaseListPanel );
		}
		p.add( Box.createVerticalGlue() );
		// inspectorPanel = JInspectorPanelFactory.build( p );
		createSecurityCheckInspector();
		// inspectorPanel.addInspector( securityChecksInspector );
		// setBackground( Color.WHITE );
		// add( inspectorPanel.getComponent() );

		secCheckPanel = buildSecurityChecksPanel();
		secCheckPanel.setPreferredSize( new Dimension( 650, 300 ) );
		splitPane = UISupport.createVerticalSplit( new JScrollPane( p ), new JScrollPane( secCheckPanel ) );
		splitPane.setPreferredSize( new Dimension( 650, 500 ) );
		splitPane.setResizeWeight( 0.1 );
		splitPane.setDividerLocation( 0.5 );
		add( splitPane, BorderLayout.CENTER );
		// testCase.addTestSuiteListener( testSuiteListener );

		// ActionList actions = ActionListBuilder.buildActions( securityTest );
		// actions.removeAction( 0 );
		// actions.removeAction( 0 );
		// setComponentPopupMenu( ActionSupport.buildPopup( actions ) );

		// DragSource dragSource = DragSource.getDefaultDragSource();
		//
		// SoapUIDragAndDropHandler dragAndDropHandler = new
		// SoapUIDragAndDropHandler(
		// new TestStepListDragAndDropable( this ), DropType.AFTER );
		//
		// dragSource.createDefaultDragGestureRecognizer( this,
		// DnDConstants.ACTION_COPY_OR_MOVE, dragAndDropHandler );
	}

	private void createSecurityCheckInspector()
	{
		securityChecksInspector = new JComponentInspector<JComponent>( buildSecurityChecksPanel(), "SecurityChecks",
				"SecurityChecks for selected TestStep", true );
	}

	protected JComponent buildSecurityChecksList()
	{
		listModel = new DefaultListModel();

		securityChecksList = new JList( listModel );
		securityChecksList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		securityChecksList.addListSelectionListener( new ListSelectionListener()
		{

			@Override
			public void valueChanged( ListSelectionEvent arg0 )
			{
				splitPane.remove( splitPane.getRightComponent() );
				splitPane.setRightComponent( buildSecurityCheckConfigPanel() );
				revalidate();
				// setSelectedCheck( getCurrentSecurityCheck() );
			}
		} );
		JScrollPane listScrollPane = new JScrollPane( securityChecksList );
		UISupport.addTitledBorder( listScrollPane, "Security Checks" );

		JPanel p = new JPanel( new BorderLayout() );
		p.add( listScrollPane, BorderLayout.CENTER );
		// p.add( createPropertiesToolbar(), BorderLayout.NORTH );

		securityCheckConfigPanel = ( JPanel )buildSecurityCheckConfigPanel();

		splitPane = UISupport.createHorizontalSplit( p, buildSecurityCheckConfigPanel() );
		splitPane.setPreferredSize( new Dimension( 650, 500 ) );
		splitPane.setResizeWeight( 0.1 );
		splitPane.setDividerLocation( 120 );
		// add( splitPane, BorderLayout.CENTER );
		return splitPane;

	}

	private JComponent buildSecurityCheckConfigPanel()
	{
		securityCheckConfigPanel = UISupport.addTitledBorder( new JPanel( new BorderLayout() ), "Configuration" );
		// securityCheckConfigPanel.setPreferredSize( new Dimension( 330, 400 ) );
		securityCheckConfigPanel.add( new JLabel( "currently no security checks" ) );
		// securityCheckConfigPanel = new securityCheckConfigPanel.setText(
		// "currently no security checks" );
		// panel.add( securityCheckConfigPanel );
		if( securityChecksList != null && securityChecksList.getSelectedValue() != null )
		{
			SecurityCheck selected = securityTest.getTestStepSecurityCheckByName( selectedTestStep.getTestStep().getId(),
					( String )securityChecksList.getSelectedValue() );
			securityCheckConfigPanel.removeAll();
			securityCheckConfigPanel.add( selected.getComponent( ) );
		}
		securityCheckConfigPanel.revalidate();
		return securityCheckConfigPanel;
	}

	public SecurityCheck getCurrentSecurityCheck()
	{
		int ix = securityChecksList.getSelectedIndex();
		return ix == -1 ? null : securityTest.getTestStepSecurityCheckAt( selectedTestStep.getTestStep().getId(), ix );
	}

	// TODO see how to change the model for this to work...which class should
	// implement securable
	protected JPanel buildSecurityChecksPanel()
	{
		if( selectedTestStep != null && AbstractSecurityCheck.isSecurable( selectedTestStep.getTestStep() )  )
		{
			return new SecurityChecksPanel( selectedTestStep.getTestStep(), securityTest );
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
		for( TestStepListPanel testCasePanel : panels.values() )
		{
			testCasePanel.reset();
		}
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		// testCase.addTestCaseListener( testSuiteListener );
	}

	@Override
	public void removeNotify()
	{
		super.removeNotify();
		// testCase.removeTestSuiteListener( testSuiteListener );
	}

	private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		@Override
		public void testStepAdded( TestStep testStep, int index )
		{
			TestStepListPanel testCaseListPanel = createTestStepListPanel( testStep );
			panels.put( testStep, testCaseListPanel );
			add( testCaseListPanel, index );
			revalidate();
			repaint();
		}

		@Override
		public void testStepRemoved( TestStep testStep, int index )
		{
			TestStepListPanel testCaseListPanel = panels.get( testStep );
			if( testCaseListPanel != null )
			{
				remove( testCaseListPanel );
				panels.remove( testStep );
				revalidate();
				repaint();
			}
		}

		@Override
		public void testStepMoved( TestStep testStep, int index, int offset )
		{
			TestStepListPanel testStepListPanel = panels.get( testStep );
			if( testStepListPanel != null )
			{
				boolean hadFocus = testStepListPanel.hasFocus();

				remove( testStepListPanel );
				add( testStepListPanel, index + offset );

				revalidate();
				repaint();

				if( hadFocus )
					testStepListPanel.requestFocus();
			}
		}
	}

	public final class TestStepListPanel extends JPanel implements Autoscroll
	{
		private final WsdlTestStep testStep;
		private JProgressBar progressBar;
		private JLabel label;
		private ProgressBarTestCaseAdapter progressBarAdapter;
		private TestCasePropertyChangeListener testCasePropertyChangeListener;
		private AutoscrollSupport autoscrollSupport;

		public TestStepListPanel( WsdlTestStep testStep )
		{
			super( new BorderLayout() );

			setFocusable( true );

			this.testStep = testStep;
			autoscrollSupport = new AutoscrollSupport( this );

			progressBar = new JProgressBar( 0, 100 )
			{
				protected void processMouseEvent( MouseEvent e )
				{
					if( e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED )
					{
						TestStepListPanel.this.processMouseEvent( translateMouseEvent( e ) );
					}
				}

				protected void processMouseMotionEvent( MouseEvent e )
				{
					TestStepListPanel.this.processMouseMotionEvent( translateMouseEvent( e ) );
				}

				/**
				 * Translates the given mouse event to the enclosing map panel's
				 * coordinate space.
				 */
				private MouseEvent translateMouseEvent( MouseEvent e )
				{
					return new MouseEvent( TestStepListPanel.this, e.getID(), e.getWhen(), e.getModifiers(), e.getX()
							+ getX(), e.getY() + getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton() );
				}
			};

			JPanel progressPanel = UISupport.createProgressBarPanel( progressBar, 5, false );

			progressBar.setMinimumSize( new Dimension( 0, 10 ) );
			progressBar.setBackground( Color.WHITE );
			progressBar.setInheritsPopupMenu( true );

			label = new JLabel( testStep.getLabel(), testStep.getIcon(), SwingConstants.LEFT );
			label.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
			label.setInheritsPopupMenu( true );
			label.setEnabled( !testStep.isDisabled() );

			add( progressPanel, BorderLayout.LINE_END );
			add( label, BorderLayout.LINE_START );

			testCasePropertyChangeListener = new TestCasePropertyChangeListener();

			initPopup( testStep );

			addMouseListener( new MouseAdapter()
			{

				@Override
				public void mousePressed( MouseEvent e )
				{
					requestFocus();
				}

				public void mouseClicked( MouseEvent e )
				{
					if( e.getClickCount() < 2 )
					{
						if( selectedTestStep != null )
							selectedTestStep.setSelected( false );

						setSelected( true );
						selectedTestStep = TestStepListPanel.this;
						return;
					}

					// UISupport.selectAndShow( TestStepListPanel.this.testStep );
					// securityChecksInspector.getComponent().removeAll();
					// securityChecksInspector.getComponent().add(
					// buildSecurityChecksPanel());
					// securityChecksInspector.getComponent().revalidate();
					splitPane.remove( secCheckPanel );
					secCheckPanel = buildSecurityChecksPanel();
					secCheckPanel.revalidate();
					splitPane.setBottomComponent( secCheckPanel );
					splitPane.revalidate();
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
			actions.insertAction( SwingActionDelegate.createDelegate( AddNewTestCaseAction.SOAPUI_ACTION_ID, securityTest,
					null, null ), 0 );
			actions.insertAction( ActionSupport.SEPARATOR_ACTION, 1 );

			setComponentPopupMenu( ActionSupport.buildPopup( actions ) );
		}

		public void addNotify()
		{
			super.addNotify();
			testStep.addPropertyChangeListener( testCasePropertyChangeListener );
			// TODO check
			// progressBarAdapter = new ProgressBarTestCaseAdapter( progressBar,
			// testStep );
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
	}

	protected int getIndexOf( TestStepListPanel panel )
	{
		return Arrays.asList( getComponents() ).indexOf( panel );
	}

	protected TestStepListPanel createTestStepListPanel( TestStep testStep )
	{
		TestStepListPanel testStepListPanel = new TestStepListPanel( ( WsdlTestStep )testStep );

		// DragSource dragSource = DragSource.getDefaultDragSource();
		//
		// SoapUIDragAndDropHandler dragAndDropHandler = new
		// SoapUIDragAndDropHandler( new TestCaseListPanelDragAndDropable(
		// testStepListPanel ), DropType.BEFORE_AND_AFTER );
		//
		// dragSource.createDefaultDragGestureRecognizer( testStepListPanel,
		// DnDConstants.ACTION_COPY_OR_MOVE,
		// dragAndDropHandler );

		return testStepListPanel;
	}

	// private class TestStepListDragAndDropable implements
	// SoapUIDragAndDropable<ModelItem>
	// {
	// private final JSecurityTestTestStepList list;
	//
	// public TestStepListDragAndDropable( JSecurityTestTestStepList list )
	// {
	// this.list = list;
	// }
	//
	// public JComponent getComponent()
	// {
	// return list;
	// }
	//
	// public Rectangle getModelItemBounds( ModelItem modelItem )
	// {
	// return list.getBounds();
	// }
	//
	// public ModelItem getModelItemForLocation( int x, int y )
	// {
	// int testCaseCount = testCase.getTestStepCount();
	// return testCaseCount == 0 ? testCase : testCase.getTestStepAt(
	// testCaseCount - 1 );
	// }
	//
	// public Component getRenderer( ModelItem modelItem )
	// {
	// return null;
	// }
	//
	// public void selectModelItem( ModelItem modelItem )
	// {
	// }
	//
	// public void setDragInfo( String dropInfo )
	// {
	// list.setToolTipText( dropInfo );
	// }
	//
	// public void toggleExpansion( ModelItem modelItem )
	// {
	// }
	// }
	//
	// private static class TestCaseListPanelDragAndDropable implements
	// SoapUIDragAndDropable<ModelItem>
	// {
	// private final TestStepListPanel testCasePanel;
	//
	// public TestCaseListPanelDragAndDropable( TestStepListPanel testCasePanel )
	// {
	// this.testCasePanel = testCasePanel;
	// }
	//
	// public JComponent getComponent()
	// {
	// return testCasePanel;
	// }
	//
	// public void setDragInfo( String dropInfo )
	// {
	// testCasePanel.setToolTipText( dropInfo.length() == 0 ? null : dropInfo );
	// }
	//
	// public Rectangle getModelItemBounds( ModelItem path )
	// {
	// return new Rectangle( testCasePanel.getSize() );
	// }
	//
	// public ModelItem getModelItemForLocation( int x, int y )
	// {
	// return testCasePanel.getModelItem();
	// }
	//
	// public Component getRenderer( ModelItem path )
	// {
	// return null;
	// }
	//
	// public void selectModelItem( ModelItem path )
	// {
	// testCasePanel.setSelected( !testCasePanel.isSelected() );
	// }
	//
	// public void toggleExpansion( ModelItem last )
	// {
	// }
	// }
}
